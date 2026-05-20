package com.berling.marketplace.ui.screens.analytics

import androidx.lifecycle.viewModelScope
import com.berling.marketplace.data.local.entities.AnalyticsEventEntity
import com.berling.marketplace.data.repository.AnalyticsRepository
import com.berling.marketplace.ui.screens.BaseViewModel
import com.berling.marketplace.ui.screens.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AnalyticsViewModel @Inject constructor(
    private val analyticsRepository: AnalyticsRepository
) : BaseViewModel() {

    private val _eventsState = MutableStateFlow<UiState<List<AnalyticsEventEntity>>>(UiState.Loading)
    val eventsState: StateFlow<UiState<List<AnalyticsEventEntity>>> = _eventsState

    private val _summaryState = MutableStateFlow<Map<String, Any>>(emptyMap())
    val summaryState: StateFlow<Map<String, Any>> = _summaryState

    private val _selectedPeriod = MutableStateFlow("7days")
    val selectedPeriod: StateFlow<String> = _selectedPeriod

    fun loadAnalytics(userId: String) {
        viewModelScope.launch {
            _eventsState.emit(UiState.Loading)
            try {
                analyticsRepository.getUserEvents(userId).collect { events ->
                    _eventsState.emit(UiState.Success(events))
                    calculateSummary(events)
                }
            } catch (e: Exception) {
                _eventsState.emit(UiState.Error(e.message ?: "Error loading analytics"))
            }
        }
    }

    private fun calculateSummary(events: List<AnalyticsEventEntity>) {
        val summary = mutableMapOf<String, Any>()
        summary["total_events"] = events.size
        summary["event_types"] = events.groupingBy { it.eventName }.eachCount()
        summary["events_synced"] = events.count { it.isSynced }
        _summaryState.value = summary
    }

    fun setPeriod(period: String) {
        _selectedPeriod.value = period
    }

    fun logEvent(eventName: String, eventData: String = "", userId: String) {
        viewModelScope.launch {
            try {
                analyticsRepository.logEvent(eventName, eventData, userId)
            } catch (e: Exception) {
                logError("Error logging event: ${e.message}")
            }
        }
    }
}
