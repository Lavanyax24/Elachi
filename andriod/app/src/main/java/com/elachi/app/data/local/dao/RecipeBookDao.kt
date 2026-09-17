package com.elachi.app.data.local.dao

import androidx.room.*
import com.elachi.app.data.local.entities.RecipeBookEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RecipeBookDao {
    @Query("SELECT * FROM recipe_books WHERE ownerId = :userId ORDER BY createdAt DESC")
    fun observeBooks(userId: String): Flow<List<RecipeBookEntity>>

    @Query("SELECT * FROM recipe_books WHERE id = :id")
    suspend fun getBook(id: String): RecipeBookEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(book: RecipeBookEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(books: List<RecipeBookEntity>)

    @Delete
    suspend fun delete(book: RecipeBookEntity)
}