package com.elachi.app.data.repository

import android.util.Log
import com.elachi.app.data.remote.RetrofitClient
import com.elachi.app.data.remote.dto.UserSyncRequest
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await

sealed class AuthResult {
    data class Success(val user: FirebaseUser) : AuthResult()
    data class Error(val message: String) : AuthResult()
}

class AuthRepository(
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance(),
) {
    val currentUser: FirebaseUser? get() = firebaseAuth.currentUser

    suspend fun signUpWithEmail(
        firstName: String,
        surname: String,
        email: String,
        password: String,
    ): AuthResult {
        return try {
            val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            val user = result.user ?: return AuthResult.Error("Registration failed. Please try again.")
            syncProfileWithBackend(user, firstName, surname)
            AuthResult.Success(user)
        } catch (e: Exception) {
            AuthResult.Error(e.localizedMessage ?: "Registration failed.")
        }
    }

    suspend fun signInWithEmail(email: String, password: String): AuthResult {
        return try {
            val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            val user = result.user ?: return AuthResult.Error("Sign in failed. Please try again.")

            syncProfileWithBackend(user, firstName = "", surname = "")
            AuthResult.Success(user)
        } catch (e: Exception) {
            AuthResult.Error(e.localizedMessage ?: "Incorrect email or password.")
        }
    }

    suspend fun signInWithGoogle(idToken: String): AuthResult {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val result = firebaseAuth.signInWithCredential(credential).await()
            val user = result.user ?: return AuthResult.Error("Google sign-in failed.")
            val nameParts = (user.displayName ?: "").split(" ", limit = 2)
            syncProfileWithBackend(
                user,
                firstName = nameParts.getOrElse(0) { "" },
                surname = nameParts.getOrElse(1) { "" },
            )
            AuthResult.Success(user)
        } catch (e: Exception) {
            AuthResult.Error(e.localizedMessage ?: "Google sign-in failed.")
        }
    }

    private suspend fun syncProfileWithBackend(user: FirebaseUser, firstName: String, surname: String) {
        try {
            val response = RetrofitClient.apiService.syncUser(
                UserSyncRequest(
                    firebaseUid = user.uid,
                    email = user.email.orEmpty(),
                    firstName = firstName,
                    surname = surname,
                ),
            )

            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                UserSession.set(body.userId, body.friendCode)
            } else {
                Log.e("AuthRepository", "Backend sync returned HTTP ${response.code()}")
            }
        } catch (e: Exception) {

            Log.e("AuthRepository", "Backend sync failed", e)
        }
    }

    fun signOut() {
        firebaseAuth.signOut()

        UserSession.clear()
    }
}