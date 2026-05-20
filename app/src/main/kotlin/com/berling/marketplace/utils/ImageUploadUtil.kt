package com.berling.marketplace.utils

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import android.provider.MediaStore
import android.graphics.Matrix
import java.io.ByteArrayOutputStream

object ImageUploadUtil {
    fun compressImage(context: Context, imageUri: Uri, maxWidth: Int = 1080, maxHeight: Int = 1080): File? {
        return try {
            val bitmap = MediaStore.Images.Media.getBitmap(context.contentResolver, imageUri)
            val compressedBitmap = resizeBitmap(bitmap, maxWidth, maxHeight)
            saveBitmapToFile(context, compressedBitmap)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun compressImageFromBitmap(context: Context, bitmap: Bitmap, maxWidth: Int = 1080, maxHeight: Int = 1080): File? {
        return try {
            val compressedBitmap = resizeBitmap(bitmap, maxWidth, maxHeight)
            saveBitmapToFile(context, compressedBitmap)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun resizeBitmap(bitmap: Bitmap, maxWidth: Int, maxHeight: Int): Bitmap {
        if (bitmap.width <= maxWidth && bitmap.height <= maxHeight) {
            return bitmap
        }

        val ratio = bitmap.width.toFloat() / bitmap.height
        val newWidth: Int
        val newHeight: Int

        if (ratio > 1) {
            newWidth = maxWidth
            newHeight = (maxWidth / ratio).toInt()
        } else {
            newHeight = maxHeight
            newWidth = (maxHeight * ratio).toInt()
        }

        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }

    private fun saveBitmapToFile(context: Context, bitmap: Bitmap): File {
        val file = File(context.cacheDir, "compressed_image_${System.currentTimeMillis()}.jpg")
        val outputStream = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
        outputStream.flush()
        outputStream.close()
        return file
    }

    fun getFileSizeInMB(file: File): Double {
        return file.length() / (1024.0 * 1024.0)
    }

    fun rotateBitmap(bitmap: Bitmap, degrees: Float): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(degrees)
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    fun bitmapToByteArray(bitmap: Bitmap, quality: Int = 80): ByteArray {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream)
        return stream.toByteArray()
    }
}
