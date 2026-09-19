package com.elachi.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elachi.app.data.local.dao.AchievementDao
import com.elachi.app.data.local.entities.RecipeEntity
import com.elachi.app.data.local.entities.StreakRecordEntity
import com.elachi.app.data.repository.PantryRepository
import com.elachi.app.data.repository.RecipeMatch
import com.elachi.app.data.repository.RecipeRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class HomeViewModel(
    private val userId: String,
    private val recipeRepository: RecipeRepository,
    private val pantryRepository: PantryRepository,
    private val achievementDao: AchievementDao,
) : ViewModel() {

    private val _matches = MutableStateFlow<List<RecipeMatch>>(emptyList())
    val matches: StateFlow<List<RecipeMatch>> = _matches.asStateFlow()

    val streak: StateFlow<StreakRecordEntity?> = achievementDao.observeStreak(userId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val allRecipes: StateFlow<List<RecipeEntity>> = recipeRepository.observeAllRecipes(userId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())


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
    }
}