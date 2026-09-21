package com.elachi.app

import android.app.Application
import com.elachi.app.data.local.AppDatabase
import com.elachi.app.data.remote.RetrofitClient
import com.elachi.app.data.remote.ApiService
import com.elachi.app.data.repository.AchievementRepository
import com.elachi.app.data.repository.AuthRepository
import com.elachi.app.data.repository.ChatRepository
import com.elachi.app.data.repository.PantryRepository
import com.elachi.app.data.repository.ProfileRepository
import com.elachi.app.data.repository.RecipeRepository
import com.elachi.app.ui.settings.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ElachiApp : Application() {
    private val TAG = "ElachiApp"

    val database: AppDatabase by lazy { AppDatabase.getInstance(this) }
    val api: ApiService by lazy { RetrofitClient.apiService }

    val authRepository: AuthRepository by lazy { AuthRepository() }
    val recipeRepository: RecipeRepository by lazy { RecipeRepository(api, database.recipeBookDao(), database.recipeDao()) }
    val pantryRepository: PantryRepository by lazy { PantryRepository(api, database.pantryDao(), database.recipeDao()) }
    val chatRepository: ChatRepository by lazy { ChatRepository(api) }
    val achievementRepository: AchievementRepository by lazy { AchievementRepository(database.achievementDao(), database.pantryDao(), database.recipeDao()) }
    val profileRepository: ProfileRepository by lazy { ProfileRepository(api) }
    val settingsRepository: SettingsRepository by lazy { SettingsRepository(this) }

    override fun onCreate() {
        super.onCreate()
        android.util.Log.d(TAG, "Application onCreate called")

        CoroutineScope(Dispatchers.IO).launch {
            try {
                achievementRepository.seedDefinitionsIfNeeded()
            } catch (e: Exception) {
                android.util.Log.e("ElachiApp", "Failed to seed achievements", e)
            }
        }
    }
}