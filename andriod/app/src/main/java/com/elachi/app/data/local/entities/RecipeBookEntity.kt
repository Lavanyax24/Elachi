package com.elachi.app.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/*
* This file defines the entities used in the Room database.
* The RecipeBookEntity represents a recipe book, with a foreign key to the UserEntity.
* The RecipeEntity represents a recipe, with a foreign key to the RecipeBookEntity.
*/

@Entity(tableName = "recipe_books")
data class RecipeBookEntity(
    @PrimaryKey val id: String,
    val ownerId: String,
    val name: String,
    val description: String?,
    val coverImageUrl: String?,
    val icon: String,
    val colour: String,
    val createdAt: Long,
    val pendingSync: Boolean = false,
    val lastModified: Long = System.currentTimeMillis(),
)