package com.elachi.app.data.repository


import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

object UserSession {
    var userId by mutableStateOf<String?>(null)
        private set
    var friendCode by mutableStateOf<String?>(null)
        private set

    fun set(userId: String, friendCode: String) {
        this.userId = userId
        this.friendCode = friendCode
    }

    fun clear() {
        userId = null
        friendCode = null
    }

    fun requireUserId(): String =
        userId ?: throw IllegalStateException("UserSession.userId accessed before sync with backend completed.")
}