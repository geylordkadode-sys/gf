package com.berling.marketplace.ui.screens.post

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

data class LocationResult(
    val name: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val city: String = "",
    val state: String = "",
    val country: String = ""
)

@Composable
fun LocationSearchField(
    location: String,
    onLocationChange: (String) -> Unit,
    onLocationSelected: (LocationResult) -> Unit,
    modifier: Modifier = Modifier,
    errorMessage: String = ""
) {
    var isExpanded by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf(location) }
    var searchResults by remember { mutableStateOf<List<LocationResult>>(emptyList()) }
    var isSearching by remember { mutableStateOf(false) }

    LaunchedEffect(searchQuery) {
        if (searchQuery.isNotEmpty() && searchQuery.length > 2) {
            isSearching = true
            delay(500) // Debounce
            searchResults = performLocationSearch(searchQuery)
            isSearching = false
        } else {
            searchResults = emptyList()
        }
    }

    Box(modifier = modifier) {
        Column {
            TextField(
                value = searchQuery,
                onValueChange = {
                    searchQuery = it
                    onLocationChange(it)
                },
                label = { Text("Location") },
                placeholder = { Text("Search location...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = true },
                leadingIcon = {
                    Icon(Icons.Default.LocationOn, contentDescription = "Location")
                },
                trailingIcon = {
                    if (isSearching) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp))
                    } else if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = {
                            searchQuery = ""
                            onLocationChange("")
                            searchResults = emptyList()
                        }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                isError = errorMessage.isNotEmpty(),
                singleLine = true,
                shape = RoundedCornerShape(8.dp)
            )

            if (errorMessage.isNotEmpty()) {
                Text(
                    errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                )
            }

            if (isExpanded && searchResults.isNotEmpty()) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(8.dp),
                    shadowElevation = 4.dp
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 200.dp)
                    ) {
                        items(searchResults) { location ->
                            LocationResultItem(
                                location = location,
                                onSelect = {
                                    onLocationSelected(location)
                                    searchQuery = location.name
                                    onLocationChange(location.name)
                                    isExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LocationResultItem(
    location: LocationResult,
    onSelect: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSelect)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            Icons.Default.LocationOn,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )

        Column(modifier = Modifier.weight(1f)) {
            Text(
                location.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                "${location.address}${if (location.city.isNotEmpty()) ", ${location.city}" else ""}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
    }

    Divider(modifier = Modifier.padding(horizontal = 12.dp))
}

@Composable
fun LocationPickerDialog(
    onLocationSelected: (LocationResult) -> Unit,
    onDismiss: () -> Unit,
    onUseCurrentLocation: () -> Unit = {},
    isOpen: Boolean = true
) {
    if (isOpen) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Select Location") },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        "Choose how to set location:",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    Button(
                        onClick = {
                            // Trigger real location retrieval from ViewModel
                            onUseCurrentLocation()
                            onDismiss()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    ) {
                        Icon(
                            Icons.Default.MyLocation,
                            contentDescription = null,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text("Use Current Location")
                    }

                    Button(
                        onClick = { onDismiss() },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors()
                    ) {
                        Icon(
                            Icons.Default.LocationOn,
                            contentDescription = null,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text("Search Location")
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun LocationChip(
    location: String,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                Icons.Default.LocationOn,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
            Text(location, style = MaterialTheme.typography.labelSmall)
            IconButton(
                onClick = onRemove,
                modifier = Modifier.size(20.dp)
            ) {
                Icon(Icons.Default.Clear, contentDescription = "Remove", modifier = Modifier.size(14.dp))
            }
        }
    }
}

// Mock location search function - in real app, use Google Places API or OSM Nominatim
private suspend fun performLocationSearch(query: String): List<LocationResult> {
    // This is a placeholder - in production, you would use LocationUtil.getLocationFromAddress()
    // For now, return empty list as real location search requires context
    return emptyList()
}
