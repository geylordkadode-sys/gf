@file:OptIn(ExperimentalMaterial3Api::class)
package com.berling.marketplace.ui.screens.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.berling.marketplace.ui.screens.BottomNavigationBar

@Composable
fun ProfileScreen(
    navController: NavHostController
) {
    var selectedTab by remember { mutableStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Profile") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { }) {
                        Icon(Icons.Default.Share, contentDescription = "Share")
                    }
                    IconButton(onClick = { }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More")
                    }
                }
            )
        },
        bottomBar = { BottomNavigationBar(navController, "profile") }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Profile Header
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Profile Photo
                    Surface(
                        modifier = Modifier.size(100.dp),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {}

                    Text(
                        text = "Ananya Sharma",
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(top = 12.dp)
                    )

                    Text(
                        text = "@ananya_sharma",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.padding(top = 4.dp)
                    )

                    // Rating and stats
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        StatItem("128", "Listings")
                        Divider(
                            modifier = Modifier
                                .height(30.dp)
                                .width(1.dp)
                        )
                        StatItem("2.5K", "Followers")
                        Divider(
                            modifier = Modifier
                                .height(30.dp)
                                .width(1.dp)
                        )
                        StatItem("180", "Following")
                    }

                    // Rating
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Star,
                            contentDescription = "Rating",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "4.8",
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                        Text(
                            text = "(128 Reviews)",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }

                    // Bio
                    Text(
                        text = "Fashion lover | Seller | Believer in good vibes ✨",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 12.dp)
                    )

                    Text(
                        text = "Selling trendy, quality & affordable products 💝",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.padding(top = 4.dp)
                    )

                    // Location and Join Date
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.LocationOn,
                            contentDescription = "Location",
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Delhi, India",
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Icon(
                            Icons.Default.Info,
                            contentDescription = "Join date",
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Joined Jan 2023",
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }

                    // Buttons
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { },
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                        ) {
                            Text("Edit Profile")
                        }
                        Button(
                            onClick = { },
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                        ) {
                            Text("Follow")
                        }
                    }
                }
            }

            // Menu Items
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    MenuItemRow("View Full Profile", Icons.Default.Person, { })
                    MenuItemRow("My Shop", Icons.Default.Store, { })
                    MenuItemRow("My Listings", Icons.Default.ShoppingBag, { })
                    MenuItemRow("Mark as Sold", Icons.Default.CheckCircle, { })
                    MenuItemRow("Achievements", Icons.Default.EmojiEvents, { })
                    MenuItemRow("Orders & Purchases", Icons.Default.ShoppingCart, { })
                    MenuItemRow("Saved Items", Icons.Default.FavoriteBorder, { })
                    MenuItemRow("Offers & Coupons", Icons.Default.LocalOffer, { })
                    MenuItemRow("My Reviews", Icons.Default.RateReview, { })
                    MenuItemRow("Wallet", Icons.Default.Wallet, { })
                    MenuItemRow("Settings", Icons.Default.Settings, { })
                    MenuItemRow("Help & Support", Icons.Default.Help, { })
                    MenuItemRow("Invite & Earn", Icons.Default.PersonAdd, { })
                    MenuItemRow("Dark Mode", Icons.Default.Brightness4, { })
                    MenuItemRow("Delete Account", Icons.Default.DeleteForever, { })
                }
            }
        }
    }
}

@Composable
fun StatItem(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.labelLarge
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.outline
        )
    }
}

@Composable
fun MenuItemRow(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    ListItem(
        headlineContent = { Text(title) },
        leadingContent = { Icon(icon, contentDescription = title) },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        trailingContent = { Icon(Icons.Default.ChevronRight, contentDescription = "Next") }
    )
}
