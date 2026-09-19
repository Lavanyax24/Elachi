package com.elachi.app.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.elachi.app.data.local.entities.RecipeEntity
import com.elachi.app.ui.common.ElachiTopBar
import com.elachi.app.ui.theme.ElachiGreen
import com.elachi.app.ui.theme.ElachiGreenLight
import com.elachi.app.ui.theme.ElachiAccent
import com.elachi.app.ui.theme.ElachiCream
import com.elachi.app.ui.theme.ElachiTextPrimary
import com.elachi.app.ui.theme.ElachiTextSecondary


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onOpenRecipe: (String) -> Unit,
    onOpenAiChef: () -> Unit,
    onOpenAchievements: () -> Unit = {},
    onOpenSettings: () -> Unit = {},
    onOpenDrawer: () -> Unit = {},
) {
    val matches by viewModel.matches.collectAsState()
    val streak by viewModel.streak.collectAsState()
    val allRecipes by viewModel.allRecipes.collectAsState()
    val pantryHealthPercent by viewModel.pantryHealthPercent.collectAsState()

    Scaffold(
        topBar = {
            ElachiTopBar(
                title = "Elachi",
                onMenuClick = onOpenDrawer,
                onSettingsClick = onOpenSettings,
                showStreakChip = true,
                streakCount = streak?.currentStreak ?: 0,
                actions = {
                    IconButton(onClick = onOpenAchievements) { Icon(Icons.Filled.EmojiEvents, contentDescription = "Achievements") }
                }
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(ElachiCream)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 24.dp),
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            // Pantry Health + Your Momentum row
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Surface(
                    modifier = Modifier.weight(1f).height(167.dp),
                    color = Color.White,
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.6.dp, Color(0xFFE5E2DD)),
                ) {
                    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Pantry Health", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.W600, fontSize = 13.sp), color = Color(0xFF45483E))
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(
                                progress = { pantryHealthPercent / 100f },
                                modifier = Modifier.size(80.dp),
                                color = ElachiGreen,
                                strokeWidth = 6.6.dp,
                                trackColor = Color(0xFFE5E2DD),
                            )
                            Text("$pantryHealthPercent%", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = ElachiGreen)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Pantry $pantryHealthPercent% stocked", style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp), color = ElachiTextSecondary)
                    }
                }

                Surface(
                    modifier = Modifier.weight(1f).height(167.dp),
                    color = Color.White,
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.6.dp, Color(0xFFE5E2DD)),
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.SpaceBetween) {
                        Text("Your Momentum", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.W600, fontSize = 13.sp), color = Color(0xFF45483E))
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("\uD83D\uDD25", fontSize = 20.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("${streak?.currentStreak ?: 0} day streak!", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.W600), color = ElachiTextPrimary)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("\uD83C\uDFC6", fontSize = 18.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Longest: ${streak?.longestStreak ?: 0} days", style = MaterialTheme.typography.bodySmall, color = Color(0xFF45483E))
                            }
                        }
                        Column {
                            val progressToNextBadge = ((streak?.currentStreak ?: 0) % 15) / 15f
                            LinearProgressIndicator(
                                progress = { progressToNextBadge },
                                modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
                                color = ElachiAccent,
                                trackColor = Color(0xFFE5E2DD),
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // What You Can Cook Now  top pantry match
            val topMatch = matches.firstOrNull()
            if (topMatch != null) {
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Text("What You Can Cook Now", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 18.sp), color = ElachiGreen)
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = ElachiGreen.copy(alpha = 0.05f),
                        border = androidx.compose.foundation.BorderStroke(1.6.dp, ElachiGreen.copy(alpha = 0.12f)),
                        shape = RoundedCornerShape(16.dp),
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Surface(color = Color(0xFF2E7D32).copy(alpha = 0.1f), shape = RoundedCornerShape(4.dp)) {
                                        Text("${topMatch.matchPercent}% Match", modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = Color(0xFF2E7D32))
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(topMatch.recipe.title, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = ElachiTextPrimary)
                                }
                                Box(modifier = Modifier.size(70.dp).clip(RoundedCornerShape(12.dp)).background(Color(0xFFF0EDE9))) {
                                    if (topMatch.recipe.imageUrl != null) {
                                        AsyncImage(model = topMatch.recipe.imageUrl, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                                    }
                                }
                            }
                            Text(
                                "${topMatch.recipe.cookTimeMinutes} min \u00B7 ${topMatch.recipe.difficulty}",
                                modifier = Modifier.padding(vertical = 8.dp),
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF45483E),
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(
                                    onClick = { onOpenRecipe(topMatch.recipe.id) },
                                    modifier = Modifier.weight(1f).height(40.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.6.dp, ElachiGreen),
                                ) {
                                    Text("View Recipe", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.W600), color = ElachiGreen)
                                }
                                Button(
                                    onClick = { onOpenRecipe(topMatch.recipe.id) },
                                    modifier = Modifier.weight(1f).height(40.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = ElachiGreenLight),
                                ) {
                                    Text("Start Cooking", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.W600), color = Color.White)
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            // For You remaining matches beyond the top one
            val forYou = matches.drop(1)
            if (forYou.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("For You", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 18.sp), color = ElachiGreen)
                }
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    forYou.forEach { match ->
                        ForYouRecipeCard(match.recipe, onClick = { onOpenRecipe(match.recipe.id) })
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Recent Activity
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text("Recent Activity", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 18.sp), color = ElachiGreen)
                Spacer(modifier = Modifier.height(10.dp))

                var selectedTab by remember { mutableIntStateOf(0) }
                Box(modifier = Modifier.fillMaxWidth()) {
                    HorizontalDivider(modifier = Modifier.align(Alignment.BottomCenter), thickness = 1.6.dp, color = Color(0xFFE5E2DD))
                    Row(modifier = Modifier.fillMaxWidth()) {
                        listOf("Cooked", "Viewed", "Saved").forEachIndexed { index, label ->
                            val isSelected = selectedTab == index
                            Column(
                                modifier = Modifier.weight(1f).clickable { selectedTab = index }.padding(vertical = 8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                Text(label, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal), color = if (isSelected) ElachiGreen else ElachiTextSecondary)
                                if (isSelected) {
                                    Box(modifier = Modifier.padding(top = 8.dp).fillMaxWidth(0.5f).height(2.4.dp).background(ElachiGreen))
                                } else {
                                    Spacer(modifier = Modifier.height(10.4.dp))
                                }
                            }
                        }
                    }
                }

                val activityList = when (selectedTab) {
                    0 -> allRecipes.filter { it.timesCooked > 0 }
                    2 -> allRecipes.filter { it.isFavourite }
                    else -> allRecipes.sortedByDescending { it.createdAt }.take(10)
                }

                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    if (activityList.isEmpty()) {
                        Text("Nothing here yet.", style = MaterialTheme.typography.bodyMedium, color = ElachiTextSecondary, modifier = Modifier.padding(vertical = 16.dp))
                    }
                    activityList.take(10).forEach { recipe ->
                        RecentActivityCard(recipe, onClick = { onOpenRecipe(recipe.id) })
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                OutlinedButton(onClick = onOpenAiChef, modifier = Modifier.fillMaxWidth()) {
                    Text("Ask the AI Chef Assistant")
                }
            }
        }
    }
}

