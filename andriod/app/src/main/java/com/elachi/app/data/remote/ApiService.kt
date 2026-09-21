package com.elachi.app.data.remote

import com.elachi.app.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.*

/*
* This file defines the Retrofit API service interface for the app.
* The ApiService interface provides methods for syncing user data, creating and updating recipe books.
*/

interface ApiService {

    // --- Users ---
    @POST("api/users/sync")
    suspend fun syncUser(@Body body: UserSyncRequest): Response<UserSyncResponse>

    @GET("api/users/me")
    suspend fun getMyProfile(): Response<UserProfileDto>

    @retrofit2.http.PATCH("api/users/me")
    suspend fun updateProfile(@Body body: UpdateProfileRequest): Response<UserProfileDto>

    // --- Recipe Books ---
    @GET("api/books")
    suspend fun getBooks(): Response<List<RecipeBookDto>>

    @POST("api/books")
    suspend fun createBook(@Body body: CreateRecipeBookRequest): Response<RecipeBookDto>

    @retrofit2.http.PATCH("api/books/{id}")
    suspend fun updateBook(@retrofit2.http.Path("id") id: String, @Body body: CreateRecipeBookRequest): Response<RecipeBookDto>

    @DELETE("api/books/{id}")
    suspend fun deleteBook(@Path("id") id: String): Response<Unit>

    // --- Recipes ---
    @GET("api/recipes")
    suspend fun getRecipes(
        @Query("bookId") bookId: String? = null,
        @Query("search") search: String? = null,
    ): Response<List<RecipeDto>>

    @POST("api/recipes")
    suspend fun createRecipe(@Body body: CreateRecipeRequest): Response<RecipeDto>

    @GET("api/recipes/{id}")
    suspend fun getRecipe(@Path("id") id: String): Response<RecipeDto>

    @PATCH("api/recipes/{id}")
    suspend fun updateRecipe(
        @Path("id") id: String,
        @Body body: CreateRecipeRequest,
    ): Response<RecipeDto>

    @DELETE("api/recipes/{id}")
    suspend fun deleteRecipe(@Path("id") id: String): Response<Unit>

    @retrofit2.http.PATCH("api/recipes/{id}")
    suspend fun patchRecipe(
        @retrofit2.http.Path("id") id: String,
        @Body body: UpdateRecipeRequest,
    ): Response<RecipeDto>

    @GET("api/recipes/suggestions")
    suspend fun getSuggestions(): Response<List<RecipeSuggestionDto>>

    // --- Pantry & Shopping List ---
    @GET("api/pantry")
    suspend fun getPantry(): Response<List<PantryItemDto>>

    @POST("api/pantry")
    suspend fun addPantryItem(@Body body: CreatePantryItemRequest): Response<PantryItemDto>

    @DELETE("api/pantry/{id}")
    suspend fun deletePantryItem(@Path("id") id: String): Response<Unit>

    @GET("api/shopping-list")
    suspend fun getShoppingList(): Response<List<ShoppingListItemDto>>

    @POST("api/shopping-list/generate")
    suspend fun generateShoppingList(@Body body: GenerateShoppingListRequest): Response<List<ShoppingListItemDto>>

    // --- Cook Sessions / Achievements / Streaks ---
    @POST("api/cook-sessions")
    suspend fun logCookSession(@Body body: CookSessionRequest): Response<CookSessionResponse>

    // --- AI Chef ---
    @POST("api/chat")
    suspend fun sendChatMessage(@Body body: ChatRequest): Response<ChatResponse>

    // --- AI-Powered OCR Recipe Structuring ---
    @POST("api/recipes/parse-text")
    suspend fun parseRecipeText(@Body body: ParseRecipeTextRequest): Response<ParsedRecipeAiDto>

    // --- Fork ---
    @POST("api/recipes/{id}/fork")
    suspend fun forkRecipe(@Path("id") id: String, @Body body: ForkRecipeRequest): Response<RecipeDto>

    // --- Discover ---
    @GET("api/recipes/discover")
    suspend fun discoverRecipes(@retrofit2.http.Query("mode") mode: String, @retrofit2.http.Query("search") search: String? = null): Response<List<DiscoverRecipeDto>>

    // --- Push Notifications ---
    @POST("api/users/me/notification-token")
    suspend fun registerNotificationToken(@Body body: NotificationTokenRequest): Response<Unit>

    @retrofit2.http.HTTP(method = "DELETE", path = "api/users/me/notification-token", hasBody = true)
    suspend fun unregisterNotificationToken(@Body body: NotificationTokenRequest): Response<Unit>

    // --- Account Deletion ---
    @DELETE("api/users/me")
    suspend fun deleteAccount(): Response<Unit>
}
