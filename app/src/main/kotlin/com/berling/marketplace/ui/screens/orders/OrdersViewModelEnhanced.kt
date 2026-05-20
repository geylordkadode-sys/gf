package com.berling.marketplace.ui.screens.orders

import androidx.lifecycle.viewModelScope
import com.berling.marketplace.data.local.entities.OrderEntity
import com.berling.marketplace.data.models.PaymentRequest
import com.berling.marketplace.data.repository.OrderRepository
import com.berling.marketplace.data.repository.PaymentRepository
import com.berling.marketplace.data.repository.NotificationRepository
import com.berling.marketplace.data.repository.AuthenticationRepository
import com.berling.marketplace.ui.screens.BaseViewModel
import com.berling.marketplace.ui.screens.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OrdersViewModelEnhanced @Inject constructor(
    private val orderRepository: OrderRepository,
    private val paymentRepository: PaymentRepository,
    private val notificationRepository: NotificationRepository,
    private val authRepository: AuthenticationRepository
) : BaseViewModel() {

    private val _buyerOrdersState = MutableStateFlow<UiState<List<OrderEntity>>>(UiState.Loading)
    val buyerOrdersState: StateFlow<UiState<List<OrderEntity>>> = _buyerOrdersState

    private val _sellerOrdersState = MutableStateFlow<UiState<List<OrderEntity>>>(UiState.Loading)
    val sellerOrdersState: StateFlow<UiState<List<OrderEntity>>> = _sellerOrdersState

    private val _selectedOrderState = MutableStateFlow<UiState<OrderEntity>>(UiState.Loading)
    val selectedOrderState: StateFlow<UiState<OrderEntity>> = _selectedOrderState

    private val _paymentState = MutableStateFlow<UiState<String>>(UiState.Loading)
    val paymentState: StateFlow<UiState<String>> = _paymentState

    private val _selectedStatus = MutableStateFlow("all")
    val selectedStatus: StateFlow<String> = _selectedStatus

    private val _supportedGateways = MutableStateFlow<List<String>>(emptyList())
    val supportedGateways: StateFlow<List<String>> = _supportedGateways

    init {
        _supportedGateways.value = paymentRepository.getSupportedGateways()
    }

    fun loadBuyerOrders() {
        viewModelScope.launch {
            _buyerOrdersState.emit(UiState.Loading)
            try {
                val userId = authRepository.getCurrentUserOrNull()?.id
                    ?: return@launch
                orderRepository.getBuyerOrders(userId).collect { orders ->
                    _buyerOrdersState.emit(UiState.Success(orders))
                }
            } catch (e: Exception) {
                _buyerOrdersState.emit(UiState.Error(e.message ?: "Error loading orders"))
            }
        }
    }

    fun loadSellerOrders() {
        viewModelScope.launch {
            _sellerOrdersState.emit(UiState.Loading)
            try {
                val userId = authRepository.getCurrentUserOrNull()?.id
                    ?: return@launch
                orderRepository.getSellerOrders(userId).collect { orders ->
                    _sellerOrdersState.emit(UiState.Success(orders))
                }
            } catch (e: Exception) {
                _sellerOrdersState.emit(UiState.Error(e.message ?: "Error loading orders"))
            }
        }
    }

    fun processPayment(orderId: String, amount: Double, gateway: String = "razorpay") {
        viewModelScope.launch {
            _paymentState.emit(UiState.Loading)
            try {
                val result = paymentRepository.initiatePayment(orderId, amount, gateway)
                result.onSuccess { response ->
                    _paymentState.emit(UiState.Success(response.paymentId))
                    logInfo("Payment initiated: ${response.paymentId}")
                }
                result.onFailure { error ->
                    _paymentState.emit(UiState.Error(error.message ?: "Payment failed"))
                }
            } catch (e: Exception) {
                _paymentState.emit(UiState.Error(e.message ?: "Error processing payment"))
            }
        }
    }

    fun verifyPayment(paymentId: String, orderId: String, signature: String) {
        viewModelScope.launch {
            try {
                val result = paymentRepository.verifyPayment(paymentId, orderId, signature)
                result.onSuccess {
                    orderRepository.updateOrderStatus(orderId, "paid")
                    loadOrderById(orderId)
                    
                    // Get order details to notify seller
                    val order = orderRepository.getOrderById(orderId)
                    if (order != null) {
                        notificationRepository.notifyPaymentReceived(
                            orderId,
                            order.sellerId,
                            order.price
                        )
                    }
                    logInfo("Payment verified successfully")
                }
                result.onFailure { error ->
                    logError("Payment verification failed: ${error.message}")
                }
            } catch (e: Exception) {
                logError("Error verifying payment: ${e.message}")
            }
        }
    }

    fun updateOrderStatus(orderId: String, status: String) {
        viewModelScope.launch {
            try {
                orderRepository.updateOrderStatus(orderId, status)
                loadOrderById(orderId)
                logInfo("Order status updated to $status")
            } catch (e: Exception) {
                logError("Error updating order status: ${e.message}")
            }
        }
    }

    fun loadOrderById(orderId: String) {
        viewModelScope.launch {
            _selectedOrderState.emit(UiState.Loading)
            try {
                val order = orderRepository.getOrderById(orderId)
                if (order != null) {
                    _selectedOrderState.emit(UiState.Success(order))
                } else {
                    _selectedOrderState.emit(UiState.Error("Order not found"))
                }
            } catch (e: Exception) {
                _selectedOrderState.emit(UiState.Error(e.message ?: "Error loading order"))
            }
        }
    }

    fun filterByStatus(status: String) {
        _selectedStatus.value = status
    }

    fun isGatewayAuthorized(gateway: String): Boolean {
        return paymentRepository.isGatewayAuthorized(gateway)
    }
}
