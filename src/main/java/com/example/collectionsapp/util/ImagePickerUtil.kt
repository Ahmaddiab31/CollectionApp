package com.example.collectionsapp.util

import android.content.Context
import android.net.Uri
import android.os.Environment
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class ImagePickerUtil(private val context: Context) {

    companion object {
        fun createTempImageFile(context: Context): File {
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val imageFileName = "JPEG_${timeStamp}_"
            val storageDir = context.cacheDir
            return File.createTempFile(imageFileName, ".jpg", storageDir)
        }
    }

    /**
     * حفظ الصورة المختارة في مجلد التطبيق وإرجاع المسار
     */
    fun saveImageToAppStorage(uri: Uri): String? {
        return try {
            val fileName = "IMG_${System.currentTimeMillis()}.jpg"
            val file = File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), fileName)

            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                FileOutputStream(file).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }

            file.absolutePath
        } catch (e: Exception) {
            null
        }
    }

    /**
     * الحصول على Uri للملف
     */
    fun getUriForFile(file: File): Uri {
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.imageprovider",
            file
        )
    }
}