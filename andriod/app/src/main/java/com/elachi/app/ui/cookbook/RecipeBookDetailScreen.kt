package com.elachi.app.ui.cookbook

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.elachi.app.data.local.entities.RecipeEntity
import com.elachi.app.ui.common.ElachiTopBar

@Composable
fun RecipeBookDetailScreen(
    viewModel: RecipeBookDetailViewModel,
    onBack: () -> Unit,
    onAddRecipe: () -> Unit,
    onOpenRecipe: (String) -> Unit,
) {
    val book by viewModel.book.collectAsState()
    val recipes by viewModel.recipes.collectAsState()

    Scaffold(
        topBar = {
            ElachiTopBar(
                title = book?.name ?: "Recipe Book",
                onBackClick = onBack,
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddRecipe) {
                Icon(Icons.Filled.Add, contentDescription = "Add recipe")
            }
        },
    ) { padding ->
        when {
            book == null -> {
                Box(
                    modifier = Modifier.padding(padding).fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }

            recipes.isEmpty() -> {
                Column(
                    modifier = Modifier.padding(padding).fillMaxSize().padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Icon(
                        Icons.Filled.Restaurant,
                        contentDescription = null,
                        modifier = Modifier.size(52.dp),
                    )
                    Spacer(Modifier.height(12.dp))
                    Text("No recipes in this book yet", style = MaterialTheme.typography.titleMedium)
                    Text("Tap + to add your first recipe.")
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier.padding(padding).fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    book?.description?.takeIf { it.isNotBlank() }?.let { description ->
                        item { Text(description) }
                    }

                    items(recipes, key = { it.id }) { recipe ->
                        RecipeRow(recipe = recipe, onClick = { onOpenRecipe(recipe.id) })
                    }

                    item { Spacer(Modifier.height(72.dp)) }
                }
            }
        }
    }
}

@Composable
private fun RecipeRow(
    recipe: RecipeEntity,
    onClick: () -> Unit,
) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (recipe.imageUrl != null) {
                AsyncImage(
                    model = recipe.imageUrl,
                    contentDescription = recipe.title,
                    modifier = Modifier.size(76.dp),
                    contentScale = ContentScale.Crop,
                )
                Spacer(Modifier.width(12.dp))
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    recipe.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "${recipe.cuisine} · ${recipe.cookTimeMinutes} min · ${recipe.difficulty}",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}