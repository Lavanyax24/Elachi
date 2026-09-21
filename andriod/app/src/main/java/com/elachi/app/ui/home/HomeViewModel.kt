package com.elachi.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elachi.app.data.local.dao.AchievementDao
import com.elachi.app.data.local.entities.RecipeEntity
import com.elachi.app.data.local.entities.StreakRecordEntity
import com.elachi.app.data.repository.PantryRepository
import com.elachi.app.data.repository.ProfileRepository
import com.elachi.app.data.repository.RecipeMatch
import com.elachi.app.data.repository.RecipeRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

// This class handles the data for the home screen

class HomeViewModel(
    private val userId: String,
    private val recipeRepository: RecipeRepository,
    private val pantryRepository: PantryRepository,
    private val achievementDao: AchievementDao,
    private val profileRepository: ProfileRepository,
) : ViewModel() {

    private val _matches = MutableStateFlow<List<RecipeMatch>>(emptyList())
    val matches: StateFlow<List<RecipeMatch>> = _matches.asStateFlow()

    val streak: StateFlow<StreakRecordEntity?> = achievementDao.observeStreak(userId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val allRecipes: StateFlow<List<RecipeEntity>> = recipeRepository.observeAllRecipes(userId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // ---------- Interests ----------
    private val _interests = MutableStateFlow<List<String>>(emptyList())
    val interests: StateFlow<List<String>> = _interests.asStateFlow()

    val interestRecipes: StateFlow<List<RecipeEntity>> =
        combine(_interests, allRecipes) { interests, recipes ->
            if (interests.isEmpty() || recipes.isEmpty()) {
                emptyList()
            } else {
                val lowerInterests = interests.map { it.lowercase() }
                recipes.filter { recipe ->
                    val haystack = buildString {
                        append(recipe.title.lowercase()).append(' ')
                        append(recipe.category.lowercase()).append(' ')
                        append(recipe.cuisine.lowercase()).append(' ')
                        append(recipe.foodType.lowercase())
                    }
                    lowerInterests.any { interest -> haystack.contains(interest) }
                }.take(10)
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pantryHealthPercent: StateFlow<Int> = allRecipes
        .map { recipes ->
            if (recipes.isEmpty()) return@map 0
            val allMatches = pantryRepository.getPantryMatches(userId, recipes)
            if (allMatches.isEmpty()) 0 else allMatches.map { it.matchPercent }.average().toInt()
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    init {
        viewModelScope.launch {
            recipeRepository.observeAllRecipes(userId).collect { recipes ->
                _matches.value = pantryRepository.getPantryMatches(userId, recipes).take(5)
            }
        }

        viewModelScope.launch {
            profileRepository.getMyProfile()
                .onSuccess { profile ->
                    _interests.value = profile.cookingInterests.orEmpty()
                }
                .onFailure {
                    android.util.Log.e("HomeViewModel", "Failed to load interests", it)
                }
        }
    }
}