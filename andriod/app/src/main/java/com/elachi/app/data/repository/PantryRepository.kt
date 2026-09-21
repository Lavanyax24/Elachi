package com.elachi.app.data.repository

import android.util.Log
import com.elachi.app.data.local.dao.PantryDao
import com.elachi.app.data.local.dao.RecipeDao
import com.elachi.app.data.local.entities.PantryItemEntity
import com.elachi.app.data.local.entities.RecipeEntity
import com.elachi.app.data.local.entities.ShoppingListItemEntity
import com.elachi.app.data.remote.ApiService
import com.elachi.app.data.remote.dto.CreatePantryItemRequest
import com.elachi.app.util.ServingScaler
import kotlinx.coroutines.flow.Flow
import kotlin.math.roundToInt
import java.util.UUID

// This class handles the pantry data.

data class RecipeMatch(val recipe: RecipeEntity, val matchPercent: Int)

data class ShoppingAddResult(
    val merged: Boolean,
    val name: String,
    val quantity: Double,
    val unit: String,
)

data class MissingIngredient(
    val name: String,
    val quantity: Double,
    val unit: String,
    val alreadyOnList: Boolean,
)

class PantryRepository(
    private val api: ApiService,
    private val pantryDao: PantryDao,
    private val recipeDao: RecipeDao,
) {
    fun observePantry(userId: String): Flow<List<PantryItemEntity>> = pantryDao.observePantry(userId)
    fun observeShoppingList(userId: String): Flow<List<ShoppingListItemEntity>> = pantryDao.observeShoppingList(userId)
     suspend fun addPantryItem(userId: String, name: String, quantity: Double, unit: String): Boolean {
        val item = PantryItemEntity(
            id = UUID.randomUUID().toString(), userId = userId, name = name,
            quantity = quantity, unit = unit, updatedAt = System.currentTimeMillis(),
        )
        pantryDao.upsert(item)
        return syncPantryItem(item)
    }

    private suspend fun syncPantryItem(item: PantryItemEntity, replaceExisting: Boolean = false): Boolean {
        return try {
            if (replaceExisting) {
                val removed = api.deletePantryItem(item.id)
                if (!removed.isSuccessful) throw java.io.IOException("delete rejected: HTTP ${removed.code()}")
            }
            val response = api.addPantryItem(
                CreatePantryItemRequest(id = item.id, name = item.name, quantity = item.quantity, unit = item.unit),
            )
            if (response.isSuccessful) {
                if (item.pendingSync) pantryDao.setPantryPendingSync(item.id, false)
                true
            } else {
                Log.e("PantryRepository", "pantry sync rejected by server: HTTP ${response.code()}")
                pantryDao.setPantryPendingSync(item.id, true)
                false
            }
        } catch (e: Exception) {
            Log.e("PantryRepository", "pantry backend sync failed", e)
            pantryDao.setPantryPendingSync(item.id, true)
            false
        }
    }

   suspend fun updatePantryItem(item: PantryItemEntity, name: String, quantity: Double, unit: String): Boolean {
        val updated = item.copy(
            name = name, quantity = quantity, unit = unit,
            updatedAt = System.currentTimeMillis(),
        )
        pantryDao.upsert(updated)
        return syncPantryItem(updated, replaceExisting = true)
    }

    suspend fun retryPendingPantrySync(userId: String) {
        pantryDao.getPendingPantryItems(userId).forEach { syncPantryItem(it, replaceExisting = true) }
    }

    suspend fun deletePantryItem(id: String): Boolean {
        pantryDao.delete(id)
        return try {
            val response = api.deletePantryItem(id)
            if (!response.isSuccessful) {
                Log.e("PantryRepository", "deletePantryItem rejected by server: HTTP ${response.code()}")
            }
            response.isSuccessful
        } catch (e: Exception) {
            Log.e("PantryRepository", "deletePantryItem backend sync failed", e)
            false
        }
    }

    suspend fun addShoppingItem(userId: String, name: String, quantity: Double, unit: String): ShoppingAddResult {
        val key = nameKey(name)
        val unitKey = unit.trim().lowercase()
        val existing = pantryDao.getShoppingListOnce(userId).firstOrNull {
            !it.isBought && nameKey(it.name) == key && it.unit.trim().lowercase() == unitKey
        }

        return if (existing != null) {
            val total = roundTo2(existing.quantity + quantity)
            pantryDao.upsertShoppingItem(existing.copy(quantity = total))
            ShoppingAddResult(merged = true, name = existing.name, quantity = total, unit = existing.unit)
        } else {
            pantryDao.upsertShoppingItem(
                ShoppingListItemEntity(
                    id = UUID.randomUUID().toString(), userId = userId, name = name,
                    quantity = quantity, unit = unit, createdAt = System.currentTimeMillis(),
                ),
            )
            ShoppingAddResult(merged = false, name = name, quantity = quantity, unit = unit)
        }
    }

    suspend fun setShoppingItemBought(id: String, bought: Boolean) = pantryDao.setBought(id, bought)
    suspend fun deleteShoppingItem(id: String) = pantryDao.deleteShoppingItem(id)

    suspend fun clearBoughtShoppingItems(userId: String) = pantryDao.clearBoughtShoppingItems(userId)
    suspend fun clearShoppingList(userId: String) = pantryDao.clearShoppingList(userId)

     suspend fun cleanUpShoppingDuplicates(userId: String) {
        val groups = pantryDao.getShoppingListOnce(userId)
            .filter { !it.isBought }
            .groupBy { nameKey(it.name) to it.unit.trim().lowercase() }
            .values
            .filter { it.size > 1 }

        for (group in groups) {
            val keeper = group.minByOrNull { it.createdAt } ?: continue
            val total = roundTo2(group.sumOf { it.quantity })
            pantryDao.upsertShoppingItem(keeper.copy(quantity = total))
            group.filter { it.id != keeper.id }.forEach { pantryDao.deleteShoppingItem(it.id) }
        }
    }

    private fun roundTo2(value: Double): Double = (value * 100).roundToInt() / 100.0

    private fun nameKey(name: String): Set<String> =
        tokens(name).ifEmpty { setOf(name.trim().lowercase()) }
    suspend fun getMissingIngredients(
        userId: String,
        recipeId: String,
        baseServings: Int,
        currentServings: Int,
    ): List<MissingIngredient> {
        val pantry = pantryDao.getPantryOnce(userId)
        val onList = pantryDao.getShoppingListOnce(userId)
            .filter { !it.isBought }
            .map { tokens(it.name) }
        val ingredients = recipeDao.getIngredientsOnce(recipeId)

        return ingredients.mapNotNull { ingredient ->
            val needed = ServingScaler.scale(ingredient.quantity, baseServings, currentServings)
            val wanted = tokens(ingredient.name)
            val inPantry = pantry.firstOrNull { wanted.isNotEmpty() && tokens(it.name).containsAll(wanted) }

            val stillNeeded: Double = when {
                inPantry == null -> needed
                inPantry.unit.trim().equals(ingredient.unit.trim(), ignoreCase = true) ->
                    needed - inPantry.quantity
                else -> 0.0
            }

            if (inPantry != null && stillNeeded <= 0.0) {
                null
            } else {
                MissingIngredient(
                    name = ingredient.name.trim(),
                    quantity = (stillNeeded * 100).roundToInt() / 100.0,
                    unit = ingredient.unit.trim(),
                    alreadyOnList = onList.any { it == wanted },
                )
            }
        }
    }

    suspend fun addShoppingItems(userId: String, items: List<MissingIngredient>) {
        items.forEach { addShoppingItem(userId, it.name, it.quantity, it.unit) }
    }

    private fun tokens(name: String): Set<String> =
        name.lowercase()
            .split(Regex("[^\\p{L}]+"))
            .filter { it.isNotBlank() }
            .map { singular(it) }
            .toSet()

    private fun singular(word: String): String = when {
        word.endsWith("oes") && word.length > 4 -> word.dropLast(2)
        word.endsWith("ss") -> word
        word.endsWith("s") && word.length > 3 -> word.dropLast(1)
        else -> word
    }

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