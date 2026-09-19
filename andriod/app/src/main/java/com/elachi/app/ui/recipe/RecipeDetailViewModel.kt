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
import com.elachi.app.data.remote.dto.CommentDto
import com.elachi.app.data.remote.dto.CookSessionRequest
import com.elachi.app.data.remote.dto.PostCommentRequest
import com.elachi.app.data.repository.AchievementRepository
import com.elachi.app.data.repository.PantryRepository
import com.elachi.app.data.repository.RecipeRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.json.JSONObject

data class ReviewUiState(
    val isLoading: Boolean = false,
    val isPosting: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
)

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

    val comments = mutableStateOf<List<CommentDto>>(emptyList())

    val reviewUiState = mutableStateOf(ReviewUiState())

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

    fun loadComments() {
        viewModelScope.launch {
            loadCommentsFromServer(showLoading = true)
        }
    }

    private suspend fun loadCommentsFromServer(
        showLoading: Boolean,
    ) {
        if (showLoading) {
            reviewUiState.value = reviewUiState.value.copy(
                isLoading = true,
                errorMessage = null,
            )
        }

        try {
            val response = api.getComments(recipeId)

            if (response.isSuccessful) {
                comments.value = response.body().orEmpty()

                reviewUiState.value = reviewUiState.value.copy(
                    isLoading = false,
                    errorMessage = null,
                )
            } else {
                val errorJson = response.errorBody()?.string()
                Log.e("RecipeDetailVM", "Get comments failed with HTTP ${response.code()}: $errorJson")
                reviewUiState.value = reviewUiState.value.copy(
                    isLoading = false,
                    errorMessage = reviewErrorMessage(
                        response.code(),
                        errorJson,
                    ),
                )
            }
        } catch (exception: Exception) {
            Log.e(
                "RecipeDetailVM",
                "Loading reviews failed",
                exception,
            )

            reviewUiState.value = reviewUiState.value.copy(
                isLoading = false,
                errorMessage =
                    "Reviews could not be loaded. Check your internet connection.",
            )
        }
    }

    fun clearReviewMessage() {
        reviewUiState.value = reviewUiState.value.copy(
            errorMessage = null,
            successMessage = null,
        )
    }

    fun postComment(
        rating: Int?,
        text: String?,
        onPosted: () -> Unit,
    ) {
        if (rating == null || rating !in 1..5) {
            reviewUiState.value = reviewUiState.value.copy(
                errorMessage = "Please select a rating from 1 to 5 stars.",
                successMessage = null,
            )
            return
        }

        val cleanedText = text?.trim()

        if (cleanedText.isNullOrBlank()) {
            reviewUiState.value = reviewUiState.value.copy(
                errorMessage = "Please enter a short review.",
                successMessage = null,
            )
            return
        }

        viewModelScope.launch {
            reviewUiState.value = ReviewUiState(
                isPosting = true,
            )

            try {
                val request = PostCommentRequest(
                    rating = rating,
                    text = cleanedText,
                )

                var response = api.postComment(
                    recipeId,
                    request,
                )

                /*
                 * A newly added recipe may exist locally but not remotely.
                 * If the review endpoint returns 404, synchronize the recipe
                 * and retry the review once.
                 */
                if (response.code() == 404) {
                    val syncResult =
                        recipeRepository.syncRecipeToBackend(
                            recipeId = recipeId,
                            currentUserId = userId,
                        )

                    if (syncResult.isSuccess) {
                        response = api.postComment(
                            recipeId,
                            request,
                        )
                    } else {
                        Log.e("RecipeDetailVM", "Recipe sync failed: ${syncResult.exceptionOrNull()?.message}")
                    }
                }

                if (response.isSuccessful) {
                    val postedReview = response.body()

                    if (postedReview != null) {
                        comments.value =
                            listOf(postedReview) + comments.value
                    } else {
                        loadCommentsFromServer(showLoading = false)
                    }

                    reviewUiState.value = ReviewUiState(
                        successMessage =
                        "Your review was posted successfully.",
                    )

                    onPosted()
                } else {
                    val errorJson = response.errorBody()?.string()
                    Log.e("RecipeDetailVM", "Post comment failed with HTTP ${response.code()}: $errorJson")
                    reviewUiState.value = ReviewUiState(
                        errorMessage = reviewErrorMessage(
                            response.code(),
                            errorJson,
                        ),
                    )
                }
            } catch (exception: Exception) {
                Log.e(
                    "RecipeDetailVM",
                    "Posting review failed",
                    exception,
                )

                reviewUiState.value = ReviewUiState(
                    errorMessage =
                        "Your review could not be posted. " +
                                "Check your internet connection and try again.",
                )
            }
        }
    }

    private fun reviewErrorMessage(
        statusCode: Int,
        errorBody: String?,
    ): String {
        val backendMessage = runCatching {
            JSONObject(errorBody.orEmpty())
                .optString("message")
                .takeIf { it.isNotBlank() }
        }.getOrNull()

        if (!backendMessage.isNullOrBlank()) {
            return backendMessage
        }

        return when (statusCode) {
            400 -> "Please select a rating and enter a valid review."
            401 -> "Your session has expired. Please sign in again."
            403 -> "You are not allowed to review this recipe."
            404 -> "This recipe has not been synchronized with the server."
            409 -> "You have already reviewed this recipe."
            429 -> "Too many requests. Please slow down."
            500, 502, 503, 504 ->
                "The review service is temporarily unavailable (HTTP $statusCode). Try again later."

            else -> "The review could not be posted. HTTP $statusCode."
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