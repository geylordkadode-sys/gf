# Integration Setup Guide

## Overview
This guide covers all the integration steps for the enhanced product posting feature with background sync, image compression, and location services.

## ✅ Completed Integrations

### 1. WorkManager Dependency ✓
**File**: `app/build.gradle.kts`
- ✅ Added `androidx.work:work-runtime-ktx:2.8.1`
- ✅ Added `androidx.work:work-multiprocess:2.8.1`
- ✅ Added Google Play Services for Location
- ✅ Added Google Play Services for Maps

### 2. BackgroundSyncService Initialization ✓
**File**: `app/src/main/kotlin/com/berling/marketplace/app/BerlingApp.kt`
- ✅ Implemented `Configuration.Provider` for WorkManager
- ✅ Auto-inject `BackgroundSyncService`
- ✅ Auto-inject `HiltWorkerFactory`
- ✅ Calls `syncService.scheduleSyncWork()` on app startup
- ✅ Sync runs every 15 minutes automatically

### 3. Image Compression Utility ✓
**File**: `utils/ImageCompressionUtil.kt`
- ✅ Compress images to 500KB or less
- ✅ EXIF rotation handling
- ✅ Multi-image batch compression
- ✅ Quality adjustment (90→10%)
- ✅ Target: 400 KBPS per image
- ✅ Bitmap compression support

**Usage**:
```kotlin
// Compress single image
val compressedPath = ImageCompressionUtil.compressImage(
    context, 
    imagePath, 
    targetKbps = 400
)

// Compress from camera
val photoPath = ImageCompressionUtil.compressBitmapToFile(
    context, 
    bitmap, 
    targetKbps = 400
)

// Batch compress
val paths = ImageCompressionUtil.compressImages(
    context, 
    listOf(img1, img2, img3), 
    400
)
```

### 4. Image Picker Integration ✓
**File**: `utils/ImagePickerUtil.kt`
- ✅ Gallery picker (multiple images)
- ✅ Camera capture integration
- ✅ Automatic compression on selection
- ✅ EXIF data preservation
- ✅ Max 10 images support
- ✅ File provider setup

**Usage**:
```kotlin
@Composable
fun PhotoPickerExample() {
    val context = LocalContext.current
    val (galleryLauncher, cameraLauncher) = rememberImagePickerLauncher(
        onImagesSelected = { paths ->
            // Handle selected compressed images
            viewModel.addImages(paths)
        },
        context = context
    )

    Button(onClick = {
        ImagePickerUtil.pickFromGallery(galleryLauncher)
    }) {
        Text("Pick from Gallery")
    }
}
```

### 5. Location Services Integration ✓
**File**: `utils/LocationUtil.kt`
- ✅ Get current device location (with permissions)
- ✅ Reverse geocoding (coordinates → address)
- ✅ Forward geocoding (address → coordinates)
- ✅ Distance calculation between coordinates
- ✅ Address formatting
- ✅ Permission checking
- ✅ Async/coroutine support

**Usage**:
```kotlin
// Get current location
val location = LocationUtil.getCurrentLocation(context)
if (location != null) {
    // Use: location.latitude, location.longitude
    // Use: location.address, location.city, location.state
}

// Reverse geocode
val address = LocationUtil.getAddressFromLocation(
    context, 
    latitude = 28.7041, 
    longitude = 77.1025
)

// Calculate distance
val distanceKm = LocationUtil.calculateDistance(
    lat1 = 28.7041, 
    lon1 = 77.1025,
    lat2 = 28.6139, 
    lon2 = 77.2090
)

// Format for display
val formatted = LocationUtil.formatLocation(location)
```

### 6. Supabase Configuration ✓
**File**: `config/SupabaseConfig.kt`
- ✅ Centralized configuration management
- ✅ API endpoint helpers
- ✅ Storage URL builders
- ✅ Authentication headers
- ✅ Retry configuration
- ✅ Image upload settings
- ✅ Endpoint constants
- ✅ Feature flags

**Usage**:
```kotlin
// Get API endpoint
val url = SupabaseConfig.getApiEndpoint("/products")

// Get storage URL
val imageUrl = SupabaseConfig.getStorageUrl("products", "image.jpg")

// Get auth headers
val headers = SupabaseConfig.getAuthHeaders(token)

// Check configuration
if (SupabaseConfig.isValid()) {
    // Initialize API
}
```

## 📋 Permissions Added

