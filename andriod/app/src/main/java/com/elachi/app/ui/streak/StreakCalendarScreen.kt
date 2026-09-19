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
import com.elachi.app.ui.theme.ElachiAccent
import com.elachi.app.ui.theme.ElachiCream
import com.elachi.app.ui.theme.ElachiGreen
import com.elachi.app.ui.theme.ElachiSurface
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

    val streak: StateFlow<StreakRecordEntity?> = achievementDao.observeStreak(userId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    init {
        loadMonth()
    }

    fun changeMonth(delta: Long) {
        currentMonth.value = currentMonth.value.plusMonths(delta)
        loadMonth()
    }

    private fun loadMonth() {
        viewModelScope.launch {
            cookedDates.value = achievementRepository.getCookedDatesInMonth(userId, currentMonth.value)
        }
    }
}

private val CookedGreen = Color(0xFF2E7D32).copy(alpha = 0.75f)

/**
 * The stored currentStreak only changes when a recipe is cooked, so if the user
 * has skipped a day it would still show the old number. A streak only counts
 * if the last cook was today or yesterday (same UTC-day logic as the repository).
 */
private fun effectiveCurrentStreak(streak: StreakRecordEntity?): Int {
    val lastDay = streak?.lastCookedDateEpochDay ?: return 0
    val today = LocalDate.now(ZoneOffset.UTC).toEpochDay()
    return if (lastDay >= today - 1) streak.currentStreak else 0
}

@Composable
fun StreakCalendarScreen(viewModel: StreakCalendarViewModel, onBack: () -> Unit) {
    val month by viewModel.currentMonth
    val cookedDates by viewModel.cookedDates
    val streak by viewModel.streak.collectAsState()

    val cookingStreak = effectiveCurrentStreak(streak)
    // Placeholder: contribution streak mirrors the cooking streak for now
    val contributionStreak = cookingStreak
    val bestStreak = maxOf(streak?.longestStreak ?: 0, cookingStreak)

    Scaffold(
        containerColor = ElachiCream,
        topBar = { ElachiTopBar(title = "Streak Calendar", onBackClick = onBack) },
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp).fillMaxSize()) {
            // Three stat types
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                StreakStatCard(
                    value = cookingStreak,
                    label = "Cooking Streak",
                    icon = Icons.Filled.LocalFireDepartment,
                    tint = ElachiAccent,
                    modifier = Modifier.weight(1f),
                )
                StreakStatCard(
                    value = contributionStreak,
                    label = "Contribution Streak",
                    icon = Icons.Filled.Bookmarks,
                    tint = ElachiGreen,
                    modifier = Modifier.weight(1f),
                )
                StreakStatCard(
                    value = bestStreak,
                    label = "Best Streak",
                    icon = Icons.Filled.EmojiEvents,
                    tint = Color(0xFFF5A623),
                    modifier = Modifier.weight(1f),
                )
            }
            Spacer(Modifier.height(16.dp))

            // Month switcher
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = { viewModel.changeMonth(-1) }) {
                    Icon(Icons.Filled.ChevronLeft, contentDescription = "Previous month")
                }
                Text(
                    "${month.month.getDisplayName(TextStyle.FULL, Locale.getDefault())} ${month.year}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )
                IconButton(onClick = { viewModel.changeMonth(1) }) {
                    Icon(Icons.Filled.ChevronRight, contentDescription = "Next month")
                }
            }
            Spacer(Modifier.height(16.dp))

            val firstDayOffset = month.atDay(1).dayOfWeek.value % 7 // Sunday-first grid
            val daysInMonth = month.lengthOfMonth()
            val today = LocalDate.now()

            LazyVerticalGrid(columns = GridCells.Fixed(7), modifier = Modifier.fillMaxWidth()) {
                items(firstDayOffset) { Box(modifier = Modifier.aspectRatio(1f)) }
                items(daysInMonth) { index ->
                    val date = month.atDay(index + 1)
                    val wasCooked = cookedDates.contains(date)
                    val isToday = date == today
                    val shape = RoundedCornerShape(6.dp)
                    Box(
                        modifier = Modifier
                            .aspectRatio(1f)
                            .padding(2.dp)
                            .background(
                                if (wasCooked) CookedGreen else Color.LightGray.copy(alpha = 0.15f),
                                shape = shape,
                            )
                            .then(if (isToday) Modifier.border(1.5.dp, ElachiGreen, shape) else Modifier),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            "${index + 1}",
                            color = if (wasCooked) Color.White else Color(0xFF45483E),
                            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(12.dp).background(CookedGreen, RoundedCornerShape(3.dp)))
                Spacer(Modifier.width(6.dp))
                Text("Cooked something that day", style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.weight(1f))
                Text(
                    "${cookedDates.size} day${if (cookedDates.size == 1) "" else "s"} this month",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                )
            }
        }
    }
}

@Composable
private fun StreakStatCard(
    value: Int,
    label: String,
    icon: ImageVector,
    tint: Color,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = ElachiSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 6.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(24.dp))
            Spacer(Modifier.height(4.dp))
            Text(
                "$value",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = tint,
            )
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                textAlign = TextAlign.Center,
                minLines = 2, // keeps all three cards the same height
            )
        }
    }
}