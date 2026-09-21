package com.elachi.app.data.remote.dto

/*
* This file defines the data transfer objects (DTOs) used in the app.
* The UserSyncRequest represents a request to sync user data with the server.
* The UserSyncResponse represents a response from the server after syncing user data.
*/

data class UserSyncRequest(
    val firebaseUid: String,
    val email: String,
    val firstName: String,
    val surname: String,
)

data class UserSyncResponse(
    val userId: String,
    val friendCode: String,
    val createdAt: String,
)

data class UserProfileDto(
    val id: String,
    val displayName: String,
    val bio: String?,
    val avatarUrl: String?,
    val friendCode: String,
    val cookingInterests: List<String>,
    val dietaryRestrictions: List<String>,
)

data class UpdateProfileRequest(
    val displayName: String? = null,
    val bio: String? = null,
    val avatarUrl: String? = null,
    val cookingInterests: List<String>? = null,
    val dietaryRestrictions: List<String>? = null,
)

data class RecipeBookDto(
    val id: String,
    val name: String,
    val description: String?,
    val coverImageUrl: String?,
    val icon: String,
    val colour: String,
    val recipeCount: Int = 0,
)


data class CreateRecipeBookRequest(
    val id: String? = null,
    val name: String,
    val description: String?,
    val coverImageUrl: String?,
    val icon: String,
    val colour: String,
)

data class IngredientDto(
    val name: String,
    val quantity: Double,
    val unit: String,
)

data class StepDto(
    val order: Int,
    val instruction: String,
    val timerSeconds: Int? = null,
)

data class RecipeDto(
    val id: String,
    val bookId: String,
    val title: String,
    val category: String,
    val cuisine: String,
    val foodType: String,
    val difficulty: String,
    val servings: Int,
    val cookTimeMinutes: Int,
    val method: String,
    val ingredients: List<IngredientDto>,
    val steps: List<StepDto>,
    val allergens: List<String>,
    val isPrivate: Boolean,
    val forkedFromRecipeId: String? = null,
    val timesCooked: Int = 0,
    val imageUrl: String? = null,
)

data class CreateRecipeRequest(
    val id: String? = null,
    val bookId: String,
    val title: String,
    val category: String,
    val cuisine: String,
    val foodType: String,
    val difficulty: String,
    val servings: Int,
    val cookTimeMinutes: Int,
    val method: String,
    val ingredients: List<IngredientDto>,
    val steps: List<StepDto>,
    val allergens: List<String>,
    val isPrivate: Boolean,
    val imageUrl: String? = null,
)

data class RecipeSuggestionDto(
    val recipe: RecipeDto,
    val matchPercent: Double,
)

data class PantryItemDto(
    val id: String,
    val name: String,
    val quantity: Double,
    val unit: String,
)

data class CreatePantryItemRequest(
    val id: String? = null,
    val name: String,
    val quantity: Double,
    val unit: String,
)

data class ShoppingListItemDto(
    val id: String,
    val name: String,
    val quantity: Double,
    val unit: String,
    val isBought: Boolean,
)

data class GenerateShoppingListRequest(val recipeId: String)

data class CookSessionRequest(
    val recipeId: String,
    val completedAt: String,
)

data class CookSessionResponse(
    val cookSessionId: String,
    val updatedStreak: Int,
    val unlockedAchievements: List<String>,
)

data class ChatRequest(
    val message: String,
    val conversationId: String? = null,
)

data class ChatResponse(
    val conversationId: String,
    val reply: String,
)

data class ParseRecipeTextRequest(
    val rawText: String,
)

data class ParsedRecipeAiDto(
    val title: String,
    val ingredients: List<IngredientDto>,
    val steps: List<String>,
    val method: String,
    val servings: Int,
    val cookTimeMinutes: Int,
)

data class ForkRecipeRequest(val bookId: String)

data class UpdateRecipeRequest(
    val title: String? = null,
    val category: String? = null,
    val cuisine: String? = null,
    val foodType: String? = null,
    val difficulty: String? = null,
    val servings: Int? = null,
    val cookTimeMinutes: Int? = null,
    val method: String? = null,
    val allergens: List<String>? = null,
    val isPrivate: Boolean? = null,
    val imageUrl: String? = null,
    val bookId: String? = null,
    val ingredients: List<IngredientDto>? = null,
    val steps: List<StepDto>? = null,
)

data class DiscoverRecipeDto(
    val id: String,
    val title: String,
    val imageUrl: String?,
    val cookTimeMinutes: Int,
    val difficulty: String,
    val cuisine: String,
    val timesCooked: Int,
    val foodType: String? = null,
    val creatorId: String,
    val creatorName: String,
    val avgRating: Double,
    val ratingCount: Int,
)

data class NotificationTokenRequest(val fcmToken: String)

