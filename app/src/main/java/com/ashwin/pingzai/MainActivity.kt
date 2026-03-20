package com.ashwin.pingzai

import android.os.Bundle
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Credentials
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : AppCompatActivity() {

    private lateinit var chatRecyclerView: RecyclerView
    private lateinit var chatAdapter: ChatAdapter
    private val messages = mutableListOf<ChatMessage>()
    private lateinit var messageInput: TextInputEditText
    private lateinit var sendButton: ImageButton
    
    // Replace with your actual backend URL
    private val BASE_URL = "http://10.0.2.2:8000/" // Android Emulator localhost

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
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        chatRecyclerView = findViewById(R.id.chatRecyclerView)
        messageInput = findViewById(R.id.messageInput)
        sendButton = findViewById(R.id.sendButton)

        chatAdapter = ChatAdapter(messages) { query, response, rating ->
            sendFeedback(query, response, rating)
        }
        chatRecyclerView.layoutManager = LinearLayoutManager(this)
        chatRecyclerView.adapter = chatAdapter

        val trainingButton: ImageButton = findViewById(R.id.trainingButton)
        trainingButton.setOnClickListener {
            val intent = android.content.Intent(this, TrainingActivity::class.java)
            intent.putExtra("BASE_URL", BASE_URL)
            startActivity(intent)
        }

        sendButton.setOnClickListener {
            val text = messageInput.text.toString().trim()
            if (text.isNotEmpty()) {
                sendMessage(text)
                messageInput.setText("")
            }
        }
    }

    private fun sendFeedback(query: String, response: String, rating: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val res = apiService.sendFeedback(FeedbackRequest(query, response, rating))
                withContext(Dispatchers.Main) {
                    if (res.isSuccessful) {
                        Toast.makeText(this@MainActivity, "Thanks for your feedback!", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                // Ignore network errors for feedback
            }
        }
    }

    private fun sendMessage(text: String) {
        // Add user message to UI
        messages.add(ChatMessage(text, true))
        chatAdapter.notifyItemInserted(messages.size - 1)
        chatRecyclerView.scrollToPosition(messages.size - 1)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = apiService.sendMessage(ChatRequest(text))
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        response.body()?.let {
                            addAIResponse(it.response)
                        }
                    } else {
                        addAIResponse("Error: ${response.code()}")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    addAIResponse("Error connecting to Pingz AI backend.")
                }
            }
        }
    }

    private fun addAIResponse(text: String) {
        messages.add(ChatMessage(text, false))
        chatAdapter.notifyItemInserted(messages.size - 1)
        chatRecyclerView.scrollToPosition(messages.size - 1)
    }
}
