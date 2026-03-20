package com.ashwin.pingzai

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Credentials
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class TrainingActivity : AppCompatActivity() {

    private lateinit var urlInput: TextInputEditText
    private lateinit var btnTrainUrl: Button
    private lateinit var btnTrainYoutube: Button
    private lateinit var btnUploadFile: Button

    private val apiService by lazy {
        val client = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val credentials = Credentials.basic("admin", "pingz123")
                val request = chain.request().newBuilder()
                    .header("Authorization", credentials)
                    .build()
                chain.proceed(request)
            }.build()

        Retrofit.Builder()
            .baseUrl(intent.getStringExtra("BASE_URL") ?: "http://10.0.2.2:8000/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }

    private val filePickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri = result.data?.data
            uri?.let { uploadFile(it) }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_training)

        urlInput = findViewById(R.id.urlInput)
        btnTrainUrl = findViewById(R.id.btnTrainUrl)
        btnTrainYoutube = findViewById(R.id.btnTrainYoutube)
        btnUploadFile = findViewById(R.id.btnUploadFile)

        btnTrainUrl.setOnClickListener {
            val url = urlInput.text.toString().trim()
            if (url.isNotEmpty()) trainViaUrl(url, isYoutube = false)
        }

        btnTrainYoutube.setOnClickListener {
            val url = urlInput.text.toString().trim()
            if (url.isNotEmpty()) trainViaUrl(url, isYoutube = true)
        }

        btnUploadFile.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "*/*"
            val mimetypes = arrayOf("application/pdf", "text/plain")
            intent.putExtra(Intent.EXTRA_MIME_TYPES, mimetypes)
            filePickerLauncher.launch(intent)
        }
    }

    private fun trainViaUrl(url: String, isYoutube: Boolean) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = if (isYoutube) {
                    apiService.trainYoutube(url)
                } else {
                    apiService.trainUrl(url)
                }
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@TrainingActivity, "Training submitted successfully", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@TrainingActivity, "Failed to train: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@TrainingActivity, "Network error", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun uploadFile(uri: Uri) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val file = getFileFromUri(uri)
                val requestFile = file.asRequestBody("multipart/form-data".toMediaTypeOrNull())
                val body = MultipartBody.Part.createFormData("file", file.name, requestFile)
                val response = apiService.trainUpload(body)
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@TrainingActivity, "File uploaded successfully", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@TrainingActivity, "Failed to upload: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@TrainingActivity, "Network error", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun getFileFromUri(uri: Uri): File {
        val contentResolver = contentResolver
        val fileName = getFileName(uri) ?: "temp_file"
        val file = File(cacheDir, fileName)
        val inputStream = contentResolver.openInputStream(uri)
        val outputStream = FileOutputStream(file)
        inputStream?.use { input ->
            outputStream.use { output ->
                input.copyTo(output)
            }
        }
        return file
    }

    private fun getFileName(uri: Uri): String? {
        var name: String? = null
        if (uri.scheme == "content") {
            val cursor = contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val index = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                    if (index != -1) {
                        name = it.getString(index)
                    }
                }
            }
        }
        if (name == null) {
            name = uri.path
            val cut = name?.lastIndexOf('/')
            if (cut != -1 && cut != null) {
                name = name?.substring(cut + 1)
            }
        }
        return name
    }
}
