package com.example.adopet.utils

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import java.io.ByteArrayOutputStream

class PinataUploader(private val jwt: String) {

    private val client = OkHttpClient()
    private val uploadUrl = "https://uploads.pinata.cloud/v3/files"

    suspend fun uploadImage(context: Context, uri: Uri): String? = withContext(Dispatchers.IO) {
        try {
            // 1) Bitmap'e çevir
            val bitmap = MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
            val baos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 75, baos)
            val imageBytes = baos.toByteArray()

            // 2) multipart form-data body
            val fileBody = RequestBody.create("image/jpeg".toMediaTypeOrNull(), imageBytes)

            val multipartBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", "adopet_${System.currentTimeMillis()}.jpg", fileBody)
                .addFormDataPart("network", "public") // public IPFS ağı
                .build()

            // 3) HTTP isteği
            val request = Request.Builder()
                .url(uploadUrl)
                .addHeader("Authorization", "Bearer $jwt")
                .post(multipartBody)
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return@withContext null
                }

                val body = response.body?.string() ?: return@withContext null

                // JSON içinden "cid" alanını regex ile çekiyoruz
                val cidRegex = """"cid"\s*:\s*"([^"]+)"""".toRegex()
                val match = cidRegex.find(body)
                val cid = match?.groups?.get(1)?.value
                return@withContext cid
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext null
        }
    }
}
