package com.berling.marketplace.ui.screens.profile

import androidx.lifecycle.viewModelScope
import com.berling.marketplace.data.local.entities.ProductEntity
import com.berling.marketplace.data.models.AuthUser
import com.berling.marketplace.data.repository.AuthenticationRepository
import com.berling.marketplace.data.repository.ProductRepository
import com.berling.marketplace.data.repository.OrderRepository
import com.berling.marketplace.ui.screens.BaseViewModel
import com.berling.marketplace.ui.screens.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SellerProfileViewModel @Inject constructor(
    private val authRepository: AuthenticationRepository,
    private val productRepository: ProductRepository,
    private val orderRepository: OrderRepository
) : BaseViewModel() {

    private val _userState = MutableStateFlow<UiState<AuthUser>>(UiState.Loading)
    val userState: StateFlow<UiState<AuthUser>> = _userState

    private val _productsState = MutableStateFlow<UiState<List<ProductEntity>>>(UiState.Loading)
    val productsState: StateFlow<UiState<List<ProductEntity>>> = _productsState

    private val _metricsState = MutableStateFlow<UiState<SellerMetricsData>>(UiState.Loading)
    val metricsState: StateFlow<UiState<SellerMetricsData>> = _metricsState

    private val _selectedProductTab = MutableStateFlow(0)
    val selectedProductTab: StateFlow<Int> = _selectedProductTab

    fun loadSellerProfile(sellerId: String = "") {
        viewModelScope.launch {
            try {
                val userId = if (sellerId.isEmpty()) {
                    authRepository.getCurrentUserOrNull()?.id ?: return@launch
                } else {
                    sellerId
                }

                // Load user profile
                _userState.emit(UiState.Loading)
                val user = authRepository.getCurrentUserOrNull()
                if (user != null) {
                    _userState.emit(UiState.Success(user))
                } else {
                    _userState.emit(UiState.Error("User not found"))
                }

                // Load seller products
                loadSellerProducts(userId)

                // Load seller metrics
                loadSellerMetrics(userId)
            } catch (e: Exception) {
                _userState.emit(UiState.Error(e.message ?: "Error loading profile"))
                logError("Error loading seller profile: ${e.message}")
            }
        }
    }

    private fun loadSellerProducts(sellerId: String) {
        viewModelScope.launch {
            _productsState.emit(UiState.Loading)
            try {
                productRepository.getSellerProducts(sellerId).collect { products ->
                    _productsState.emit(UiState.Success(products))
                    logInfo("Loaded ${products.size} seller products")
                }
            } catch (e: Exception) {
                _productsState.emit(UiState.Error(e.message ?: "Error loading products"))
                logError("Error loading seller products: ${e.message}")
            }
        }
    }

    private fun loadSellerMetrics(sellerId: String) {
        viewModelScope.launch {
            _metricsState.emit(UiState.Loading)
            try {
                orderRepository.getSellerOrders(sellerId).collect { orders ->
                    val totalSales = orders.filter { it.status == "delivered" }
                        .sumOf { it.price }
                    val itemsSold = orders.count { it.status == "delivered" }
                    val totalOrders = orders.size
                    val successRate = if (totalOrders > 0) {
                        ((totalOrders - orders.count { it.status == "cancelled" }).toDouble() / totalOrders) * 100
                    } else {
                        0.0
                    }

                    val metrics = SellerMetricsData(
                        totalSales = totalSales,
                        itemsSold = itemsSold,
                        successRate = successRate,
                        responseTime = "2h",
                        repeatBuyers = 85.0,
                        positiveReviews = 128
                    )

                    _metricsState.emit(UiState.Success(metrics))
                    logInfo("Seller metrics loaded")
                }
            } catch (e: Exception) {
                _metricsState.emit(UiState.Error(e.message ?: "Error loading metrics"))
                logError("Error loading seller metrics: ${e.message}")
            }
        }
    }

    fun selectProductTab(tabIndex: Int) {
        _selectedProductTab.value = tabIndex
    }

    fun updateProfile(name: String, bio: String) {
        viewModelScope.launch {
            try {
                authRepository.updateProfile(
                    mapOf(
                        "name" to name,
                        "bio" to bio
                    )
                )
                loadSellerProfile()
            } catch (e: Exception) {
                logError("Error updating profile: ${e.message}")
            }
        }
    }
}
