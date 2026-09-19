package com.elachi.app.ui.recipe

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
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
    val comments by viewModel.comments
    var servingMultiplier by remember { mutableIntStateOf(1) }
    var newCommentText by remember { mutableStateOf("") }
    var newCommentRating by remember { mutableIntStateOf(0) }
    val reviewState by viewModel.reviewUiState

    LaunchedEffect(recipe?.id) { if (recipe != null) viewModel.loadComments() }

    val baseServings = recipe?.servings ?: 1
    val currentServings = baseServings * servingMultiplier

    Scaffold(
        topBar = {
            ElachiTopBar(
                title = recipe?.title.orEmpty(),
                onBackClick = onBack,
                actions = {
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

                item {
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Reviews",
                        style = MaterialTheme.typography.titleLarge,
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Select a rating",
                        style = MaterialTheme.typography.labelLarge,
                    )

                    Row(
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                    ) {
                        (1..5).forEach { star ->
                            IconButton(
                                onClick = {
                                    newCommentRating = star
                                    viewModel.clearReviewMessage()
                                },
                                modifier = Modifier.size(40.dp),
                            ) {
                                Icon(
                                    imageVector =
                                        if (star <= newCommentRating) {
                                            Icons.Filled.Star
                                        } else {
                                            Icons.Filled.StarBorder
                                        },
                                    contentDescription = "$star stars",
                                    tint = androidx.compose.ui.graphics.Color(0xFFF5A623),
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = newCommentText,
                        onValueChange = {
                            newCommentText = it
                            viewModel.clearReviewMessage()
                        },
                        label = {
                            Text("Your review")
                        },
                        placeholder = {
                            Text("What did you think about this recipe?")
                        },
                        minLines = 3,
                        maxLines = 5,
                        modifier = Modifier.fillMaxWidth(),
                    )

                    reviewState.errorMessage?.let { message ->
                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = message,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }

                    reviewState.successMessage?.let { message ->
                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = message,
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            viewModel.postComment(
                                rating = newCommentRating.takeIf { it > 0 },
                                text = newCommentText,
                                onPosted = {
                                    newCommentText = ""
                                    newCommentRating = 0
                                },
                            )
                        },
                        enabled =
                            newCommentRating in 1..5 &&
                                    newCommentText.isNotBlank() &&
                                    !reviewState.isPosting,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        if (reviewState.isPosting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary,
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            Text("Posting...")
                        } else {
                            Text("Post Review")
                        }
                    }
                }

                if (reviewState.isLoading) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                } else if (comments.isEmpty()) {
                    item {
                        Text(
                            text = "No reviews yet. Be the first to leave one.",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                } else {
                    items(
                        items = comments,
                        key = { comment -> comment.id },
                    ) { comment ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                            ) {
                                Text(
                                    text = comment.displayName,
                                    fontWeight = FontWeight.Bold,
                                )

                                comment.rating?.let { rating ->
                                    Row {
                                        repeat(5) { index ->
                                            Icon(
                                                imageVector =
                                                if (index < rating) {
                                                    Icons.Filled.Star
                                                } else {
                                                    Icons.Filled.StarBorder
                                                },
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp),
                                                tint = androidx.compose.ui.graphics.Color(
                                                    0xFFF5A623,
                                                ),
                                            )
                                        }
                                    }
                                }

                                comment.text
                                    ?.takeIf { it.isNotBlank() }
                                    ?.let { reviewText ->
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(reviewText, style = MaterialTheme.typography.bodyMedium)
                                    }
                            }
                        }
                    }
                }

                item { Spacer(Modifier.height(24.dp)) }
            }
        } ?: Box(modifier = Modifier.padding(padding).fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
            CircularProgressIndicator()
        }
    }
}