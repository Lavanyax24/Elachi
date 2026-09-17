package com.elachi.app.data.local.dao

import androidx.room.*
import com.elachi.app.data.local.entities.AchievementDefinitionEntity
import com.elachi.app.data.local.entities.StreakRecordEntity
import com.elachi.app.data.local.entities.UserAchievementProgressEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AchievementDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun seedDefinitions(defs: List<AchievementDefinitionEntity>)

    @Query("SELECT * FROM achievement_definitions")
    fun observeDefinitions(): Flow<List<AchievementDefinitionEntity>>

    @Query("SELECT * FROM achievement_definitions WHERE conditionType = :conditionType")
    suspend fun getDefinitionsFor(conditionType: String): List<AchievementDefinitionEntity>

    @Query("SELECT * FROM user_achievement_progress")
    fun observeProgress(): Flow<List<UserAchievementProgressEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertProgress(progress: UserAchievementProgressEntity)

    @Query("SELECT * FROM user_achievement_progress WHERE achievementId = :id")
    suspend fun getProgress(id: String): UserAchievementProgressEntity?

    @Query("SELECT * FROM streak_record WHERE userId = :userId")
    suspend fun getStreak(userId: String): StreakRecordEntity?

    @Query("SELECT * FROM streak_record WHERE userId = :userId")
    fun observeStreak(userId: String): Flow<StreakRecordEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertStreak(streak: StreakRecordEntity)
}