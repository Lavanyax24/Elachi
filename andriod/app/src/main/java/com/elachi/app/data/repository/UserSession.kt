package com.elachi.app.data.repository


object UserSession {
    var userId: String? = null
        private set
    var friendCode: String? = null
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