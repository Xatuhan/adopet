package com.example.adopet.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream
import java.io.InputStream
import kotlin.math.roundToInt

class PinataUploader(private val jwt: String) {

    private data class PinataResponse(val IpfsHash: String?)

    private val client = OkHttpClient()
    private val gson = Gson()
    private val uploadUrl = " https://api.pinata.cloud/pinning/pinFileToIPFS "

    private val MAX_IMAGE_DIMENSION = 1080

    suspend fun uploadImage(context: Context, uri: Uri): String? = withContext(Dispatchers.IO) {
        try {
            val resizedBitmap = resizeBitmap(context, uri)
            if (resizedBitmap == null) {
                Log.e("PinataUploader", "Resim küçültülemedi veya okunamadı.")
                return@withContext null
            }
            
            val baos = ByteArrayOutputStream()
            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 85, baos)
            val imageBytes = baos.toByteArray()

            val fileName = "adopet_${System.currentTimeMillis()}.jpg"
            val fileBody = imageBytes.toRequestBody("image/jpeg".toMediaTypeOrNull())

            val metadata = """{"name": "$fileName"}"""

            val multipartBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", fileName, fileBody)
                .addFormDataPart("pinataMetadata", metadata)
                .build()

            val request = Request.Builder()
                .url(uploadUrl)
                .addHeader("Authorization", "Bearer $jwt")
                .post(multipartBody)
                .build()

            client.newCall(request).execute().use { response ->
                val responseBodyString = response.body?.string()

                if (!response.isSuccessful) {
                    Log.e("PinataUploader", "Yükleme başarısız oldu: ${response.code}")
                    Log.e("PinataUploader", "Cevap: $responseBodyString")
                    return@withContext null
                }

                Log.d("PinataUploader", "Başarılı cevap: $responseBodyString")

                if (responseBodyString.isNullOrEmpty()) {
                    Log.e("PinataUploader", "Pinata cevap gövdesi boş.")
                    return@withContext null
                }

                val pinataResponse = gson.fromJson(responseBodyString, PinataResponse::class.java)
                return@withContext pinataResponse.IpfsHash
            }
        } catch (e: Exception) {
            Log.e("PinataUploader", "Fotoğraf yüklenirken istisna oluştu:", e)
            return@withContext null
        }
    }

    private fun resizeBitmap(context: Context, uri: Uri): Bitmap? {
        var inputStream: InputStream? = null
        try {
            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            inputStream = context.contentResolver.openInputStream(uri)
            BitmapFactory.decodeStream(inputStream, null, options)
            inputStream?.close()

            var scale = 1
            while (options.outWidth / scale / 2 >= MAX_IMAGE_DIMENSION &&
                options.outHeight / scale / 2 >= MAX_IMAGE_DIMENSION) {
                scale *= 2
            }

            val decodeOptions = BitmapFactory.Options().apply { inSampleSize = scale }
            inputStream = context.contentResolver.openInputStream(uri)
            return BitmapFactory.decodeStream(inputStream, null, decodeOptions)
        } catch (e: Exception) {
            Log.e("PinataUploader", "Resim boyutlandırılamadı", e)
            return null
        } finally {
            inputStream?.close()
        }
    }
}