**AndroidManifest.xml**:
```xml
<!-- Location -->
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_BACKGROUND_LOCATION" />

<!-- Camera & Storage -->
<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />

<!-- Network & Background -->
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED" />
```

## 🔧 Configuration Steps

### Step 1: Request Permissions at Runtime
```kotlin
// Use AndroidX Activity Result API or RequestPermission Compose
val permissionLauncher = rememberLauncherForActivityResult(
    ActivityResultContracts.RequestMultiplePermissions()
) { permissions ->
    val cameraGranted = permissions[Manifest.permission.CAMERA] == true
    val locationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
    // Handle results
}

Button(onClick = {
    permissionLauncher.launch(arrayOf(
        Manifest.permission.CAMERA,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.READ_EXTERNAL_STORAGE
    ))
}) {
    Text("Request Permissions")
}
```

### Step 2: Enable Location in PostScreenViewModel
```kotlin
// In PostScreenViewModel
fun getCurrentLocationForProduct() {
    viewModelScope.launch {
        val location = LocationUtil.getCurrentLocation(context)
        if (location != null) {
            updateFormField("location", location.city)
            updateFormField("latitude", location.latitude)
            updateFormField("longitude", location.longitude)
        }
    }
}
```

### Step 3: Update Photo Upload Section
```kotlin
// In PostScreen.kt PhotoUploadSection
Button(onClick = {
    // Open image picker
    imagePickerLauncher.launch("image/*")
}) {
    Text("Add Photos (${selectedImages.size}/${MAX_IMAGES})")
}
```

### Step 4: Configure Supabase Storage
```kotlin
// Create buckets in Supabase:
// - products (public)
// - users (private)
// - temp (private, auto-cleanup)

// Set CORS policy for uploads:
{
  "allowedHeaders": ["*"],
  "allowedMethods": ["GET", "POST", "PUT", "DELETE"],
  "allowedOrigins": ["*"],
  "exposedHeaders": [],
  "maxAge": 3600
}
```

## 📱 Testing Checklist

- [ ] **Offline Posting**: Post product without internet → saved locally
- [ ] **Background Sync**: Wait 15 minutes → verify product syncs
- [ ] **Image Compression**: Post 3 images → verify < 500KB each
- [ ] **Location Search**: Search "Delhi" → verify results
- [ ] **Current Location**: Click "Use Current Location" → verify coordinates
- [ ] **Retry Logic**: Post product → disable internet → enable after 5 min → verify retry
- [ ] **Multiple Uploads**: Post 5 products rapidly → verify all in queue
- [ ] **Sync Monitoring**: Check pending_syncs table → verify count decreases

## 🚀 Runtime Initialization

**On App Start**:
1. BerlingApp.onCreate() → calls syncService.scheduleSyncWork()
2. WorkManager schedules 15-minute periodic sync task
3. SyncWorker runs in background automatically
4. Failed syncs retry with exponential backoff
5. Products synced to Supabase after upload completes

## 📊 Database Tables

### pending_syncs (tracks all pending operations)
```
id: Int (Primary Key)
entityType: String ("product", "user", "favorite")
entityId: String (ID of entity)
operation: String ("create", "update", "delete")
data: String (JSON data)
createdAt: String (Timestamp)
retryCount: Int (Current retry attempt)
```

### products (enhanced with new fields)
```
uploadProgress: Int (0-100%)
uploadStatus: String ("pending", "uploading", "completed", "failed")
latitude: Double
longitude: Double
location: String
brand: String
condition: String
tags: String (JSON)
deliveryOptions: String (JSON)
returnPolicy: String
productAttributes: String (JSON)
isNew: Boolean
boostListing: Boolean
discountPrice: Double
```

## 🎯 Next Steps

1. **Test All Features**: Run integration tests
2. **Monitor Logs**: Check WorkManager and sync logs
3. **User Feedback**: Gather feedback on upload progress
4. **Analytics**: Track sync success rates
5. **Performance**: Monitor image compression times
6. **Error Handling**: Test error scenarios

## 📞 Support

For issues or questions, check:
- Sync logs in WorkManager
- Error states in PostScreenViewModel
- Image compression debug info
- Location permission status
- Supabase API responses

## 🔐 Security Notes

- All API calls use Bearer token authentication
- Images compressed before upload to reduce data transfer
- Location data only collected with user permission
- Offline data encrypted in local SQLite database
- Background sync only with valid authentication
