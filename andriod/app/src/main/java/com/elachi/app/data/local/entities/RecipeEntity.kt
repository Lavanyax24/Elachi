package com.elachi.app.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "recipes",
    foreignKeys = [
        ForeignKey(
            entity = RecipeBookEntity::class,
            parentColumns = ["id"],
            childColumns = ["bookId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
data class RecipeEntity(
    @PrimaryKey val id: String,
    val ownerId: String,
    val bookId: String,
    val title: String,
    val category: String,
    val cuisine: String,
    val foodType: String,
    val difficulty: String,
    val servings: Int,
    val cookTimeMinutes: Int,
    val method: String,
    val allergensCsv: String,
    val isPrivate: Boolean,
    val isFavourite: Boolean = false,
    val forkedFromRecipeId: String? = null,
    val forkCount: Int = 0,
    val timesCooked: Int = 0,
    val imageUrl: String? = null,
    val createdAt: Long,
    val updatedAt: Long,
    val pendingSync: Boolean = false,
    val lastModified: Long = System.currentTimeMillis(),
)

@Entity(tableName = "ingredients")
data class IngredientEntity(
    @PrimaryKey val id: String,
    val recipeId: String,
    val name: String,
    val quantity: Double,
    val unit: String,
    val sortOrder: Int,
)

@Entity(tableName = "steps")
data class StepEntity(
    @PrimaryKey val id: String,
    val recipeId: String,
    val order: Int,
    val instruction: String,
    val timerSeconds: Int? = null,
)