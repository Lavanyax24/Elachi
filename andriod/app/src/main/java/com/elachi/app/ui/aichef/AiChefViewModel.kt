package com.elachi.app.ui.aichef

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elachi.app.data.repository.ChatRepository
import com.elachi.app.data.repository.ChatTurn
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AiChefViewModel(private val chatRepository: ChatRepository) : ViewModel() {

    private val _messages = MutableStateFlow(
        listOf(
            ChatTurn(
                fromUser = false,
                text = "Hi! I'm your AI Chef Assistant. I can help with substitutions, recipe ideas from leftovers, nutritional info, and cooking tips. What can I cook up for you today?",
            ),
        ),
    )
    val messages: StateFlow<List<ChatTurn>> = _messages.asStateFlow()

    private val _isSending = MutableStateFlow(false)
    val isSending: StateFlow<Boolean> = _isSending.asStateFlow()

    fun sendMessage(text: String) {
        if (text.isBlank() || _isSending.value) return
        _messages.value = _messages.value + ChatTurn(fromUser = true, text = text)
        _isSending.value = true
        viewModelScope.launch {
            chatRepository.sendMessage(text)
                .onSuccess { reply -> _messages.value = _messages.value + ChatTurn(fromUser = false, text = reply) }
                .onFailure { error -> _messages.value = _messages.value + ChatTurn(fromUser = false, text = error.message ?: "Something went wrong.") }
            _isSending.value = false
        }
    }
}