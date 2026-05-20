# Product Post Screen Enhancement Guide

## Overview
This document describes the comprehensive enhancements made to the product posting functionality in the Berling Marketplace app.

## Features Implemented

### 1. Enhanced Product Data Model
The `ProductEntity` now includes additional fields for complete product information:
- **Location Fields**: `location`, `latitude`, `longitude`
- **Product Details**: `brand`, `condition`, `tags`, `productAttributes`
- **Delivery & Returns**: `deliveryOptions`, `returnPolicy`
- **Pricing**: `discountPrice` for promotional pricing
- **Listing Options**: `isNew`, `boostListing`
- **Upload Tracking**: `uploadProgress`, `uploadStatus` for monitoring upload progress

### 2. Geolocation & Location Search
**File**: `LocationSearchComposables.kt`

Features:
- Search locations with autocomplete
- Integration with maps (mock data included for demo)
- Current location detection (ready for GPS integration)
- Location selection with coordinates (latitude/longitude)
- Display selected location as chips

Usage:
```kotlin
LocationSearchField(
    location = formState.location,
    onLocationChange = { onFormChange("location", it) },
    onLocationSelected = { location ->
        // Handle location selection
    }
)
```

### 3. Comprehensive Post Screen UI
**File**: `PostScreen.kt`

The new Post Screen includes:
- **Step 1 - Basic Information**:
  - Photo upload (up to 10 photos)
  - Product title and description
  - Category, brand, condition selection
  - Price and discount price
  - Product attributes (size, color, material, style, pattern)
  - Location selection with search
  - Delivery options (pickup, shipping)
  - Return policy selection

- **Step 2 - Details & Options**:
  - Product tags for discoverability
  - Price type selection (fixed vs negotiable)
  - Mark as new toggle
  - Boost listing toggle
  - Safe & secure marketplace info

### 4. Upload Progress Tracking
**File**: `PostScreenViewModel.kt`

Features:
- Real-time upload progress display (0-100%)
- File-by-file tracking
- Total and uploaded file counts
- Current file name display
- Upload status states: pending, uploading, completed, failed

### 5. Local Database Sync
**Implementation**: `ProductRepository.kt`

When posting a product:
1. **Immediate Local Save**: Product is saved to local database first
2. **Pending Sync Entry**: Entry added to pending_syncs table
3. **Metadata Storage**: All product data (including images) stored locally
4. **Retry Mechanism**: Failed syncs tracked with retry count

Benefits:
- Offline support - products saved even without internet
- Faster UI response
- Ability to queue multiple posts
- Reliable sync tracking

### 6. Background Sync Service
**File**: `BackgroundSyncService.kt`

Features:
- Automatic sync scheduling (every 15 minutes)
- Immediate sync trigger on-demand
- Retry logic with exponential backoff
- Separate handling for create, update, delete operations
- WorkManager integration for persistent background tasks

Usage:
```kotlin
// Schedule periodic sync
syncService.scheduleSyncWork()

// Trigger immediate sync
syncService.triggerImmediateSync()

// Sync pending products
syncService.syncPendingProducts(token)
```

### 7. ViewModel for Post Screen
**File**: `PostScreenViewModel.kt`

The `PostScreenViewModel` manages:
- Form state (all product details)
- Upload progress tracking
- Image selection and management
- Post creation with local database storage
- Sync state management
- Form validation

## File Structure

```
app/src/main/kotlin/com/berling/marketplace/
├── data/
│   ├── local/
│   │   ├── entities/Entities.kt (Updated ProductEntity)
│   │   ├── PendingSyncDao.kt (Updated with new methods)
│   │   └── ProductDao.kt
│   ├── remote/
│   │   └── models/ApiModels.kt (Updated ProductCreateRequest)
│   ├── repository/
│   │   └── ProductRepository.kt (Updated with sync logic)
│   └── sync/
│       └── BackgroundSyncService.kt (NEW)
├── di/
│   └── WorkerModule.kt (NEW)
└── ui/screens/post/
    ├── PostScreen.kt (Updated - comprehensive UI)
    ├── PostScreenViewModel.kt (NEW)
    └── LocationSearchComposables.kt (NEW)
```

