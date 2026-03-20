package com.ashwin.pingzai

data class ChatMessage(val text: String, val isUser: Boolean)
data class ChatRequest(val message: String)
data class ChatResponse(val response: String)
data class FeedbackRequest(val message: String, val response: String, val rating: Int)
