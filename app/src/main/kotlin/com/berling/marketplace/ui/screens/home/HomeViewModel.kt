package com.berling.marketplace.ui.screens.home

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
class HomeViewModel @Inject constructor(
    private val productRepository: ProductRepository
) : BaseViewModel() {

    private val _productsState = MutableStateFlow<UiState<List<ProductEntity>>>(UiState.Loading)
    val productsState: StateFlow<UiState<List<ProductEntity>>> = _productsState

    private val _selectedCategory = MutableStateFlow("Popular")
    val selectedCategory: StateFlow<String> = _selectedCategory

    init {
        loadProducts()
    }

    fun loadProducts() {
        viewModelScope.launch {
            _productsState.emit(UiState.Loading)
            productRepository.getAllProducts().collect { products ->
                _productsState.emit(UiState.Success(products))
            }
        }
    }

    fun selectCategory(category: String) {
        viewModelScope.launch {
            _selectedCategory.emit(category)
            if (category.lowercase() == "all" || category.lowercase() == "popular") {
                loadProducts()
            } else {
                _productsState.emit(UiState.Loading)
                productRepository.searchByCategory(category).collect { products ->
                    _productsState.emit(UiState.Success(products))
                }
            }
        }
    }

    fun searchProducts(query: String) {
        viewModelScope.launch {
            if (query.isBlank()) {
                loadProducts()
            } else {
                _productsState.emit(UiState.Loading)
                productRepository.searchProducts(query).collect { products ->
                    _productsState.emit(UiState.Success(products))
                }
            }
        }
    }

    fun searchByLocation(location: String) {
        viewModelScope.launch {
            if (location.isBlank()) {
                loadProducts()
            } else {
                _productsState.emit(UiState.Loading)
                productRepository.searchByLocation(location).collect { products ->
                    _productsState.emit(UiState.Success(products))
                }
            }
        }
    }
}
