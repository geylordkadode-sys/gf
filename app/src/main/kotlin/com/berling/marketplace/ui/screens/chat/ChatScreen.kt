package com.berling.marketplace.ui.screens.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.berling.marketplace.data.local.entities.ConversationEntity
import com.berling.marketplace.data.local.entities.MessageEntity
import com.berling.marketplace.ui.screens.UiState
import com.berling.marketplace.ui.theme.AppColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    navController: NavController,
    viewModel: ChatViewModel = hiltViewModel(),
    sellerId: String = "",
    sellerName: String = "",
    productId: String = "",
    productTitle: String = "",
    productImage: String = ""
) {
    val conversationsState by viewModel.conversationsState.collectAsState()
    val selectedConversation by viewModel.selectedConversation.collectAsState()
    val messagesState by viewModel.messagesState.collectAsState()
    val isLoading by viewModel.isLoadingChat.collectAsState()
    val chatError by viewModel.chatError.collectAsState()

    LaunchedEffect(sellerId) {
        if (sellerId.isNotEmpty() && productId.isNotEmpty()) {
            viewModel.startChatWithSeller(
                sellerId = sellerId,
                sellerName = sellerName,
                productId = productId,
                productTitle = productTitle,
                productImage = productImage
            )
        }
    }

    if (selectedConversation == null) {
        ConversationListScreen(
            conversationsState = conversationsState,
            onConversationClick = { conversation ->
                viewModel.selectConversation(conversation)
            },
            onSearch = { query ->
                viewModel.searchConversations(query)
            }
        )
    } else {
        ChatDetailScreen(
            conversation = selectedConversation!!,
            messagesState = messagesState,
            onBackClick = { 
                viewModel.goBack()
            },
            onSendMessage = { message ->
                viewModel.sendMessage(selectedConversation!!.id, message)
            },
            isLoading = isLoading,
            chatError = chatError,
            onErrorDismiss = { viewModel.clearError() }
        )
    }
}

