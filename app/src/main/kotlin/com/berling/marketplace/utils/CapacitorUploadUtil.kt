package com.berling.marketplace.utils

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

object CapacitorUploadUtil {
    /**
     * Prepare file for upload - compress if image
     */
    suspend fun prepareFileForUpload(
        context: Context,
        fileUri: Uri,
        isImage: Boolean = false
    ): File = withContext(Dispatchers.IO) {
        val inputStream = context.contentResolver.openInputStream(fileUri)
        val file = File(context.cacheDir, "upload_${System.currentTimeMillis()}")
        
        inputStream?.use { input ->
            file.outputStream().use { output ->
                input.copyTo(output)
            }
        }

        return@withContext if (isImage) {
            File(ImageCompressionUtil.compressImage(context, file.absolutePath, 300))
        } else {
            file
        }
    }

    /**
     * Get MIME type for file
     */
    fun getMimeType(filename: String): String {
        return when {
            filename.endsWith(".jpg") || filename.endsWith(".jpeg") -> "image/jpeg"
            filename.endsWith(".png") -> "image/png"
            filename.endsWith(".webp") -> "image/webp"
            filename.endsWith(".gif") -> "image/gif"
            filename.endsWith(".pdf") -> "application/pdf"
            filename.endsWith(".txt") -> "text/plain"
            else -> "application/octet-stream"
        }
    }

    /**
     * Check if file is valid for upload
     */
    fun isValidFileSize(file: File, maxSizeInMb: Int = 10): Boolean {
        val maxBytes = maxSizeInMb * 1024 * 1024L
        return file.length() <= maxBytes
    }
}
