package com.elachi.app.data.repository

import android.util.Log
import com.elachi.app.data.remote.RetrofitClient
import com.elachi.app.data.remote.dto.UserSyncRequest
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

sealed class AuthResult {
    data class Success(val user: FirebaseUser, val isNewUser: Boolean = false) : AuthResult()
    data class Error(val message: String) : AuthResult()
}

class AuthRepository(
    private val firebaseAuth: FirebaseAuth? = try { FirebaseAuth.getInstance() } catch (e: Exception) { null },
) {
    val currentUser: FirebaseUser? get() = firebaseAuth?.currentUser

    val authState: Flow<FirebaseUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            trySend(auth.currentUser)
        }
        firebaseAuth?.addAuthStateListener(listener)
        awaitClose {
            firebaseAuth?.removeAuthStateListener(listener)
        }
    }

    suspend fun signUpWithEmail(
        firstName: String,
        surname: String,
        email: String,
        password: String,
    ): AuthResult {
        val auth = firebaseAuth ?: return AuthResult.Error("Firebase not initialized correctly.")
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val user = result.user ?: return AuthResult.Error("Registration failed. Please try again.")
            syncProfileWithBackend(user, firstName, surname)
            AuthResult.Success(user, isNewUser = true)
        } catch (e: Exception) {
            AuthResult.Error(e.localizedMessage ?: "Registration failed.")
        }
    }

    suspend fun signInWithEmail(email: String, password: String): AuthResult {
        val auth = firebaseAuth ?: return AuthResult.Error("Firebase not initialized correctly.")
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val user = result.user ?: return AuthResult.Error("Sign in failed. Please try again.")

            // Derive name from the Firebase user's displayName if present.
            // For email/password sign-ins this is often blank, so fall back to
            // an empty string — the backend generates a profile either way.
            val (first, last) = splitDisplayName(user.displayName)
            syncProfileWithBackend(user, firstName = first, surname = last)

            AuthResult.Success(user, isNewUser = false)
        } catch (e: Exception) {
            AuthResult.Error(e.localizedMessage ?: "Incorrect email or password.")
        }
    }

    suspend fun signInWithGoogle(idToken: String): AuthResult {
        val auth = firebaseAuth ?: return AuthResult.Error("Firebase not initialized correctly.")
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val result = auth.signInWithCredential(credential).await()
            val user = result.user ?: return AuthResult.Error("Google sign-in failed.")
            val isNewUser = result.additionalUserInfo?.isNewUser == true

            val (first, last) = splitDisplayName(user.displayName)
            syncProfileWithBackend(user, firstName = first, surname = last)

            AuthResult.Success(user, isNewUser = isNewUser)
        } catch (e: Exception) {
            AuthResult.Error(e.localizedMessage ?: "Google sign-in failed.")
        }
    }

    /**
     * Splits a Firebase displayName into (first, last). Google sign-ins
     * typically return "First Last"; email/password sign-ups often return
     * null, in which case both parts come back blank and the backend
     * generates a fallback display name.
     */
    private fun splitDisplayName(displayName: String?): Pair<String, String> {
        if (displayName.isNullOrBlank()) return "" to ""
        val parts = displayName.trim().split(" ", limit = 2)
        val first = parts.getOrElse(0) { "" }
        val last = parts.getOrElse(1) { "" }
        return first to last
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
                Log.i("AuthRepository", "Synced userId=${body.userId}, code=${body.friendCode}")
            } else {
                Log.e("AuthRepository", "Backend sync returned HTTP ${response.code()} — body: ${response.errorBody()?.string()}")
            }
        } catch (e: Exception) {
            Log.e("AuthRepository", "Backend sync failed", e)
        }
    }

    fun signOut() {
        firebaseAuth?.signOut()
        UserSession.clear()
    }

    /**
     * Attempts to restore the UserSession from the current Firebase user.
     * Should be called at app startup if currentUser is not null.
     */
    suspend fun restoreSession(): Boolean {
        val user = currentUser ?: return false
        val (first, last) = splitDisplayName(user.displayName)
        syncProfileWithBackend(user, firstName = first, surname = last)
        return UserSession.userId != null
    }
}