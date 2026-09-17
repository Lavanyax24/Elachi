package com.elachi.app.data.local.dao

import androidx.room.*
import com.elachi.app.data.local.entities.CookSessionEntity
import com.elachi.app.data.local.entities.PantryItemEntity
import com.elachi.app.data.local.entities.ShoppingListItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PantryDao {
    @Query("SELECT * FROM pantry_items WHERE userId = :userId ORDER BY name ASC")
    fun observePantry(userId: String): Flow<List<PantryItemEntity>>

    @Query("SELECT * FROM pantry_items WHERE userId = :userId")
    suspend fun getPantryOnce(userId: String): List<PantryItemEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: PantryItemEntity)

    @Query("DELETE FROM pantry_items WHERE id = :id")
    suspend fun delete(id: String)

    @Query("SELECT * FROM shopping_list_items WHERE userId = :userId ORDER BY createdAt DESC")
    fun observeShoppingList(userId: String): Flow<List<ShoppingListItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertShoppingItem(item: ShoppingListItemEntity)

    @Query("UPDATE shopping_list_items SET isBought = :bought WHERE id = :id")
    suspend fun setBought(id: String, bought: Boolean)

    @Query("DELETE FROM shopping_list_items WHERE id = :id")
    suspend fun deleteShoppingItem(id: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCookSession(session: CookSessionEntity)

    @Query("SELECT COUNT(*) FROM cook_sessions WHERE userId = :userId")
    suspend fun countCookSessions(userId: String): Int

    @Query("SELECT completedAt FROM cook_sessions WHERE userId = :userId")
    suspend fun getCookSessionTimestamps(userId: String): List<Long>
}