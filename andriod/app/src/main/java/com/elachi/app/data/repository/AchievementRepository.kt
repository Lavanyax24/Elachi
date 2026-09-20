package com.elachi.app.data.repository

import com.elachi.app.data.local.dao.AchievementDao
import com.elachi.app.data.local.dao.PantryDao
import com.elachi.app.data.local.dao.RecipeDao
import com.elachi.app.data.local.entities.AchievementDefinitionEntity
import com.elachi.app.data.local.entities.CookSessionEntity
import com.elachi.app.data.local.entities.StreakRecordEntity
import com.elachi.app.data.local.entities.UserAchievementProgressEntity
import java.time.LocalDate
import java.time.Instant
import java.time.YearMonth
import java.time.ZoneOffset
import java.util.UUID

data class AchievementView(
    val definition: AchievementDefinitionEntity,
    val progress: Int,
    val unlocked: Boolean,
)


class AchievementRepository(
    private val achievementDao: AchievementDao,
    private val pantryDao: PantryDao,
    private val recipeDao: RecipeDao,
) {

    suspend fun seedDefinitionsIfNeeded() {
        achievementDao.seedDefinitions(
            listOf(
                AchievementDefinitionEntity("first_cook", "First Cook", "Cook your very first recipe", "Cooking", "recipesCooked", 1),
                AchievementDefinitionEntity("home_cook", "Home Cook", "Cook 5 recipes", "Cooking", "recipesCooked", 5),
                AchievementDefinitionEntity("kitchen_regular", "Kitchen Regular", "Cook 10 recipes", "Cooking", "recipesCooked", 10),
                AchievementDefinitionEntity("master_cook", "Master Cook", "Cook 25 recipes", "Cooking", "recipesCooked", 25),
                AchievementDefinitionEntity("cooking_legend", "Cooking Legend", "Cook 50 recipes", "Cooking", "recipesCooked", 50),
                AchievementDefinitionEntity("streak_spark", "Streak Spark", "Maintain a 3-day cooking streak", "Cooking", "streakDays", 3),
                AchievementDefinitionEntity("streak_starter", "Streak Starter", "Maintain a 7-day cooking streak", "Cooking", "streakDays", 7),
                AchievementDefinitionEntity("streak_champion", "Streak Champion", "Maintain a 30-day cooking streak", "Cooking", "streakDays", 30),
                AchievementDefinitionEntity("first_recipe", "Recipe Rookie", "Add your first recipe", "Contribution", "recipesAdded", 1),
                AchievementDefinitionEntity("recipe_collector", "Recipe Collector", "Save 10 recipes to your cookbook", "Contribution", "recipesAdded", 10),
                AchievementDefinitionEntity("recipe_hoarder", "Recipe Hoarder", "Save 25 recipes to your cookbook", "Contribution", "recipesAdded", 25),
                AchievementDefinitionEntity("recipe_archivist", "Recipe Archivist", "Save 50 recipes to your cookbook", "Contribution", "recipesAdded", 50),
                AchievementDefinitionEntity("community_star", "Community Star", "Get 50 cooks on your recipes", "Contribution", "cooksOnMyRecipes", 50),
                AchievementDefinitionEntity("fork_master", "Fork Master", "Fork 10 public recipes", "Contribution", "forksCount", 10),
                AchievementDefinitionEntity("pantry_starter", "Pantry Starter", "Add 5 pantry items", "Contribution", "pantryItemsAdded", 5),
                AchievementDefinitionEntity("pantry_pro", "Pantry Pro", "Add 20 pantry items", "Contribution", "pantryItemsAdded", 20),
                AchievementDefinitionEntity("pantry_master", "Pantry Master", "Add 50 pantry items", "Contribution", "pantryItemsAdded", 50),
                AchievementDefinitionEntity("welcome_wagon", "Welcome Wagon", "Add your first friend", "Community", "friendsAdded", 1),
                AchievementDefinitionEntity("social_butterfly", "Social Butterfly", "Add 5 friends", "Community", "friendsAdded", 5),
                AchievementDefinitionEntity("community_connector", "Community Connector", "Add 10 friends", "Community", "friendsAdded", 10),
                AchievementDefinitionEntity("community_builder", "Community Builder", "Add 25 friends", "Community", "friendsAdded", 25),
                AchievementDefinitionEntity("community_legend", "Community Legend", "Add 50 friends", "Community", "friendsAdded", 50),
                AchievementDefinitionEntity("rising_star", "Rising Star", "Get 10 cooks on your recipes", "Community", "cooksOnMyRecipes", 10),
                AchievementDefinitionEntity("crowd_favourite", "Crowd Favourite", "Get 100 cooks on your recipes", "Community", "cooksOnMyRecipes", 100),
                AchievementDefinitionEntity("recipe_explorer", "Recipe Explorer", "Cook 10 different recipes", "Community", "uniqueRecipesCooked", 10),
                AchievementDefinitionEntity("chef_extraordinaire", "Chef Extraordinaire", "Cook 50 different recipes", "Community", "uniqueRecipesCooked", 50),
                AchievementDefinitionEntity("culinary_icon", "Culinary Icon", "Cook 100 different recipes", "Community", "uniqueRecipesCooked", 100),
            ),
        )
    }

    fun observeAchievements(defsFlow: kotlinx.coroutines.flow.Flow<List<AchievementDefinitionEntity>>) = defsFlow


    suspend fun onRecipeCooked(userId: String, recipeId: String): List<String> {
        val newlyUnlocked = mutableListOf<String>()


        pantryDao.insertCookSession(
            CookSessionEntity(id = UUID.randomUUID().toString(), userId = userId, recipeId = recipeId, completedAt = System.currentTimeMillis()),
        )

        // 1. Cook-session counter -> "recipesCooked" achievements
        val totalCooked = pantryDao.countCookSessions(userId)
        newlyUnlocked += checkAndUnlock("recipesCooked", totalCooked)

        // 2. Streak calculation
        val today = LocalDate.now(ZoneOffset.UTC)
        val existing = achievementDao.getStreak(userId)
        val newStreak = when (val lastDay = existing?.lastCookedDateEpochDay?.let { LocalDate.ofEpochDay(it) }) {
            null -> 1
            today -> existing.currentStreak // already logged today, no change
            today.minusDays(1) -> existing.currentStreak + 1
            else -> 1 // streak broken
        }
        val longest = maxOf(newStreak, existing?.longestStreak ?: 0)
        achievementDao.upsertStreak(
            StreakRecordEntity(userId, newStreak, longest, today.toEpochDay()),
        )
        newlyUnlocked += checkAndUnlock("streakDays", newStreak)

        return newlyUnlocked
    }

    suspend fun onRecipeAdded(totalRecipesForUser: Int): List<String> =
        checkAndUnlock("recipesAdded", totalRecipesForUser)

    suspend fun onPantryItemAdded(totalPantryItems: Int): List<String> =
        checkAndUnlock("pantryItemsAdded", totalPantryItems)


    suspend fun getCookedDatesInMonth(userId: String, yearMonth: java.time.YearMonth): Set<java.time.LocalDate> {
        return pantryDao.getCookSessionTimestamps(userId)
            .map { java.time.Instant.ofEpochMilli(it).atZone(java.time.ZoneOffset.UTC).toLocalDate() }
            .filter { java.time.YearMonth.from(it) == yearMonth }
            .toSet()
    }

    suspend fun getContributionDates(userId: String): Set<LocalDate> =
        recipeDao.getRecipeCreationTimestamps(userId)
            .map { Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate() }
            .toSet()

    suspend fun getContributionDatesInMonth(userId: String, yearMonth: YearMonth): Set<LocalDate> =
        getContributionDates(userId)
            .filter { YearMonth.from(it) == yearMonth }
            .toSet()

    private suspend fun checkAndUnlock(conditionType: String, counterValue: Int): List<String> {
        val unlockedNames = mutableListOf<String>()
        val defs = achievementDao.getDefinitionsFor(conditionType)
        for (def in defs) {
            val existing = achievementDao.getProgress(def.id)
            if (existing?.unlocked == true) continue
            val nowUnlocked = counterValue >= def.thresholdValue
            achievementDao.upsertProgress(
                UserAchievementProgressEntity(
                    achievementId = def.id,
                    progress = counterValue,
                    unlocked = nowUnlocked,
                    unlockedAt = if (nowUnlocked) System.currentTimeMillis() else null,
                ),
            )
            if (nowUnlocked) unlockedNames += def.name
        }
        return unlockedNames
    }
}
