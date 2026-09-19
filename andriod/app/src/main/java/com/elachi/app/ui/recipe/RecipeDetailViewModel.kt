package com.elachi.app.ui.recipe

import android.util.Log
import androidx.compose.runtime.mutableStateOf
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

    val isDeleting = mutableStateOf(false)
    val deleteError = mutableStateOf<String?>(null)

    val recipe: StateFlow<RecipeEntity?> =
        recipeRepository.observeRecipe(recipeId)
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5_000),
                null,
            )

    val availableBooks: StateFlow<List<RecipeBookEntity>> =
        recipeRepository.observeBooks(userId)
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5_000),
                emptyList(),
            )

    val ingredients: StateFlow<List<IngredientEntity>> =
        recipeRepository.observeIngredients(recipeId)
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5_000),
                emptyList(),
            )

    val steps: StateFlow<List<StepEntity>> =
        recipeRepository.observeSteps(recipeId)
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5_000),
                emptyList(),
            )

    fun toggleFavourite(current: Boolean) {
        viewModelScope.launch {
            recipeRepository.toggleFavourite(recipeId, !current)
        }
    }

    fun addMissingIngredientsToShoppingList() {
        viewModelScope.launch {
            pantryRepository.generateShoppingListFromRecipe(
                userId,
                recipeId,
            )
        }
    }

    fun deleteRecipe(onDeleted: () -> Unit) {
        if (isDeleting.value) return

        viewModelScope.launch {
            isDeleting.value = true
            deleteError.value = null

            try {
                recipeRepository.deleteRecipe(recipeId)
                onDeleted()
            } catch (exception: Exception) {
                Log.e("RecipeDetailVM", "deleteRecipe failed", exception)
                deleteError.value = "The recipe could not be deleted. Please try again."
            } finally {
                isDeleting.value = false
            }
        }
    }

    fun onCookModeFinished(
        onUnlocked: (List<String>) -> Unit,
    ) {
        viewModelScope.launch {
            recipeRepository.markCooked(recipeId)

            val unlocked =
                achievementRepository.onRecipeCooked(
                    userId,
                    recipeId,
                )

            try {
                api.logCookSession(
                    CookSessionRequest(
                        recipeId,
                        java.time.Instant.now().toString(),
                    ),
                )
            } catch (exception: Exception) {
                Log.e(
                    "RecipeDetailVM",
                    "logCookSession failed",
                    exception,
                )
            }

            onUnlocked(unlocked)
        }
    }
}
