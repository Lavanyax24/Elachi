package com.elachi.app.ui.recipe

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.elachi.app.ui.common.ElachiTopBar
import com.elachi.app.util.RecipePdfExporter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeDetailScreen(
    viewModel: RecipeDetailViewModel,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onDeleted: () -> Unit,
    onStartCookMode: () -> Unit,
) {
    val context = LocalContext.current
    val recipe by viewModel.recipe.collectAsState()
    val ingredients by viewModel.ingredients.collectAsState()
    val steps by viewModel.steps.collectAsState()
    var servingMultiplier by remember { mutableIntStateOf(1) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    val baseServings = recipe?.servings ?: 1
    val currentServings = baseServings * servingMultiplier

    Scaffold(
        topBar = {
            ElachiTopBar(
                title = recipe?.title.orEmpty(),
                onBackClick = onBack,
                actions = {
                    IconButton(onClick = onEdit) {
                        Icon(
                            imageVector = Icons.Filled.Edit,
                            contentDescription = "Edit recipe",
                        )
                    }

                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = "Delete recipe",
                            tint = MaterialTheme.colorScheme.error,
                        )
                    }

                    IconButton(onClick = { recipe?.let { viewModel.toggleFavourite(it.isFavourite) } }) {
                        Icon(
                            if (recipe?.isFavourite == true) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                            contentDescription = "Favourite",
                        )
                    }
                },
            )
        },
    ) { padding ->
        recipe?.let { r ->
            LazyColumn(modifier = Modifier.padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (r.imageUrl != null) {
                    item {
                        AsyncImage(
                            model = r.imageUrl,
                            contentDescription = r.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxWidth().height(200.dp),
                        )
                    }
                }
                item {
                    Text("${r.cuisine} · ${r.cookTimeMinutes} min · ${r.difficulty}", style = MaterialTheme.typography.bodyMedium)
                }

                if (r.allergensCsv.isNotBlank()) {
                    item {
                        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                            Text(
                                "Contains: ${r.allergensCsv.replace(",", ", ")}",
                                modifier = Modifier.padding(12.dp),
                                color = MaterialTheme.colorScheme.onErrorContainer,
                            )
                        }
                    }
                }

                item {
                    Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                        Text("Servings: $currentServings", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.weight(1f))
                        IconButton(onClick = { if (servingMultiplier > 1) servingMultiplier-- }) { Icon(Icons.Filled.Remove, contentDescription = "Fewer servings") }
                        Text("$servingMultiplier×")
                        IconButton(onClick = { servingMultiplier++ }) { Icon(Icons.Filled.Add, contentDescription = "More servings") }
                    }
                }

                // Action row: Fork (disabled for Part 2), PDF export, Cook Mode
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        OutlinedButton(
                            onClick = { },
                            enabled = false,
                            modifier = Modifier.weight(1f),
                        ) { Text("Fork") }

                        OutlinedButton(
                            onClick = {
                                try {
                                    val file = RecipePdfExporter.export(context, r, ingredients, steps, currentServings)
                                    RecipePdfExporter.share(context, file)
                                } catch (e: Exception) {
                                    android.util.Log.e("RecipeDetail", "PDF export failed", e)
                                    Toast.makeText(context, "Couldn't create the PDF.", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.weight(1f),
                        ) { Text("PDF") }

                        Button(
                            onClick = onStartCookMode,
                            modifier = Modifier.weight(1f),
                        ) {
                            Text("Cook")
                        }
                    }
                    Text(
                        "Fork: coming in final version",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }

                item {
                    Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                        Text("Ingredients", style = MaterialTheme.typography.titleLarge)
                        Spacer(Modifier.weight(1f))
                        TextButton(onClick = { viewModel.addMissingIngredientsToShoppingList() }) {
                            Text("Add missing to list")
                        }
                    }
                }
                items(ingredients) { ingredient ->
                    // Serving scaler (FR-2.4) — see util/ServingScaler.kt, which
                    // carries the actual math and its own unit tests.
                    val displayQty = com.elachi.app.util.ServingScaler.scale(ingredient.quantity, baseServings, currentServings)
                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text(ingredient.name)
                        Text("$displayQty ${ingredient.unit}")
                    }
                }

                item { Text("Steps", style = MaterialTheme.typography.titleLarge) }
                items(steps) { step ->
                    Text("${step.order + 1}. ${step.instruction}")
                }

                item { Spacer(Modifier.height(24.dp)) }
            }
        } ?: Box(modifier = Modifier.padding(padding).fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
            CircularProgressIndicator()
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = {
                if (!viewModel.isDeleting.value) showDeleteDialog = false
            },
            title = { Text("Delete recipe?") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("This permanently deletes the recipe, its ingredients, and its cooking steps.")
                    viewModel.deleteError.value?.let { message ->
                        Text(
                            text = message,
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteRecipe {
                            showDeleteDialog = false
                            onDeleted()
                        }
                    },
                    enabled = !viewModel.isDeleting.value,
                ) {
                    if (viewModel.isDeleting.value) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                        )
                    } else {
                        Text(
                            text = "Delete",
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteDialog = false },
                    enabled = !viewModel.isDeleting.value,
                ) {
                    Text("Cancel")
                }
            },
        )
    }
}
