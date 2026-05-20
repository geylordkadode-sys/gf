@file:OptIn(ExperimentalMaterial3Api::class)
package com.berling.marketplace.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.berling.marketplace.data.models.AuthUser
import com.berling.marketplace.ui.screens.UiState

@Composable
fun ProfileMenuScreen(
    navController: NavHostController,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val userState by viewModel.userState.collectAsState()
    var showDarkModeDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile Menu") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Profile header in menu
            when (userState) {
                is UiState.Success -> {
                    val user = (userState as UiState.Success<AuthUser>).data
                    item {
                        ProfileHeaderInMenu(user)
                    }
                }
                else -> {}
            }

            // Menu Items
            items(
                listOf(
                    "View Full Profile" to Icons.Default.Person,
                    "My Shop" to Icons.Default.Store,
                    "My Listings" to Icons.Default.ShoppingBag,
                    "Mark as Sold" to Icons.Default.CheckCircle,
                    "Achievements" to Icons.Default.EmojiEvents,
                    "Orders & Purchases" to Icons.Default.ShoppingCart,
                    "Saved Items" to Icons.Default.FavoriteBorder,
                    "Offers & Coupons" to Icons.Default.LocalOffer,
                    "My Reviews" to Icons.Default.RateReview,
                    "Wallet" to Icons.Default.Wallet,
                    "Settings" to Icons.Default.Settings,
                    "Help & Support" to Icons.Default.Help,
                    "Invite & Earn" to Icons.Default.PersonAdd
                )
            ) { (title, icon) ->
                MenuItemRow(
                    title = title,
                    icon = icon,
                    onClick = {
                        when (title) {
                            "View Full Profile" -> navController.navigate("seller_profile")
                            "My Shop" -> navController.navigate("my_shop")
                            "My Listings" -> navController.navigate("my_listings")
                            "Orders & Purchases" -> navController.navigate("orders/current_user")
                            "Settings" -> navController.navigate("settings")
                            "Wallet" -> navController.navigate("wallet")
                            "Help & Support" -> navController.navigate("help_support")
                            else -> {} // Handle other items as needed
                        }
                    }
                )
            }

            // Dark Mode Toggle
            item {
                MenuItemToggle(
                    title = "Dark Mode",
                    icon = Icons.Default.Brightness4,
                    isEnabled = false,
                    onToggle = { showDarkModeDialog = true }
                )
            }

            // Delete Account
            item {
                MenuItemRow(
                    title = "Delete Account",
                    icon = Icons.Default.DeleteForever,
                    textColor = MaterialTheme.colorScheme.error,
                    onClick = { showDeleteConfirmation = true }
                )
            }
        }
    }

    if (showDeleteConfirmation) {
        DeleteAccountConfirmationDialog(
            onConfirm = {
                viewModel.logout()
                navController.navigate("splash") {
                    popUpTo(0) { inclusive = true }
                }
            },
            onDismiss = { showDeleteConfirmation = false }
        )
    }
}

@Composable
private fun ProfileHeaderInMenu(user: AuthUser) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Profile Photo
        AsyncImage(
            model = user.photoUrl,
            contentDescription = "Profile photo",
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentScale = ContentScale.Crop
        )

        // Name
        Text(
            text = user.name,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 12.dp)
        )

        // Email
        Text(
            text = user.email,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.outline,
            modifier = Modifier.padding(top = 4.dp)
        )

        Divider(modifier = Modifier.padding(top = 16.dp))
    }
}

@Composable
private fun MenuItemRow(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    textColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface,
    onClick: () -> Unit
) {
    ListItem(
        headlineContent = {
            Text(
                title,
                color = textColor,
                fontWeight = if (textColor == MaterialTheme.colorScheme.error) FontWeight.Bold else FontWeight.Normal
            )
        },
        leadingContent = {
            Icon(
                icon,
                contentDescription = title,
                tint = textColor
            )
        },
        trailingContent = {
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = "Navigate",
                tint = textColor
            )
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        colors = ListItemDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    )
}

@Composable
private fun MenuItemToggle(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isEnabled: Boolean,
    onToggle: (Boolean) -> Unit
) {
    ListItem(
        headlineContent = { Text(title) },
        leadingContent = { Icon(icon, contentDescription = title) },
        trailingContent = {
            Switch(
                checked = isEnabled,
                onCheckedChange = onToggle
            )
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        colors = ListItemDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    )
}

@Composable
private fun DeleteAccountConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete Account") },
        text = {
            Text(
                "Are you sure you want to delete your account? This action cannot be undone. " +
                        "All your data including products, orders, and messages will be permanently deleted."
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Delete")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
