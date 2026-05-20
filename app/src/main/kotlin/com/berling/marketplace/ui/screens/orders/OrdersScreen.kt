package com.berling.marketplace.ui.screens.orders

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import java.util.Locale
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.berling.marketplace.data.local.entities.OrderEntity
import com.berling.marketplace.ui.screens.UiState
import com.berling.marketplace.ui.theme.AppColors

@Composable
fun OrdersScreen(
    navController: NavController,
    viewModel: OrdersViewModel = hiltViewModel(),
    buyerId: String = ""
) {
    val buyerOrdersState by viewModel.buyerOrdersState.collectAsState()
    val selectedStatus by viewModel.selectedStatus.collectAsState()

    LaunchedEffect(Unit) {
        if (buyerId.isNotEmpty()) {
            viewModel.loadBuyerOrders(buyerId)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp)
    ) {
        Text(
            "My Orders",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Status Filter
        StatusFilterRow(
            selectedStatus = selectedStatus,
            onStatusSelected = { status ->
                viewModel.filterByStatus(status)
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Orders List
        when (val state = buyerOrdersState) {
            UiState.Idle, UiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .wrapContentSize(Alignment.Center)
                ) {
                    CircularProgressIndicator()
                }
            }
            is UiState.Success -> {
                val orders = state.data
                val filteredOrders = if (selectedStatus == "all") {
                    orders
                } else {
                    orders.filter { it.status == selectedStatus }
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredOrders) { order ->
                        OrderItem(
                            order = order,
                            onClick = {
                                navController.navigate("order_detail/${order.id}")
                            }
                        )
                    }
                }
            }
            is UiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .wrapContentSize(Alignment.Center)
                ) {
                    Text(state.message)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatusFilterRow(
    selectedStatus: String,
    onStatusSelected: (String) -> Unit
) {
    val statuses = listOf("all", "pending", "paid", "shipped", "delivered", "cancelled")

    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(statuses) { status ->
            FilterChip(
                selected = selectedStatus == status,
                onClick = { onStatusSelected(status) },
                label = {
                    Text(
                        status.uppercase(),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                },
                modifier = Modifier.height(32.dp),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = AppColors.Primary,
                    selectedLabelColor = Color.White,
                    containerColor = Color(0xFFF0F0F0),
                    labelColor = Color.Black
                )
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderItem(
    order: OrderEntity,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFAFAFA)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Product Title and Status
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    order.productTitle,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    modifier = Modifier.weight(1f)
                )
                StatusBadge(status = order.status)
            }

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            // Order Details
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Order ID", fontSize = 10.sp, color = Color.Gray)
                    Text(order.id, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }
                Column {
                    Text("Quantity", fontSize = 10.sp, color = Color.Gray)
                    Text(order.quantity.toString(), fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Price", fontSize = 10.sp, color = Color.Gray)
                    Text("₹${String.format(Locale.getDefault(), "%.2f", order.price)}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AppColors.Primary)
                }
                if (order.trackingNumber.isNotEmpty()) {
                    Column {
                        Text("Tracking", fontSize = 10.sp, color = Color.Gray)
                        Text(order.trackingNumber, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }

            Text(
                "Ordered: ${order.createdAt}",
                fontSize = 10.sp,
                color = Color.LightGray,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Composable
fun StatusBadge(status: String) {
    val (backgroundColor, textColor) = when (status) {
        "pending" -> Color(0xFFFFE082) to Color(0xFF666600)
        "paid" -> Color(0xFF81C784) to Color.White
        "shipped" -> Color(0xFF64B5F6) to Color.White
        "delivered" -> Color(0xFF4CAF50) to Color.White
        "cancelled" -> Color(0xFFEF5350) to Color.White
        else -> Color.Gray to Color.White
    }

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = backgroundColor,
        modifier = Modifier.padding(4.dp)
    ) {
        Text(
            status.uppercase(),
            color = textColor,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
        )
    }
}
