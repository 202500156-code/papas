package com.example.myapplication

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import java.io.ByteArrayOutputStream

object ImageUtils {
    fun uriToCompressedBase64(context: Context, uri: Uri, maxSide: Int = 360): String {
        return try {
            val input = context.contentResolver.openInputStream(uri) ?: return ""
            val original = BitmapFactory.decodeStream(input)
            input.close()
            if (original == null) return ""

            val ratio = minOf(maxSide.toFloat() / original.width, maxSide.toFloat() / original.height, 1f)
            val width = (original.width * ratio).toInt().coerceAtLeast(1)
            val height = (original.height * ratio).toInt().coerceAtLeast(1)
            val scaled = Bitmap.createScaledBitmap(original, width, height, true)

            val out = ByteArrayOutputStream()
            scaled.compress(Bitmap.CompressFormat.JPEG, 75, out)
            Base64.encodeToString(out.toByteArray(), Base64.NO_WRAP)
        } catch (e: Exception) {
            ""
        }
    }

    fun base64ToImageBitmap(base64: String): ImageBitmap? {
        if (base64.isBlank()) return null
        return try {
            val bytes = Base64.decode(base64, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
        } catch (e: Exception) {
            null
        }
    }
}