package com.elachi.app.data.local.dao

import androidx.room.*
import com.elachi.app.data.local.entities.IngredientEntity
import com.elachi.app.data.local.entities.RecipeEntity
import com.elachi.app.data.local.entities.StepEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RecipeDao {

    @Query("SELECT * FROM recipes WHERE ownerId = :userId ORDER BY createdAt DESC")
    fun observeAllRecipes(userId: String): Flow<List<RecipeEntity>>

    @Query("SELECT * FROM recipes WHERE bookId = :bookId ORDER BY createdAt DESC")
    fun observeRecipesInBook(bookId: String): Flow<List<RecipeEntity>>

    @Query("SELECT * FROM recipes WHERE id = :id")
    suspend fun getRecipe(id: String): RecipeEntity?

    @Query("SELECT * FROM recipes WHERE id = :id")
    fun observeRecipe(id: String): Flow<RecipeEntity?>

    @Query("SELECT * FROM ingredients WHERE recipeId = :recipeId ORDER BY sortOrder ASC")
    fun observeIngredients(recipeId: String): Flow<List<IngredientEntity>>

    @Query("SELECT * FROM steps WHERE recipeId = :recipeId ORDER BY `order` ASC")
    fun observeSteps(recipeId: String): Flow<List<StepEntity>>

    @Query("SELECT * FROM ingredients WHERE recipeId = :recipeId ORDER BY sortOrder ASC")
    suspend fun getIngredientsOnce(recipeId: String): List<IngredientEntity>

    @Query("SELECT * FROM steps WHERE recipeId = :recipeId ORDER BY `order` ASC")
    suspend fun getStepsOnce(recipeId: String): List<StepEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertRecipe(recipe: RecipeEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertIngredients(ingredients: List<IngredientEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSteps(steps: List<StepEntity>)

    @Query("DELETE FROM ingredients WHERE recipeId = :recipeId")
    suspend fun clearIngredients(recipeId: String)

    @Query("DELETE FROM steps WHERE recipeId = :recipeId")
    suspend fun clearSteps(recipeId: String)

    @Query("DELETE FROM recipes WHERE id = :id")
    suspend fun deleteRecipe(id: String)

    @Query("UPDATE recipes SET timesCooked = timesCooked + 1 WHERE id = :id")
    suspend fun incrementTimesCooked(id: String)

    @Query("UPDATE recipes SET isFavourite = :favourite WHERE id = :id")
    suspend fun setFavourite(id: String, favourite: Boolean)
}