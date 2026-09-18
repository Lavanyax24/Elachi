package com.elachi.app.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.elachi.app.ElachiApp
import com.elachi.app.data.repository.UserSession
import com.elachi.app.navigation.Screen
import com.elachi.app.ui.auth.AuthViewModel
import com.elachi.app.ui.auth.LoginScreen
import com.elachi.app.ui.auth.SignUpScreen
import com.elachi.app.ui.common.AppDrawerContent
import com.elachi.app.ui.common.ElachiBottomNavBar
import com.elachi.app.ui.common.ElachiTopBar
import com.elachi.app.ui.onboarding.CompleteProfileScreen
import com.elachi.app.ui.onboarding.CompleteProfileViewModel
import com.elachi.app.ui.onboarding.CreateFirstBookScreen
import com.elachi.app.ui.onboarding.OnboardingScreen
import com.elachi.app.ui.splash.SplashScreen
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

private val bottomNavRoutes = setOf(
    Screen.Home.route,
    Screen.Cookbook.route,
    Screen.Discover.route,
    Screen.Pantry.route,
)

private val drawerEnabledRoutes = setOf(
    Screen.Home.route,
    Screen.Cookbook.route,
    Screen.Pantry.route,
    Screen.AiChef.route,
    Screen.Achievements.route,
    Screen.Settings.route,
    Screen.Profile.route,
    Screen.StreakCalendar.route,
    Screen.Help.route,
    Screen.Discover.route,
)

