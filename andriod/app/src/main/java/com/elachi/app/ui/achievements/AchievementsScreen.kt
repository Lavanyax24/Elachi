package com.elachi.app.ui.achievements

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmarks
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Kitchen
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elachi.app.data.local.dao.AchievementDao
import com.elachi.app.data.local.entities.AchievementDefinitionEntity
import com.elachi.app.data.local.entities.StreakRecordEntity
import com.elachi.app.data.local.entities.UserAchievementProgressEntity
import com.elachi.app.ui.common.ElachiTopBar
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

// This is the AchievementsViewModel class, it handles the data for the AchievementsScreen.
// It works by observing the data from the database and updating the UI accordingly.

class AchievementsViewModel(
    private val userId: String,
    private val achievementDao: AchievementDao,
) : ViewModel() {
    val definitions: StateFlow<List<AchievementDefinitionEntity>> =
        achievementDao.observeDefinitions().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val progress: StateFlow<List<UserAchievementProgressEntity>> =
        achievementDao.observeProgress().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val streak: StateFlow<StreakRecordEntity?> =
        achievementDao.observeStreak(userId).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
}

private fun iconFor(achievementId: String): ImageVector = when (achievementId) {
    "first_cook", "home_cook", "kitchen_regular", "master_cook", "cooking_legend" -> Icons.Filled.Restaurant
    "streak_spark", "streak_starter", "streak_champion" -> Icons.Filled.LocalFireDepartment
    "first_recipe", "recipe_collector", "recipe_hoarder", "recipe_archivist" -> Icons.Filled.Bookmarks
    "community_star", "rising_star", "crowd_favourite" -> Icons.Filled.Star
    "fork_master" -> Icons.Filled.ContentCopy
    "pantry_starter", "pantry_pro", "pantry_master" -> Icons.Filled.Kitchen
    "welcome_wagon", "social_butterfly", "community_connector", "community_builder", "community_legend" -> Icons.Filled.Group
    else -> Icons.Filled.EmojiEvents
}

// This is the AchievementsScreen composable function, it handles the UI for the AchievementsScreen.
@Composable
fun AchievementsScreen(viewModel: AchievementsViewModel, onBack: () -> Unit) {
    val definitions by viewModel.definitions.collectAsState()
    val progress by viewModel.progress.collectAsState()
    val streak by viewModel.streak.collectAsState()
    var tab by remember { mutableIntStateOf(0) }
    val categories = listOf("Cooking", "Contribution", "Community")
    val progressById = remember(progress) { progress.associateBy { it.achievementId } }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { ElachiTopBar(title = "Achievements", onBackClick = onBack) },
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            streak?.let {
                Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                    StreakStat("${it.currentStreak}", "Day Streak")
                    StreakStat("${it.longestStreak}", "Longest Streak")
                }
            }
            TabRow(
                selectedTabIndex = tab, 
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                categories.forEachIndexed { i, cat ->
                    Tab(selected = tab == i, onClick = { tab = i }, text = { Text(cat) })
                }
            }
            val categoryDefs = definitions.filter { it.category == categories[tab] }
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(categoryDefs, key = { it.id }) { def ->
                    BadgeCard(def = def, progressValue = progressById[def.id]?.progress ?: 0, unlocked = progressById[def.id]?.unlocked == true)
                }
            }
        }
    }
}

@Composable
private fun BadgeCard(def: AchievementDefinitionEntity, progressValue: Int, unlocked: Boolean) {
    val threshold = def.thresholdValue.coerceAtLeast(1)
    val fraction = (progressValue.toFloat() / threshold).coerceIn(0f, 1f)
    Card(
        modifier = Modifier.fillMaxWidth().heightIn(min = 220.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (unlocked) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant
        ),
        border = if (unlocked) BorderStroke(1.2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)) else BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)),
    ) {
        Column(modifier = Modifier.padding(14.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier.size(48.dp).clip(CircleShape).background(if (unlocked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(iconFor(def.id), contentDescription = null, tint = if (unlocked) Color.White else MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(26.dp))
            }
            Spacer(Modifier.height(10.dp))
            Text(def.name, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurface)
            Spacer(Modifier.height(4.dp))
            Text(def.description, style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.weight(1f))
            LinearProgressIndicator(
                progress = { fraction },
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f),
            )
            Spacer(Modifier.height(6.dp))
            if (unlocked) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Unlocked", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
            } else {
                Text("${progressValue.coerceAtMost(threshold)}/${def.thresholdValue}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun StreakStat(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
    }
}
