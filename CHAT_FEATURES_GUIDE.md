# Chat & Inbox Features - Developer Guide

## Quick Start

### 1. Navigate to Inbox
- Tap the "Chats" icon in the bottom navigation bar
- Shows list of all conversations sorted by latest message

### 2. Chat with a Seller (From Product)
- Open any product listing
- Tap "Chat Seller" button → Automatically opens inbox with that seller
- Product name appears in initial message context

### 3. Send Message
- Type in the message input field at the bottom
- Tap send button (arrow icon) to send
- Message appears immediately (optimistic update)
- Syncs to Supabase in background

## Features Available

### Search
- Type in the search bar at top of inbox
- Filters conversations by:
  - Seller name
  - Message content
  - Real-time as you type

### Tab Filtering
Select conversation type:
- **All** - All conversations
- **Unread** - Only unread messages
- **Orders** - Conversations about orders
- **Offers** - Conversations about offers

### Online Status
- Green dot indicator shows seller is online
- "Online" text appears in chat header

### Location Sharing
- Tap location button in message input
- Modal shows location preview map
- "Open in Maps" button to navigate

### File Attachment
- Tap attachment button to add files/images
- Ready for implementation of upload handler

### Conversation Management
- Swipe to delete conversation (planned)
- Long-press for options menu (planned)

## Architecture

### Data Flow
```
User Action (send message)
    ↓
ChatViewModel.sendMessage()
    ↓
MessageRepository.sendMessage()
    ↓
MessageEntity saved locally
    ↓
SupabaseApi.sendMessage() (background)
    ↓
isSynced flag updated
```

### Product Chat Flow
```
ProductDetailScreen
    ↓ (Chat Seller button clicked)
Navigation: chat_with_seller/{params}
    ↓
ChatScreen (with seller/product params)
    ↓
ChatViewModel.startChatWithSeller()
    ↓
MessageRepository.getOrCreateConversation()
    ↓
ConversationEntity created
    ↓
Initial message posted about product
    ↓
ChatDetailScreen shows conversation
```

## State Management

### ChatViewModel States
- `conversationsState` - List of all conversations
- `selectedConversation` - Currently opened conversation
- `messagesState` - Messages in selected conversation
- `isLoadingChat` - Loading indicator state
- `chatError` - Error messages for user feedback

### Error Handling
- Authentication errors → "User not authenticated"
- Empty messages → "Message cannot be empty"
- Send failures → "Failed to send message: [reason]"
- Error displays in red banner with dismiss button

## Testing Checklist

### Basic Functionality
- [ ] Open inbox → Shows existing conversations
- [ ] Search works → Filters by name/message
- [ ] Tab switching → Filters change correctly
- [ ] Open conversation → Shows all messages
- [ ] Send message → Appears in chat
- [ ] Message sends → Stays after app restart

### Product Chat
- [ ] Open product → "Chat Seller" button visible
- [ ] Click Chat Seller → Navigates to inbox
- [ ] Conversation created → With product name
- [ ] Initial message → References product
- [ ] Seller context → Shows seller name/avatar

### UI/UX
- [ ] Avatars display → With initials
- [ ] Online status → Shows correctly
- [ ] Unread badges → Show count
- [ ] Timestamps → Show message time
- [ ] Error messages → Display properly
- [ ] Loading states → Buttons disabled while loading

### Location Feature
- [ ] Location button visible
- [ ] Modal shows on tap
- [ ] Close button works
- [ ] "Open in Maps" button clickable
- [ ] Location text displays correctly

## Configuration

### Supabase Setup (local.properties)
```properties
supabase_url=https://fkeuioagahwqgpqjuwqj.supabase.co
supabase_anon_key=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
supabase_project_id=fkeuioagahwqgpqjuwqj
```

### Required Database Tables
- `conversations` - Stores conversation metadata
- `messages` - Stores individual messages
- `users` - User profiles
- `products` - Product information

## Debugging Tips

### View Logs
- Chat operations log to: `logInfo()`, `logError()`
- Check Android Studio Logcat for debug messages
- Search for "ChatViewModel" or "MessageRepository"

### Check Local Database
- Use Android Studio Database Inspector
- Navigate to `app/src/main/kotlin/.../database`
- Inspect `conversations` and `messages` tables

### Verify Supabase Connection
- Check `SupabaseApi` calls
- Verify authentication token in headers
- Check network tab for API requests

## Common Issues

### Messages not sending
- Check user is authenticated
- Verify internet connection
- Check Supabase credentials in local.properties
- View error message displayed in chat

### Conversations not loading
- Force refresh inbox (pull down when implemented)
- Check Supabase table permissions
- Verify auth token validity

### Chat not opening from product
- Check ProductDetailScreen routing
- Verify seller ID is not empty
- Check Navigation graph includes chat_with_seller route

## Future Enhancements

- [ ] Real-time message updates (WebSocket)
- [ ] Image upload in messages
- [ ] Voice/video calling
- [ ] Typing indicators
- [ ] Message reactions/emojis
- [ ] Conversation archiving
- [ ] Message search within conversation
- [ ] Conversation blocking/reporting
- [ ] Message editing/deletion
- [ ] Push notifications for new messages