## API Models

### ProductCreateRequest (Updated)
```kotlin
data class ProductCreateRequest(
    // Original fields
    val title: String,
    val description: String,
    val price: Double,
    val category: String,
    val imageUrl: String,
    val imageUrls: List<String> = emptyList(),
    
    // New fields
    val brand: String = "",
    val condition: String = "",
    val location: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val deliveryOptions: List<String> = emptyList(),
    val returnPolicy: String = "",
    val tags: List<String> = emptyList(),
    val productAttributes: Map<String, String> = emptyMap(),
    val isNew: Boolean = false,
    val boostListing: Boolean = false,
    val discountPrice: Double = 0.0
)
```

## Database Changes

### ProductEntity (Updated)
New fields added:
- brand, condition
- location, latitude, longitude
- deliveryOptions, returnPolicy
- tags, productAttributes
- isNew, boostListing
- discountPrice
- uploadProgress, uploadStatus

### PendingSyncEntity (Used for tracking)
Tracks pending operations:
- entityType: "product"
- entityId: product ID
- operation: "create", "update", "delete"
- data: JSON data
- retryCount: number of retry attempts

## Usage Guide

### Step 1: Initialize Sync Service
In your Application or MainActivity:
```kotlin
@HiltViewModel
class PostViewModel @Inject constructor(
    private val syncService: BackgroundSyncService
) {
    init {
        // Schedule background sync on app startup
        syncService.scheduleSyncWork()
    }
}
```

### Step 2: Post a Product
```kotlin
viewModel.postProduct(token, sellerId)
```

This will:
1. Save product to local database
2. Create pending sync entry
3. Show upload progress
4. Attempt immediate Supabase sync
5. Queue for background sync if upload fails

### Step 3: Monitor Sync Status
```kotlin
val pendingSyncCount = productRepository.getPendingSyncCount()
val pendingSyncs = productRepository.getPendingSyncs()
```

## Integration Requirements

### Dependencies to Add (if not already present)
```gradle
// WorkManager for background sync
implementation "androidx.work:work-runtime-ktx:$work_version"

// Hilt for dependency injection
implementation "com.google.dagger:hilt-android:$hilt_version"

// Serialization
implementation "org.jetbrains.kotlinx:kotlinx-serialization-json:$serialization_version"
```

### Permissions to Add (AndroidManifest.xml)
```xml
<!-- For location features -->
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />

<!-- For background sync -->
<uses-permission android:name="android.permission.INTERNET" />
```

## Future Enhancements

1. **Real Location Integration**
   - Integrate Google Places API or OpenStreetMap Nominatim
   - GPS location detection
   - Map preview in product posting

2. **Image Optimization**
   - Automatic image compression
   - Multiple resolution support
   - Direct cloud upload with progress

3. **Enhanced Sync**
   - Bandwidth-aware sync (WiFi only option)
   - Priority queue for sync operations
   - Detailed sync logs

4. **Offline Support**
   - Complete offline browsing
   - Queued actions
   - Conflict resolution

5. **Analytics**
   - Track upload success rates
   - Monitor sync failures
   - User behavior analytics

## Testing

To test the features:

1. **Local Sync**: Post a product without internet, verify it's saved locally
2. **Background Sync**: Post multiple products, wait 15 minutes, verify all synced
3. **Retry Logic**: Disable internet after posting, enable after 5 minutes, verify retry
4. **Upload Progress**: Post product with multiple images, verify progress bar updates
5. **Location Search**: Search for location, verify results show correctly

## Troubleshooting

### Products not syncing
- Check `pending_syncs` table for stuck entries
- Verify internet connectivity
- Check API token validity
- Review WorkManager configuration

### Upload progress stuck
- Verify file permissions
- Check image file sizes
- Ensure sufficient storage space
- Check network connectivity

### Location search not working
- Mock data enabled for demo - integrate real API
- Check location permission status
- Verify coordinates are valid
