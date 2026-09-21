package com.elachi.app.ui.streak

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmarks
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elachi.app.data.local.dao.AchievementDao
import com.elachi.app.data.local.entities.StreakRecordEntity
import com.elachi.app.data.repository.AchievementRepository
import com.elachi.app.ui.common.ElachiTopBar
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneOffset
import java.time.format.TextStyle
import java.util.Locale

class StreakCalendarViewModel(
    private val userId: String,
    private val achievementRepository: AchievementRepository,
    private val achievementDao: AchievementDao,
) : ViewModel() {
    var currentMonth = mutableStateOf(YearMonth.now())
    var cookedDates = mutableStateOf<Set<LocalDate>>(emptySet())
    var contributionDates = mutableStateOf<Set<LocalDate>>(emptySet())
    var allContributionDates = mutableStateOf<Set<LocalDate>>(emptySet())
    val streak: StateFlow<StreakRecordEntity?> = achievementDao.observeStreak(userId).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
    init { loadMonth() }
    fun changeMonth(delta: Long) { currentMonth.value = currentMonth.value.plusMonths(delta); loadMonth() }
    private fun loadMonth() {
        viewModelScope.launch {
            cookedDates.value = achievementRepository.getCookedDatesInMonth(userId, currentMonth.value)
            contributionDates.value = achievementRepository.getContributionDatesInMonth(userId, currentMonth.value)
            allContributionDates.value = achievementRepository.getContributionDates(userId)
        }
    }
}

private val CookingOrange = Color(0xFFE07A2D)
private val ContributionGreen = Color(0xFF2E7D32)
private enum class StreakCalendarTab(val title: String) { COOKING("Cooking"), CONTRIBUTION("Contribution") }
private data class CalculatedStreak(val current: Int = 0, val longest: Int = 0)

private fun calculateStreak(dates: Set<LocalDate>): CalculatedStreak {
    if (dates.isEmpty()) return CalculatedStreak()
    val sortedDates = dates.sorted()
    var longest = 1
    var running = 1
    for (index in 1 until sortedDates.size) {
        running = if (sortedDates[index] == sortedDates[index - 1].plusDays(1)) running + 1 else 1
        longest = maxOf(longest, running)
    }
    val today = LocalDate.now(ZoneOffset.UTC)
    val latest = sortedDates.last()
    if (latest != today && latest != today.minusDays(1)) return CalculatedStreak(current = 0, longest = longest)
    var current = 1
    var cursor = latest
    while (dates.contains(cursor.minusDays(1))) { current++; cursor = cursor.minusDays(1) }
    return CalculatedStreak(current = current, longest = longest)
}

private fun effectiveCurrentStreak(streak: StreakRecordEntity?): Int {
    val lastDay = streak?.lastCookedDateEpochDay ?: return 0
    val today = LocalDate.now(ZoneOffset.UTC).toEpochDay()
    return if (lastDay >= today - 1) streak.currentStreak else 0
}

@Composable
fun StreakCalendarScreen(viewModel: StreakCalendarViewModel, onBack: () -> Unit) {
    val month by viewModel.currentMonth
    val cookedDates by viewModel.cookedDates
    val contributionDates by viewModel.contributionDates
    val allContributionDates by viewModel.allContributionDates
    val streak by viewModel.streak.collectAsState()
    var selectedTab by rememberSaveable { mutableStateOf(StreakCalendarTab.COOKING) }
    val cookingStreak = effectiveCurrentStreak(streak)
    val contributionSummary = remember(allContributionDates) { calculateStreak(allContributionDates) }
    val contributionStreak = contributionSummary.current
    val bestStreak = maxOf(streak?.longestStreak ?: 0, contributionSummary.longest)
    val selectedDates = if (selectedTab == StreakCalendarTab.COOKING) cookedDates else contributionDates
    val selectedColour = if (selectedTab == StreakCalendarTab.COOKING) CookingOrange else ContributionGreen

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { ElachiTopBar(title = "Streak Calendar", onBackClick = onBack) },
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp).fillMaxSize()) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StreakStatCard(value = cookingStreak, label = "Cooking Streak", icon = Icons.Filled.LocalFireDepartment, tint = CookingOrange, modifier = Modifier.weight(1f))
                StreakStatCard(value = contributionStreak, label = "Contribution Streak", icon = Icons.Filled.Bookmarks, tint = ContributionGreen, modifier = Modifier.weight(1f))
                StreakStatCard(value = bestStreak, label = "Best Streak", icon = Icons.Filled.EmojiEvents, tint = Color(0xFFF5A623), modifier = Modifier.weight(1f))
            }
            Spacer(Modifier.height(16.dp))
            TabRow(
                selectedTabIndex = selectedTab.ordinal, 
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = selectedColour
            ) {
                StreakCalendarTab.entries.forEach { tab ->
                    Tab(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        text = { Text(tab.title) },
                        icon = { Icon(if (tab == StreakCalendarTab.COOKING) Icons.Filled.LocalFireDepartment else Icons.Filled.Bookmarks, contentDescription = null) }
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { viewModel.changeMonth(-1) }) { Icon(Icons.Filled.ChevronLeft, contentDescription = "Prev", tint = MaterialTheme.colorScheme.onSurface) }
                Text("${month.month.getDisplayName(TextStyle.FULL, Locale.getDefault())} ${month.year}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                IconButton(onClick = { viewModel.changeMonth(1) }) { Icon(Icons.Filled.ChevronRight, contentDescription = "Next", tint = MaterialTheme.colorScheme.onSurface) }
            }
            Spacer(Modifier.height(16.dp))
            val firstDayOffset = month.atDay(1).dayOfWeek.value % 7
            val daysInMonth = month.lengthOfMonth()
            val today = LocalDate.now(ZoneOffset.UTC)
            LazyVerticalGrid(columns = GridCells.Fixed(7), modifier = Modifier.fillMaxWidth().weight(1f)) {
                items(firstDayOffset) { Box(modifier = Modifier.aspectRatio(1f)) }
                items(daysInMonth) { index ->
                    val date = month.atDay(index + 1)
                    val wasActive = selectedDates.contains(date)
                    val isToday = date == today
                    val shape = RoundedCornerShape(6.dp)
                    Box(
                        modifier = Modifier.aspectRatio(1f).padding(2.dp)
                            .background(if (wasActive) selectedColour.copy(alpha = 0.85f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f), shape = shape)
                            .then(if (isToday) Modifier.border(1.5.dp, MaterialTheme.colorScheme.primary, shape) else Modifier),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("${index + 1}", color = if (wasActive) Color.White else MaterialTheme.colorScheme.onSurface, fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal)
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(12.dp).background(selectedColour, RoundedCornerShape(3.dp)))
                Spacer(Modifier.width(6.dp))
                Text(if (selectedTab == StreakCalendarTab.COOKING) "Cooked something" else "Added a recipe", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.weight(1f))
                Text("${selectedDates.size} days this month", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun StreakStatCard(value: Int, label: String, icon: ImageVector, tint: Color, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)),
    ) {
        Column(modifier = Modifier.padding(vertical = 12.dp, horizontal = 6.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(24.dp))
            Spacer(Modifier.height(4.dp))
            Text("$value", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = tint)
            Text(label, style = MaterialTheme.typography.labelSmall, textAlign = TextAlign.Center, minLines = 2, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
