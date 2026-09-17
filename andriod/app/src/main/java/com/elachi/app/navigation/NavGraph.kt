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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.elachi.app.navigation.Screen
import com.elachi.app.ui.common.AppDrawerContent
import com.elachi.app.ui.common.ElachiBottomNavBar
import com.elachi.app.ui.common.ElachiTopBar
import com.elachi.app.ui.onboarding.OnboardingScreen
import com.elachi.app.ui.splash.SplashScreen
import kotlinx.coroutines.launch

/** Routes that show the bottom navigation bar. */
private val bottomNavRoutes = setOf(
    Screen.Home.route,
    Screen.Cookbook.route,
    Screen.Discover.route,
    Screen.Pantry.route,
)

/** Routes that show the hamburger drawer. */
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
                    // TODO: clear session once AuthRepository is added in Phase 1
                    navController.navigate(Screen.Login.route) { popUpTo(0) }
                },
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
                    Placeholder("Login") {
                        navController.navigate(Screen.SignUp.route)
                    }
                }

                composable(Screen.SignUp.route) {
                    Placeholder("Sign Up") {
                        navController.navigate(Screen.Onboarding.route)
                    }
                }

                composable(Screen.Onboarding.route) {
                    OnboardingScreen(
                        onFinish = {
                            navController.navigate(Screen.Home.route) {
                                popUpTo(0)
                            }
                        },
                    )
                }

                composable(Screen.Home.route) {
                    ScreenShell(
                        title = "Elachi",
                        route = currentRoute,
                        navController = navController,
                        drawerState = drawerState,
                    ) { Placeholder("Home") {} }
                }

                composable(Screen.Cookbook.route) {
                    ScreenShell(
                        title = "My Cookbook",
                        route = currentRoute,
                        navController = navController,
                        drawerState = drawerState,
                    ) { Placeholder("My Cookbook") {} }
                }

                composable(Screen.Discover.route) {
                    ScreenShell(
                        title = "Discover",
                        route = currentRoute,
                        navController = navController,
                        drawerState = drawerState,
                    ) { Placeholder("Discover") {} }
                }

                composable(Screen.Pantry.route) {
                    ScreenShell(
                        title = "Pantry",
                        route = currentRoute,
                        navController = navController,
                        drawerState = drawerState,
                    ) { Placeholder("Pantry") {} }
                }

                composable(Screen.Profile.route) {
                    ScreenShell(
                        title = "My Profile",
                        route = currentRoute,
                        navController = navController,
                        drawerState = drawerState,
                    ) { Placeholder("Profile") {} }
                }

                composable(Screen.Settings.route) {
                    ScreenShell(
                        title = "Settings",
                        route = currentRoute,
                        navController = navController,
                        drawerState = drawerState,
                    ) { Placeholder("Settings") {} }
                }

                composable(Screen.AiChef.route) {
                    ScreenShell(
                        title = "AI Chef Assistant",
                        route = currentRoute,
                        navController = navController,
                        drawerState = drawerState,
                    ) { Placeholder("AI Chef Assistant") {} }
                }

                composable(Screen.Achievements.route) {
                    ScreenShell(
                        title = "Achievements",
                        route = currentRoute,
                        navController = navController,
                        drawerState = drawerState,
                    ) { Placeholder("Achievements") {} }
                }

                composable(Screen.StreakCalendar.route) {
                    ScreenShell(
                        title = "Streak Calendar",
                        route = currentRoute,
                        navController = navController,
                        drawerState = drawerState,
                    ) { Placeholder("Streak Calendar") {} }
                }

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