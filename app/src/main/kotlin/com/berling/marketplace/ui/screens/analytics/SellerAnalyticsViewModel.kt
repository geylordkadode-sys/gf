package com.berling.marketplace.ui.screens.analytics

import androidx.lifecycle.viewModelScope
import com.berling.marketplace.data.repository.AnalyticsRepository
import com.berling.marketplace.data.repository.OrderRepository
import com.berling.marketplace.data.repository.AuthenticationRepository
import com.berling.marketplace.ui.screens.BaseViewModel
import com.berling.marketplace.ui.screens.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import javax.inject.Inject

data class SellerMetrics(
    val totalRevenue: Double = 0.0,
    val totalOrders: Int = 0,
    val completedOrders: Int = 0,
    val pendingOrders: Int = 0,
    val failedOrders: Int = 0,
    val avgOrderValue: Double = 0.0,
    val conversionRate: Double = 0.0,
    val customerSatisfaction: Double = 0.0,
    val refundRate: Double = 0.0,
    val averageProcessingTime: Double = 0.0
)

data class DailyRevenue(
    val date: LocalDate,
    val amount: Double,
    val orderCount: Int
)

@HiltViewModel
class SellerAnalyticsViewModel @Inject constructor(
    private val analyticsRepository: AnalyticsRepository,
    private val orderRepository: OrderRepository,
    private val authRepository: AuthenticationRepository
) : BaseViewModel() {

    private val _sellerMetrics = MutableStateFlow<UiState<SellerMetrics>>(UiState.Loading)
    val sellerMetrics: StateFlow<UiState<SellerMetrics>> = _sellerMetrics

    private val _dailyRevenueState = MutableStateFlow<UiState<List<DailyRevenue>>>(UiState.Loading)
    val dailyRevenueState: StateFlow<UiState<List<DailyRevenue>>> = _dailyRevenueState

    private val _topProductsState = MutableStateFlow<UiState<List<Pair<String, Int>>>>(UiState.Loading)
    val topProductsState: StateFlow<UiState<List<Pair<String, Int>>>> = _topProductsState

    private val _selectedPeriod = MutableStateFlow("30days")
    val selectedPeriod: StateFlow<String> = _selectedPeriod

    private val _refreshState = MutableStateFlow<UiState<Unit>>(UiState.Success(Unit))
    val refreshState: StateFlow<UiState<Unit>> = _refreshState

    init {
        loadAllAnalytics()
    }

    fun loadAllAnalytics() {
        viewModelScope.launch {
            try {
                val sellerId = authRepository.getCurrentUserOrNull()?.id
                    ?: return@launch

                // Load metrics in parallel
                loadSellerMetrics(sellerId)
                loadDailyRevenue(sellerId)
                loadTopProducts(sellerId)
            } catch (e: Exception) {
                logError("Error loading analytics: ${e.message}")
            }
        }
    }

    private fun loadSellerMetrics(sellerId: String) {
        viewModelScope.launch {
            _sellerMetrics.emit(UiState.Loading)
            try {
                orderRepository.getSellerOrders(sellerId).collect { orders ->
                    val totalOrders = orders.size
                    val completedOrders = orders.count { it.status == "delivered" }
                    val pendingOrders = orders.count { it.status == "pending" || it.status == "processing" }
                    val failedOrders = orders.count { it.status == "payment_failed" || it.status == "cancelled" }
                    val refundedOrders = orders.count { it.status == "refunded" }

                    val totalRevenue = orders.filter { it.status == "delivered" }
                        .sumOf { it.price }
                    val avgOrderValue = if (totalOrders > 0) totalRevenue / totalOrders else 0.0
                    val conversionRate = if (totalOrders > 0) (completedOrders.toDouble() / totalOrders) * 100 else 0.0
                    val refundRate = if (totalOrders > 0) (refundedOrders.toDouble() / totalOrders) * 100 else 0.0

                    val metrics = SellerMetrics(
                        totalRevenue = totalRevenue,
                        totalOrders = totalOrders,
                        completedOrders = completedOrders,
                        pendingOrders = pendingOrders,
                        failedOrders = failedOrders,
                        avgOrderValue = avgOrderValue,
                        conversionRate = conversionRate,
                        customerSatisfaction = 4.5, // Mock - would come from reviews
                        refundRate = refundRate,
                        averageProcessingTime = 2.0 // Mock - would calculate from order data
                    )

                    _sellerMetrics.emit(UiState.Success(metrics))
                    logInfo("Seller metrics loaded successfully")
                }
            } catch (e: Exception) {
                _sellerMetrics.emit(UiState.Error(e.message ?: "Error loading metrics"))
            }
        }
    }

    private fun loadDailyRevenue(sellerId: String) {
        viewModelScope.launch {
            _dailyRevenueState.emit(UiState.Loading)
            try {
                orderRepository.getSellerOrders(sellerId).collect { orders ->
                    val dailyMap = mutableMapOf<LocalDate, Pair<Double, Int>>()

                    orders.filter { it.status == "delivered" }.forEach { order ->
                        val date = try {
                            LocalDateTime.parse(order.createdAt).toLocalDate()
                        } catch (e: Exception) {
                            LocalDate.now()
                        }

                        val current = dailyMap[date] ?: Pair(0.0, 0)
                        dailyMap[date] = Pair(current.first + order.price, current.second + 1)
                    }

                    val dailyRevenue = dailyMap
                        .map { (date, pair) -> DailyRevenue(date, pair.first, pair.second) }
                        .sortedBy { it.date }
                        .takeLast(30)

                    _dailyRevenueState.emit(UiState.Success(dailyRevenue))
                    logInfo("Daily revenue loaded: ${dailyRevenue.size} days")
                }
            } catch (e: Exception) {
                _dailyRevenueState.emit(UiState.Error(e.message ?: "Error loading revenue"))
            }
        }
    }

    private fun loadTopProducts(sellerId: String) {
        viewModelScope.launch {
            _topProductsState.emit(UiState.Loading)
            try {
                orderRepository.getSellerOrders(sellerId).collect { orders ->
                    val productSales = orders
                        .filter { it.status == "delivered" }
                        .groupingBy { it.productId }
                        .eachCount()
                        .toList()
                        .sortedByDescending { it.second }
                        .take(10)

                    _topProductsState.emit(UiState.Success(productSales))
                    logInfo("Top products loaded: ${productSales.size} products")
                }
            } catch (e: Exception) {
                _topProductsState.emit(UiState.Error(e.message ?: "Error loading products"))
            }
        }
    }

    fun setPeriod(period: String) {
        _selectedPeriod.value = period
        loadAllAnalytics()
    }

    fun refreshAnalytics() {
        viewModelScope.launch {
            _refreshState.emit(UiState.Loading)
            try {
                loadAllAnalytics()
                _refreshState.emit(UiState.Success(Unit))
                logInfo("Analytics refreshed")
            } catch (e: Exception) {
                _refreshState.emit(UiState.Error(e.message ?: "Refresh failed"))
            }
        }
    }

    fun getRevenueFormatted(): String {
        val metrics = (_sellerMetrics.value as? UiState.Success)?.data
        return metrics?.let { String.format("₹%.2f", it.totalRevenue) } ?: "₹0.00"
    }

    fun getConversionRateFormatted(): String {
        val metrics = (_sellerMetrics.value as? UiState.Success)?.data
        return metrics?.let { String.format("%.1f%%", it.conversionRate) } ?: "0.0%"
    }

    fun getAverageOrderValueFormatted(): String {
        val metrics = (_sellerMetrics.value as? UiState.Success)?.data
        return metrics?.let { String.format("₹%.2f", it.avgOrderValue) } ?: "₹0.00"
    }
}
