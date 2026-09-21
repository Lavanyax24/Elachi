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
import com.elachi.app.data.repository.MissingIngredient
import com.elachi.app.data.repository.PantryRepository
import com.elachi.app.data.repository.RecipeRepository
import kotlinx.coroutines.NonCancellable
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

    val isLoadingRemote = mutableStateOf(false)
    val remoteRecipe = mutableStateOf<com.elachi.app.data.remote.dto.RecipeDto?>(null)

    init {
        // If the recipe is not found locally, try to fetch it from the API
        viewModelScope.launch {
            val local = recipeRepository.getRecipeOnce(recipeId)
            if (local == null) {
                isLoadingRemote.value = true
                try {
                    val response = api.getRecipe(recipeId)
                    if (response.isSuccessful) {
                        remoteRecipe.value = response.body()
                    }
                } catch (e: Exception) {
                    Log.e("RecipeDetailVM", "Failed to fetch remote recipe", e)
                } finally {
                    isLoadingRemote.value = false
                }
            }
        }
    }
    val availableBooks: StateFlow<List<com.elachi.app.data.local.entities.RecipeBookEntity>> =
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

    fun toggleVisibility(current: Boolean) {
        viewModelScope.launch {
            recipeRepository.updateRecipeVisibility(recipeId, !current)
        }
    }

    /** null = dialog closed, empty list = user already has everything. */
    val missingIngredients = mutableStateOf<List<MissingIngredient>?>(null)
    val shoppingMessage = mutableStateOf<String?>(null)

    fun loadMissingIngredients(baseServings: Int, currentServings: Int) {
        viewModelScope.launch {
            missingIngredients.value = try {
                pantryRepository.getMissingIngredients(userId, recipeId, baseServings, currentServings)
            } catch (exception: Exception) {
                Log.e("RecipeDetailVM", "loadMissingIngredients failed", exception)
                shoppingMessage.value = "Couldn't check your pantry. Please try again."
                null
            }
        }
    }

    fun dismissMissingIngredients() {
        missingIngredients.value = null
    }

    fun addSelectedToShoppingList(selected: List<MissingIngredient>) {
        viewModelScope.launch {
            try {
                pantryRepository.addShoppingItems(userId, selected)
                shoppingMessage.value =
                    if (selected.size == 1) "Added 1 item to your shopping list"
                    else "Added ${selected.size} items to your shopping list"
            } catch (exception: Exception) {
                Log.e("RecipeDetailVM", "addSelectedToShoppingList failed", exception)
                shoppingMessage.value = "Couldn't add to your shopping list. Please try again."
            }
            missingIngredients.value = null
        }
    }

    fun clearShoppingMessage() {
        shoppingMessage.value = null
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
            // Local work only (fast), so the screen can close straight away.
            recipeRepository.markCooked(recipeId)

            val unlocked =
                achievementRepository.onRecipeCooked(
                    userId,
                    recipeId,
                )

            // Sync with the server in the background. NonCancellable is needed
            // because leaving the screen clears this ViewModel, which would
            // otherwise cancel the request before it is sent.
            val completedAt = java.time.Instant.now().toString()
            viewModelScope.launch(NonCancellable) {
                try {
                    api.logCookSession(CookSessionRequest(recipeId, completedAt))
                } catch (exception: Exception) {
                    Log.e("RecipeDetailVM", "logCookSession failed", exception)
                }
            }

            onUnlocked(unlocked)
        }
    }
}