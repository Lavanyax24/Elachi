package com.elachi.app.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "achievement_definitions")
data class AchievementDefinitionEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String,
    val category: String,
    val conditionType: String,
    val thresholdValue: Int,
)

@Entity(tableName = "user_achievement_progress")
data class UserAchievementProgressEntity(
    @PrimaryKey val achievementId: String,
    val progress: Int = 0,
    val unlocked: Boolean = false,
    val unlockedAt: Long? = null,
)

@Entity(tableName = "streak_record")
data class StreakRecordEntity(
    @PrimaryKey val userId: String,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val lastCookedDateEpochDay: Long? = null,
)