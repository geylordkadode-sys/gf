package com.berling.marketplace.ui.screens.product

import androidx.lifecycle.viewModelScope
import com.berling.marketplace.data.local.entities.ProductEntity
import com.berling.marketplace.data.models.AuthUser
import com.berling.marketplace.data.repository.ProductRepository
import com.berling.marketplace.data.repository.AuthenticationRepository
import com.berling.marketplace.ui.screens.BaseViewModel
import com.berling.marketplace.ui.screens.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ReviewData(
    val id: String,
    val buyerId: String,
    val buyerName: String,
    val buyerPhoto: String,
    val rating: Int,
    val comment: String,
    val verifiedPurchase: Boolean,
    val daysAgo: Int,
    val timestamp: String
)

data class ProductRatings(
    val average: Double = 0.0,
    val totalReviews: Int = 0,
    val fiveStarCount: Int = 0,
    val fourStarCount: Int = 0,
    val threeStarCount: Int = 0,
    val twoStarCount: Int = 0,
    val oneStarCount: Int = 0
)

data class ProductDetailData(
    val product: ProductEntity,
    val seller: AuthUser,
    val ratings: ProductRatings,
    val reviews: List<ReviewData>,
    val relatedProducts: List<ProductEntity>,
    val isFavorite: Boolean,
    val sellerRating: Double,
    val responseRate: Int,
    val followersCount: Int
)

@HiltViewModel
class ProductDetailViewModel @Inject constructor(
    private val productRepository: ProductRepository,
    private val authRepository: AuthenticationRepository
) : BaseViewModel() {

    private val _productDetailState = MutableStateFlow<UiState<ProductDetailData>>(UiState.Loading)
    val productDetailState: StateFlow<UiState<ProductDetailData>> = _productDetailState

    private val _isFavoriteState = MutableStateFlow(false)
    val isFavoriteState: StateFlow<Boolean> = _isFavoriteState

    private val _reviewsState = MutableStateFlow<UiState<List<ReviewData>>>(UiState.Loading)
    val reviewsState: StateFlow<UiState<List<ReviewData>>> = _reviewsState

    private val _selectedImageIndex = MutableStateFlow(0)
    val selectedImageIndex: StateFlow<Int> = _selectedImageIndex

    private val _userFollowsSellerState = MutableStateFlow(false)
    val userFollowsSellerState: StateFlow<Boolean> = _userFollowsSellerState

    fun loadProductDetail(productId: String) {
        viewModelScope.launch {
            _productDetailState.emit(UiState.Loading)
            try {
                val product = productRepository.getProduct(productId)
                if (product != null) {
                    // Load seller info (mock - in real app would fetch from API)
                    val seller = AuthUser(
                        id = product.sellerId,
                        email = "seller@example.com",
                        name = product.sellerName,
                        photoUrl = product.sellerPhotoUrl,
                        role = "seller",
                        isVerified = true
                    )

                    // Load ratings and reviews (mock)
                    val ratings = ProductRatings(
                        average = 4.7,
                        totalReviews = 256,
                        fiveStarCount = 184,
                        fourStarCount = 48,
                        threeStarCount = 16,
                        twoStarCount = 5,
                        oneStarCount = 3
                    )

                    // Load mock reviews
                    val reviews = listOf(
                        ReviewData(
                            id = "review_1",
                            buyerId = "buyer_1",
                            buyerName = "Priya Singh",
                            buyerPhoto = "",
                            rating = 5,
                            comment = "Very beautiful bag 💝 The quality is amazing. Same as shown in pictures.",
                            verifiedPurchase = true,
                            daysAgo = 2,
                            timestamp = System.currentTimeMillis().toString()
                        ),
                        ReviewData(
                            id = "review_2",
                            buyerId = "buyer_2",
                            buyerName = "Neha Verma",
                            buyerPhoto = "",
                            rating = 4,
                            comment = "Good product at this price. Very spacious and stylish.",
                            verifiedPurchase = true,
                            daysAgo = 7,
                            timestamp = System.currentTimeMillis().toString()
                        ),
                        ReviewData(
                            id = "review_3",
                            buyerId = "buyer_3",
                            buyerName = "Riya Patel",
                            buyerPhoto = "",
                            rating = 5,
                            comment = "Amazing quality and fast delivery! Highly recommend.",
                            verifiedPurchase = true,
                            daysAgo = 14,
                            timestamp = System.currentTimeMillis().toString()
                        )
                    )

                    // Load related products (mock)
                    val relatedProducts = emptyList<ProductEntity>()

                    val currentUser = authRepository.getCurrentUserOrNull()
                    val isFavorite = if (currentUser != null) {
                        productRepository.isFavorite(productId, currentUser.id)
                    } else {
                        false
                    }

                    val detailData = ProductDetailData(
                        product = product,
                        seller = seller,
                        ratings = ratings,
                        reviews = reviews,
                        relatedProducts = relatedProducts,
                        isFavorite = isFavorite,
                        sellerRating = 4.9,
                        responseRate = 98,
                        followersCount = 12800
                    )

                    _productDetailState.emit(UiState.Success(detailData))
                    _isFavoriteState.emit(isFavorite)
                    _reviewsState.emit(UiState.Success(reviews))
                    logInfo("Product detail loaded successfully")
                } else {
                    _productDetailState.emit(UiState.Error("Product not found"))
                }
            } catch (e: Exception) {
                _productDetailState.emit(UiState.Error(e.message ?: "Error loading product"))
                logError("Error loading product detail: ${e.message}")
            }
        }
    }

    fun toggleFavorite(productId: String) {
        viewModelScope.launch {
            try {
                val currentUser = authRepository.getCurrentUserOrNull() ?: return@launch
                val isFav = _isFavoriteState.value

                if (isFav) {
                    productRepository.removeFromFavorites(productId, currentUser.id)
                    _isFavoriteState.emit(false)
                } else {
                    productRepository.addToFavorites(productId, currentUser.id)
                    _isFavoriteState.emit(true)
                }
                logInfo("Favorite toggled: $isFav")
            } catch (e: Exception) {
                logError("Error toggling favorite: ${e.message}")
            }
        }
    }

    fun setSelectedImage(index: Int) {
        _selectedImageIndex.value = index
    }

    fun followSeller(sellerId: String) {
        viewModelScope.launch {
            try {
                // Mock follow implementation
                _userFollowsSellerState.emit(true)
                logInfo("Following seller: $sellerId")
            } catch (e: Exception) {
                logError("Error following seller: ${e.message}")
            }
        }
    }

    fun unfollowSeller(sellerId: String) {
        viewModelScope.launch {
            try {
                _userFollowsSellerState.emit(false)
                logInfo("Unfollowed seller: $sellerId")
            } catch (e: Exception) {
                logError("Error unfollowing seller: ${e.message}")
            }
        }
    }

    fun addToCart(productId: String, quantity: Int = 1) {
        viewModelScope.launch {
            try {
                logInfo("Added to cart: $productId x$quantity")
                emitEvent(com.berling.marketplace.ui.screens.UiEvent.ShowMessage("Added to cart"))
            } catch (e: Exception) {
                logError("Error adding to cart: ${e.message}")
            }
        }
    }

    fun buyNow(productId: String) {
        viewModelScope.launch {
            try {
                logInfo("Buy now: $productId")
                emitEvent(com.berling.marketplace.ui.screens.UiEvent.Navigate("checkout/$productId"))
            } catch (e: Exception) {
                logError("Error with buy now: ${e.message}")
            }
        }
    }
}
