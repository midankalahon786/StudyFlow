package com.example.lmsapp.ui.network

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

object FileUtils {
    fun createMultipartFromUri(context: Context, uri: Uri, partName: String): MultipartBody.Part? {
        val inputStream = context.contentResolver.openInputStream(uri) ?: return null

        // Get original file name and extract extension
        val originalFileName = getFileName(context, uri)
        val extension = originalFileName.substringAfterLast('.', "tmp")

        // Create temp file with same extension
        val tempFile = File.createTempFile("upload_", ".$extension", context.cacheDir)

        val outputStream = FileOutputStream(tempFile)
        inputStream.copyTo(outputStream)
        outputStream.close()
        inputStream.close()

        val requestFile = tempFile.asRequestBody("application/octet-stream".toMediaTypeOrNull())

        // Send the original file name to the server
        return MultipartBody.Part.createFormData(partName, originalFileName, requestFile)
    }

    fun getFileName(context: Context, uri: Uri): String {
        var result: String? = null
        if (uri.scheme == "content") {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (nameIndex != -1) {
                        result = it.getString(nameIndex)
                    }
                }
            }
        }
        if (result == null) {
            result = uri.path
            val cut = result?.lastIndexOf('/')
            if (cut != null && cut != -1) {
                result = result?.substring(cut + 1)
            }
        }
        return result ?: "unknown_file.tmp"
    }
    fun getResourceTypeFromExtension(fileName: String): String {
        return when (fileName.substringAfterLast('.', "").lowercase()) {
            "pdf" -> "PDF"
            "doc", "docx" -> "DOCUMENT"
            "ppt", "pptx" -> "PRESENTATION"
            "xls", "xlsx" -> "SPREADSHEET"
            "jpg", "jpeg", "png" -> "IMAGE"
            "mp4", "mov", "avi", "mkv" -> "VIDEO"
            else -> "FILE" // fallback
        }
    }

}


fun formatDate(dateString: String?): String {
    if (dateString.isNullOrEmpty()) return ""
    return try {
        // Example backend format: "2023-10-27T10:00:00.000Z"
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        inputFormat.timeZone = TimeZone.getTimeZone("UTC") // Important: Parse UTC date from backend

        val date = inputFormat.parse(dateString)

        // Desired output format
        val outputFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
        // You might want to set outputFormat.timeZone to your local timezone if you want localized display
        if (date != null) {
            outputFormat.format(date)
        } else {
            dateString // Return original string if parsing fails
        }
    } catch (e: Exception) {
        // Log the exception for debugging if date parsing fails frequently
        // Log.e("DateFormatter", "Error parsing date: $dateString", e)
        dateString // Return original string if parsing fails
    }
}
