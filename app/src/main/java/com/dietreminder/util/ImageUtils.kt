package com.dietreminder.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

object ImageUtils {

    fun createMealPhotoFile(context: Context): File {
        val dir = File(context.filesDir, "meal_photos")
        if (!dir.exists()) dir.mkdirs()
        val ts = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        return File(dir, "meal_$ts.jpg")
    }

    fun compressAndSave(sourcePath: String, targetFile: File, quality: Int = 60): String? {
        return try {
            val options = BitmapFactory.Options().apply { inSampleSize = 2 }
            val bitmap = BitmapFactory.decodeFile(sourcePath, options) ?: return null
            FileOutputStream(targetFile).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, quality, out)
            }
            bitmap.recycle()
            targetFile.absolutePath
        } catch (e: Exception) { null }
    }

    fun deleteOldPhotos(context: Context, cutoffMs: Long) {
        val dir = File(context.filesDir, "meal_photos")
        if (!dir.exists()) return
        dir.listFiles()?.forEach { file ->
            if (file.lastModified() < cutoffMs) file.delete()
        }
    }
}
