package com.elachi.app.ui.discover

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil.compose.AsyncImage
import com.elachi.app.data.remote.ApiService
import com.elachi.app.data.remote.dto.DiscoverRecipeDto
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val CUISINE_OPTIONS = listOf("Italian", "Asian", "Mexican", "Indian", "Mediterranean", "American")
private val DIETARY_OPTIONS = listOf("Vegetarian", "Vegan", "Gluten-Free", "Pescatarian")

class DiscoverViewModel(private val api: ApiService) : ViewModel() {
    var tab = mutableStateOf(0)
    var search = mutableStateOf("")
    var recipes = mutableStateOf<List<DiscoverRecipeDto>>(emptyList())
    var isLoading = mutableStateOf(false)
    var errorMessage = mutableStateOf<String?>(null)
    var selectedCuisine = mutableStateOf<String?>(null)
    var selectedDietary = mutableStateOf<String?>(null)
    private var loadJob: Job? = null
    init { load() }
    fun setTab(index: Int) { tab.value = index; load() }
    fun onSearchChange(value: String) { search.value = value; load(debounce = true) }
    fun toggleCuisine(value: String) { selectedCuisine.value = if (selectedCuisine.value == value) null else value }
    fun toggleDietary(value: String) { selectedDietary.value = if (selectedDietary.value == value) null else value }
    fun clearFilters() { selectedCuisine.value = null; selectedDietary.value = null }
    fun refresh() { load() }
    private fun load(debounce: Boolean = false) {
        val mode = if (tab.value == 1) "personalised" else "global"
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            if (debounce) delay(300)
            isLoading.value = true
            errorMessage.value = null
            fetch(mode, search.value.trim().ifBlank { null })
                .onSuccess { recipes.value = it }
                .onFailure { e -> recipes.value = emptyList(); errorMessage.value = e.message }
            isLoading.value = false
        }
    }
    private suspend fun fetch(mode: String, search: String?): Result<List<DiscoverRecipeDto>> = try {
        val response = api.discoverRecipes(mode, search)
        val body = response.body()
        if (response.isSuccessful && body != null) Result.success(body) else Result.failure(Exception("Request failed."))
    } catch (e: CancellationException) { throw e } catch (e: Exception) { Result.failure(Exception("Connection failed.")) }
}

private fun List<DiscoverRecipeDto>.applyFilters(cuisine: String?, dietary: String?): List<DiscoverRecipeDto> =
    filter { r -> (cuisine == null || r.cuisine.contains(cuisine, ignoreCase = true)) && (dietary == null || r.foodType.equals(dietary, ignoreCase = true)) }

@Composable
fun DiscoverScreen(viewModel: DiscoverViewModel, onOpenRecipe: (String) -> Unit) {
    LaunchedEffect(Unit) { viewModel.refresh() }
    val tab by viewModel.tab
    val search by viewModel.search
    val recipes by viewModel.recipes
    val isLoading by viewModel.isLoading
    val errorMessage by viewModel.errorMessage
    val selectedCuisine by viewModel.selectedCuisine
    val selectedDietary by viewModel.selectedDietary
    val filtered = remember(recipes, selectedCuisine, selectedDietary) { recipes.applyFilters(selectedCuisine, selectedDietary) }
    val filtersActive = selectedCuisine != null || selectedDietary != null

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0.dp, 0.dp, 0.dp, 0.dp),
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize().background(MaterialTheme.colorScheme.background)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = search,
                        onValueChange = { viewModel.onSearchChange(it) },
                        placeholder = { Text("Search recipes, ingredient...") },
                        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        )
                    )
                    Spacer(Modifier.width(8.dp))
                    IconButton(
                        onClick = { viewModel.refresh() },
                        colors = IconButtonDefaults.iconButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Filled.Refresh, contentDescription = "Refresh")
                    }
                }
                Spacer(Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    listOf("Global", "Personalised").forEachIndexed { index, label ->
                        val isSelected = tab == index
                        Column(
                            modifier = Modifier.clickable { viewModel.setTab(index) }.padding(vertical = 4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text(
                                label,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal),
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            if (isSelected) {
                                Box(modifier = Modifier.padding(top = 4.dp).width(40.dp).height(2.dp).background(MaterialTheme.colorScheme.primary))
                            }
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(CUISINE_OPTIONS) { option ->
                        FilterChip(
                            selected = selectedCuisine == option,
                            onClick = { viewModel.toggleCuisine(option) },
                            label = { Text(option) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
                Spacer(Modifier.height(4.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(DIETARY_OPTIONS) { option ->
                        FilterChip(
                            selected = selectedDietary == option,
                            onClick = { viewModel.toggleDietary(option) },
                            label = { Text(option) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outline)
            when {
                isLoading && recipes.isEmpty() -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                errorMessage != null -> Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) { Text(errorMessage ?: "", color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center) }
                filtered.isEmpty() -> Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            when {
                                filtersActive -> "No recipes match your filters."
                                tab == 1 -> "No matches yet. Update your interests in profile."
                                else -> "No public recipes yet."
                            },
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (filtersActive) {
                            TextButton(onClick = { viewModel.clearFilters() }) { Text("Clear filters", color = MaterialTheme.colorScheme.primary) }
                        }
                    }
                }
                else -> LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(filtered, key = { it.id }) { recipe -> DiscoverCard(recipe, onClick = { onOpenRecipe(recipe.id) }) }
                }
            }
        }
    }
}

@Composable
private fun DiscoverCard(recipe: DiscoverRecipeDto, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(28.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Filled.Person, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                }
                Spacer(Modifier.width(8.dp))
                Text(recipe.creatorName, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.W600, color = MaterialTheme.colorScheme.onSurface)
            }
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(recipe.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    Spacer(Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Surface(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), shape = RoundedCornerShape(4.dp)) {
                            Text(recipe.difficulty, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                        }
                        if (recipe.cuisine.isNotBlank()) {
                            Surface(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), shape = RoundedCornerShape(4.dp)) {
                                Text(recipe.cuisine, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                    Spacer(Modifier.height(6.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Star, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color(0xFFF5A623))
                        Text(" ${"%.1f".format(recipe.avgRating)} (${recipe.ratingCount})", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("  \u23F1\uFE0F ${recipe.cookTimeMinutes} min", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Box(modifier = Modifier.size(80.dp).clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.surfaceVariant)) {
                    if (recipe.imageUrl != null) {
                        AsyncImage(model = recipe.imageUrl, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Favorite, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
                Text(" ${recipe.timesCooked}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
