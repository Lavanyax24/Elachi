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
    onStartCookMode: () -> Unit,
) {
    val context = LocalContext.current
    val recipe by viewModel.recipe.collectAsState()
    val ingredients by viewModel.ingredients.collectAsState()
    val steps by viewModel.steps.collectAsState()

    var servingMultiplier by remember {
        mutableIntStateOf(1)
    }

    val baseServings = recipe?.servings ?: 1
    val currentServings = baseServings * servingMultiplier

    Scaffold(
        topBar = {
            ElachiTopBar(
                title = recipe?.title.orEmpty(),
                onBackClick = onBack,
                actions = {
                    IconButton(
                        onClick = {
                            recipe?.let {
                                viewModel.toggleFavourite(it.isFavourite)
                            }
                        },
                    ) {
                        Icon(
                            imageVector =
                                if (recipe?.isFavourite == true) {
                                    Icons.Filled.Favorite
                                } else {
                                    Icons.Filled.FavoriteBorder
                                },
                            contentDescription = "Favourite",
                        )
                    }
                },
            )
        },
    ) { paddingValues ->

        recipe?.let { currentRecipe ->

            LazyColumn(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                currentRecipe.imageUrl
                    ?.takeIf { it.isNotBlank() }
                    ?.let { imageUrl ->
                        item {
                            AsyncImage(
                                model = imageUrl,
                                contentDescription = currentRecipe.title,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp),
                            )
                        }
                    }

                item {
                    Text(
                        text =
                            "${currentRecipe.cuisine} · " +
                                    "${currentRecipe.cookTimeMinutes} min · " +
                                    currentRecipe.difficulty,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }

                if (currentRecipe.allergensCsv.isNotBlank()) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor =
                                    MaterialTheme.colorScheme.errorContainer,
                            ),
                        ) {
                            Text(
                                text =
                                    "Contains: ${
                                        currentRecipe.allergensCsv.replace(
                                            ",",
                                            ", ",
                                        )
                                    }",
                                modifier = Modifier.padding(12.dp),
                                color =
                                    MaterialTheme.colorScheme.onErrorContainer,
                            )
                        }
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment =
                            androidx.compose.ui.Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "Servings: $currentServings",
                            style = MaterialTheme.typography.titleMedium,
                        )

                        Spacer(modifier = Modifier.weight(1f))

                        IconButton(
                            onClick = {
                                if (servingMultiplier > 1) {
                                    servingMultiplier--
                                }
                            },
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Remove,
                                contentDescription = "Fewer servings",
                            )
                        }

                        Text(text = "$servingMultiplier×")

                        IconButton(
                            onClick = {
                                servingMultiplier++
                            },
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Add,
                                contentDescription = "More servings",
                            )
                        }
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        OutlinedButton(
                            onClick = {},
                            enabled = false,
                            modifier = Modifier.weight(1f),
                        ) {
                            Text("Fork")
                        }

                        OutlinedButton(
                            onClick = {
                                try {
                                    val pdfFile =
                                        RecipePdfExporter.export(
                                            context = context,
                                            recipe = currentRecipe,
                                            ingredients = ingredients,
                                            steps = steps,
                                            currentServings = currentServings,
                                        )

                                    RecipePdfExporter.share(
                                        context,
                                        pdfFile,
                                    )
                                } catch (exception: Exception) {
                                    android.util.Log.e(
                                        "RecipeDetail",
                                        "PDF export failed",
                                        exception,
                                    )

                                    Toast.makeText(
                                        context,
                                        "Couldn't create the PDF.",
                                        Toast.LENGTH_SHORT,
                                    ).show()
                                }
                            },
                            modifier = Modifier.weight(1f),
                        ) {
                            Text("PDF")
                        }

                        Button(
                            onClick = onStartCookMode,
                            modifier = Modifier.weight(1f),
                        ) {
                            Text("Cook")
                        }
                    }

                    Text(
                        text = "Fork: coming in final version",
                        style = MaterialTheme.typography.labelSmall,
                        color =
                            MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment =
                            androidx.compose.ui.Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "Ingredients",
                            style = MaterialTheme.typography.titleLarge,
                        )

                        Spacer(modifier = Modifier.weight(1f))

                        TextButton(
                            onClick = {
                                viewModel
                                    .addMissingIngredientsToShoppingList()
                            },
                        ) {
                            Text("Add missing to list")
                        }
                    }
                }

                items(ingredients) { ingredient ->
                    val displayQuantity =
                        com.elachi.app.util.ServingScaler.scale(
                            ingredient.quantity,
                            baseServings,
                            currentServings,
                        )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement =
                            Arrangement.SpaceBetween,
                    ) {
                        Text(text = ingredient.name)

                        Text(
                            text = "$displayQuantity ${ingredient.unit}",
                        )
                    }
                }

                item {
                    Text(
                        text = "Steps",
                        style = MaterialTheme.typography.titleLarge,
                    )
                }

                items(steps) { step ->
                    Text(
                        text = "${step.order + 1}. ${step.instruction}",
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        } ?: Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize(),
            contentAlignment =
                androidx.compose.ui.Alignment.Center,
        ) {
            CircularProgressIndicator()
        }
    }
}