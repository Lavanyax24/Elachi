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

        recipeDao.replaceRecipeDetails(
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

    suspend fun toggleFavourite(id: String, favourite: Boolean) = recipeDao.setFavourite(id, favourite)
    suspend fun markCooked(id: String) = recipeDao.incrementTimesCooked(id)

    suspend fun updateBook(bookId: String, userId: String, name: String, description: String?, icon: String, colour: String, coverImageUrl: String?) {
        val existing = bookDao.getBook(bookId) ?: return
        bookDao.upsert(existing.copy(name = name, description = description, icon = icon, colour = colour, coverImageUrl = coverImageUrl))
        try {
            api.updateBook(bookId, CreateRecipeBookRequest(name = name, description = description, coverImageUrl = coverImageUrl, icon = icon, colour = colour))
        } catch (e: Exception) {

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
        if (response.isSuccessful && response.body() != null) {
            Result.success(response.body()!!)
        } else {
            Result.failure(Exception("AI recipe parsing failed (HTTP ${response.code()})."))
        }
    } catch (e: Exception) {
        Result.failure(Exception("Couldn't reach the AI recipe parser."))
    }

    suspend fun upsertRecipeFromServer(ownerId: String, dto: com.elachi.app.data.remote.dto.RecipeDto) {
        val now = System.currentTimeMillis()

        val existing = recipeDao.getRecipe(dto.id)
        recipeDao.clearIngredients(dto.id)
        recipeDao.clearSteps(dto.id)

        recipeDao.upsertRecipe(
            RecipeEntity(
                id = dto.id, ownerId = ownerId, bookId = dto.bookId, title = dto.title,
                category = dto.category, cuisine = dto.cuisine, foodType = dto.foodType,
                difficulty = dto.difficulty, servings = dto.servings, cookTimeMinutes = dto.cookTimeMinutes,
                method = dto.method, allergensCsv = dto.allergens.joinToString(","),
                isPrivate = dto.isPrivate, forkedFromRecipeId = dto.forkedFromRecipeId,
                timesCooked = dto.timesCooked, imageUrl = dto.imageUrl, createdAt = now, updatedAt = now,
            ),
        )
        recipeDao.upsertIngredients(
            dto.ingredients.mapIndexed { index, ing ->
                IngredientEntity(id = UUID.randomUUID().toString(), recipeId = dto.id, name = ing.name, quantity = ing.quantity, unit = ing.unit, sortOrder = index)
            },
        )
        recipeDao.upsertSteps(
            dto.steps.map { step ->
                StepEntity(id = UUID.randomUUID().toString(), recipeId = dto.id, order = step.order, instruction = step.instruction, timerSeconds = step.timerSeconds)
            },
        )
    }

    suspend fun refreshRecipesFromNetwork(userId: String) {
        try {
            val response = api.getRecipes()
            if (!response.isSuccessful) {
                Log.e("RecipeRepository", "refreshRecipes HTTP ${response.code()}")
                return
            }
            response.body().orEmpty().forEach { upsertRecipeFromServer(userId, it) }
        } catch (e: Exception) {
            Log.e("RecipeRepository", "refreshRecipesFromNetwork failed", e)
        }
    }

    suspend fun syncRecipeToBackend(
        recipeId: String,
        currentUserId: String,
    ): Result<Unit> {
        return try {
            val recipe = recipeDao.getRecipe(recipeId)
                ?: return Result.failure(
                    IllegalStateException("Recipe was not found on this device."),
                )

            if (recipe.ownerId != currentUserId) {
                return Result.failure(
                    IllegalStateException("This recipe belongs to another user."),
                )
            }

            // Check whether the recipe already exists on the backend.
            val existingRecipeResponse = api.getRecipe(recipeId)

            if (existingRecipeResponse.isSuccessful) {
                return Result.success(Unit)
            }

            if (existingRecipeResponse.code() != 404) {
                return Result.failure(
                    IllegalStateException(
                        "Could not check recipe synchronization. " +
                                "HTTP ${existingRecipeResponse.code()}",
                    ),
                )
            }

            val book = bookDao.getBook(recipe.bookId)
                ?: return Result.failure(
                    IllegalStateException("The recipe book was not found."),
                )

            /*
             * Make sure the book exists remotely before creating its recipe.
             * HTTP 409 means that it already exists, which is acceptable.
             */
            val bookResponse = api.createBook(
                CreateRecipeBookRequest(
                    id = book.id,
                    name = book.name,
                    description = book.description,
                    coverImageUrl = book.coverImageUrl,
                    icon = book.icon,
                    colour = book.colour,
                ),
            )

            if (!bookResponse.isSuccessful && bookResponse.code() != 409) {
                return Result.failure(
                    IllegalStateException(
                        "The recipe book could not be synchronized. " +
                                "HTTP ${bookResponse.code()}",
                    ),
                )
            }

            val ingredients = recipeDao.getIngredientsOnce(recipeId)
            val steps = recipeDao.getStepsOnce(recipeId)

            val recipeResponse = api.createRecipe(
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
                    ingredients = ingredients.map { ingredient ->
                        IngredientDto(
                            name = ingredient.name,
                            quantity = ingredient.quantity,
                            unit = ingredient.unit,
                        )
                    },
                    steps = steps.map { step ->
                        StepDto(
                            order = step.order,
                            instruction = step.instruction,
                            timerSeconds = step.timerSeconds,
                        )
                    },
                    allergens = recipe.allergensCsv
                        .split(",")
                        .map { it.trim() }
                        .filter { it.isNotBlank() },
                    isPrivate = recipe.isPrivate,
                    imageUrl = recipe.imageUrl,
                ),
            )

            if (recipeResponse.isSuccessful || recipeResponse.code() == 409) {
                Result.success(Unit)
            } else {
                Result.failure(
                    IllegalStateException(
                        "The recipe could not be synchronized. " +
                                "HTTP ${recipeResponse.code()}",
                    ),
                )
            }
        } catch (exception: Exception) {
            Log.e(
                "RecipeRepository",
                "syncRecipeToBackend failed",
                exception,
            )

            Result.failure(exception)
        }
    }
}
