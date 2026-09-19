package com.elachi.app.ui.recipe

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elachi.app.data.local.entities.IngredientEntity
import com.elachi.app.data.local.entities.RecipeBookEntity
import com.elachi.app.data.local.entities.RecipeEntity
import com.elachi.app.data.local.entities.StepEntity
import com.elachi.app.data.remote.ApiService
import com.elachi.app.data.remote.dto.CookSessionRequest
import com.elachi.app.data.repository.AchievementRepository
import com.elachi.app.data.repository.PantryRepository
import com.elachi.app.data.repository.RecipeRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class RecipeDetailViewModel(
    private val userId: String,
    private val recipeId: String,
    private val recipeRepository: RecipeRepository,
    private val pantryRepository: PantryRepository,
    private val achievementRepository: AchievementRepository,
    private val api: ApiService,
) : ViewModel() {

    val recipe: StateFlow<RecipeEntity?> =
        recipeRepository.observeRecipe(recipeId)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = null,
            )

    val availableBooks: StateFlow<List<RecipeBookEntity>> =
        recipeRepository.observeBooks(userId)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyList(),
            )

    val ingredients: StateFlow<List<IngredientEntity>> =
        recipeRepository.observeIngredients(recipeId)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyList(),
            )

    val steps: StateFlow<List<StepEntity>> =
        recipeRepository.observeSteps(recipeId)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyList(),
            )

    fun toggleFavourite(currentFavouriteValue: Boolean) {
        viewModelScope.launch {
            recipeRepository.toggleFavourite(
                recipeId,
                !currentFavouriteValue,
            )
        }
    }

    fun addMissingIngredientsToShoppingList() {
        viewModelScope.launch {
            pantryRepository.generateShoppingListFromRecipe(
                userId = userId,
                recipeId = recipeId,
            )
        }
    }

    fun onCookModeFinished(
        onUnlocked: (List<String>) -> Unit,
    ) {
        viewModelScope.launch {
            recipeRepository.markCooked(recipeId)

            val unlockedAchievements =
                achievementRepository.onRecipeCooked(
                    userId = userId,
                    recipeId = recipeId,
                )

            try {
                api.logCookSession(
                    CookSessionRequest(
                        recipeId = recipeId,
                        completedAt =
                            java.time.Instant.now().toString(),
                    ),
                )
            } catch (exception: Exception) {
                Log.e(
                    "RecipeDetailVM",
                    "Logging cook session failed",
                    exception,
                )
            }

            onUnlocked(unlockedAchievements)
        }
    }
}