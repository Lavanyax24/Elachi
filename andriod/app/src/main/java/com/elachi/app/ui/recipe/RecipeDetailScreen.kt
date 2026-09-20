package com.elachi.app.ui.recipe

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.elachi.app.data.repository.MissingIngredient
import com.elachi.app.ui.common.ElachiTopBar
import com.elachi.app.util.RecipePdfExporter
import kotlinx.coroutines.launch

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
    var exportingPdf by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val missingIngredients = viewModel.missingIngredients.value
    val shoppingMessage = viewModel.shoppingMessage.value

    // Confirmation / error message after adding to the shopping list
    LaunchedEffect(shoppingMessage) {
        if (shoppingMessage != null) {
            snackbarHostState.showSnackbar(shoppingMessage)
            viewModel.clearShoppingMessage()
        }
    }

    val baseServings = recipe?.servings ?: 1
    val currentServings = baseServings * servingMultiplier

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
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
                                scope.launch {
                                    exportingPdf = true
                                    try {
                                        val file = RecipePdfExporter.export(context, r, ingredients, steps, currentServings)
                                        RecipePdfExporter.share(context, file)
                                    } catch (e: Exception) {
                                        android.util.Log.e("RecipeDetail", "PDF export failed", e)
                                        Toast.makeText(context, "Couldn't create the PDF.", Toast.LENGTH_SHORT).show()
                                    } finally {
                                        exportingPdf = false
                                    }
                                }
                            },
                            enabled = !exportingPdf,
                            modifier = Modifier.weight(1f),
                        ) { Text(if (exportingPdf) "Creating…" else "PDF") }

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
                        TextButton(onClick = { viewModel.loadMissingIngredients(baseServings, currentServings) }) {
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

    missingIngredients?.let { items ->
        MissingIngredientsDialog(
            items = items,
            onConfirm = { selected -> viewModel.addSelectedToShoppingList(selected) },
            onDismiss = { viewModel.dismissMissingIngredients() },
        )
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

private fun formatQty(q: Double): String =
    if (q % 1.0 == 0.0) q.toInt().toString() else q.toString()

@Composable
private fun MissingIngredientsDialog(
    items: List<MissingIngredient>,
    onConfirm: (List<MissingIngredient>) -> Unit,
    onDismiss: () -> Unit,
) {
    if (items.isEmpty()) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("You're all set!") },
            text = { Text("Your pantry already has everything this recipe needs.") },
            confirmButton = { TextButton(onClick = onDismiss) { Text("OK") } },
        )
        return
    }

    // Everything is ticked by default, except items that are already on the list.
    val checked = remember(items) {
        mutableStateListOf<Boolean>().apply { addAll(items.map { !it.alreadyOnList }) }
    }
    val selectedCount = checked.count { it }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add to shopping list") },
        text = {
            Column {
                Text(
                    "Tick the ingredients you want to add. Anything already in your pantry is left out.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(8.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            val newValue = selectedCount != items.size
                            for (i in checked.indices) checked[i] = newValue
                        },
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Checkbox(
                        checked = selectedCount == items.size,
                        onCheckedChange = { all -> for (i in checked.indices) checked[i] = all },
                    )
                    Text("Select all", style = MaterialTheme.typography.titleSmall)
                }
                HorizontalDivider()

                Column(
                    modifier = Modifier
                        .heightIn(max = 300.dp)
                        .verticalScroll(rememberScrollState()),
                ) {
                    items.forEachIndexed { index, item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { checked[index] = !checked[index] },
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Checkbox(
                                checked = checked[index],
                                onCheckedChange = { checked[index] = it },
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(item.name)
                                if (item.alreadyOnList) {
                                    Text(
                                        "Already on your list · quantity will be added",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                            Text(
                                "${formatQty(item.quantity)} ${item.unit}".trim(),
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = selectedCount > 0,
                onClick = { onConfirm(items.filterIndexed { index, _ -> checked[index] }) },
            ) { Text(if (selectedCount == 0) "Add to list" else "Add $selectedCount to list") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}