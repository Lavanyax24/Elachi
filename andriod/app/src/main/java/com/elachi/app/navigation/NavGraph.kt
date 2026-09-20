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
import com.elachi.app.ui.auth.AuthViewModel
import com.elachi.app.ui.auth.LoginScreen
import com.elachi.app.ui.auth.SignUpScreen
import com.elachi.app.ui.common.AppDrawerContent
import com.elachi.app.ui.common.ElachiBottomNavBar
import com.elachi.app.ui.common.ElachiTopBar
import com.elachi.app.ui.cookbook.CookbookViewModel
import com.elachi.app.ui.help.HelpScreen
import com.elachi.app.ui.home.HomeScreen
import com.elachi.app.ui.home.HomeViewModel
import com.elachi.app.ui.onboarding.CompleteProfileScreen
import com.elachi.app.ui.onboarding.CompleteProfileViewModel
import com.elachi.app.ui.onboarding.CreateFirstBookScreen
import com.elachi.app.ui.onboarding.OnboardingScreen
import com.elachi.app.ui.profile.ProfileScreen
import com.elachi.app.ui.profile.ProfileViewModel
import com.elachi.app.ui.recipe.AddRecipeScreen
import com.elachi.app.ui.recipe.AddRecipeViewModel
import com.elachi.app.ui.recipe.CameraCaptureScreen
import com.elachi.app.ui.recipe.CameraCaptureViewModel
import com.elachi.app.ui.cookbook.CookbookScreen
import com.elachi.app.ui.cookbook.RecipeBookDetailScreen
import com.elachi.app.ui.cookbook.RecipeBookDetailViewModel
import com.elachi.app.ui.recipe.CookModeScreen
import com.elachi.app.ui.recipe.RecipeDetailScreen
import com.elachi.app.ui.recipe.RecipeDetailViewModel
import com.elachi.app.ui.settings.PrivacyPolicyScreen
import com.elachi.app.ui.settings.SettingsScreen
import com.elachi.app.ui.settings.SettingsViewModel
import com.elachi.app.ui.settings.TermsOfServiceScreen
import com.elachi.app.ui.splash.SplashScreen
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.elachi.app.ui.discover.DiscoverScreen
import com.elachi.app.ui.discover.DiscoverViewModel
import com.elachi.app.ui.pantry.PantryScreen
import com.elachi.app.ui.pantry.PantryViewModel
import com.elachi.app.ui.achievements.AchievementsScreen
import com.elachi.app.ui.achievements.AchievementsViewModel
import com.elachi.app.ui.aichef.AiChefScreen
import com.elachi.app.ui.aichef.AiChefViewModel
import com.elachi.app.ui.streak.StreakCalendarScreen
import com.elachi.app.ui.streak.StreakCalendarViewModel

private val bottomNavRoutes = setOf(
    Screen.Home.route,
    Screen.Cookbook.route,
    Screen.Discover.route,
    Screen.Pantry.route,
    Screen.Profile.route,
    Screen.Settings.route,
)

