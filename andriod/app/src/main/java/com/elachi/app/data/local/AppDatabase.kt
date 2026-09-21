package com.elachi.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.elachi.app.data.local.dao.AchievementDao
import com.elachi.app.data.local.dao.PantryDao
import com.elachi.app.data.local.dao.RecipeBookDao
import com.elachi.app.data.local.dao.RecipeDao
import com.elachi.app.data.local.entities.*

@Database(
    entities = [
        RecipeBookEntity::class,
        RecipeEntity::class,
        IngredientEntity::class,
        StepEntity::class,
        PantryItemEntity::class,
        ShoppingListItemEntity::class,
        CookSessionEntity::class,
        AchievementDefinitionEntity::class,
        UserAchievementProgressEntity::class,
        StreakRecordEntity::class,
    ],
    version = 2,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun recipeBookDao(): RecipeBookDao
    abstract fun recipeDao(): RecipeDao
    abstract fun pantryDao(): PantryDao
    abstract fun achievementDao(): AchievementDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "elachi.db",
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
    }
}