@Composable
fun ConversationListScreen(
    conversationsState: UiState<List<ConversationEntity>>,
    onConversationClick: (ConversationEntity) -> Unit,
    onSearch: (String) -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("All", "Unread", "Orders", "Offers")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Header with search and filter
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                "Inbox",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF6B4E71)
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(onClick = { /* Search action */ }) {
                    Icon(Icons.Default.Search, contentDescription = "Search", tint = AppColors.Primary)
                }
                IconButton(onClick = { /* Filter action */ }) {
                    Icon(Icons.Default.Tune, contentDescription = "Filter", tint = AppColors.Primary)
                }
            }
        }

        // Search bar
        TextField(
            value = searchQuery,
            onValueChange = { 
                searchQuery = it
                onSearch(it)
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            placeholder = { Text("Search messages or users...", color = Color.Gray) },
            shape = RoundedCornerShape(20.dp),
            colors = TextFieldDefaults.colors(
                unfocusedContainerColor = Color(0xFFF5F5F5),
                focusedContainerColor = Color(0xFFF5F5F5),
                unfocusedIndicatorColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent
            )
        )

        // Tabs
        TabRow(
            selectedTabIndex = selectedTab,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            containerColor = Color.White,
            contentColor = AppColors.Primary,
            indicator = { tabPositions ->
                TabRowDefaults.Indicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    height = 3.dp,
                    color = AppColors.Primary
                )
            }
        ) {
            tabs.forEachIndexed { index, tab ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { 
                        Text(
                            tab,
                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
            }
        }

        Divider(color = Color(0xFFEEEEEE), thickness = 1.dp)

        // Conversations list
        when (conversationsState) {
            is UiState.Idle, is UiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .wrapContentSize(Alignment.Center)
                ) {
                    CircularProgressIndicator()
                }
            }
            is UiState.Success -> {
                val conversations = conversationsState.data
                if (conversations.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .wrapContentSize(Alignment.Center)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text("No conversations yet", fontWeight = FontWeight.Bold)
                            Text("Start chatting with sellers!", color = Color.Gray, modifier = Modifier.padding(top = 8.dp))
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(0.dp)
                    ) {
                        items(conversations) { conversation ->
                            ConversationItem(
                                conversation = conversation,
                                onClick = { onConversationClick(conversation) }
                            )
                        }
                    }
                }
            }
            is UiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .wrapContentSize(Alignment.Center)
                ) {
                    Text((conversationsState as UiState.Error).message, color = Color.Red)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversationItem(
    conversation: ConversationEntity,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // User Avatar with online status
        Box(modifier = Modifier.size(56.dp)) {
            Surface(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape),
                color = AppColors.Primary,
                shape = CircleShape
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFFE8D5F2)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        conversation.participantName.take(1).uppercase(),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.Primary
                    )
                }
            }
            // Online status indicator
            Surface(
                modifier = Modifier
                    .size(14.dp)
                    .align(Alignment.BottomEnd)
                    .clip(CircleShape),
                color = Color(0xFF4CAF50),
                shape = CircleShape
            ) {}
        }

        // Conversation info
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    conversation.participantName,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    maxLines = 1
                )
                Text(
                    conversation.lastMessageTime,
                    fontSize = 11.sp,
                    color = Color.Gray
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                conversation.lastMessage,
                fontSize = 12.sp,
                color = Color.Gray,
                maxLines = 1
            )
        }

        // Unread badge
        if (conversation.unreadCount > 0) {
            Badge(
                containerColor = AppColors.Primary,
                contentColor = Color.White,
                modifier = Modifier.size(24.dp)
            ) {
                Text(
                    conversation.unreadCount.toString(),
                    fontSize = 10.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
    Divider(color = Color(0xFFEEEEEE), thickness = 1.dp)
}

@Composable
fun ChatDetailScreen(
    conversation: ConversationEntity,
    messagesState: UiState<List<MessageEntity>>,
    onBackClick: () -> Unit,
    onSendMessage: (String) -> Unit,
    isLoading: Boolean = false,
    chatError: String? = null,
    onErrorDismiss: () -> Unit = {}
) {
    var messageText by remember { mutableStateOf("") }
    var showLocationShare by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Enhanced Header
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            color = Color.White,
            shadowElevation = 2.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    IconButton(onClick = onBackClick) {
                        Text("←", fontSize = 24.sp)
                    }
                    
                    // User Avatar
                    Box(modifier = Modifier.size(40.dp)) {
                        Surface(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape),
                            color = AppColors.Primary,
                            shape = CircleShape
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color(0xFFE8D5F2)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    conversation.participantName.take(1).uppercase(),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AppColors.Primary
                                )
                            }
                        }
                        Surface(
                            modifier = Modifier
                                .size(10.dp)
                                .align(Alignment.BottomEnd)
                                .clip(CircleShape),
                            color = Color(0xFF4CAF50),
                            shape = CircleShape
                        ) {}
                    }
                    
                    Column {
                        Text(
                            conversation.participantName,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                        Text(
                            "Online",
                            fontSize = 11.sp,
                            color = Color(0xFF4CAF50)
                        )
                    }
                }
                
                IconButton(onClick = { }) {
                    Text("⋮", fontSize = 20.sp)
                }
            }
        }

        // Error message if any
        if (!chatError.isNullOrEmpty()) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                color = Color(0xFFFFEBEE),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        chatError,
                        fontSize = 12.sp,
                        color = Color.Red,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = onErrorDismiss, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Dismiss", tint = Color.Red)
                    }
                }
            }
        }

        // Messages
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            when (messagesState) {
                is UiState.Idle, is UiState.Loading -> {
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
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        reverseLayout = true
                    ) {
                        items(messagesState.data) { message ->
                            MessageBubble(message)
                        }
                    }
                }
                is UiState.Error -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .wrapContentSize(Alignment.Center)
                    ) {
                        Text(messagesState.message)
                    }
                }
            }
        }

        // Location Share Modal (if enabled)
        if (showLocationShare) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                color = Color(0xFFFFF3E0),
                shape = RoundedCornerShape(12.dp),
                shadowElevation = 4.dp
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Location", fontWeight = FontWeight.Bold)
                        IconButton(onClick = { showLocationShare = false }, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Gray)
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp),
                        color = Color(0xFFE0E0E0),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("📍 Connaught Place, New Delhi, Delhi 110001, India", textAlign = TextAlign.Center)
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { showLocationShare = false },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = AppColors.Primary)
                    ) {
                        Text("Open in Maps")
                    }
                }
            }
        }

        // Input Field
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            color = Color.White,
            shadowElevation = 4.dp,
            shape = RoundedCornerShape(24.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                IconButton(
                    onClick = { },
                    modifier = Modifier.size(36.dp),
                    enabled = !isLoading
                ) {
                    Icon(Icons.Default.AttachFile, contentDescription = "Attach", tint = AppColors.Primary)
                }

                TextField(
                    value = messageText,
                    onValueChange = { messageText = it },
                    modifier = Modifier
                        .weight(1f),
                    placeholder = { Text("Type a message...", color = Color.Gray) },
                    shape = RoundedCornerShape(20.dp),
                    colors = TextFieldDefaults.colors(
                        unfocusedContainerColor = Color(0xFFF5F5F5),
                        focusedContainerColor = Color(0xFFF5F5F5),
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent
                    ),
                    singleLine = true,
                    enabled = !isLoading
                )

                IconButton(
                    onClick = { showLocationShare = true },
                    modifier = Modifier.size(36.dp),
                    enabled = !isLoading
                ) {
                    Icon(Icons.Default.LocationOn, contentDescription = "Location", tint = AppColors.Primary)
                }

                IconButton(
                    onClick = {
                        if (messageText.isNotEmpty() && !isLoading) {
                            onSendMessage(messageText)
                            messageText = ""
                        }
                    },
                    modifier = Modifier.size(36.dp),
                    enabled = !isLoading && messageText.isNotEmpty()
                ) {
                    Icon(Icons.Default.Send, contentDescription = "Send", tint = AppColors.Primary)
                }
            }
        }
    }
}

@Composable
fun MessageBubble(message: MessageEntity) {
    val isOwn = message.senderId.isEmpty()
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = if (isOwn) Arrangement.End else Arrangement.Start
    ) {
        Column(
            modifier = Modifier.widthIn(max = 280.dp),
            horizontalAlignment = if (isOwn) Alignment.End else Alignment.Start
        ) {
            Surface(
                modifier = Modifier.padding(horizontal = 4.dp),
                shape = RoundedCornerShape(
                    topStart = 12.dp,
                    topEnd = 12.dp,
                    bottomStart = if (isOwn) 12.dp else 0.dp,
                    bottomEnd = if (isOwn) 0.dp else 12.dp
                ),
                color = if (isOwn) AppColors.Primary else Color(0xFFF0F0F0),
                shadowElevation = 2.dp
            ) {
                Text(
                    message.content,
                    modifier = Modifier.padding(12.dp),
                    color = if (isOwn) Color.White else Color.Black,
                    fontSize = 14.sp
                )
            }
            
            Text(
                message.timestamp,
                fontSize = 10.sp,
                color = Color.Gray,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
    }
}
