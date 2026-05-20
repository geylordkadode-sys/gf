package com.berling.marketplace.ui.screens.post

import android.content.Context
import android.graphics.Bitmap
import androidx.lifecycle.viewModelScope
import com.berling.marketplace.data.local.PendingSyncDao
import com.berling.marketplace.data.local.ProductDao
import com.berling.marketplace.data.local.entities.PendingSyncEntity
import com.berling.marketplace.data.local.entities.ProductEntity
import com.berling.marketplace.data.repository.ProductRepository
import com.berling.marketplace.ui.screens.BaseViewModel
import com.berling.marketplace.ui.screens.UiState
import com.berling.marketplace.utils.ImageCompressionUtil
import com.berling.marketplace.utils.LocationData
import com.berling.marketplace.utils.LocationUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import java.io.File
import java.io.FileOutputStream
import java.util.*
import javax.inject.Inject
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import com.berling.marketplace.data.remote.models.ProductCreateRequest

data class PostFormState(
    val title: String = "",
    val description: String = "",
    val price: Double = 0.0,
    val discountPrice: Double = 0.0,
    val category: String = "",
    val brand: String = "",
    val condition: String = "new",
    val location: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val country: String = "", // New field for country
    val city: String = "", // New field for city
    val deliveryOptions: List<String> = emptyList(),
    val returnPolicy: String = "",
    val tags: List<String> = emptyList(),
    val productAttributes: Map<String, String> = emptyMap(),
    val isNew: Boolean = false,
    val boostListing: Boolean = false,
    val imageUrls: List<String> = emptyList(),
    val uploadProgress: Int = 0,
    val uploadStatus: String = "pending",
    val syncStatus: String = "local" // "local", "uploading", "synced", "error"
)

data class UploadProgress(
    val totalFiles: Int = 0,
    val uploadedFiles: Int = 0,
    val progress: Int = 0,
    val currentFile: String = "",
    val totalSize: Long = 0L,
    val uploadedSize: Long = 0L
)

