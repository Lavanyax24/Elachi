package com.elachi.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.elachi.app.navigation.ElachiNavGraph
import com.elachi.app.ui.theme.ElachiTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            enableEdgeToEdge()
        } catch (e: Exception) {
            android.util.Log.e("MainActivity", "edge-to-edge failed", e)
        }

        val app = application as ElachiApp
        val settingsRepository = app.settingsRepository

        setContent {
            val isDarkTheme by settingsRepository.isDarkTheme.collectAsState(initial = false)

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