@Composable
private fun ForYouRecipeCard(recipe: RecipeEntity, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.width(160.dp).height(175.dp).clickable(onClick = onClick),
        color = Color.White,
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.6.dp, Color(0xFFE5E2DD)),
    ) {
        Column {
            Box(modifier = Modifier.fillMaxWidth().height(110.dp).background(Color(0xFFF0EDE9))) {
                if (recipe.imageUrl != null) {
                    AsyncImage(model = recipe.imageUrl, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                }
            }
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
                Text(recipe.title, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.W600), color = ElachiTextPrimary, maxLines = 1)
                Text("${recipe.cookTimeMinutes} min", style = MaterialTheme.typography.labelSmall, color = ElachiTextSecondary)
            }
        }
    }
}

@Composable
private fun RecentActivityCard(recipe: RecipeEntity, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.width(140.dp).height(147.dp).clickable(onClick = onClick),
        color = Color.White,
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.6.dp, Color(0xFFE5E2DD)),
    ) {
        Column {
            Box(modifier = Modifier.fillMaxWidth().height(80.dp).background(Color(0xFFF0EDE9))) {
                if (recipe.imageUrl != null) {
                    AsyncImage(model = recipe.imageUrl, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                }
            }
            Column(modifier = Modifier.padding(8.dp)) {
                Text(recipe.title, style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.W600), color = ElachiTextPrimary, maxLines = 1)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Re-cook \u2192",
                    style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace, fontSize = 10.sp),
                    color = ElachiGreen,
                )
            }
        }
    }
}