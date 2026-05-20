@file:OptIn(ExperimentalMaterial3Api::class)
package com.berling.marketplace.ui.screens.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.berling.marketplace.data.local.entities.ProductEntity
import com.berling.marketplace.ui.screens.BottomNavigationBar
import com.berling.marketplace.ui.screens.UiState

@Composable
fun SearchScreen(
    navController: NavHostController,
    viewModel: SearchViewModel = hiltViewModel()
) {
    var searchQuery by remember { mutableStateOf("") }
    val searchResults by viewModel.searchResults.collectAsState()
    val searchHistory by viewModel.searchHistory.collectAsState()
    val countries by viewModel.countries.collectAsState()
    val selectedCountry by viewModel.selectedCountry.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val minPrice by viewModel.minPrice.collectAsState()
    val maxPrice by viewModel.maxPrice.collectAsState()

    var showFilters by remember { mutableStateOf(false) }
    var localMinPrice by remember { mutableStateOf(minPrice.toString()) }
    var localMaxPrice by remember { mutableStateOf(maxPrice.toString()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { 
                            searchQuery = it
                            if (it.isNotEmpty()) {
                                viewModel.searchProducts(it)
                            }
                        },
                        placeholder = { Text("Search products, brands...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true
                    )
                }
            )
        },
        bottomBar = { BottomNavigationBar(navController, "search") }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Filter and Sort Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilterChip(
                    selected = showFilters,
                    onClick = { showFilters = !showFilters },
                    label = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Filters")
                        }
                    }
                )

                if (selectedCategory != null) {
                    FilterChip(
                        selected = true,
                        onClick = { viewModel.clearFilters() },
                        label = { Text(selectedCategory!!) },
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear",
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    )
                }

                if (selectedCountry != null) {
                    FilterChip(
                        selected = true,
                        onClick = { viewModel.clearFilters() },
                        label = { Text(selectedCountry!!) },
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear",
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    )
                }
            }

            // Filters Panel
            if (showFilters) {
                FiltersPanel(
                    countries = countries,
                    selectedCountry = selectedCountry,
                    minPrice = localMinPrice,
                    maxPrice = localMaxPrice,
                    onCountrySelected = { country ->
                        viewModel.searchByCountry(country)
                    },
                    onPriceRangeApplied = { min, max ->
                        localMinPrice = min
                        localMaxPrice = max
                        viewModel.searchByPriceRange(min.toDouble(), max.toDouble())
                    },
                    onClearFilters = {
                        viewModel.clearFilters()
                        showFilters = false
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                )
            }

            // Search Results or History
            when {
                searchQuery.isEmpty() && searchResults is UiState.Idle -> {
                    // Show search history
                    SearchHistory(
                        history = searchHistory,
                        onHistoryItemClick = { item ->
                            searchQuery = item
                            viewModel.searchProducts(item)
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
                searchResults is UiState.Loading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                searchResults is UiState.Success -> {
                    val products = (searchResults as UiState.Success<List<ProductEntity>>).data
                    if (products.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Default.SearchOff,
                                    contentDescription = null,
                                    modifier = Modifier.size(64.dp)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text("No products found", fontWeight = FontWeight.Medium)
                                Text("Try adjusting your filters or search query")
                            }
                        }
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(products) { product ->
                                ProductSearchCard(
                                    product = product,
                                    onClick = {
                                        navController.navigate("product_detail/${product.id}")
                                    }
                                )
                            }
                        }
                    }
                }
                searchResults is UiState.Error -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            (searchResults as UiState.Error).message,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchHistory(
    history: List<String>,
    onHistoryItemClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (history.isEmpty()) {
        Box(
            modifier = modifier,
            contentAlignment = Alignment.Center
        ) {
            Text("No search history", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    } else {
        LazyColumn(modifier = modifier.padding(16.dp)) {
            item {
                Text(
                    "Recent Searches",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }

            items(history) { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.History,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            item,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onHistoryItemClick(item) }
                        )
                    }

                    IconButton(onClick = { /* TODO: Remove from history */ }) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Remove",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Divider()
            }
        }
    }
}

@Composable
private fun FiltersPanel(
    countries: List<String>,
    selectedCountry: String?,
    minPrice: String,
    maxPrice: String,
    onCountrySelected: (String) -> Unit,
    onPriceRangeApplied: (String, String) -> Unit,
    onClearFilters: () -> Unit,
    modifier: Modifier = Modifier
) {
    var localMinPrice by remember { mutableStateOf(minPrice) }
    var localMaxPrice by remember { mutableStateOf(maxPrice) }
    var expandedCountries by remember { mutableStateOf(false) }

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Filters", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

            Spacer(modifier = Modifier.height(16.dp))

            // Country Filter
            Text("Country", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium)
            ExposedDropdownMenuBox(
                expanded = expandedCountries,
                onExpandedChange = { expandedCountries = !expandedCountries },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = selectedCountry ?: "All Countries",
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCountries) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    shape = RoundedCornerShape(8.dp)
                )

                ExposedDropdownMenu(
                    expanded = expandedCountries,
                    onDismissRequest = { expandedCountries = false }
                ) {
                    countries.forEach { country ->
                        DropdownMenuItem(
                            text = { Text(country) },
                            onClick = {
                                onCountrySelected(country)
                                expandedCountries = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Price Range Filter
            Text("Price Range", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = localMinPrice,
                    onValueChange = { localMinPrice = it },
                    label = { Text("Min") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp)
                )

                OutlinedTextField(
                    value = localMaxPrice,
                    onValueChange = { localMaxPrice = it },
                    label = { Text("Max") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onClearFilters,
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp)
                ) {
                    Text("Clear")
                }

                Button(
                    onClick = {
                        onPriceRangeApplied(localMinPrice, localMaxPrice)
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp)
                ) {
                    Text("Apply")
                }
            }
        }
    }
}

@Composable
private fun ProductSearchCard(
    product: ProductEntity,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Product Image
            if (product.imageUrl.isNotEmpty()) {
                AsyncImage(
                    model = product.imageUrl,
                    contentDescription = product.title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Image, contentDescription = null)
                }
            }

            Column(modifier = Modifier.padding(12.dp)) {
                // Product Title
                Text(
                    product.title,
                    style = MaterialTheme.typography.labelLarge,
                    maxLines = 2,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Location
                if (product.city.isNotEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.LocationOn,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "${product.city}, ${product.country}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                }

                // Price
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        if (product.discountPrice > 0 && product.discountPrice < product.price) {
                            Text(
                                "₹${product.discountPrice.toInt()}",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                "₹${product.price.toInt()}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier
                            )
                        } else {
                            Text(
                                "₹${product.price.toInt()}",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    if (product.condition.isNotEmpty()) {
                        Text(
                            product.condition,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
