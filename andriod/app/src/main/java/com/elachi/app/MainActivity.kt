package com.elachi.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.elachi.app.navigation.Screen
import com.elachi.app.ui.onboarding.OnboardingScreen
import com.elachi.app.ui.splash.SplashScreen
import com.elachi.app.ui.theme.ElachiTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ElachiTheme {
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

@androidx.compose.runtime.Composable
private fun ElachiNavGraph() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route,
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
            )
        }

        composable(Screen.Login.route) {
            PlaceholderScreen("Login")
        }

        composable(Screen.SignUp.route) {
            PlaceholderScreen("Sign Up")
        }

        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                onFinish = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                },
            )
        }

        composable(Screen.Home.route) {
            PlaceholderScreen("Home")
        }

        composable(Screen.Cookbook.route) {
            PlaceholderScreen("My Cookbook")
        }

        composable(Screen.Discover.route) {
            PlaceholderScreen("Discover")
        }

        composable(Screen.Pantry.route) {
            PlaceholderScreen("Pantry")
        }

        composable(Screen.Profile.route) {
            PlaceholderScreen("Profile")
        }

        composable(Screen.Settings.route) {
            PlaceholderScreen("Settings")
        }

        composable(Screen.AiChef.route) {
            PlaceholderScreen("AI Chef Assistant")
        }

        composable(Screen.Achievements.route) {
            PlaceholderScreen("Achievements")
        }

        composable(Screen.StreakCalendar.route) {
            PlaceholderScreen("Streak Calendar")
        }

        composable(Screen.Help.route) {
            PlaceholderScreen("Help & Support")
        }
    }
}

@androidx.compose.runtime.Composable
private fun PlaceholderScreen(name: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text = name,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}