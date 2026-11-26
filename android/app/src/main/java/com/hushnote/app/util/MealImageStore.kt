package com.hushnote.app.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.ExifInterface
import android.net.Uri
import com.hushnote.app.model.MealConstants
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.UUID

object MealImageStore {
    private val exifFormats = listOf(
        SimpleDateFormat("yyyy:MM:dd HH:mm:ss", Locale.US),
        SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
    )

    data class StoredImages(
        val id: String,
        val imageFile: File,
        val thumbFile: File,
        val timestamp: Long
    )

    fun storeFromUri(
        context: Context,
        uri: Uri,
        imageDir: File,
        thumbDir: File,
        fallbackTimestamp: Long = System.currentTimeMillis()
    ): StoredImages {
        imageDir.mkdirs()
        thumbDir.mkdirs()
        val id = UUID.randomUUID().toString()
        val imageFile = File(imageDir, "$id.jpg")
        val thumbFile = File(thumbDir, "$id.jpg")

        context.contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(imageFile).use { output ->
                input.copyTo(output)
            }
        } ?: throw IllegalStateException("无法读取选中的图片")

        compressIfNeeded(imageFile)
        createThumbnail(imageFile, thumbFile)

        val timestamp = readExifTimestamp(imageFile)
            ?: runCatching { imageFile.lastModified().takeIf { it > 0 } }.getOrNull()
            ?: fallbackTimestamp

        return StoredImages(id, imageFile, thumbFile, timestamp)
    }

    fun storeFromCameraFile(
        cameraFile: File,
        imageDir: File,
        thumbDir: File,
        fallbackTimestamp: Long = System.currentTimeMillis()
    ): StoredImages {
        imageDir.mkdirs()
        thumbDir.mkdirs()
        val id = UUID.randomUUID().toString()
        val imageFile = File(imageDir, "$id.jpg")
        val thumbFile = File(thumbDir, "$id.jpg")

        cameraFile.copyTo(imageFile, overwrite = true)
        cameraFile.delete()
        compressIfNeeded(imageFile)
        createThumbnail(imageFile, thumbFile)

        val timestamp = readExifTimestamp(imageFile)
            ?: runCatching { imageFile.lastModified().takeIf { it > 0 } }.getOrNull()
            ?: fallbackTimestamp

        return StoredImages(id, imageFile, thumbFile, timestamp)
    }

    fun createTempCameraFile(cacheDir: File): File {
        val dir = File(cacheDir, "meal_capture").apply { mkdirs() }
        return File(dir, "capture_${System.currentTimeMillis()}.jpg")
    }

    fun deletePair(imagePath: String, thumbPath: String) {
        runCatching { File(imagePath).delete() }
        runCatching { File(thumbPath).delete() }
    }

    private fun compressIfNeeded(file: File) {
        if (file.length() <= MealConstants.MAX_UPLOAD_BYTES) {
            // 仍限制过大边长，避免讯飞 10029/10041
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeFile(file.absolutePath, bounds)
            val maxSide = maxOf(bounds.outWidth, bounds.outHeight)
            if (maxSide in 1..5999 && file.length() <= MealConstants.MAX_UPLOAD_BYTES) {
                return
            }
        }
        val bitmap = decodeScaled(file, maxSide = 2000) ?: return
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, out)
        }
        bitmap.recycle()
        // 若仍超 4MB，再降质量
        var quality = 80
        while (file.length() > MealConstants.MAX_UPLOAD_BYTES && quality >= 50) {
            val again = decodeScaled(file, maxSide = 1600) ?: break
            FileOutputStream(file).use { out ->
                again.compress(Bitmap.CompressFormat.JPEG, quality, out)
            }
            again.recycle()
            quality -= 10
        }
    }

    private fun createThumbnail(source: File, target: File) {
        val bitmap = decodeScaled(source, MealConstants.THUMB_MAX_SIDE) ?: return
        FileOutputStream(target).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, MealConstants.THUMB_JPEG_QUALITY, out)
        }
        bitmap.recycle()
    }

    private fun decodeScaled(file: File, maxSide: Int): Bitmap? {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(file.absolutePath, bounds)
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null
        var sample = 1
        val larger = maxOf(bounds.outWidth, bounds.outHeight)
        while (larger / sample > maxSide * 2) {
            sample *= 2
        }
        val opts = BitmapFactory.Options().apply { inSampleSize = sample }
        val decoded = BitmapFactory.decodeFile(file.absolutePath, opts) ?: return null
        val w = decoded.width
        val h = decoded.height
        val scale = maxSide.toFloat() / maxOf(w, h).toFloat()
        if (scale >= 1f) return decoded
        val resized = Bitmap.createScaledBitmap(
            decoded,
            (w * scale).toInt().coerceAtLeast(1),
            (h * scale).toInt().coerceAtLeast(1),
            true
        )
        if (resized !== decoded) decoded.recycle()
        return resized
    }

    fun readExifTimestamp(file: File): Long? {
        return runCatching {
            val exif = ExifInterface(file.absolutePath)
            val raw = exif.getAttribute(ExifInterface.TAG_DATETIME_ORIGINAL)
                ?: exif.getAttribute(ExifInterface.TAG_DATETIME)
                ?: return null
            for (format in exifFormats) {
                val date = runCatching { format.parse(raw) }.getOrNull()
                if (date != null) return date.time
            }
            null
        }.getOrNull()
    }
}
