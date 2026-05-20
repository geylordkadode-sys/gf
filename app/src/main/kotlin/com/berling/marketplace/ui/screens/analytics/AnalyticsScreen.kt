package com.berling.marketplace.ui.screens.analytics

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.berling.marketplace.data.local.entities.AnalyticsEventEntity
import com.berling.marketplace.ui.screens.UiState
import com.berling.marketplace.ui.theme.AppColors

@Composable
fun AnalyticsScreen(
    navController: NavController,
    viewModel: AnalyticsViewModel = hiltViewModel(),
    userId: String = ""
) {
    val eventsState by viewModel.eventsState.collectAsState()
    val summaryState by viewModel.summaryState.collectAsState()
    val selectedPeriod by viewModel.selectedPeriod.collectAsState()

    LaunchedEffect(Unit) {
        if (userId.isNotEmpty()) {
            viewModel.loadAnalytics(userId)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp)
    ) {
        Text(
            "Analytics",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Period Filter
        PeriodFilterRow(
            selectedPeriod = selectedPeriod,
            onPeriodSelected = { period ->
                viewModel.setPeriod(period)
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Summary Cards
        SummaryCardsRow(summaryState = summaryState)

        Spacer(modifier = Modifier.height(16.dp))

        // Events List
        Text(
            "Event Details",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        when (val state = eventsState) {
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
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(state.data) { event ->
                        EventItem(event = event)
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
fun PeriodFilterRow(
    selectedPeriod: String,
    onPeriodSelected: (String) -> Unit
) {
    val periods = listOf(
        "7days" to "7 Days",
        "30days" to "30 Days",
        "90days" to "90 Days",
        "all" to "All Time"
    )

    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(periods.size) { index ->
            val (key, label) = periods[index]
            FilterChip(
                selected = selectedPeriod == key,
                onClick = { onPeriodSelected(key) },
                label = {
                    Text(label, fontSize = 12.sp, fontWeight = FontWeight.Medium)
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

@Composable
fun SummaryCardsRow(summaryState: Map<String, Any>) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SummaryCard(
            title = "Total Events",
            value = "${summaryState["total_events"] ?: 0}",
            modifier = Modifier.weight(1f),
            backgroundColor = Color(0xFFE3F2FD)
        )

        SummaryCard(
            title = "Synced",
            value = "${summaryState["events_synced"] ?: 0}",
            modifier = Modifier.weight(1f),
            backgroundColor = Color(0xFFF1F8E9)
        )
    }
}

@Composable
fun SummaryCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color(0xFFF5F5F5)
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                value,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = AppColors.Primary
            )
            Text(
                title,
                fontSize = 11.sp,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun EventItem(event: AnalyticsEventEntity) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFAFAFA)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    event.eventName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Text(
                    event.timestamp,
                    fontSize = 11.sp,
                    color = Color.Gray
                )
            }
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (event.isSynced) Color(0xFF4CAF50) else Color(0xFFFFA726),
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Text(
                    if (event.isSynced) "✓" else "⟳",
                    color = Color.White,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(6.dp)
                )
            }
        }
    }
}
