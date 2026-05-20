package com.berling.marketplace.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.berling.marketplace.data.local.entities.ProductEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    @Insert
    suspend fun insertProduct(product: ProductEntity)

    @Update
    suspend fun updateProduct(product: ProductEntity)

    @Query("SELECT * FROM products WHERE id = :productId")
    suspend fun getProductById(productId: String): ProductEntity?

    @Query("SELECT * FROM products ORDER BY createdAt DESC")
    fun getAllProducts(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE category = :category ORDER BY createdAt DESC")
    fun getProductsByCategory(category: String): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE sellerId = :sellerId ORDER BY createdAt DESC")
    fun getSellerProducts(sellerId: String): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE isActive = 1 ORDER BY createdAt DESC")
    fun getActiveProducts(): Flow<List<ProductEntity>>

    @Delete
    suspend fun deleteProduct(product: ProductEntity)

    @Query("DELETE FROM products WHERE id = :productId")
    suspend fun deleteProductById(productId: String)

    @Query("UPDATE products SET isActive = :isActive WHERE id = :productId")
    suspend fun updateProductStatus(productId: String, isActive: Boolean)

    @Query("SELECT * FROM products WHERE (title LIKE :query OR description LIKE :query OR brand LIKE :query OR tags LIKE :query) AND isActive = 1 ORDER BY createdAt DESC")
    fun searchProducts(query: String): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE (location LIKE :location OR city LIKE :location OR country LIKE :location) AND isActive = 1 ORDER BY createdAt DESC")
    fun searchByLocation(location: String): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE category = :category AND isActive = 1 ORDER BY createdAt DESC")
    fun searchByCategory(category: String): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE (title LIKE :query OR description LIKE :query OR brand LIKE :query) AND category = :category AND isActive = 1 ORDER BY createdAt DESC")
    fun searchByCategoryAndQuery(category: String, query: String): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE (title LIKE :query OR description LIKE :query OR brand LIKE :query OR tags LIKE :query OR location LIKE :query) AND isActive = 1 ORDER BY createdAt DESC LIMIT :limit")
    fun searchWithLimit(query: String, limit: Int = 50): Flow<List<ProductEntity>>

    @Query("SELECT COUNT(*) FROM products WHERE sellerId = :sellerId AND isActive = 1")
    suspend fun getSellerProductCount(sellerId: String): Int

    @Query("SELECT * FROM products WHERE price BETWEEN :minPrice AND :maxPrice AND isActive = 1 ORDER BY createdAt DESC")
    fun searchByPriceRange(minPrice: Double, maxPrice: Double): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE country = :country AND isActive = 1 ORDER BY createdAt DESC")
    fun searchByCountry(country: String): Flow<List<ProductEntity>>

    @Query("SELECT DISTINCT country FROM products WHERE country IS NOT NULL AND country != '' ORDER BY country")
    fun getAllCountries(): Flow<List<String>>
}
