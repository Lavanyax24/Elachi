package com.elachi.app.data.repository

import android.util.Log
import com.elachi.app.data.local.dao.PantryDao
import com.elachi.app.data.local.dao.RecipeDao
import com.elachi.app.data.local.entities.PantryItemEntity
import com.elachi.app.data.local.entities.RecipeEntity
import com.elachi.app.data.local.entities.ShoppingListItemEntity
import com.elachi.app.data.remote.ApiService
import com.elachi.app.data.remote.dto.CreatePantryItemRequest
import kotlinx.coroutines.flow.Flow
import java.util.UUID

data class RecipeMatch(val recipe: RecipeEntity, val matchPercent: Int)

class PantryRepository(
    private val api: ApiService,
    private val pantryDao: PantryDao,
    private val recipeDao: RecipeDao,
) {
    fun observePantry(userId: String): Flow<List<PantryItemEntity>> = pantryDao.observePantry(userId)
    fun observeShoppingList(userId: String): Flow<List<ShoppingListItemEntity>> = pantryDao.observeShoppingList(userId)

    suspend fun addPantryItem(userId: String, name: String, quantity: Double, unit: String) {
        val id = UUID.randomUUID().toString()
        val item = PantryItemEntity(
            id = id, userId = userId, name = name,
            quantity = quantity, unit = unit, updatedAt = System.currentTimeMillis(),
        )
        pantryDao.upsert(item)
        try {

            api.addPantryItem(CreatePantryItemRequest(id = id, name, quantity, unit))
        } catch (e: Exception) {
            Log.e("PantryRepository", "addPantryItem backend sync failed", e) // FIX: was silently swallowed
        }
    }

    suspend fun deletePantryItem(id: String) {
        pantryDao.delete(id)
        try { api.deletePantryItem(id) } catch (e: Exception) {
            Log.e("PantryRepository", "deletePantryItem backend sync failed", e) // FIX: was silently swallowed
        }
    }

    suspend fun addShoppingItem(userId: String, name: String, quantity: Double, unit: String) {
        pantryDao.upsertShoppingItem(
            ShoppingListItemEntity(
                id = UUID.randomUUID().toString(), userId = userId, name = name,
                quantity = quantity, unit = unit, createdAt = System.currentTimeMillis(),
            ),
        )
    }

    suspend fun setShoppingItemBought(id: String, bought: Boolean) = pantryDao.setBought(id, bought)
    suspend fun deleteShoppingItem(id: String) = pantryDao.deleteShoppingItem(id)

    suspend fun generateShoppingListFromRecipe(userId: String, recipeId: String) {
        val pantryNames = pantryDao.getPantryOnce(userId).map { it.name.trim().lowercase() }.toSet()
        val ingredients = recipeDao.getIngredientsOnce(recipeId)
        ingredients
            .filter { it.name.trim().lowercase() !in pantryNames }
            .forEach { addShoppingItem(userId, it.name, it.quantity, it.unit) }
    }

    suspend fun getPantryMatches(userId: String, recipes: List<RecipeEntity>): List<RecipeMatch> {
        val pantryNames = pantryDao.getPantryOnce(userId).map { it.name.trim().lowercase() }.toSet()
        return recipes.map { recipe ->
            val ingredients = recipeDao.getIngredientsOnce(recipe.id)
            if (ingredients.isEmpty()) {
                RecipeMatch(recipe, 0)
            } else {
                val haveCount = ingredients.count { it.name.trim().lowercase() in pantryNames }
                RecipeMatch(recipe, (haveCount * 100) / ingredients.size)
            }
        }.sortedByDescending { it.matchPercent }
    }
}