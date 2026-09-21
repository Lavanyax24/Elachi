package com.elachi.app.data.repository

import android.util.Log
import com.elachi.app.data.local.dao.RecipeBookDao
import com.elachi.app.data.local.dao.RecipeDao
import com.elachi.app.data.local.entities.IngredientEntity
import com.elachi.app.data.local.entities.RecipeBookEntity
import com.elachi.app.data.local.entities.RecipeEntity
import com.elachi.app.data.local.entities.StepEntity
import com.elachi.app.data.remote.ApiService
import com.elachi.app.data.remote.dto.CreateRecipeBookRequest
import com.elachi.app.data.remote.dto.CreateRecipeRequest
import com.elachi.app.data.remote.dto.IngredientDto
import com.elachi.app.data.remote.dto.StepDto
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class RecipeRepository(
    private val api: ApiService,
    private val bookDao: RecipeBookDao,
    private val recipeDao: RecipeDao,
) {
    fun observeBooks(userId: String): Flow<List<RecipeBookEntity>> = bookDao.observeBooks(userId)
    fun observeBook(bookId: String): Flow<RecipeBookEntity?> = bookDao.observeBook(bookId)
    fun observeAllRecipes(userId: String): Flow<List<RecipeEntity>> = recipeDao.observeAllRecipes(userId)
    fun observeRecipesInBook(bookId: String): Flow<List<RecipeEntity>> = recipeDao.observeRecipesInBook(bookId)
    fun observeRecipe(id: String) = recipeDao.observeRecipe(id)
    fun observeIngredients(recipeId: String) = recipeDao.observeIngredients(recipeId)
    fun observeSteps(recipeId: String) = recipeDao.observeSteps(recipeId)

    suspend fun getRecipeOnce(recipeId: String) = recipeDao.getRecipe(recipeId)
    suspend fun getIngredientsOnce(recipeId: String) = recipeDao.getIngredientsOnce(recipeId)
    suspend fun getStepsOnce(recipeId: String) = recipeDao.getStepsOnce(recipeId)

    suspend fun createBook(
        userId: String, name: String, description: String?, icon: String, colour: String,
        coverImageUrl: String? = null,
    ) {
        val id = UUID.randomUUID().toString()
        val now = System.currentTimeMillis()
        bookDao.upsert(
            RecipeBookEntity(
                id = id, ownerId = userId, name = name, description = description,
                coverImageUrl = coverImageUrl, icon = icon, colour = colour, createdAt = now,
            ),
        )
        try {

            api.createBook(CreateRecipeBookRequest(id = id, name, description, coverImageUrl, icon, colour))
        } catch (e: Exception) {
            Log.e("RecipeRepository", "createBook backend sync failed", e)
        }
    }

    suspend fun createRecipe(
        userId: String, bookId: String, title: String, category: String, cuisine: String,
        foodType: String, difficulty: String, servings: Int, cookTimeMinutes: Int, method: String,
        ingredients: List<Pair<String, Pair<Double, String>>>,
        steps: List<String>, allergens: List<String>, isPrivate: Boolean, imageUrl: String? = null,
    ): String {
        val recipeId = UUID.randomUUID().toString()
        val now = System.currentTimeMillis()

        recipeDao.upsertRecipe(
            RecipeEntity(
                id = recipeId, ownerId = userId, bookId = bookId, title = title,
                category = category, cuisine = cuisine, foodType = foodType,
                difficulty = difficulty, servings = servings, cookTimeMinutes = cookTimeMinutes,
                method = method, allergensCsv = allergens.joinToString(","),
                isPrivate = isPrivate, imageUrl = imageUrl, createdAt = now, updatedAt = now,
            ),
        )
        recipeDao.upsertIngredients(
            ingredients.mapIndexed { index, (name, qtyUnit) ->
                IngredientEntity(
                    id = UUID.randomUUID().toString(), recipeId = recipeId, name = name,
                    quantity = qtyUnit.first, unit = qtyUnit.second, sortOrder = index,
                )
            },
        )
        recipeDao.upsertSteps(
            steps.mapIndexed { index, instruction ->
                StepEntity(
                    id = UUID.randomUUID().toString(), recipeId = recipeId,
                    order = index, instruction = instruction,
                )
            },
        )

        try {

            api.createRecipe(
                CreateRecipeRequest(
                    id = recipeId, bookId = bookId, title = title, category = category, cuisine = cuisine,
                    foodType = foodType, difficulty = difficulty, servings = servings,
                    cookTimeMinutes = cookTimeMinutes, method = method,
                    ingredients = ingredients.map { (name, qtyUnit) -> IngredientDto(name, qtyUnit.first, qtyUnit.second) },
                    steps = steps.mapIndexed { i, s -> StepDto(i, s) },
                    allergens = allergens, isPrivate = isPrivate, imageUrl = imageUrl,
                ),
            )
        } catch (e: Exception) {
            Log.e("RecipeRepository", "createRecipe backend sync failed", e)
        }
        return recipeId
    }

    suspend fun deleteRecipe(id: String) {
        recipeDao.deleteRecipe(id)
        try { api.deleteRecipe(id) } catch (e: Exception) {
            Log.e("RecipeRepository", "deleteRecipe backend sync failed", e)
        }
    }

    suspend fun updateRecipe(
        recipeId: String,
        userId: String,
        bookId: String,
        title: String,
        category: String,
        cuisine: String,
        foodType: String,
        difficulty: String,
        servings: Int,
        cookTimeMinutes: Int,
        method: String,
        ingredients: List<Pair<String, Pair<Double, String>>>,
        steps: List<String>,
        allergens: List<String>,
        isPrivate: Boolean,
        imageUrl: String?,
    ) {
        val existing = recipeDao.getRecipe(recipeId)
            ?: throw IllegalArgumentException("Recipe not found.")

        if (existing.ownerId != userId) {
            throw IllegalStateException("You cannot edit another user's recipe.")
        }

        val now = System.currentTimeMillis()
        val updatedRecipe = existing.copy(
            bookId = bookId,
            title = title,
            category = category,
            cuisine = cuisine,
            foodType = foodType,
            difficulty = difficulty,
            servings = servings,
            cookTimeMinutes = cookTimeMinutes,
            method = method,
            allergensCsv = allergens.joinToString(","),
            isPrivate = isPrivate,
            imageUrl = imageUrl,
            updatedAt = now,
            lastModified = now,
            pendingSync = true,
        )

        val updatedIngredients = ingredients.mapIndexed { index, (name, qtyUnit) ->
            IngredientEntity(
                id = UUID.randomUUID().toString(),
                recipeId = recipeId,
                name = name,
                quantity = qtyUnit.first,
                unit = qtyUnit.second,
                sortOrder = index,
            )
        }

        val updatedSteps = steps.mapIndexed { index, instruction ->
            StepEntity(
                id = UUID.randomUUID().toString(),
                recipeId = recipeId,
                order = index,
                instruction = instruction,
            )
        }

        recipeDao.upsertRecipeWithDetails(
            recipe = updatedRecipe,
            ingredients = updatedIngredients,
            steps = updatedSteps,
        )

        try {
            val response = api.updateRecipe(
                recipeId,
                CreateRecipeRequest(
                    id = recipeId,
                    bookId = bookId,
                    title = title,
                    category = category,
                    cuisine = cuisine,
                    foodType = foodType,
                    difficulty = difficulty,
                    servings = servings,
                    cookTimeMinutes = cookTimeMinutes,
                    method = method,
                    ingredients = ingredients.map { (name, qtyUnit) ->
                        IngredientDto(name, qtyUnit.first, qtyUnit.second)
                    },
                    steps = steps.mapIndexed { index, instruction ->
                        StepDto(index, instruction)
                    },
                    allergens = allergens,
                    isPrivate = isPrivate,
                    imageUrl = imageUrl,
                ),
            )

            if (response.isSuccessful) {
                recipeDao.upsertRecipe(updatedRecipe.copy(pendingSync = false))
            } else {
                Log.e("RecipeRepository", "updateRecipe HTTP ${response.code()}")
            }
        } catch (exception: Exception) {
            Log.e("RecipeRepository", "updateRecipe backend sync failed", exception)
        }
    }

    suspend fun markCooked(id: String) = recipeDao.incrementTimesCooked(id)

    suspend fun toggleFavourite(id: String, favourite: Boolean) = recipeDao.setFavourite(id, favourite)

    suspend fun updateRecipeVisibility(recipeId: String, isPrivate: Boolean) {
        recipeDao.setPrivate(recipeId, isPrivate)
        try {
            val response = api.patchRecipe(recipeId, com.elachi.app.data.remote.dto.UpdateRecipeRequest(isPrivate = isPrivate))
            if (response.isSuccessful && response.body() != null) {
                upsertRecipeFromServer(UserSession.requireUserId(), response.body()!!)
            } else {
                Log.e("RecipeRepository", "updateRecipeVisibility failed: HTTP ${response.code()} — ${response.errorBody()?.string()}")
            }
        } catch (e: Exception) {
            Log.e("RecipeRepository", "updateRecipeVisibility threw", e)
        }
    }

    suspend fun updateBook(bookId: String, name: String, description: String?, icon: String, colour: String, coverImageUrl: String?) {
        val existing = bookDao.getBook(bookId) ?: return
        bookDao.upsert(existing.copy(name = name, description = description, icon = icon, colour = colour, coverImageUrl = coverImageUrl))
        try {
            api.updateBook(bookId, CreateRecipeBookRequest(name = name, description = description, coverImageUrl = coverImageUrl, icon = icon, colour = colour))
        } catch (_: Exception) {}
    }

    suspend fun retryPendingSyncs() {
        val pending = recipeDao.getPendingRecipes()
        pending.forEach { recipe ->
            try {
                val ingredients = recipeDao.getIngredientsOnce(recipe.id)
                val steps = recipeDao.getStepsOnce(recipe.id)
                
                api.updateRecipe(
                    recipe.id,
                    CreateRecipeRequest(
                        id = recipe.id,
                        bookId = recipe.bookId,
                        title = recipe.title,
                        category = recipe.category,
                        cuisine = recipe.cuisine,
                        foodType = recipe.foodType,
                        difficulty = recipe.difficulty,
                        servings = recipe.servings,
                        cookTimeMinutes = recipe.cookTimeMinutes,
                        method = recipe.method,
                        ingredients = ingredients.map { IngredientDto(it.name, it.quantity, it.unit) },
                        steps = steps.map { StepDto(it.order, it.instruction) },
                        allergens = recipe.allergensCsv.split(",").filter { it.isNotBlank() },
                        isPrivate = recipe.isPrivate,
                        imageUrl = recipe.imageUrl,
                    ),
                )
                recipeDao.upsertRecipe(recipe.copy(pendingSync = false))
            } catch (_: Exception) {}
        }
    }

    suspend fun deleteBook(book: RecipeBookEntity) {
        bookDao.delete(book)
        try { api.deleteBook(book.id) } catch (e: Exception) {
            Log.e("RecipeRepository", "deleteBook backend sync failed", e)
        }
    }

    suspend fun refreshBooksFromNetwork(userId: String) {
        try {
            val remoteBooks = api.getBooks().body().orEmpty()
            bookDao.upsertAll(
                remoteBooks.map {
                    RecipeBookEntity(
                        id = it.id, ownerId = userId, name = it.name, description = it.description,
                        coverImageUrl = it.coverImageUrl, icon = it.icon, colour = it.colour,
                        createdAt = System.currentTimeMillis(),
                    )
                },
            )
        } catch (e: Exception) {
            // Same note as updateBook above.
        }
    }

    suspend fun parseRecipeTextViaAi(rawText: String): Result<com.elachi.app.data.remote.dto.ParsedRecipeAiDto> = try {
        val response = api.parseRecipeText(com.elachi.app.data.remote.dto.ParseRecipeTextRequest(rawText))
        if (response.isSuccessful && (response.body() != null)) {
            Result.success(response.body()!!)
        } else {
            Result.failure(Exception("AI recipe parsing failed (HTTP ${response.code()})."))
        }
    } catch (_: Exception) {
        Result.failure(Exception("Couldn't reach the AI recipe parser."))
    }

    suspend fun upsertRecipeFromServer(ownerId: String, dto: com.elachi.app.data.remote.dto.RecipeDto) {
        val now = System.currentTimeMillis()
        val existing = recipeDao.getRecipe(dto.id)
        if (existing?.pendingSync == true) return

        val recipe = RecipeEntity(
            id = dto.id, ownerId = ownerId, bookId = dto.bookId, title = dto.title,
            category = dto.category, cuisine = dto.cuisine, foodType = dto.foodType,
            difficulty = dto.difficulty, servings = dto.servings, cookTimeMinutes = dto.cookTimeMinutes,
            method = dto.method, allergensCsv = dto.allergens.joinToString(","),
            isPrivate = dto.isPrivate, isFavourite = existing?.isFavourite ?: false,
            forkedFromRecipeId = dto.forkedFromRecipeId, forkCount = existing?.forkCount ?: 0,
            timesCooked = dto.timesCooked, imageUrl = dto.imageUrl, 
            createdAt = existing?.createdAt ?: now, updatedAt = now, pendingSync = false
        )

        val ingredients = dto.ingredients.mapIndexed { index, ing ->
            IngredientEntity(id = UUID.randomUUID().toString(), recipeId = dto.id, name = ing.name, quantity = ing.quantity, unit = ing.unit, sortOrder = index)
        }

        val steps = dto.steps.map { step ->
            StepEntity(id = UUID.randomUUID().toString(), recipeId = dto.id, order = step.order, instruction = step.instruction, timerSeconds = step.timerSeconds)
        }

        recipeDao.upsertRecipeWithDetails(recipe, ingredients, steps)
    }

    suspend fun refreshRecipesFromNetwork(userId: String) {
        try {
            val response = api.getRecipes()
            if (!response.isSuccessful) {
                Log.e("RecipeRepository", "refreshRecipes HTTP ${response.code()}")
                return
            }
            response.body().orEmpty().forEach { upsertRecipeFromServer(userId, it) }
        } catch (_: Exception) {
            Log.e("RecipeRepository", "refreshRecipesFromNetwork failed")
        }
    }
}