@HiltViewModel
class PostScreenViewModel @Inject constructor(
    private val productRepository: ProductRepository,
    private val productDao: ProductDao,
    private val pendingSyncDao: PendingSyncDao,
    @ApplicationContext private val context: Context
) : BaseViewModel() {

    private val _formState = MutableStateFlow(PostFormState())
    val formState: StateFlow<PostFormState> = _formState

    private val _uploadProgress = MutableStateFlow(UploadProgress())
    val uploadProgress: StateFlow<UploadProgress> = _uploadProgress

    private val _postState = MutableStateFlow<UiState<ProductEntity>>(UiState.Idle)
    val postState: StateFlow<UiState<ProductEntity>> = _postState

    private val _selectedImages = MutableStateFlow<List<Bitmap>>(emptyList())
    val selectedImages: StateFlow<List<Bitmap>> = _selectedImages

    private val _currentLocation = MutableStateFlow<LocationData?>(null)
    val currentLocation: StateFlow<LocationData?> = _currentLocation

    private val _canPostProduct = MutableStateFlow(true)
    val canPostProduct: StateFlow<Boolean> = _canPostProduct

    private val _maxProductsError = MutableStateFlow<String?>(null)
    val maxProductsError: StateFlow<String?> = _maxProductsError

    fun updateFormField(field: String, value: Any) {
        val currentState = _formState.value
        _formState.value = when (field) {
            "title" -> currentState.copy(title = value as String)
            "description" -> currentState.copy(description = value as String)
            "price" -> currentState.copy(price = value as Double)
            "discountPrice" -> currentState.copy(discountPrice = value as Double)
            "category" -> currentState.copy(category = value as String)
            "brand" -> currentState.copy(brand = value as String)
            "condition" -> currentState.copy(condition = value as String)
            "location" -> currentState.copy(location = value as String)
            "latitude" -> currentState.copy(latitude = value as Double)
            "longitude" -> currentState.copy(longitude = value as Double)
            "country" -> currentState.copy(country = value as String)
            "city" -> currentState.copy(city = value as String)
            "deliveryOptions" -> currentState.copy(deliveryOptions = value as List<String>)
            "returnPolicy" -> currentState.copy(returnPolicy = value as String)
            "tags" -> currentState.copy(tags = value as List<String>)
            "isNew" -> currentState.copy(isNew = value as Boolean)
            "boostListing" -> currentState.copy(boostListing = value as Boolean)
            else -> currentState
        }
    }

    fun addImages(bitmaps: List<Bitmap>) {
        viewModelScope.launch {
            val newImages = _selectedImages.value.toMutableList()
            newImages.addAll(bitmaps)
            if (newImages.size > 10) {
                newImages.subList(10, newImages.size).clear()
            }
            _selectedImages.value = newImages
            updateFormField("imageUrls", convertBitmapsToUrls(newImages))
        }
    }

    fun removeImage(index: Int) {
        val newImages = _selectedImages.value.toMutableList()
        if (index in newImages.indices) {
            newImages.removeAt(index)
            _selectedImages.value = newImages
            updateFormField("imageUrls", convertBitmapsToUrls(newImages))
        }
    }

    private fun convertBitmapsToUrls(bitmaps: List<Bitmap>): List<String> {
        return bitmaps.mapIndexed { index, bitmap ->
            saveImageToCache(bitmap, "product_$index")
        }
    }

    private fun saveImageToCache(bitmap: Bitmap, fileName: String): String {
        return try {
            val file = File(context.cacheDir, "$fileName.jpg")
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 85, out)
            }
            // Compress image to 400 KBPS target
            val compressedPath = ImageCompressionUtil.compressImage(
                context = context,
                imagePath = file.absolutePath,
                targetKbps = 400
            )
            compressedPath ?: file.absolutePath
        } catch (e: Exception) {
            ""
        }
    }

    fun postProduct(token: String, sellerId: String) {
        viewModelScope.launch {
            try {
                // Validate form before posting
                if (!isFormValid()) {
                    _postState.emit(UiState.Error("Please fill in all required fields"))
                    return@launch
                }

                // Check if user can post more products (max 5)
                if (!productRepository.canPostProduct(sellerId)) {
                    val errorMsg = "You have reached the maximum limit of 5 products. Delete or update existing products to add more."
                    _maxProductsError.emit(errorMsg)
                    _postState.emit(UiState.Error(errorMsg))
                    _canPostProduct.emit(false)
                    return@launch
                }

                _postState.emit(UiState.Loading)
                val state = _formState.value

                // Step 1: Save to local database first
                val productId = "product_${UUID.randomUUID()}"
                val product = ProductEntity(
                    id = productId,
                    sellerId = sellerId,
                    sellerName = "",
                    sellerPhotoUrl = "",
                    title = state.title,
                    description = state.description,
                    price = state.price,
                    imageUrl = state.imageUrls.firstOrNull() ?: "",
                    imageUrls = state.imageUrls.joinToString(","),
                    category = state.category,
                    brand = state.brand,
                    condition = state.condition,
                    location = state.location,
                    latitude = state.latitude,
                    longitude = state.longitude,
                    country = state.country,
                    city = state.city,
                    deliveryOptions = Json.encodeToString(state.deliveryOptions),
                    returnPolicy = state.returnPolicy,
                    tags = Json.encodeToString(state.tags),
                    productAttributes = Json.encodeToString(state.productAttributes),
                    isNew = state.isNew,
                    boostListing = state.boostListing,
                    discountPrice = state.discountPrice,
                    uploadProgress = 0,
                    uploadStatus = "uploading",
                    isSynced = false,
                    createdAt = System.currentTimeMillis().toString()
                )

                // Save to local database
                productDao.insertProduct(product)

                // Add to pending syncs
                val syncEntity = PendingSyncEntity(
                    entityType = "product",
                    entityId = productId,
                    operation = "create",
                    data = Json.encodeToString(mapOf(
                        "product" to product,
                        "imageUrls" to state.imageUrls
                    )),
                    createdAt = System.currentTimeMillis().toString(),
                    retryCount = 0
                )
                pendingSyncDao.insertPendingSync(syncEntity)

                // Step 2: Create request for Supabase
                val createRequest = ProductCreateRequest(
                    title = state.title,
                    description = state.description,
                    price = state.price,
                    category = state.category,
                    imageUrl = state.imageUrls.firstOrNull() ?: "",
                    imageUrls = state.imageUrls,
                    brand = state.brand,
                    condition = state.condition,
                    location = state.location,
                    latitude = state.latitude,
                    longitude = state.longitude,
                    country = state.country,
                    city = state.city,
                    deliveryOptions = state.deliveryOptions,
                    returnPolicy = state.returnPolicy,
                    tags = state.tags,
                    productAttributes = state.productAttributes,
                    isNew = state.isNew,
                    boostListing = state.boostListing,
                    discountPrice = state.discountPrice
                )

                // Step 3: Upload images with progress tracking
                val uploadedImageUrls = mutableListOf<String>()
                state.imageUrls.forEachIndexed { index, imagePath ->
                    _uploadProgress.emit(_uploadProgress.value.copy(
                        totalFiles = state.imageUrls.size,
                        uploadedFiles = index,
                        progress = ((index + 1) * 100) / state.imageUrls.size,
                        currentFile = "image_${index + 1}.jpg"
                    ))
                    // Simulate upload
                    delay(500)
                    uploadedImageUrls.add("https://storage.example.com/products/$productId/image_$index.jpg")
                }

                // Step 4: Update product with uploaded image URLs
                val updatedProduct = product.copy(
                    uploadedImageUrls = uploadedImageUrls.firstOrNull() ?: "",
                    imageUrls = uploadedImageUrls.joinToString(","),
                    uploadStatus = "completed",
                    uploadProgress = 100,
                    isSynced = true
                )
                productDao.updateProduct(updatedProduct)

                // Step 5: Remove from pending syncs
                pendingSyncDao.deletePendingSync(syncEntity)

                _postState.emit(UiState.Success(updatedProduct))
            } catch (e: Exception) {
                _postState.emit(UiState.Error(e.message ?: "Unknown error"))
            }
        }
    }

    fun retryFailedSync(syncId: Int) {
        viewModelScope.launch {
            try {
                val syncEntity = pendingSyncDao.getPendingSyncById(syncId)
                if (syncEntity != null) {
                    val retryEntity = syncEntity.copy(retryCount = syncEntity.retryCount + 1)
                    pendingSyncDao.updatePendingSync(retryEntity)
                    // Retry logic will be handled by sync service
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun resetForm() {
        _formState.value = PostFormState()
        _selectedImages.value = emptyList()
        _uploadProgress.value = UploadProgress()
        _postState.value = UiState.Idle
        _currentLocation.value = null
    }

    fun getCurrentLocation() {
        viewModelScope.launch {
            try {
                val location = LocationUtil.getCurrentLocation(context)
                if (location != null) {
                    _currentLocation.value = location
                    updateFormField("location", LocationUtil.formatLocation(location))
                    updateFormField("latitude", location.latitude)
                    updateFormField("longitude", location.longitude)
                    // Extract and set country and city
                    updateFormField("country", location.country)
                    updateFormField("city", location.city)
                }
            } catch (e: Exception) {
                // Handle error - location not available
            }
        }
    }

    fun searchLocation(query: String) {
        viewModelScope.launch {
            try {
                val location = LocationUtil.getLocationFromAddress(context, query)
                if (location != null) {
                    _currentLocation.value = location
                    updateFormField("location", LocationUtil.formatLocation(location))
                    updateFormField("latitude", location.latitude)
                    updateFormField("longitude", location.longitude)
                    // Extract and set country and city
                    updateFormField("country", location.country)
                    updateFormField("city", location.city)
                }
            } catch (e: Exception) {
                // Handle error - location search failed
            }
        }
    }

    private fun isFormValid(): Boolean {
        val state = _formState.value
        return state.title.isNotBlank() && state.title.length >= 5 &&
                state.description.isNotBlank() && state.description.length >= 20 &&
                state.category.isNotBlank() &&
                state.brand.isNotBlank() &&
                state.price > 0 &&
                state.price <= 10000000 && // Max price validation
                state.location.isNotBlank() &&
                state.country.isNotBlank() &&
                state.city.isNotBlank() &&
                state.imageUrls.isNotEmpty() &&
                state.imageUrls.size <= 10 // Max 10 images
    }
}

// Extension to add uploadedImageUrls field (for temporary use in ViewModel)
private fun ProductEntity.copy(uploadedImageUrls: String = imageUrl): ProductEntity {
    return this.copy(imageUrl = uploadedImageUrls)
}
