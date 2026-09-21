package com.elachi.app

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.elachi.app.navigation.ElachiNavGraph
import com.elachi.app.notifications.ElachiMessagingService
import com.elachi.app.ui.theme.ElachiTheme
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            enableEdgeToEdge()
        } catch (e: Exception) {
            android.util.Log.e("MainActivity", "edge-to-edge failed", e)
        }

        if (Build.VERSION.SDK_INT >= 33) {
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 0)
        }

        val app = application as ElachiApp
        val settingsRepository = app.settingsRepository

        app.authRepository.currentUser?.let { user ->
            FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
                ElachiMessagingService.registerTokenWithBackend(applicationContext, token)
            }
            
            lifecycleScope.launch {
                try {
                    app.recipeRepository.retryPendingSyncs()
                    app.pantryRepository.retryPendingPantrySync(user.uid)
                } catch (e: Exception) {
                    android.util.Log.e("MainActivity", "Pending sync retry failed", e)
                }
            }
        }

        setContent {
            val user by app.authRepository.authState.collectAsState(initial = app.authRepository.currentUser)
            val isDarkTheme by remember(user) {
                settingsRepository.isDarkTheme(user?.uid)
            }.collectAsState(initial = false)

            ElachiTheme(useDarkTheme = isDarkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    ElachiNavGraph()
                }
            }
        }
    }
}