package com.berling.marketplace.ui.screens.chat

import androidx.lifecycle.viewModelScope
import com.berling.marketplace.data.local.entities.ConversationEntity
import com.berling.marketplace.data.local.entities.MessageEntity
import com.berling.marketplace.data.repository.MessageRepository
import com.berling.marketplace.data.repository.AuthenticationRepository
import com.berling.marketplace.ui.screens.BaseViewModel
import com.berling.marketplace.ui.screens.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val messageRepository: MessageRepository,
    private val authRepository: AuthenticationRepository
) : BaseViewModel() {

    private val _conversationsState = MutableStateFlow<UiState<List<ConversationEntity>>>(UiState.Loading)
    val conversationsState: StateFlow<UiState<List<ConversationEntity>>> = _conversationsState

    private val _messagesState = MutableStateFlow<UiState<List<MessageEntity>>>(UiState.Loading)
    val messagesState: StateFlow<UiState<List<MessageEntity>>> = _messagesState

    private val _selectedConversation = MutableStateFlow<ConversationEntity?>(null)
    val selectedConversation: StateFlow<ConversationEntity?> = _selectedConversation

    private val _isLoadingChat = MutableStateFlow(false)
    val isLoadingChat: StateFlow<Boolean> = _isLoadingChat

    private val _chatError = MutableStateFlow<String?>(null)
    val chatError: StateFlow<String?> = _chatError

    init {
        loadConversations()
    }

    fun loadConversations() {
        viewModelScope.launch {
            _conversationsState.emit(UiState.Loading)
            try {
                messageRepository.getAllConversations().collect { conversations ->
                    _conversationsState.emit(UiState.Success(conversations))
                }
            } catch (e: Exception) {
                _conversationsState.emit(UiState.Error(e.message ?: "Error loading conversations"))
                logError("Error loading conversations: ${e.message}")
            }
        }
    }

    fun loadMessages(conversationId: String) {
        viewModelScope.launch {
            _messagesState.emit(UiState.Loading)
            _isLoadingChat.emit(true)
            try {
                messageRepository.getConversationMessages(conversationId).collect { messages ->
                    _messagesState.emit(UiState.Success(messages))
                    _isLoadingChat.emit(false)
                }
            } catch (e: Exception) {
                _messagesState.emit(UiState.Error(e.message ?: "Error loading messages"))
                _isLoadingChat.emit(false)
                logError("Error loading messages: ${e.message}")
            }
        }
    }

    fun selectConversation(conversation: ConversationEntity) {
        _selectedConversation.value = conversation
        _chatError.value = null
        viewModelScope.launch {
            try {
                messageRepository.markConversationAsRead(conversation.id)
                loadMessages(conversation.id)
                logInfo("Selected conversation: ${conversation.id}")
            } catch (e: Exception) {
                logError("Error selecting conversation: ${e.message}")
            }
        }
    }

    fun sendMessage(conversationId: String, content: String) {
        viewModelScope.launch {
            try {
                val currentUser = authRepository.getCurrentUserOrNull()
                    ?: run {
                        _chatError.value = "User not authenticated"
                        return@launch
                    }

                if (content.isBlank()) {
                    _chatError.value = "Message cannot be empty"
                    return@launch
                }

                messageRepository.sendMessage(
                    conversationId = conversationId,
                    content = content,
                    senderId = currentUser.id,
                    senderName = currentUser.name
                )

                loadMessages(conversationId)
                logInfo("Message sent successfully")
            } catch (e: Exception) {
                _chatError.value = "Failed to send message: ${e.message}"
                logError("Error sending message: ${e.message}")
            }
        }
    }

    fun startChatWithSeller(
        sellerId: String,
        sellerName: String,
        productId: String,
        productTitle: String,
        productImage: String
    ) {
        viewModelScope.launch {
            try {
                _isLoadingChat.emit(true)
                val currentUser = authRepository.getCurrentUserOrNull()
                    ?: run {
                        _chatError.value = "User not authenticated"
                        _isLoadingChat.emit(false)
                        return@launch
                    }

                val conversation = messageRepository.getOrCreateConversation(
                    userId = currentUser.id,
                    userName = currentUser.name,
                    sellerId = sellerId,
                    sellerName = sellerName,
                    productId = productId,
                    productTitle = productTitle,
                    productImage = productImage
                )

                selectConversation(conversation)
                _isLoadingChat.emit(false)
                logInfo("Chat with seller started: $sellerId for product: $productId")
            } catch (e: Exception) {
                _chatError.value = "Error starting chat: ${e.message}"
                _isLoadingChat.emit(false)
                logError("Error starting chat with seller: ${e.message}")
            }
        }
    }

    fun searchConversations(query: String) {
        viewModelScope.launch {
            try {
                if (query.isBlank()) {
                    loadConversations()
                    return@launch
                }

                val searchQuery = "%$query%"
                val results = messageRepository.searchConversations(searchQuery)
                _conversationsState.emit(UiState.Success(results))
                logInfo("Search completed for: $query")
            } catch (e: Exception) {
                _conversationsState.emit(UiState.Error("Search failed: ${e.message}"))
                logError("Error searching conversations: ${e.message}")
            }
        }
    }

    fun clearError() {
        _chatError.value = null
    }

    fun goBack() {
        _selectedConversation.value = null
        _chatError.value = null
    }
}
