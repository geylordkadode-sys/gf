package com.berling.marketplace.ui.screens.chat

import androidx.lifecycle.viewModelScope
import android.content.Context
import android.net.Uri
import com.berling.marketplace.data.local.entities.ConversationEntity
import com.berling.marketplace.data.local.entities.MessageEntity
import com.berling.marketplace.data.repository.MessageRepository
import com.berling.marketplace.data.repository.NotificationRepository
import com.berling.marketplace.data.repository.AuthenticationRepository
import com.berling.marketplace.ui.screens.BaseViewModel
import com.berling.marketplace.ui.screens.UiState
import com.berling.marketplace.utils.ImageUploadUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModelEnhanced @Inject constructor(
    private val messageRepository: MessageRepository,
    private val notificationRepository: NotificationRepository,
    private val authRepository: AuthenticationRepository,
    @ApplicationContext private val context: Context
) : BaseViewModel() {

    private val _conversationsState = MutableStateFlow<UiState<List<ConversationEntity>>>(UiState.Loading)
    val conversationsState: StateFlow<UiState<List<ConversationEntity>>> = _conversationsState

    private val _messagesState = MutableStateFlow<UiState<List<MessageEntity>>>(UiState.Loading)
    val messagesState: StateFlow<UiState<List<MessageEntity>>> = _messagesState

    private val _selectedConversation = MutableStateFlow<ConversationEntity?>(null)
    val selectedConversation: StateFlow<ConversationEntity?> = _selectedConversation

    private val _imageUploadProgress = MutableStateFlow(0)
    val imageUploadProgress: StateFlow<Int> = _imageUploadProgress

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
            }
        }
    }

    fun loadMessages(conversationId: String) {
        viewModelScope.launch {
            _messagesState.emit(UiState.Loading)
            try {
                messageRepository.getConversationMessages(conversationId).collect { messages ->
                    _messagesState.emit(UiState.Success(messages))
                }
            } catch (e: Exception) {
                _messagesState.emit(UiState.Error(e.message ?: "Error loading messages"))
            }
        }
    }

    fun selectConversation(conversation: ConversationEntity) {
        _selectedConversation.value = conversation
        viewModelScope.launch {
            messageRepository.markConversationAsRead(conversation.id)
            loadMessages(conversation.id)
        }
    }

    fun sendMessage(conversationId: String, content: String) {
        viewModelScope.launch {
            try {
                val currentUser = authRepository.getCurrentUserOrNull()
                    ?: return@launch

                messageRepository.sendMessage(
                    conversationId = conversationId,
                    content = content,
                    senderId = currentUser.id,
                    senderName = currentUser.name
                )

                loadMessages(conversationId)
                
                // Notify recipient
                val conversation = _selectedConversation.value
                if (conversation != null) {
                    notificationRepository.notifyNewMessage(
                        conversationId,
                        conversation.participantId,
                        currentUser.name
                    )
                }
            } catch (e: Exception) {
                logError("Error sending message: ${e.message}")
            }
        }
    }

    fun sendImageMessage(conversationId: String, imageUri: Uri) {
        viewModelScope.launch {
            _imageUploadProgress.emit(10)

            val compressedFile = ImageUploadUtil.compressImage(context, imageUri, 800, 800)
            if (compressedFile == null) {
                logError("Failed to compress image")
                return@launch
            }

            _imageUploadProgress.emit(50)

            try {
                val photoUrl = "file://${compressedFile.absolutePath}"
                sendMessage(conversationId, "[IMAGE]: $photoUrl")
                _imageUploadProgress.emit(100)
            } catch (e: Exception) {
                logError("Failed to send image: ${e.message}")
                _imageUploadProgress.emit(0)
            }
        }
    }
}
