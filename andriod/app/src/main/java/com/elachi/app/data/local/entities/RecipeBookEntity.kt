package com.elachi.app.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

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