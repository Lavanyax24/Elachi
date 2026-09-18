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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ElachiApp : Application() {

    lateinit var database: AppDatabase
        private set
    lateinit var authRepository: AuthRepository
        private set
    lateinit var recipeRepository: RecipeRepository
        private set
    lateinit var pantryRepository: PantryRepository
        private set
    lateinit var chatRepository: ChatRepository
        private set
    lateinit var achievementRepository: AchievementRepository
        private set
    lateinit var profileRepository: ProfileRepository
        private set
    lateinit var api: ApiService
        private set

    override fun onCreate() {
        super.onCreate()

        database = AppDatabase.getInstance(this)
        api = RetrofitClient.apiService

        authRepository = AuthRepository()
        recipeRepository = RecipeRepository(api, database.recipeBookDao(), database.recipeDao())
        pantryRepository = PantryRepository(api, database.pantryDao(), database.recipeDao())
        chatRepository = ChatRepository(api)
        achievementRepository = AchievementRepository(database.achievementDao(), database.pantryDao(), database.recipeDao())
        profileRepository = ProfileRepository(api)


        CoroutineScope(Dispatchers.IO).launch {
            achievementRepository.seedDefinitionsIfNeeded()
        }
    }
}