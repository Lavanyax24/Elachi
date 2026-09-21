package com.elachi.app.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/*
* This file defines the entities used in the Room database.
* The PantryItemEntity represents an item in the user's pantry.
* The ShoppingListItemEntity represents an item in the user's shopping list.
*/

@Entity(tableName = "pantry_items")
data class PantryItemEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val name: String,
    val quantity: Double,
    val unit: String,
    val updatedAt: Long,
    val pendingSync: Boolean = false,
)

@Entity(tableName = "shopping_list_items")
data class ShoppingListItemEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val name: String,
    val quantity: Double,
    val unit: String,
    val isBought: Boolean = false,
    val createdAt: Long,
    val pendingSync: Boolean = false,
)

@Entity(tableName = "cook_sessions")
data class CookSessionEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val recipeId: String,
    val completedAt: Long,
)