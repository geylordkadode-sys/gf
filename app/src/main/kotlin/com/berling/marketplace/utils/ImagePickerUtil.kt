package com.berling.marketplace.utils

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

object ImagePickerUtil {
    
    const val MAX_IMAGES = 10
    const val IMAGE_COMPRESSION_TARGET_KBPS = 400
    
    /**
     * Create output file for camera captures
     */
    fun createImageFile(context: Context): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val storageDir: File? = context.getExternalFilesDir(null)
        return File.createTempFile(
            "JPEG_${timeStamp}_",
            ".jpg",
            storageDir
        )
    }

    /**
     * Get content URI for a file (for camera)
     */
    fun getImageUri(context: Context, imageFile: File): Uri {
        return androidx.core.content.FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            imageFile
        )
    }

    /**
     * Process selected images from gallery
     */
    fun processGalleryImages(
        context: Context,
        uris: List<Uri>,
        onProcessed: (List<String>) -> Unit
    ) {
        val processedPaths = mutableListOf<String>()
        
        uris.take(MAX_IMAGES).forEach { uri ->
            try {
                val bitmap = MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                if (bitmap != null) {
                    val compressedPath = ImageCompressionUtil.compressBitmapToFile(
                        context,
                        bitmap,
                        IMAGE_COMPRESSION_TARGET_KBPS
                    )
                    if (compressedPath.isNotEmpty()) {
                        processedPaths.add(compressedPath)
                    }
                    bitmap.recycle()
                }
            } catch (e: Exception) {
                // Handle error - skip this image
            }
        }
        
        onProcessed(processedPaths)
    }

    /**
     * Process camera captured image
     */
    fun processCameraImage(
        context: Context,
        imageFile: File,
        onProcessed: (String) -> Unit
    ) {
        try {
            val bitmap = MediaStore.Images.Media.getBitmap(context.contentResolver, Uri.fromFile(imageFile))
            if (bitmap != null) {
                val compressedPath = ImageCompressionUtil.compressBitmapToFile(
                    context,
                    bitmap,
                    IMAGE_COMPRESSION_TARGET_KBPS
                )
                if (compressedPath.isNotEmpty()) {
                    imageFile.delete() // Delete original camera file
                    onProcessed(compressedPath)
                } else {
                    onProcessed(imageFile.absolutePath)
                }
                bitmap.recycle()
            } else {
                onProcessed(imageFile.absolutePath)
            }
        } catch (e: Exception) {
            onProcessed(imageFile.absolutePath)
        }
    }

    /**
     * Start camera intent
     */
    fun takeCameraPhoto(
        context: Context,
        imageFile: File,
        launcher: ActivityResultLauncher<Uri>
    ) {
        val imageUri = getImageUri(context, imageFile)
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
            putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
        }
        launcher.launch(imageUri)
    }

    /**
     * Start gallery picker intent
     */
    fun pickFromGallery(
        launcher: ActivityResultLauncher<String>
    ) {
        launcher.launch("image/*")
    }

    /**
     * Check if app has camera permission granted
     */
    fun hasCameraPermission(context: Context): Boolean {
        return context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)
    }

    /**
     * Open app settings for permission request
     */
    fun openAppSettings(context: Context) {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", context.packageName, null)
        }
        context.startActivity(intent)
    }
}

@Composable
fun rememberImagePickerLauncher(
    onImagesSelected: (List<String>) -> Unit,
    context: Context
): Pair<ActivityResultLauncher<String>, ActivityResultLauncher<Uri>> {
    
    // Gallery picker launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        ImagePickerUtil.processGalleryImages(context, uris) { paths ->
            onImagesSelected(paths)
        }
    }

    // Camera launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            // Image was saved to the file
            onImagesSelected(emptyList()) // Will be handled by the caller
        }
    }

    return Pair(galleryLauncher as ActivityResultLauncher<String>, cameraLauncher)
}

@Composable
fun rememberCameraLauncher(
    onPhotoTaken: (String) -> Unit,
    context: Context
): ActivityResultLauncher<Uri> {
    return rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            onPhotoTaken("") // Will be handled by the caller
        }
    }
}
