package com.ashwin.pingzai

import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface ApiService {
    @POST("chat")
    suspend fun sendMessage(@Body request: ChatRequest): Response<ChatResponse>

    @FormUrlEncoded
    @POST("train/url")
    suspend fun trainUrl(@Field("url") url: String): Response<Map<String, String>>

    @FormUrlEncoded
    @POST("train/youtube")
    suspend fun trainYoutube(@Field("url") url: String): Response<Map<String, String>>

    @Multipart
    @POST("train/upload")
    suspend fun trainUpload(@Part file: MultipartBody.Part): Response<Map<String, String>>

    @POST("feedback")
    suspend fun sendFeedback(@Body feedback: FeedbackRequest): Response<Map<String, String>>
}
