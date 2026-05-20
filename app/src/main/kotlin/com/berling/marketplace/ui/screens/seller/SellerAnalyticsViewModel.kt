package com.berling.marketplace.ui.screens.seller

import androidx.lifecycle.viewModelScope
import com.berling.marketplace.data.repository.OrderRepository
import com.berling.marketplace.data.repository.AnalyticsRepository
import com.berling.marketplace.data.repository.AuthenticationRepository
import com.berling.marketplace.ui.screens.BaseViewModel
import com.berling.marketplace.ui.screens.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SellerAnalyticsViewModel @Inject constructor(
    private val orderRepository: OrderRepository,
    private val analyticsRepository: AnalyticsRepository,
    private val authRepository: AuthenticationRepository
) : BaseViewModel() {

    private val _totalSalesState = MutableStateFlow(0.0)
    val totalSalesState: StateFlow<Double> = _totalSalesState

    private val _itemsSoldState = MutableStateFlow(0)
    val itemsSoldState: StateFlow<Int> = _itemsSoldState

    private val _successRateState = MutableStateFlow(0.0)
    val successRateState: StateFlow<Double> = _successRateState

    private val _responseTimeState = MutableStateFlow("N/A")
    val responseTimeState: StateFlow<String> = _responseTimeState

    private val _repeatBuyersState = MutableStateFlow(0.0)
    val repeatBuyersState: StateFlow<Double> = _repeatBuyersState

    private val _positiveReviewsState = MutableStateFlow(0)
    val positiveReviewsState: StateFlow<Int> = _positiveReviewsState

    private val _monthlyTrendState = MutableStateFlow<Map<String, Double>>(emptyMap())
    val monthlyTrendState: StateFlow<Map<String, Double>> = _monthlyTrendState

    private val _achievementsState = MutableStateFlow<List<Achievement>>(emptyList())
    val achievementsState: StateFlow<List<Achievement>> = _achievementsState

    private val _selectedPeriod = MutableStateFlow("30days")
    val selectedPeriod: StateFlow<String> = _selectedPeriod

    data class Achievement(
        val id: String,
        val title: String,
        val description: String,
        val icon: String,
        val date: String
    )

    init {
        loadSellerAnalytics()
    }

    fun loadSellerAnalytics() {
        viewModelScope.launch {
            try {
                val sellerId = authRepository.getCurrentUserOrNull()?.id
                    ?: return@launch

                // Load total earnings
                val totalEarnings = orderRepository.getTotalEarnings(sellerId)
                _totalSalesState.emit(totalEarnings)

                // Load items sold
                val itemsSold = orderRepository.getCompletedOrdersCount(sellerId)
                _itemsSoldState.emit(itemsSold)

                // Calculate success rate (example: 98%)
                _successRateState.emit(98.0)

                // Set response time (example: 2h)
                _responseTimeState.emit("2h")

                // Set repeat buyers percentage (example: 85%)
                _repeatBuyersState.emit(85.0)

                // Set positive reviews (example: 128)
                _positiveReviewsState.emit(128)

                // Generate mock monthly trend
                val mockTrend = mapOf(
                    "Jan" to 50000.0,
                    "Feb" to 62000.0,
                    "Mar" to 75000.0,
                    "Apr" to 88000.0,
                    "May" to 125410.0
                )
                _monthlyTrendState.emit(mockTrend)

                // Load achievements
                _achievementsState.emit(
                    listOf(
                        Achievement("1", "Top Seller", "Achieved top seller status", "⭐", "May 2024"),
                        Achievement("2", "Fast Responder", "Responded within 1 hour 100 times", "⚡", "Apr 2024"),
                        Achievement("3", "100+ Sales", "Completed 100 successful sales", "🏆", "Mar 2024"),
                        Achievement("4", "5 Star Seller", "Maintained 5-star rating", "✨", "Feb 2024")
                    )
                )

                logInfo("Seller analytics loaded")
            } catch (e: Exception) {
                logError("Error loading analytics: ${e.message}")
            }
        }
    }

    fun setPeriod(period: String) {
        _selectedPeriod.value = period
        loadSellerAnalytics()
    }

    fun getMonthlySalesData(): List<Pair<String, Double>> {
        return _monthlyTrendState.value.toList()
    }
}
