package com.berling.marketplace.ui.screens.orders

import androidx.lifecycle.viewModelScope
import com.berling.marketplace.data.local.entities.OrderEntity
import com.berling.marketplace.data.repository.OrderRepository
import com.berling.marketplace.ui.screens.BaseViewModel
import com.berling.marketplace.ui.screens.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OrdersViewModel @Inject constructor(
    private val orderRepository: OrderRepository
) : BaseViewModel() {

    private val _buyerOrdersState = MutableStateFlow<UiState<List<OrderEntity>>>(UiState.Loading)
    val buyerOrdersState: StateFlow<UiState<List<OrderEntity>>> = _buyerOrdersState

    private val _sellerOrdersState = MutableStateFlow<UiState<List<OrderEntity>>>(UiState.Loading)
    val sellerOrdersState: StateFlow<UiState<List<OrderEntity>>> = _sellerOrdersState

    private val _selectedOrderState = MutableStateFlow<UiState<OrderEntity>>(UiState.Loading)
    val selectedOrderState: StateFlow<UiState<OrderEntity>> = _selectedOrderState

    private val _selectedStatus = MutableStateFlow("all")
    val selectedStatus: StateFlow<String> = _selectedStatus

    fun loadBuyerOrders(buyerId: String) {
        viewModelScope.launch {
            _buyerOrdersState.emit(UiState.Loading)
            try {
                orderRepository.getBuyerOrders(buyerId).collect { orders ->
                    _buyerOrdersState.emit(UiState.Success(orders))
                }
            } catch (e: Exception) {
                _buyerOrdersState.emit(UiState.Error(e.message ?: "Error loading orders"))
            }
        }
    }

    fun loadSellerOrders(sellerId: String) {
        viewModelScope.launch {
            _sellerOrdersState.emit(UiState.Loading)
            try {
                orderRepository.getSellerOrders(sellerId).collect { orders ->
                    _sellerOrdersState.emit(UiState.Success(orders))
                }
            } catch (e: Exception) {
                _sellerOrdersState.emit(UiState.Error(e.message ?: "Error loading orders"))
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

    fun updateOrderStatus(orderId: String, status: String) {
        viewModelScope.launch {
            try {
                orderRepository.updateOrderStatus(orderId, status)
                loadOrderById(orderId)
            } catch (e: Exception) {
                logError("Error updating order status: ${e.message}")
            }
        }
    }

    fun filterByStatus(status: String) {
        _selectedStatus.value = status
    }
}
