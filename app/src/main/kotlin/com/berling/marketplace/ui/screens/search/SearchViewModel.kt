package com.berling.marketplace.ui.screens.search

import androidx.lifecycle.viewModelScope
import com.berling.marketplace.data.local.entities.ProductEntity
import com.berling.marketplace.data.repository.ProductRepository
import com.berling.marketplace.ui.screens.BaseViewModel
import com.berling.marketplace.ui.screens.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val productRepository: ProductRepository
) : BaseViewModel() {

    private val _searchResults = MutableStateFlow<UiState<List<ProductEntity>>>(UiState.Idle)
    val searchResults: StateFlow<UiState<List<ProductEntity>>> = _searchResults

    private val _searchHistory = MutableStateFlow<List<String>>(emptyList())
    val searchHistory: StateFlow<List<String>> = _searchHistory

    private val _selectedCategory = MutableStateFlow<String?>(null)
    val selectedCategory: StateFlow<String?> = _selectedCategory

    private val _minPrice = MutableStateFlow(0.0)
    val minPrice: StateFlow<Double> = _minPrice

    private val _maxPrice = MutableStateFlow(100000.0)
    val maxPrice: StateFlow<Double> = _maxPrice

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _countries = MutableStateFlow<List<String>>(emptyList())
    val countries: StateFlow<List<String>> = _countries

    private val _selectedCountry = MutableStateFlow<String?>(null)
    val selectedCountry: StateFlow<String?> = _selectedCountry

    init {
        loadCountries()
    }

    fun searchProducts(query: String) {
        if (query.isBlank()) {
            _searchResults.value = UiState.Idle
            return
        }

        _searchQuery.value = query
        addToSearchHistory(query)

        viewModelScope.launch {
            try {
                _searchResults.emit(UiState.Loading)
                productRepository.searchProducts(query).collect { products ->
                    // Apply additional filters if selected
                    val filtered = applyFilters(products)
                    _searchResults.emit(UiState.Success(filtered))
                }
            } catch (e: Exception) {
                _searchResults.emit(UiState.Error(e.message ?: "Search failed"))
            }
        }
    }

    fun searchByLocation(location: String) {
        if (location.isBlank()) {
            _searchResults.value = UiState.Idle
            return
        }

        viewModelScope.launch {
            try {
                _searchResults.emit(UiState.Loading)
                productRepository.searchByLocation(location).collect { products ->
                    val filtered = applyFilters(products)
                    _searchResults.emit(UiState.Success(filtered))
                }
            } catch (e: Exception) {
                _searchResults.emit(UiState.Error(e.message ?: "Location search failed"))
            }
        }
    }

    fun searchByCategory(category: String) {
        viewModelScope.launch {
            try {
                _selectedCategory.emit(category)
                _searchResults.emit(UiState.Loading)
                productRepository.searchByCategory(category).collect { products ->
                    val filtered = applyFilters(products)
                    _searchResults.emit(UiState.Success(filtered))
                }
            } catch (e: Exception) {
                _searchResults.emit(UiState.Error(e.message ?: "Category search failed"))
            }
        }
    }

    fun searchByCountry(country: String) {
        viewModelScope.launch {
            try {
                _selectedCountry.emit(country)
                _searchResults.emit(UiState.Loading)
                productRepository.searchByCountry(country).collect { products ->
                    val filtered = applyFilters(products)
                    _searchResults.emit(UiState.Success(filtered))
                }
            } catch (e: Exception) {
                _searchResults.emit(UiState.Error(e.message ?: "Country search failed"))
            }
        }
    }

    fun searchByPriceRange(minPrice: Double, maxPrice: Double) {
        viewModelScope.launch {
            try {
                _minPrice.emit(minPrice)
                _maxPrice.emit(maxPrice)
                _searchResults.emit(UiState.Loading)
                productRepository.searchByPriceRange(minPrice, maxPrice).collect { products ->
                    val filtered = applyFilters(products)
                    _searchResults.emit(UiState.Success(filtered))
                }
            } catch (e: Exception) {
                _searchResults.emit(UiState.Error(e.message ?: "Price filter failed"))
            }
        }
    }

    fun clearFilters() {
        viewModelScope.launch {
            _selectedCategory.emit(null)
            _selectedCountry.emit(null)
            _minPrice.emit(0.0)
            _maxPrice.emit(100000.0)
            _searchQuery.emit("")
            _searchResults.emit(UiState.Idle)
        }
    }

    private fun applyFilters(products: List<ProductEntity>): List<ProductEntity> {
        var filtered = products

        // Apply category filter
        _selectedCategory.value?.let { category ->
            filtered = filtered.filter { it.category == category }
        }

        // Apply country filter
        _selectedCountry.value?.let { country ->
            filtered = filtered.filter { it.country == country }
        }

        // Apply price range filter
        val minPrice = _minPrice.value
        val maxPrice = _maxPrice.value
        if (minPrice > 0.0 || maxPrice < 100000.0) {
            filtered = filtered.filter { it.price in minPrice..maxPrice }
        }

        return filtered
    }

    private fun addToSearchHistory(query: String) {
        val currentHistory = _searchHistory.value.toMutableList()
        currentHistory.remove(query) // Remove if already exists
        currentHistory.add(0, query) // Add to front
        if (currentHistory.size > 10) {
            currentHistory.removeLast() // Keep only last 10
        }
        viewModelScope.launch {
            _searchHistory.emit(currentHistory)
        }
    }

    private fun loadCountries() {
        viewModelScope.launch {
            try {
                productRepository.getAllCountries().collect { countries ->
                    _countries.emit(countries)
                }
            } catch (e: Exception) {
                // Handle error - countries not loaded
            }
        }
    }
}
