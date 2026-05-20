package com.berling.marketplace.data.repository

import com.berling.marketplace.data.local.ConversationDao
import com.berling.marketplace.data.local.MessageDao
import com.berling.marketplace.data.local.entities.ConversationEntity
import com.berling.marketplace.data.local.entities.MessageEntity
import com.berling.marketplace.data.remote.SupabaseApi
import com.berling.marketplace.data.remote.models.MessageRequest
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class MessageRepository @Inject constructor(
    private val messageDao: MessageDao,
    private val conversationDao: ConversationDao,
    private val api: SupabaseApi
) {
    
    fun getAllConversations(): Flow<List<ConversationEntity>> {
        return conversationDao.getAllConversations()
    }

    fun getConversationMessages(conversationId: String): Flow<List<MessageEntity>> {
        return messageDao.getConversationMessages(conversationId)
    }

    suspend fun sendMessage(
        conversationId: String,
        content: String,
        senderId: String,
        senderName: String
    ) {
        val message = MessageEntity(
            id = System.currentTimeMillis().toString(),
            conversationId = conversationId,
            senderId = senderId,
            senderName = senderName,
            content = content,
            timestamp = System.currentTimeMillis().toString(),
            isRead = false,
            isSynced = false
        )
        
        // Save to local DB
        messageDao.insertMessage(message)
        
        // Attempt to sync to remote
        try {
            val request = MessageRequest(
                conversationId = conversationId,
                senderId = senderId,
                senderName = senderName,
                content = content,
                timestamp = message.timestamp
            )
            api.sendMessage("", request)
            messageDao.markMessageAsSynced(message.id)
        } catch (e: Exception) {
            // Message saved locally, will be synced later
        }
    }

    suspend fun insertConversation(conversation: ConversationEntity) {
        conversationDao.insertConversation(conversation)
    }

    suspend fun updateConversation(conversation: ConversationEntity) {
        conversationDao.updateConversation(conversation)
    }

    suspend fun markConversationAsRead(conversationId: String) {
        conversationDao.markConversationAsRead(conversationId)
    }

    /**
     * Create or get conversation with seller for a product
     */
    suspend fun getOrCreateConversation(
        userId: String,
        userName: String,
        sellerId: String,
        sellerName: String,
        productId: String,
        productTitle: String,
        productImage: String
    ): ConversationEntity {
        // Check if conversation exists with this seller for this product
        val existingConversation = conversationDao.getConversationWithParticipant(sellerId)
        
        if (existingConversation != null) {
            return existingConversation
        }
        
        // Create new conversation
        val conversationId = "conv_${System.currentTimeMillis()}"
        val conversation = ConversationEntity(
            id = conversationId,
            participantId = sellerId,
            participantName = sellerName,
            participantPhotoUrl = "",
            lastMessage = "Started conversation about $productTitle",
            lastMessageTime = System.currentTimeMillis().toString(),
            unreadCount = 0,
            productId = productId,
            productTitle = productTitle,
            productImageUrl = productImage,
            isSynced = false
        )
        
        insertConversation(conversation)
        
        // Add initial message about the product
        val initialMessage = MessageEntity(
            id = System.currentTimeMillis().toString(),
            conversationId = conversationId,
            senderId = userId,
            senderName = userName,
            content = "Hi, I'm interested in this product: $productTitle",
            timestamp = System.currentTimeMillis().toString(),
            isRead = false,
            isSynced = false
        )
        
        messageDao.insertMessage(initialMessage)
        
        return conversation
    }

    /**
     * Search conversations by participant name or message content
     */
    suspend fun searchConversations(query: String): List<ConversationEntity> {
        return conversationDao.searchConversations(query)
    }

    /**
     * Get unsynced messages for background sync
     */
    suspend fun getUnsyncedMessages(): List<MessageEntity> {
        return messageDao.getUnsyncedMessages()
    }

    /**
     * Mark message as read
     */
    suspend fun markMessageAsRead(messageId: String) {
        val message = messageDao.getMessageById(messageId)
        if (message != null) {
            messageDao.updateMessage(message.copy(isRead = true))
        }
    }
}
