package com.berling.marketplace.data.repository

import com.berling.marketplace.data.local.ProductDao
import com.berling.marketplace.data.local.FavoriteDao
import com.berling.marketplace.data.local.PendingSyncDao
import com.berling.marketplace.data.local.entities.ProductEntity
import com.berling.marketplace.data.local.entities.FavoriteEntity
import com.berling.marketplace.data.local.entities.PendingSyncEntity
import com.berling.marketplace.data.remote.SupabaseApi
import com.berling.marketplace.data.remote.models.*
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class ProductRepository @Inject constructor(
    private val api: SupabaseApi,
    private val productDao: ProductDao,
    private val favoriteDao: FavoriteDao,
    private val pendingSyncDao: PendingSyncDao
) {
    fun getAllProducts(): Flow<List<ProductEntity>> = productDao.getActiveProducts()

    fun getProductsByCategory(category: String): Flow<List<ProductEntity>> = 
        productDao.getProductsByCategory(category)

    fun getSellerProducts(sellerId: String): Flow<List<ProductEntity>> = 
        productDao.getSellerProducts(sellerId)

    suspend fun getProduct(productId: String): ProductEntity? = productDao.getProductById(productId)

    suspend fun createProduct(
        token: String,
        sellerId: String,
        request: ProductCreateRequest
    ): Result<ProductEntity> {
        return try {
            val productId = "product_${System.currentTimeMillis()}"
            
            // Step 1: Save to local database first
            val product = ProductEntity(
                id = productId,
                sellerId = sellerId,
                sellerName = "",
                sellerPhotoUrl = "",
                title = request.title,
                description = request.description,
                price = request.price,
                imageUrl = request.imageUrl,
                imageUrls = request.imageUrls.joinToString(","),
                category = request.category,
                brand = request.brand,
                condition = request.condition,
                location = request.location,
                latitude = request.latitude,
                longitude = request.longitude,
                deliveryOptions = Json.encodeToString(request.deliveryOptions),
                returnPolicy = request.returnPolicy,
                tags = Json.encodeToString(request.tags),
                productAttributes = Json.encodeToString(request.productAttributes),
                isNew = request.isNew,
                boostListing = request.boostListing,
                discountPrice = request.discountPrice,
                uploadStatus = "uploading",
                isSynced = false,
                createdAt = System.currentTimeMillis().toString()
            )
            
            // Save product to local database
            productDao.insertProduct(product)
            
            // Add to pending syncs for background sync
            val syncEntity = PendingSyncEntity(
                entityType = "product",
                entityId = productId,
                operation = "create",
                data = Json.encodeToString(request),
                createdAt = System.currentTimeMillis().toString(),
                retryCount = 0
            )
            pendingSyncDao.insertPendingSync(syncEntity)
            
            // Step 2: Try to sync immediately
            try {
                val response = api.createProduct("Bearer $token", request)
                if (response.error == null && response.data != null) {
                    val syncedProduct = product.copy(
                        isSynced = true,
                        uploadStatus = "completed"
                    )
                    productDao.updateProduct(syncedProduct)
                    pendingSyncDao.deletePendingSync(syncEntity)
                    Result.success(syncedProduct)
                } else {
                    Result.success(product) // Success - will sync later
                }
            } catch (e: Exception) {
                Result.success(product) // Success - will sync later in background
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateProduct(
        token: String,
        productId: String,
        request: ProductUpdateRequest
    ): Result<ProductEntity> {
        return try {
            val product = productDao.getProductById(productId) ?: return Result.failure(Exception("Product not found"))
            
            // Update local database
            val updatedProduct = product.copy(
                title = request.title ?: product.title,
                description = request.description ?: product.description,
                price = request.price ?: product.price,
                category = request.category ?: product.category,
                imageUrl = request.imageUrl ?: product.imageUrl,
                imageUrls = if (request.imageUrls != null) request.imageUrls.joinToString(",") else product.imageUrls,
                brand = request.brand ?: product.brand,
                condition = request.condition ?: product.condition,
                location = request.location ?: product.location,
                latitude = request.latitude ?: product.latitude,
                longitude = request.longitude ?: product.longitude,
                country = request.country ?: product.country,
                city = request.city ?: product.city,
                deliveryOptions = if (request.deliveryOptions != null) Json.encodeToString(request.deliveryOptions) else product.deliveryOptions,
                returnPolicy = request.returnPolicy ?: product.returnPolicy,
                tags = if (request.tags != null) Json.encodeToString(request.tags) else product.tags,
                productAttributes = if (request.productAttributes != null) Json.encodeToString(request.productAttributes) else product.productAttributes,
                isNew = request.isNew ?: product.isNew,
                boostListing = request.boostListing ?: product.boostListing,
                discountPrice = request.discountPrice ?: product.discountPrice
            )
            
            productDao.updateProduct(updatedProduct)
            
            // Add to pending syncs
            val syncEntity = PendingSyncEntity(
                entityType = "product",
                entityId = productId,
                operation = "update",
                data = Json.encodeToString(request),
                createdAt = System.currentTimeMillis().toString(),
                retryCount = 0
            )
            pendingSyncDao.insertPendingSync(syncEntity)
            
            Result.success(updatedProduct)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteProduct(
        token: String,
        productId: String
    ): Result<Unit> {
        return try {
            productDao.deleteProductById(productId)
            
            // Add to pending syncs
            val syncEntity = PendingSyncEntity(
                entityType = "product",
                entityId = productId,
                operation = "delete",
                createdAt = System.currentTimeMillis().toString(),
                retryCount = 0
            )
            pendingSyncDao.insertPendingSync(syncEntity)
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getPendingSyncs(): List<PendingSyncEntity> = 
        pendingSyncDao.getPendingSyncsToSync()

    suspend fun getPendingSyncCount(): Int = 
        pendingSyncDao.getPendingSyncCount()

    suspend fun addToFavorites(productId: String, userId: String): Result<Unit> {
        return try {
            val favorite = FavoriteEntity(
                id = "fav_${System.currentTimeMillis()}",
                productId = productId,
                userId = userId,
                isSynced = false,
                createdAt = System.currentTimeMillis().toString()
            )
            favoriteDao.insertFavorite(favorite)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun removeFromFavorites(productId: String, userId: String): Result<Unit> {
        return try {
            favoriteDao.deleteFavoriteByProductAndUser(productId, userId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getUserFavorites(userId: String): Flow<List<FavoriteEntity>> = 
        favoriteDao.getUserFavorites(userId)

    suspend fun isFavorite(productId: String, userId: String): Boolean {
        return favoriteDao.isFavorite(productId, userId) > 0
    }

    // Search and filter methods
    fun searchProducts(query: String): Flow<List<ProductEntity>> {
        val searchQuery = "%$query%"
        return productDao.searchProducts(searchQuery)
    }

    fun searchByLocation(location: String): Flow<List<ProductEntity>> {
        val searchQuery = "%$location%"
        return productDao.searchByLocation(searchQuery)
    }

    fun searchByCategory(category: String): Flow<List<ProductEntity>> {
        return productDao.searchByCategory(category)
    }

    fun searchByCategoryAndQuery(category: String, query: String): Flow<List<ProductEntity>> {
        val searchQuery = "%$query%"
        return productDao.searchByCategoryAndQuery(category, searchQuery)
    }

    fun searchWithLimit(query: String, limit: Int = 50): Flow<List<ProductEntity>> {
        val searchQuery = "%$query%"
        return productDao.searchWithLimit(searchQuery, limit)
    }

    fun searchByPriceRange(minPrice: Double, maxPrice: Double): Flow<List<ProductEntity>> {
        return productDao.searchByPriceRange(minPrice, maxPrice)
    }

    fun searchByCountry(country: String): Flow<List<ProductEntity>> {
        return productDao.searchByCountry(country)
    }

    fun getAllCountries(): Flow<List<String>> {
        return productDao.getAllCountries()
    }

    suspend fun getSellerProductCount(sellerId: String): Int {
        return productDao.getSellerProductCount(sellerId)
    }

    suspend fun canPostProduct(sellerId: String): Boolean {
        val count = getSellerProductCount(sellerId)
        return count < 5 // Max 5 products per seller
    }

    /**
     * Observe products table in real-time from Supabase
     */
    fun observeProductsRealtime(): Flow<ProductEntity> = kotlinx.coroutines.flow.flow {
        try {
            api.observeTableRealtime("products").collect { data ->
                // Map the dynamic map from Supabase to ProductEntity
                val product = ProductEntity(
                    id = data["id"]?.toString() ?: "",
                    sellerId = data["seller_id"]?.toString() ?: "",
                    sellerName = data["seller_name"]?.toString() ?: "",
                    sellerPhotoUrl = data["seller_photo_url"]?.toString() ?: "",
                    title = data["title"]?.toString() ?: "",
                    description = data["description"]?.toString() ?: "",
                    price = data["price"]?.toString()?.toDoubleOrNull() ?: 0.0,
                    imageUrl = data["image_url"]?.toString() ?: "",
                    imageUrls = data["image_urls"]?.toString() ?: "",
                    category = data["category"]?.toString() ?: "",
                    brand = data["brand"]?.toString() ?: "",
                    condition = data["condition"]?.toString() ?: "",
                    location = data["location"]?.toString() ?: "",
                    latitude = data["latitude"]?.toString()?.toDoubleOrNull() ?: 0.0,
                    longitude = data["longitude"]?.toString()?.toDoubleOrNull() ?: 0.0,
                    country = data["country"]?.toString() ?: "",
                    city = data["city"]?.toString() ?: "",
                    deliveryOptions = data["delivery_options"]?.toString() ?: "",
                    returnPolicy = data["return_policy"]?.toString() ?: "",
                    tags = data["tags"]?.toString() ?: "",
                    productAttributes = data["product_attributes"]?.toString() ?: "",
                    isNew = data["is_new"]?.toString()?.toBoolean() ?: true,
                    boostListing = data["boost_listing"]?.toString()?.toBoolean() ?: false,
                    discountPrice = data["discount_price"]?.toString()?.toDoubleOrNull(),
                    uploadStatus = "completed",
                    isSynced = true,
                    createdAt = data["created_at"]?.toString() ?: System.currentTimeMillis().toString()
                )
                emit(product)
            }
        } catch (e: Exception) {
            // Log error
        }
    }
}
