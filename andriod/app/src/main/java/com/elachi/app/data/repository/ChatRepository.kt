package com.elachi.app.data.repository

import com.elachi.app.data.remote.ApiService
import com.elachi.app.data.remote.dto.ChatRequest

//This class handles the chat data.

data class ChatTurn(val fromUser: Boolean, val text: String)


class ChatRepository(private val api: ApiService) {

    private var conversationId: String? = null

    suspend fun sendMessage(message: String): Result<String> = try {
        val response = api.sendChatMessage(ChatRequest(message = message, conversationId = conversationId))
        if (response.isSuccessful && response.body() != null) {
            val body = response.body()!!
            conversationId = body.conversationId
            Result.success(body.reply)
        } else {
            Result.failure(Exception("The AI Chef Assistant is unavailable right now (HTTP ${response.code()})."))
        }
    } catch (e: Exception) {
        Result.failure(Exception("Couldn't reach the AI Chef Assistant. Check your connection and try again."))
    }
}