package com.elachi.app.ui.profile

import androidx.compose.foundation.background
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

class ProfileViewModel(
    userId: String,
    private val profileRepository: ProfileRepository,
    recipeRepository: RecipeRepository,
    achievementDao: com.elachi.app.data.local.dao.AchievementDao,
) : ViewModel() {
    var profile = mutableStateOf<UserProfileDto?>(null)
    var isLoading = mutableStateOf(true)

    val myRecipes: StateFlow<List<RecipeEntity>> = recipeRepository.observeAllRecipes(userId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val streak: StateFlow<StreakRecordEntity?> = achievementDao.observeStreak(userId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    init {
        viewModelScope.launch {
            profileRepository.getMyProfile().onSuccess { profile.value = it }
            isLoading.value = false
        }
    }
}


@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProfileScreen(viewModel: ProfileViewModel, onEdit: () -> Unit) {
    val profile by viewModel.profile
    val isLoading by viewModel.isLoading
    val myRecipes by viewModel.myRecipes.collectAsState()
    val streak by viewModel.streak.collectAsState()

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center,
                ) {
                    if (profile?.avatarUrl != null) {
                        AsyncImage(
                            model = profile?.avatarUrl,
                            contentDescription = "Avatar",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize(),
                        )
                    } else {
                        Icon(Icons.Filled.Person, contentDescription = null, modifier = Modifier.size(44.dp))
                    }
                }
                Spacer(Modifier.height(10.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        profile?.displayName ?: "",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(Modifier.width(6.dp))
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Filled.Edit, contentDescription = "Edit profile")
                    }
                }

                profile?.friendCode?.let {
                    AssistChip(onClick = {}, label = { Text("Friend code: $it") })
                }
                profile?.bio?.takeIf { it.isNotBlank() }?.let {
                    Spacer(Modifier.height(6.dp))
                    Text(
                        it,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    )
                }
            }
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                StatColumn(myRecipes.size.toString(), "Recipes")
                StatColumn("${streak?.currentStreak ?: 0}", "Streak")
                // "Friends" stat removed no social graph in Part 2.
            }
        }

        if (!profile?.cookingInterests.isNullOrEmpty()) {
            item { Text("Cooking Interests", style = MaterialTheme.typography.titleMedium) }
            item {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    profile!!.cookingInterests.forEach { interest ->
                        AssistChip(onClick = {}, label = { Text(interest) })
                    }
                }
            }
        }

        item { Text("My Recipes (${myRecipes.size})", style = MaterialTheme.typography.titleMedium) }
        if (myRecipes.isEmpty()) {
            item {
                Text("You haven't saved any recipes yet.", style = MaterialTheme.typography.bodyMedium)
            }
        }
        items(myRecipes) { recipe ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(recipe.title, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                        AssistChip(
                            onClick = {},
                            label = { Text(if (recipe.isPrivate) "Private" else "Public") },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = if (recipe.isPrivate) Color(0xFFF0EDE9) else Color(0xFFE8F0E3),
                            ),
                        )
                    }
                    Text(
                        "${recipe.cuisine} \u00B7 ${recipe.cookTimeMinutes} min",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }
    }
}

@Composable
private fun StatColumn(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.bodyMedium)
    }
}