@Composable
fun ElachiNavGraph() {

    val context = LocalContext.current
    val app = remember(context) {
        try {
            context.applicationContext as ElachiApp
        } catch (e: Exception) {
            null
        }
    }

    if (app == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Critical Error: Application context mismatch")
        }
        return
    }

    val elachiApp = app // Smart cast holder
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showBottomBar = bottomNavRoutes.any { it == currentRoute }
    val showDrawer = drawerEnabledRoutes.any { it == currentRoute }
    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = showDrawer,
        drawerContent = {
            AppDrawerContent(
                currentRoute = currentRoute,
                userDisplayName = "Chef",
                userEmail = "",
                onNavigate = { route ->
                    scope.launch { drawerState.close() }
                    navController.navigate(route) {
                        popUpTo(Screen.Home.route) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onLogout = {
                    scope.launch { drawerState.close() }
                    scope.launch {
                        try {
                            val token = com.google.firebase.messaging.FirebaseMessaging.getInstance().token.await()
                            try {
                                elachiApp.api.unregisterNotificationToken(
                                    com.elachi.app.data.remote.dto.NotificationTokenRequest(token)
                                )
                            } catch (e: Exception) {
                                android.util.Log.e("Logout", "Token unregister failed (non-fatal)", e)
                            }
                        } catch (e: Exception) {
                            android.util.Log.e("Logout", "Couldn't fetch FCM token", e)
                        }
                        elachiApp.authRepository.signOut()
                        navController.navigate(Screen.Login.route) { popUpTo(0) }
                    }
                }
            )
        },
    ) {
        Scaffold(
            bottomBar = { if (showBottomBar) ElachiBottomNavBar(navController) },
        ) { padding ->
            NavHost(
                navController = navController,
                startDestination = Screen.Splash.route,
                modifier = Modifier.padding(if (showBottomBar) padding else PaddingValues(0.dp)),
            ) {
                // ---------- Splash ----------
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

                // ---------- Login ----------
                composable(Screen.Login.route) {
                    val vm: AuthViewModel = viewModel(factory = SimpleViewModelFactory { AuthViewModel(elachiApp.authRepository) })
                    LoginScreen(
                        viewModel = vm,
                        onLoginSuccess = { isNewUser ->
                            if (isNewUser) {
                                navController.navigate(Screen.Onboarding.route) { popUpTo(0) }
                            } else {
                                navController.navigate(Screen.Home.route) { popUpTo(0) }
                            }
                        },
                        onNavigateToSignUp = { navController.navigate(Screen.SignUp.route) },
                    )
                }

                // ---------- Sign Up ----------
                composable(Screen.SignUp.route) {
                    val vm: AuthViewModel = viewModel(factory = SimpleViewModelFactory { AuthViewModel(elachiApp.authRepository) })
                    SignUpScreen(
                        viewModel = vm,
                        onSignUpSuccess = { isNewUser ->
                            if (isNewUser) {
                                navController.navigate(Screen.Onboarding.route) {
                                    popUpTo(Screen.SignUp.route) { inclusive = true }
                                }
                            } else {
                                navController.navigate(Screen.Home.route) { popUpTo(0) }
                            }
                        },
                        onNavigateToLogin = { navController.popBackStack() },
                    )
                }

                // ---------- Onboarding carousel ----------
                composable(Screen.Onboarding.route) {
                    OnboardingScreen(
                        onFinish = {
                            navController.navigate(Screen.CompleteProfile.route) {
                                popUpTo(Screen.Onboarding.route) { inclusive = true }
                            }
                        },
                    )
                }

                // ---------- Complete Profile ----------
                composable(Screen.CompleteProfile.route) {
                    val vm: CompleteProfileViewModel = viewModel(
                        factory = SimpleViewModelFactory { CompleteProfileViewModel(elachiApp.profileRepository) },
                    )
                    CompleteProfileScreen(
                        viewModel = vm,
                        isEditMode = false,
                        onDone = {
                            navController.navigate(Screen.CreateFirstBook.route) {
                                popUpTo(Screen.CompleteProfile.route) { inclusive = true }
                            }
                        },
                    )
                }

                // ---------- Create First Book ----------
                composable(Screen.CreateFirstBook.route) {
                    CreateFirstBookScreen(
                        onCreated = { name, description, coverImageUrl, icon, colour ->
                            val userId = UserSession.userId
                            if (userId != null) {
                                scope.launch {
                                    elachiApp.recipeRepository.createBook(
                                        userId = userId,
                                        name = name,
                                        description = description,
                                        icon = icon,
                                        colour = colour,
                                        coverImageUrl = coverImageUrl,
                                    )
                                }
                            }
                            navController.navigate(Screen.Home.route) { popUpTo(0) }
                        },
                        onSkip = {
                            navController.navigate(Screen.Home.route) { popUpTo(0) }
                        },
                    )
                }

                // ---------- Home ----------
                composable(Screen.Home.route) {
                    ScreenShell(
                        title = "Elachi",
                        route = currentRoute,
                        navController = navController,
                        drawerState = drawerState,
                    ) { Placeholder("Home") {} }
                }

                // ---------- Cookbook ----------
                composable(Screen.Cookbook.route) {
                    val userId = UserSession.userId
                    if (userId != null) {
                        val vm: com.elachi.app.ui.cookbook.CookbookViewModel = viewModel(
                            factory = SimpleViewModelFactory {
                                com.elachi.app.ui.cookbook.CookbookViewModel(userId, elachiApp.recipeRepository)
                            },
                        )
                        ScreenShell(
                            title = "My Cookbook",
                            route = currentRoute,
                            navController = navController,
                            drawerState = drawerState,
                        ) {
                            com.elachi.app.ui.cookbook.CookbookScreen(viewModel = vm)
                        }
                    } else {
                        ScreenShell(
                            title = "My Cookbook",
                            route = currentRoute,
                            navController = navController,
                            drawerState = drawerState,
                        ) { Placeholder("My Cookbook") {} }
                    }
                }

                // ---------- Discover ----------
                composable(Screen.Discover.route) {
                    ScreenShell(
                        title = "Discover",
                        route = currentRoute,
                        navController = navController,
                        drawerState = drawerState,
                    ) { Placeholder("Discover") {} }
                }

                // ---------- Pantry ----------
                composable(Screen.Pantry.route) {
                    ScreenShell(
                        title = "Pantry",
                        route = currentRoute,
                        navController = navController,
                        drawerState = drawerState,
                    ) { Placeholder("Pantry") {} }
                }

                // ---------- Profile ----------
                composable(Screen.Profile.route) {
                    ScreenShell(
                        title = "My Profile",
                        route = currentRoute,
                        navController = navController,
                        drawerState = drawerState,
                    ) { Placeholder("Profile") {} }
                }

                // ---------- Settings ----------
                composable(Screen.Settings.route) {
                    val vm: com.elachi.app.ui.settings.SettingsViewModel = viewModel(
                        factory = SimpleViewModelFactory {
                            com.elachi.app.ui.settings.SettingsViewModel(
                                elachiApp.settingsRepository,
                                elachiApp.authRepository,
                            )
                        },
                    )
                    com.elachi.app.ui.settings.SettingsScreen(
                        viewModel = vm,
                        onLoggedOut = {
                            navController.navigate(Screen.Login.route) { popUpTo(0) }
                        },
                        onNavigateToPrivacyPolicy = {
                            navController.navigate(Screen.PrivacyPolicy.route)
                        },
                        onNavigateToTerms = {
                            navController.navigate(Screen.TermsOfService.route)
                        },
                        onNavigateToEditProfile = { /* Profile edit screen is Phase 7 follow-up */ },
                    )
                }

                // ---------- Privacy Policy ----------
                composable(Screen.PrivacyPolicy.route) {
                    com.elachi.app.ui.settings.PrivacyPolicyScreen(
                        onBack = { navController.popBackStack() },
                    )
                }

                // ---------- Terms of Service ----------
                composable(Screen.TermsOfService.route) {
                    com.elachi.app.ui.settings.TermsOfServiceScreen(
                        onBack = { navController.popBackStack() },
                    )
                }

                // ---------- AI Chef ----------
                composable(Screen.AiChef.route) {
                    ScreenShell(
                        title = "AI Chef Assistant",
                        route = currentRoute,
                        navController = navController,
                        drawerState = drawerState,
                    ) { Placeholder("AI Chef Assistant") {} }
                }

                // ---------- Achievements ----------
                composable(Screen.Achievements.route) {
                    ScreenShell(
                        title = "Achievements",
                        route = currentRoute,
                        navController = navController,
                        drawerState = drawerState,
                    ) { Placeholder("Achievements") {} }
                }

                // ---------- Streak Calendar ----------
                composable(Screen.StreakCalendar.route) {
                    ScreenShell(
                        title = "Streak Calendar",
                        route = currentRoute,
                        navController = navController,
                        drawerState = drawerState,
                    ) { Placeholder("Streak Calendar") {} }
                }

                // ---------- Help ----------
                composable(Screen.Help.route) {
                    ScreenShell(
                        title = "Help & Support",
                        route = currentRoute,
                        navController = navController,
                        drawerState = drawerState,
                    ) { Placeholder("Help & Support") {} }
                }
            }
        }
    }
}

@Composable
private fun ScreenShell(
    title: String,
    route: String?,
    navController: androidx.navigation.NavController,
    drawerState: androidx.compose.material3.DrawerState,
    content: @Composable () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val canOpenDrawer = route in drawerEnabledRoutes

    Scaffold(
        topBar = {
            ElachiTopBar(
                title = title,
                onMenuClick = if (canOpenDrawer) {
                    { scope.launch { drawerState.open() } }
                } else null,
                onBackClick = if (!canOpenDrawer) {
                    { navController.popBackStack() }
                } else null,
            )
        },
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            content()
        }
    }
}

@Composable
private fun Placeholder(name: String, onClick: () -> Unit = {}) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}