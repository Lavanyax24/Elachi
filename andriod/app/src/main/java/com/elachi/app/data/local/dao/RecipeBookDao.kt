package com.elachi.app.data.local.dao

import androidx.room.*
import com.elachi.app.data.local.entities.RecipeBookEntity
import kotlinx.coroutines.flow.Flow

/*
* This class defines the Data Access Object (DAO) for the RecipeBook entity.
* The DAO provides methods for inserting, updating, and deleting recipe books.
*/

@Dao
interface RecipeBookDao {
    @Query("SELECT * FROM recipe_books WHERE ownerId = :userId ORDER BY createdAt DESC")
    fun observeBooks(userId: String): Flow<List<RecipeBookEntity>>

    @Query("SELECT * FROM recipe_books WHERE id = :id")
    suspend fun getBook(id: String): RecipeBookEntity?

    @Query("SELECT * FROM recipe_books WHERE id = :id")
    fun observeBook(id: String): Flow<RecipeBookEntity?>

    @Upsert
    suspend fun upsert(book: RecipeBookEntity)

    @Upsert
    suspend fun upsertAll(books: List<RecipeBookEntity>)

    @Delete
    suspend fun delete(book: RecipeBookEntity)
}
