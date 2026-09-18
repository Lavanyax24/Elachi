package com.elachi.app.navigation

sealed class Screen(val route: String) {
    data object Splash : Screen("splash")
    data object Onboarding : Screen("onboarding")
    data object Login : Screen("login")
    data object SignUp : Screen("signup")

    // Post-registration flow — shown only after a NEW sign-up
    data object CompleteProfile : Screen("complete_profile")
    data object CreateFirstBook : Screen("create_first_book")

    // Main app
    data object Home : Screen("home")
    data object Cookbook : Screen("cookbook")
    data object RecipeBookDetail : Screen("recipe_book/{bookId}") {
        fun createRoute(bookId: String) = "recipe_book/$bookId"
    }
    data object AddRecipe : Screen("add_recipe/{bookId}") {
        fun createRoute(bookId: String) = "add_recipe/$bookId"
    }
    data object CameraCapture : Screen("camera_capture/{bookId}") {
        fun createRoute(bookId: String) = "camera_capture/$bookId"
    }
    data object RecipeDetail : Screen("recipe_detail/{recipeId}") {
        fun createRoute(recipeId: String) = "recipe_detail/$recipeId"
    }
    data object CookMode : Screen("cook_mode/{recipeId}") {
        fun createRoute(recipeId: String) = "cook_mode/$recipeId"
    }
    data object Pantry : Screen("pantry")
    data object AiChef : Screen("ai_chef")
    data object Discover : Screen("discover")
    data object Achievements : Screen("achievements")
    data object StreakCalendar : Screen("streak_calendar")
    data object Profile : Screen("profile")
    data object EditProfile : Screen("edit_profile")
    data object Settings : Screen("settings")
    data object Help : Screen("help")
    data object PrivacyPolicy : Screen("privacy_policy")
    data object TermsOfService : Screen("terms_of_service")
    data object Tools : Screen("tools")
    data object Calculator : Screen("calculator")
}
