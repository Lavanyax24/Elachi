package com.elachi.app.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil.compose.AsyncImage
import com.elachi.app.data.local.entities.RecipeEntity
import com.elachi.app.data.local.entities.StreakRecordEntity
import com.elachi.app.data.remote.dto.UserProfileDto
import com.elachi.app.data.repository.ProfileRepository
import com.elachi.app.data.repository.RecipeRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// This is the profile screen, it displays the users preferences and allows them to edit them

class ProfileViewModel(
    userId: String,
    private val profileRepository: ProfileRepository,
    recipeRepository: RecipeRepository,
    achievementDao: com.elachi.app.data.local.dao.AchievementDao,
) : ViewModel() {
    var profile = mutableStateOf<UserProfileDto?>(null); var isLoading = mutableStateOf(true)
    val myRecipes: StateFlow<List<RecipeEntity>> = recipeRepository.observeAllRecipes(userId).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val streak: StateFlow<StreakRecordEntity?> = achievementDao.observeStreak(userId).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    init { viewModelScope.launch { profileRepository.getMyProfile().onSuccess { profile.value = it }; isLoading.value = false } }
    fun refreshProfile() { viewModelScope.launch { profileRepository.getMyProfile().onSuccess { profile.value = it } } }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProfileScreen(viewModel: ProfileViewModel, onEdit: () -> Unit, onRecipeClick: (String) -> Unit) {
    LaunchedEffect(Unit) { viewModel.refreshProfile() }
    val profile by viewModel.profile; val isLoading by viewModel.isLoading
    val myRecipes by viewModel.myRecipes.collectAsState(); val streak by viewModel.streak.collectAsState()

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        return
    }

    LazyColumn(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier.size(90.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant)
                        .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    if (profile?.avatarUrl != null) AsyncImage(model = profile?.avatarUrl, contentDescription = "Avatar", contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                    else Icon(Icons.Filled.Person, contentDescription = null, modifier = Modifier.size(44.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Spacer(Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(profile?.displayName ?: "Chef", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    Spacer(Modifier.width(6.dp))
                    IconButton(onClick = onEdit) { Icon(Icons.Filled.Edit, contentDescription = null, tint = MaterialTheme.colorScheme.primary) }
                }
                profile?.friendCode?.let { AssistChip(onClick = {}, label = { Text("Friend code: $it") }, colors = AssistChipDefaults.assistChipColors(labelColor = MaterialTheme.colorScheme.onSurfaceVariant)) }
                profile?.bio?.takeIf { it.isNotBlank() }?.let { Text(it, style = MaterialTheme.typography.bodyMedium, textAlign = androidx.compose.ui.text.style.TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 6.dp)) }
            }
        }
        item {
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary), shape = RoundedCornerShape(16.dp)) {
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                    StatColumn(myRecipes.size.toString(), "Recipes", textColor = Color.White)
                    StatColumn("${streak?.currentStreak ?: 0}", "Streak", textColor = Color.White)
                }
            }
        }
        if (!profile?.cookingInterests.isNullOrEmpty()) {
            item { Text("Cooking Interests", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface) }
            item {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    profile!!.cookingInterests.forEach { AssistChip(onClick = {}, label = { Text(it) }, colors = AssistChipDefaults.assistChipColors(labelColor = MaterialTheme.colorScheme.onSurface)) }
                }
            }
        }
        if (!profile?.dietaryRestrictions.isNullOrEmpty()) {
            item { Text("Dietary Restrictions", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface) }
            item {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    profile!!.dietaryRestrictions.forEach { AssistChip(onClick = {}, label = { Text(it) }, colors = AssistChipDefaults.assistChipColors(labelColor = MaterialTheme.colorScheme.onSurface)) }
                }
            }
        }
        item { Text("My Recipes (${myRecipes.size})", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface) }
        items(myRecipes) { recipe ->
            Card(modifier = Modifier.fillMaxWidth().clickable { onRecipeClick(recipe.id) }, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(recipe.title, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
                        Surface(color = (if (recipe.isPrivate) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)), shape = RoundedCornerShape(8.dp)) {
                            Text(if (recipe.isPrivate) "Private" else "Public", style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), color = if (recipe.isPrivate) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.primary)
                        }
                    }
                    Text("${recipe.cuisine} \u00B7 ${recipe.cookTimeMinutes} min", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun StatColumn(value: String, label: String, textColor: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = textColor)
        Text(label, style = MaterialTheme.typography.bodyMedium, color = textColor.copy(alpha = 0.8f))
    }
}