private val drawerEnabledRoutes = setOf(
    Screen.Home.route,
    Screen.Cookbook.route,
    Screen.Pantry.route,
    Screen.Settings.route,
    Screen.Profile.route,
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

    // ---------- Drawer header data ----------
    // Scoped to NavGraph so the drawer has access to real profile data.
    // Only constructed when a user is signed in.
    val currentUserId = UserSession.userId
    val profileVm: ProfileViewModel? = if (currentUserId != null) {
        viewModel(
            factory = SimpleViewModelFactory {
                ProfileViewModel(
                    currentUserId,
                    elachiApp.profileRepository,
                    elachiApp.recipeRepository,
                    elachiApp.database.achievementDao(),
                )
            },
        )
    } else null

    // Real display name from the profile fetch; falls back to "Chef" while
    // the request is in flight or if the profile has a blank name.
    val drawerDisplayName = profileVm?.profile?.value?.displayName
        ?.takeIf { it.isNotBlank() }
        ?: "Chef"

    // UserProfileDto has no email field, and the drawer gracefully handles an
    // empty subtitle. Left blank rather than faking a value.
    val drawerEmail = ""

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = showDrawer,
        drawerContent = {
            AppDrawerContent(
                currentRoute = currentRoute,
                userDisplayName = drawerDisplayName,
                userEmail = drawerEmail,
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
                            val uid = UserSession.userId
                            if (uid != null) {
                                scope.launch {
                                    elachiApp.recipeRepository.createBook(
                                        userId = uid,
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
                    val uid = UserSession.userId
                    if (uid != null) {
                        val vm: HomeViewModel = viewModel(
                            factory = SimpleViewModelFactory {
                                HomeViewModel(
                                    uid,
                                    elachiApp.recipeRepository,
                                    elachiApp.pantryRepository,
                                    elachiApp.database.achievementDao(),
                                    elachiApp.profileRepository,
                                )
                            },
                        )
                        HomeScreen(
                            viewModel = vm,
                            onOpenRecipe = { id -> navController.navigate(Screen.RecipeDetail.createRoute(id)) },
                            onOpenAiChef = { navController.navigate(Screen.AiChef.route) },
                            onOpenAchievements = { navController.navigate(Screen.Achievements.route) },
                            onOpenSettings = { navController.navigate(Screen.Settings.route) },
                            onOpenDrawer = { scope.launch { drawerState.open() } },
                        )
                    } else {
                        Placeholder("Please sign in again") {
                            navController.navigate(Screen.Login.route) { popUpTo(0) }
                        }
                    }
                }

                // ---------- Cookbook ----------
                composable(Screen.Cookbook.route) {
                    val uid = UserSession.userId
                    if (uid != null) {
                        val vm: CookbookViewModel = viewModel(
                            factory = SimpleViewModelFactory {
                                CookbookViewModel(uid, elachiApp.recipeRepository)
                            },
                        )
                        ScreenShell(
                            title = "My Cookbook",
                            route = currentRoute,
                            navController = navController,
                            drawerState = drawerState,
                        ) {
                            CookbookScreen(
                                viewModel = vm,
                                onOpenBook = { bookId ->
                                    navController.navigate(Screen.RecipeBookDetail.createRoute(bookId))
                                },
                                onOpenRecipe = { recipeId ->
                                    navController.navigate(Screen.RecipeDetail.createRoute(recipeId))
                                },
                                onAddRecipe = { bookId ->
                                    navController.navigate(Screen.AddRecipe.createRoute(bookId))
                                },
                            )
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

                composable(Screen.RecipeBookDetail.route) { backStackEntry ->
                    val bookId = backStackEntry.arguments?.getString("bookId").orEmpty()
                    val vm: RecipeBookDetailViewModel = viewModel(
                        factory = SimpleViewModelFactory {
                            RecipeBookDetailViewModel(
                                bookId = bookId,
                                recipeRepository = elachiApp.recipeRepository,
                            )
                        },
                    )

                    RecipeBookDetailScreen(
                        viewModel = vm,
                        onBack = { navController.popBackStack() },
                        onAddRecipe = {
                            navController.navigate(Screen.AddRecipe.createRoute(bookId))
                        },
                        onOpenRecipe = { recipeId ->
                            navController.navigate(Screen.RecipeDetail.createRoute(recipeId))
                        },
                    )
                }

                // ---------- Add Recipe ----------
                composable(Screen.AddRecipe.route) { backStackEntry ->
                    val uid = UserSession.userId
                    val bookId = backStackEntry.arguments?.getString("bookId").orEmpty()

                    if (uid != null) {
                        val vm: AddRecipeViewModel = viewModel(
                            factory = SimpleViewModelFactory {
                                AddRecipeViewModel(
                                    userId = uid,
                                    initialBookId = bookId,
                                    recipeRepository = elachiApp.recipeRepository,
                                )
                            },
                        )

                        AddRecipeScreen(
                            viewModel = vm,
                            onClose = { navController.popBackStack() },
                            onOpenCamera = {
                                navController.navigate(Screen.CameraCapture.createRoute(bookId))
                            },
                            onSaved = { recipeId ->
                                navController.navigate(Screen.RecipeDetail.createRoute(recipeId)) {
                                    popUpTo(backStackEntry.destination.id) { inclusive = true }
                                }
                            },
                        )
                    } else {
                        Placeholder("Please sign in again") {
                            navController.navigate(Screen.Login.route) { popUpTo(0) }
                        }
                    }
                }

                // ---------- Edit Recipe ----------
                composable(Screen.EditRecipe.route) { backStackEntry ->
                    val uid = UserSession.userId
                    val recipeId = backStackEntry.arguments
                        ?.getString("recipeId")
                        .orEmpty()

                    if (uid != null) {
                        val vm: AddRecipeViewModel = viewModel(
                            factory = SimpleViewModelFactory {
                                AddRecipeViewModel(
                                    userId = uid,
                                    initialBookId = "",
                                    recipeRepository = elachiApp.recipeRepository,
                                    editingRecipeId = recipeId,
                                )
                            },
                        )

                        AddRecipeScreen(
                            viewModel = vm,
                            onClose = { navController.popBackStack() },
                            onOpenCamera = {
                                navController.navigate(
                                    Screen.CameraCapture.createRoute(
                                        vm.selectedBookId.value,
                                    ),
                                )
                            },
                            onSaved = {
                                navController.popBackStack()
                            },
                        )
                    } else {
                        Placeholder("Please sign in again") {
                            navController.navigate(Screen.Login.route) {
                                popUpTo(0)
                            }
                        }
                    }
                }

                // ---------- Camera and Screenshot OCR ----------
                composable(Screen.CameraCapture.route) { backStackEntry ->
                    val uid = UserSession.userId
                    val bookId = backStackEntry.arguments?.getString("bookId").orEmpty()
                    val addRecipeEntry = remember(backStackEntry) {
                        navController.previousBackStackEntry
                    }

                    if (uid != null && addRecipeEntry != null) {
                        val addRecipeVm: AddRecipeViewModel = viewModel(
                            viewModelStoreOwner = addRecipeEntry,
                            factory = SimpleViewModelFactory {
                                AddRecipeViewModel(
                                    userId = uid,
                                    initialBookId = bookId,
                                    recipeRepository = elachiApp.recipeRepository,
                                )
                            },
                        )
                        val cameraVm: CameraCaptureViewModel = viewModel(
                            factory = SimpleViewModelFactory {
                                CameraCaptureViewModel(elachiApp.recipeRepository)
                            },
                        )

                        CameraCaptureScreen(
                            viewModel = cameraVm,
                            onTextRecognized = { title, ingredients, steps, method, servings, cookTime ->
                                addRecipeVm.prefillFromParsedRecipe(
                                    parsedTitle = title,
                                    parsedIngredients = ingredients,
                                    parsedSteps = steps,
                                    parsedMethod = method,
                                    parsedServings = servings,
                                    parsedCookTimeMinutes = cookTime,
                                )
                                navController.popBackStack()
                            },
                            onCancel = { navController.popBackStack() },
                        )
                    } else {
                        Placeholder("Unable to open recipe capture") {
                            navController.popBackStack()
                        }
                    }
                }

                // ---------- Recipe Detail ----------
                composable(Screen.RecipeDetail.route) { backStackEntry ->
                    val uid = UserSession.userId
                    val recipeId = backStackEntry.arguments?.getString("recipeId").orEmpty()

                    if (uid != null) {
                        val vm: RecipeDetailViewModel = viewModel(
                            factory = SimpleViewModelFactory {
                                RecipeDetailViewModel(
                                    userId = uid,
                                    recipeId = recipeId,
                                    recipeRepository = elachiApp.recipeRepository,
                                    pantryRepository = elachiApp.pantryRepository,
                                    achievementRepository = elachiApp.achievementRepository,
                                    api = elachiApp.api,
                                )
                            },
                        )

                        RecipeDetailScreen(
                            viewModel = vm,
                            onBack = { navController.popBackStack() },
                            onEdit = {
                                navController.navigate(
                                    Screen.EditRecipe.createRoute(recipeId),
                                )
                            },
                            onDeleted = {
                                navController.popBackStack()
                            },
                            onStartCookMode = {
                                navController.navigate(Screen.CookMode.createRoute(recipeId))
                            },
                        )
                    } else {
                        Placeholder("Please sign in again") {
                            navController.navigate(Screen.Login.route) { popUpTo(0) }
                        }
                    }
                }

                // ---------- Cook Mode ----------
                composable(Screen.CookMode.route) { backStackEntry ->
                    val uid = UserSession.userId
                    val recipeId = backStackEntry.arguments?.getString("recipeId").orEmpty()

                    if (uid != null) {
                        val vm: RecipeDetailViewModel = viewModel(
                            factory = SimpleViewModelFactory {
                                RecipeDetailViewModel(
                                    userId = uid,
                                    recipeId = recipeId,
                                    recipeRepository = elachiApp.recipeRepository,
                                    pantryRepository = elachiApp.pantryRepository,
                                    achievementRepository = elachiApp.achievementRepository,
                                    api = elachiApp.api,
                                )
                            },
                        )
                        val steps by vm.steps.collectAsState()

                        CookModeScreen(
                            steps = steps.map { it.instruction },
                            onExit = { navController.popBackStack() },
                            onFinish = {
                                vm.onCookModeFinished { unlocked ->
                                    val message = if (unlocked.isEmpty()) {
                                        "Cooking session completed!"
                                    } else {
                                        "Achievement unlocked: ${unlocked.joinToString()}"
                                    }
                                    android.widget.Toast.makeText(
                                        context,
                                        message,
                                        android.widget.Toast.LENGTH_LONG,
                                    ).show()
                                    navController.popBackStack()
                                }
                            },
                        )
                    } else {
                        Placeholder("Please sign in again") {
                            navController.navigate(Screen.Login.route) { popUpTo(0) }
                        }
                    }
                }

                // ---------- Discover ----------
                composable(Screen.Discover.route) {
                    val vm: DiscoverViewModel = viewModel(
                        factory = SimpleViewModelFactory { DiscoverViewModel(elachiApp.api) },
                    )
                    ScreenShell(
                        title = "Discover",
                        route = currentRoute,
                        navController = navController,
                        drawerState = drawerState,
                    ) {
                        DiscoverScreen(
                            viewModel = vm,
                            onOpenRecipe = { recipeId ->
                                navController.navigate(Screen.RecipeDetail.createRoute(recipeId))
                            },
                        )
                    }
                }

                // ---------- Pantry ----------
                composable(Screen.Pantry.route) {
                    val uid = UserSession.userId
                    if (uid != null) {
                        val vm: PantryViewModel = viewModel(
                            factory = SimpleViewModelFactory {
                                PantryViewModel(
                                    userId = uid,
                                    pantryRepository = elachiApp.pantryRepository,
                                    achievementRepository = elachiApp.achievementRepository,
                                )
                            },
                        )
                        ScreenShell(
                            title = "Pantry & Tools",
                            route = currentRoute,
                            navController = navController,
                            drawerState = drawerState,
                        ) { PantryScreen(viewModel = vm) }
                    } else {
                        ScreenShell(
                            title = "Pantry & Tools",
                            route = currentRoute,
                            navController = navController,
                            drawerState = drawerState,
                        ) {
                            Placeholder("Please sign in again") {
                                navController.navigate(Screen.Login.route) { popUpTo(0) }
                            }
                        }
                    }
                }

                // ---------- Profile ----------
                composable(Screen.Profile.route) {
                    val uid = UserSession.userId
                    if (uid != null) {
                        val vm: ProfileViewModel = viewModel(
                            factory = SimpleViewModelFactory {
                                ProfileViewModel(
                                    uid,
                                    elachiApp.profileRepository,
                                    elachiApp.recipeRepository,
                                    elachiApp.database.achievementDao(),
                                )
                            },
                        )
                        ScreenShell(
                            title = "My Profile",
                            route = currentRoute,
                            navController = navController,
                            drawerState = drawerState,
                        ) {
                            ProfileScreen(
                                viewModel = vm,
                                onEdit = { navController.navigate(Screen.EditProfile.route) },
                            )
                        }
                    } else {
                        ScreenShell(
                            title = "My Profile",
                            route = currentRoute,
                            navController = navController,
                            drawerState = drawerState,
                        ) {
                            Placeholder("Please sign in again") {
                                navController.navigate(Screen.Login.route) { popUpTo(0) }
                            }
                        }
                    }
                }

                // ---------- Edit Profile ----------
                composable(Screen.EditProfile.route) {
                    val vm: CompleteProfileViewModel = viewModel(
                        factory = SimpleViewModelFactory {
                            CompleteProfileViewModel(elachiApp.profileRepository)
                        },
                    )
                    CompleteProfileScreen(
                        viewModel = vm,
                        isEditMode = true,
                        onDone = { navController.popBackStack() },
                    )
                }

                // ---------- Settings ----------
                composable(Screen.Settings.route) {
                    val vm: SettingsViewModel = viewModel(
                        factory = SimpleViewModelFactory {
                            SettingsViewModel(
                                elachiApp.settingsRepository,
                                elachiApp.authRepository,
                            )
                        },
                    )
                    SettingsScreen(
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
                        onNavigateToEditProfile = {
                            navController.navigate(Screen.EditProfile.route)
                        },
                    )
                }

                // ---------- Privacy Policy ----------
                composable(Screen.PrivacyPolicy.route) {
                    PrivacyPolicyScreen(
                        onBack = { navController.popBackStack() },
                    )
                }

                // ---------- Terms of Service ----------
                composable(Screen.TermsOfService.route) {
                    TermsOfServiceScreen(
                        onBack = { navController.popBackStack() },
                    )
                }

                // ---------- AI Chef ----------
                composable(Screen.AiChef.route) {
                    val vm: AiChefViewModel = viewModel(
                        factory = SimpleViewModelFactory { AiChefViewModel(elachiApp.chatRepository) },
                    )
                    AiChefScreen(
                        viewModel = vm,
                        onBack = { navController.popBackStack() },
                    )
                }

                // ---------- Achievements ----------
                composable(Screen.Achievements.route) {
                    val uid = UserSession.userId
                    if (uid != null) {
                        val vm: AchievementsViewModel = viewModel(
                            factory = SimpleViewModelFactory {
                                AchievementsViewModel(
                                    userId = uid,
                                    achievementDao = elachiApp.database.achievementDao(),
                                )
                            },
                        )
                        AchievementsScreen(
                            viewModel = vm,
                            onBack = { navController.popBackStack() },
                        )
                    } else {
                        Placeholder("Please sign in again") {
                            navController.navigate(Screen.Login.route) { popUpTo(0) }
                        }
                    }
                }

                // ---------- Streak Calendar ----------
                composable(Screen.StreakCalendar.route) {
                    val uid = UserSession.userId
                    if (uid != null) {
                        val vm: StreakCalendarViewModel = viewModel(
                            factory = SimpleViewModelFactory {
                                StreakCalendarViewModel(
                                    userId = uid,
                                    achievementRepository = elachiApp.achievementRepository,
                                    achievementDao = elachiApp.database.achievementDao(),
                                )
                            },
                        )
                        StreakCalendarScreen(
                            viewModel = vm,
                            onBack = { navController.popBackStack() },
                        )
                    } else {
                        Placeholder("Please sign in again") {
                            navController.navigate(Screen.Login.route) { popUpTo(0) }
                        }
                    }
                }

                // ---------- Help ----------
                composable(Screen.Help.route) {
                    HelpScreen(
                        onBack = { navController.popBackStack() },
                        onNavigateToPrivacyPolicy = {
                            navController.navigate(Screen.PrivacyPolicy.route)
                        },
                        onNavigateToTerms = {
                            navController.navigate(Screen.TermsOfService.route)
                        },
                        onOpenGettingStarted = {
                            navController.navigate("onboarding_tutorial")
                        },
                    )
                }

                // Tutorial-only onboarding — returns to Help when finished, does not
                // continue into Complete Profile / Create First Book.
                composable("onboarding_tutorial") {
                    OnboardingScreen(
                        tutorialMode = true,
                        onFinish = { navController.popBackStack() },
                    )
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
