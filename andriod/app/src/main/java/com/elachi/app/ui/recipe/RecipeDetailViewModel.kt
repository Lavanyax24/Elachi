package com.elachi.app.ui.recipe

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elachi.app.data.local.entities.IngredientEntity
import com.elachi.app.data.local.entities.RecipeEntity
import com.elachi.app.data.local.entities.StepEntity
import com.elachi.app.data.remote.ApiService
import com.elachi.app.data.remote.dto.CommentDto
import com.elachi.app.data.remote.dto.PostCommentRequest
import com.elachi.app.data.repository.AchievementRepository
import com.elachi.app.data.repository.PantryRepository
import com.elachi.app.data.repository.RecipeRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import com.elachi.app.data.remote.dto.CookSessionRequest

class RecipeDetailViewModel(
    private val userId: String,
    private val recipeId: String,
    private val recipeRepository: RecipeRepository,
    private val pantryRepository: PantryRepository,
    private val achievementRepository: AchievementRepository,
    private val api: ApiService,
) : ViewModel() {

    val recipe: StateFlow<RecipeEntity?> = recipeRepository.observeRecipe(recipeId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val availableBooks: StateFlow<List<com.elachi.app.data.local.entities.RecipeBookEntity>> = recipeRepository.observeBooks(userId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val ingredients: StateFlow<List<IngredientEntity>> = recipeRepository.observeIngredients(recipeId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val steps: StateFlow<List<StepEntity>> = recipeRepository.observeSteps(recipeId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    var comments = mutableStateOf<List<CommentDto>>(emptyList())


    fun toggleFavourite(current: Boolean) = viewModelScope.launch {
        recipeRepository.toggleFavourite(recipeId, !current)
    }

    fun addMissingIngredientsToShoppingList() = viewModelScope.launch {
        pantryRepository.generateShoppingListFromRecipe(userId, recipeId)
    }

    /** Called once Cook Mode's final step is completed (FR-5.4, FR-9.2). */
    fun onCookModeFinished(onUnlocked: (List<String>) -> Unit) = viewModelScope.launch {
        recipeRepository.markCooked(recipeId)
        val unlocked = achievementRepository.onRecipeCooked(userId, recipeId)
        try {
            api.logCookSession(CookSessionRequest(recipeId, java.time.Instant.now().toString()))
        } catch (e: Exception) {
            android.util.Log.e("RecipeDetailVM", "logCookSession failed", e)
        }
        onUnlocked(unlocked)
    }

    fun loadComments() = viewModelScope.launch {
        try {
            val response = api.getComments(recipeId)
            if (response.isSuccessful) comments.value = response.body().orEmpty()
        } catch (e: Exception) {
            // Comments are a nice-to-have on this screen — fail quietly rather
            // than blocking the rest of Recipe Detail from rendering.
        }
    }

    fun postComment(rating: Int?, text: String?, onPosted: () -> Unit) = viewModelScope.launch {
        try {
            val response = api.postComment(recipeId, PostCommentRequest(rating, text))
            if (response.isSuccessful) {
                loadComments()
                onPosted()
            }
        } catch (e: Exception) {
            // Same reasoning as loadComments — the user can just retry the Post button.
        }
    }
}