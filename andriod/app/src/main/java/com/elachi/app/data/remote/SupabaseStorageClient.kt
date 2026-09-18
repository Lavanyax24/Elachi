package com.elachi.app.data.remote

import android.content.Context
import android.net.Uri
import com.elachi.app.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream
import java.util.UUID


object SupabaseStorageClient {

    private val client = OkHttpClient()

    suspend fun uploadImage(context: Context, uri: Uri, folder: String): Result<String> =
        withContext(Dispatchers.IO) {
            try {
                val fileName = "${folder}/${UUID.randomUUID()}.jpg"
                val tempFile = copyUriToTempFile(context, uri)

                val request = Request.Builder()
                    .url("${BuildConfig.SUPABASE_URL}/storage/v1/object/${BuildConfig.SUPABASE_BUCKET}/$fileName")
                    .addHeader("apikey", BuildConfig.SUPABASE_ANON_KEY)
                    .addHeader("Authorization", "Bearer ${BuildConfig.SUPABASE_ANON_KEY}")
                    .post(tempFile.asRequestBody("image/jpeg".toMediaTypeOrNull()))
                    .build()

                client.newCall(request).execute().use { response ->
                    tempFile.delete()
                    if (!response.isSuccessful) {
                        return@withContext Result.failure(
                            Exception("Image upload failed (HTTP ${response.code}). Check your Supabase bucket policy allows uploads."),
                        )
                    }
                    val publicUrl = "${BuildConfig.SUPABASE_URL}/storage/v1/object/public/${BuildConfig.SUPABASE_BUCKET}/$fileName"
                    Result.success(publicUrl)
                }
            } catch (e: Exception) {
                Result.failure(Exception("Couldn't upload image: ${e.localizedMessage}"))
            }
        }

    private fun copyUriToTempFile(context: Context, uri: Uri): File {
        val tempFile = File.createTempFile("upload_", ".jpg", context.cacheDir)
        context.contentResolver.openInputStream(uri).use { input ->
            FileOutputStream(tempFile).use { output ->
                input?.copyTo(output)
            }
        }
        return tempFile
    }
}