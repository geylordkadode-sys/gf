package com.berling.marketplace.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import kotlin.math.roundToInt
import kotlin.math.sqrt

object ImageCompressionUtil {
    
    // Target 500KB per image
    private const val TARGET_SIZE_KB = 500
    private const val TARGET_KBPS = 400
    
    /**
     * Compress image to target size in KB (default 500KB)
     * @param context Android context
     * @param imagePath Path to image file
     * @param targetKbps Target size in kilobits per second (default 400)
     * @return Compressed image file path
     */
    fun compressImage(
        context: Context,
        imagePath: String,
        targetKbps: Int = TARGET_KBPS
    ): String {
        val file = File(imagePath)
        val bitmap = BitmapFactory.decodeFile(imagePath) ?: return imagePath

        try {
            // Rotate if needed based on EXIF
            val rotatedBitmap = rotateImageIfRequired(imagePath, bitmap)
            
            // Resize to reasonable dimensions (max 1080x1080)
            val resizedBitmap = resizeImage(rotatedBitmap, maxWidth = 1080, maxHeight = 1080)
            
            // Calculate target bytes (kbps * 1024 bits / 8 = bytes)
            val targetBytes = (targetKbps * 1024) / 8
            var quality = 90
            var compressedFile: File? = null

            while (quality >= 10) {
                compressedFile = File(context.cacheDir, "compressed_${System.currentTimeMillis()}_$quality.jpg")
                
                FileOutputStream(compressedFile).use { fos ->
                    resizedBitmap.compress(Bitmap.CompressFormat.JPEG, quality, fos)
                }

                if (compressedFile.length() <= targetBytes) {
                    return compressedFile.absolutePath
                }

                quality -= 5
            }

            // Return best attempt if we can't reach target
            return compressedFile?.absolutePath ?: imagePath
        } catch (e: Exception) {
            return imagePath
        } finally {
            bitmap.recycle()
        }
    }

    /**
     * Compress multiple images at once
     * @param context Android context
     * @param imagePaths List of image file paths
     * @param targetKbps Target size per image in KBPS
     * @return List of compressed image file paths
     */
    fun compressImages(
        context: Context,
        imagePaths: List<String>,
        targetKbps: Int = TARGET_KBPS
    ): List<String> {
        return imagePaths.map { path ->
            try {
                compressImage(context, path, targetKbps)
            } catch (e: Exception) {
                path
            }
        }
    }

    /**
     * Compress bitmap from camera directly
     * @param context Android context
     * @param bitmap Bitmap from camera
     * @param targetKbps Target size in KBPS
     * @return Path to compressed image file
     */
    fun compressBitmapToFile(
        context: Context,
        bitmap: Bitmap,
        targetKbps: Int = TARGET_KBPS
    ): String {
        return try {
            val resized = resizeImage(bitmap, maxWidth = 1080, maxHeight = 1080)
            val targetBytes = (targetKbps * 1024) / 8
            var quality = 90
            var compressedFile: File? = null

            while (quality >= 10) {
                compressedFile = File(context.cacheDir, "camera_${System.currentTimeMillis()}_$quality.jpg")
                FileOutputStream(compressedFile).use { fos ->
                    resized.compress(Bitmap.CompressFormat.JPEG, quality, fos)
                }

                if (compressedFile.length() <= targetBytes) {
                    return compressedFile.absolutePath
                }
                quality -= 5
            }
            compressedFile?.absolutePath ?: ""
        } catch (e: Exception) {
            ""
        }
    }

    /**
     * Resize bitmap to max dimensions while maintaining aspect ratio
     */
    fun resizeImage(
        bitmap: Bitmap,
        maxWidth: Int = 1080,
        maxHeight: Int = 1080
    ): Bitmap {
        val width = bitmap.width
        val height = bitmap.height

        if (width <= maxWidth && height <= maxHeight) {
            return bitmap
        }

        val scale = sqrt((maxWidth * maxHeight).toFloat() / (width * height))
        val newWidth = (width * scale).toInt()
        val newHeight = (height * scale).toInt()

        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }

    /**
     * Rotate image based on EXIF orientation
     */
    private fun rotateImageIfRequired(imagePath: String, bitmap: Bitmap): Bitmap {
        return try {
            val exif = ExifInterface(imagePath)
            val orientation = exif.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )

            val rotationDegrees = when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> 90
                ExifInterface.ORIENTATION_ROTATE_180 -> 180
                ExifInterface.ORIENTATION_ROTATE_270 -> 270
                else -> 0
            }

            if (rotationDegrees == 0) {
                bitmap
            } else {
                val matrix = Matrix().apply {
                    postRotate(rotationDegrees.toFloat())
                }
                Bitmap.createBitmap(
                    bitmap,
                    0,
                    0,
                    bitmap.width,
                    bitmap.height,
                    matrix,
                    true
                )
            }
        } catch (e: Exception) {
            bitmap
        }
    }

    /**
     * Get URI for file using FileProvider
     */
    fun getCompressedImageUri(context: Context, file: File): String {
        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file).toString()
    }

    /**
     * Get file size in kilobytes
     */
    fun getFileSizeInKb(file: File): Double {
        return file.length() / 1024.0
    }

    /**
     * Get file size in kilobits per second
     */
    fun getFileSizeInKbps(file: File): Double {
        return (file.length() * 8) / 1024.0
    }

    /**
     * Get image dimensions without loading full bitmap (memory efficient)
     */
    fun getImageDimensions(imagePath: String): Pair<Int, Int> {
        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        BitmapFactory.decodeFile(imagePath, options)
        return Pair(options.outWidth, options.outHeight)
    }

    /**
     * Check if image needs compression
     */
    fun needsCompression(
        filePath: String,
        targetKbps: Int = TARGET_KBPS
    ): Boolean {
        val file = File(filePath)
        val fileSizeKbps = getFileSizeInKbps(file)
        return fileSizeKbps > targetKbps
    }
}


