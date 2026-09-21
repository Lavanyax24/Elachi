package com.elachi.app.ui.recipe

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
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
    val ingredientsState by viewModel.ingredients.collectAsState()
    val stepsState by viewModel.steps.collectAsState()
    val remoteRecipe by viewModel.remoteRecipe
    val isLoadingRemote by viewModel.isLoadingRemote
    var servingMultiplier by remember { mutableIntStateOf(1) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var exportingPdf by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val missingIngredients = viewModel.missingIngredients.value
    val shoppingMessage = viewModel.shoppingMessage.value
    val isSavingToCookbook by viewModel.isSavingToCookbook
    val saveToCookbookMessage = viewModel.saveToCookbookMessage.value

    LaunchedEffect(shoppingMessage) {
        if (shoppingMessage != null) {
            snackbarHostState.showSnackbar(shoppingMessage)
            viewModel.clearShoppingMessage()
        }
    }

    LaunchedEffect(saveToCookbookMessage) {
        if (saveToCookbookMessage != null) {
            snackbarHostState.showSnackbar(saveToCookbookMessage)
            viewModel.clearSaveToCookbookMessage()
        }
    }

    val baseServings = recipe?.servings ?: remoteRecipe?.servings ?: 1
    val currentServings = baseServings * servingMultiplier

    if (isLoadingRemote) {
        Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val displayTitle = recipe?.title ?: remoteRecipe?.title ?: ""

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            ElachiTopBar(
                title = displayTitle,
                onBackClick = onBack,
                actions = {
                    if (recipe != null) {
                        IconButton(onClick = onEdit) {
                            Icon(imageVector = Icons.Filled.Edit, contentDescription = "Edit recipe")
                        }
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(imageVector = Icons.Filled.Delete, contentDescription = "Delete recipe", tint = MaterialTheme.colorScheme.error)
                        }
                        IconButton(onClick = { viewModel.toggleFavourite(recipe!!.isFavourite) }) {
                            Icon(if (recipe!!.isFavourite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder, contentDescription = "Favourite")
                        }

                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(start = 4.dp)) {
                            Icon(
                                imageVector = if (recipe!!.isPrivate) Icons.Filled.Lock else Icons.Filled.Public,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Switch(
                                checked = recipe!!.isPrivate,
                                onCheckedChange = { viewModel.toggleVisibility(recipe!!.isPrivate) },
                                modifier = Modifier.scale(0.8f),
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = MaterialTheme.colorScheme.primary,
                                    uncheckedThumbColor = Color.White,
                                    uncheckedTrackColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                    uncheckedBorderColor = Color.Transparent
                                )
                            )
                        }
                    }
                },
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        val r = recipe
        val rr = remoteRecipe

        if (r == null && rr == null) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Recipe not found.", color = MaterialTheme.colorScheme.onSurface)
            }
            return@Scaffold
        }

        val imageUrl = r?.imageUrl ?: rr?.imageUrl
        val cuisine = r?.cuisine ?: rr?.cuisine ?: ""
        val cookTime = r?.cookTimeMinutes ?: rr?.cookTimeMinutes ?: 0
        val difficulty = r?.difficulty ?: rr?.difficulty ?: ""
        val allergens = r?.allergensCsv?.takeIf { it.isNotBlank() } ?: rr?.allergens?.joinToString(", ")

        val ingredients = if (r != null) ingredientsState.map { it.name to (it.quantity to it.unit) }
        else rr?.ingredients?.map { it.name to (it.quantity to it.unit) } ?: emptyList()

        val steps = if (r != null) stepsState.map { it.instruction }
        else rr?.steps?.map { it.instruction } ?: emptyList()

        LazyColumn(modifier = Modifier.padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            if (imageUrl != null) {
                item {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = displayTitle,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxWidth().height(200.dp),
                    )
                }
            }
            item {
                Text(
                    "$cuisine · $cookTime min · $difficulty",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (!allergens.isNullOrBlank()) {
                item {
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                        Text(
                            "Contains: ${allergens.replace(",", ", ")}",
                            modifier = Modifier.padding(12.dp),
                            color = MaterialTheme.colorScheme.onErrorContainer,
                        )
                    }
                }
            }

            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Servings: $currentServings", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                    Spacer(Modifier.weight(1f))
                    IconButton(onClick = { if (servingMultiplier > 1) servingMultiplier-- }) {
                        Icon(Icons.Filled.Remove, contentDescription = "Fewer servings", tint = MaterialTheme.colorScheme.primary)
                    }
                    Text("$servingMultiplier×", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                    IconButton(onClick = { servingMultiplier++ }) {
                        Icon(Icons.Filled.Add, contentDescription = "More servings", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    if (r != null) {
                        OutlinedButton(
                            onClick = { },
                            enabled = false,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
                        ) { Text("Fork") }

                        OutlinedButton(
                            onClick = {
                                scope.launch {
                                    exportingPdf = true
                                    try {
                                        val file = RecipePdfExporter.export(context, r, ingredientsState, stepsState, currentServings)
                                        RecipePdfExporter.share(context, file)
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Couldn't create the PDF.", Toast.LENGTH_SHORT).show()
                                    } finally {
                                        exportingPdf = false
                                    }
                                }
                            },
                            enabled = !exportingPdf,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                        ) { Text(if (exportingPdf) "Creating…" else "PDF") }

                        Button(
                            onClick = onStartCookMode,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Cook", color = Color.White)
                        }
                    } else {
                        Button(
                            onClick = { viewModel.saveRemoteRecipeToCookbook() },
                            enabled = !isSavingToCookbook,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            if (isSavingToCookbook) {
                                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = Color.White)
                            } else {
                                Text("Save to Cookbook", color = Color.White)
                            }
                        }
                    }
                }
            }

            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Ingredients", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
                    if (r != null) {
                        Spacer(Modifier.weight(1f))
                        TextButton(onClick = { viewModel.loadMissingIngredients(baseServings, currentServings) }) {
                            Text("Add missing to list", color = MaterialTheme.colorScheme.secondary)
                        }
                    }
                }
            }
            items(ingredients) { (name, qtyUnit) ->
                val displayQty = com.elachi.app.util.ServingScaler.scale(qtyUnit.first, baseServings, currentServings)
                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Text(name, color = MaterialTheme.colorScheme.onSurface)
                    Text("$displayQty ${qtyUnit.second}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            item { Text("Steps", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary) }
            items(steps.size) { index ->
                Text("${index + 1}. ${steps[index]}", color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.padding(vertical = 4.dp))
            }

            item { Spacer(Modifier.height(24.dp)) }
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
            onDismissRequest = { if (!viewModel.isDeleting.value) showDeleteDialog = false },
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            textContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            title = { Text("Delete recipe?") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("This permanently deletes the recipe, its ingredients, and its cooking steps.")
                    viewModel.deleteError.value?.let { message ->
                        Text(text = message, color = MaterialTheme.colorScheme.error)
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
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                    } else {
                        Text(text = "Delete", color = MaterialTheme.colorScheme.error)
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteDialog = false },
                    enabled = !viewModel.isDeleting.value,
                ) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
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
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            textContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            title = { Text("You're all set!") },
            text = { Text("Your pantry already has everything this recipe needs.") },
            confirmButton = { TextButton(onClick = onDismiss) { Text("OK", color = MaterialTheme.colorScheme.primary) } },
        )
        return
    }

    val checked = remember(items) {
        mutableStateListOf<Boolean>().apply { addAll(items.map { !it.alreadyOnList }) }
    }
    val selectedCount = checked.count { it }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        titleContentColor = MaterialTheme.colorScheme.onSurface,
        textContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
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
                    modifier = Modifier.fillMaxWidth().clickable {
                        val newValue = selectedCount != items.size
                        for (i in checked.indices) checked[i] = newValue
                    },
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Checkbox(
                        checked = selectedCount == items.size,
                        onCheckedChange = { all -> for (i in checked.indices) checked[i] = all },
                    )
                    Text("Select all", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                Column(modifier = Modifier.heightIn(max = 300.dp).verticalScroll(rememberScrollState())) {
                    items.forEachIndexed { index, item ->
                        Row(
                            modifier = Modifier.fillMaxWidth().clickable { checked[index] = !checked[index] },
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Checkbox(checked = checked[index], onCheckedChange = { checked[index] = it })
                            Column(modifier = Modifier.weight(1f)) {
                                Text(item.name, color = MaterialTheme.colorScheme.onSurface)
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
                                color = MaterialTheme.colorScheme.onSurfaceVariant
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
            ) { Text(if (selectedCount == 0) "Add to list" else "Add $selectedCount to list", color = MaterialTheme.colorScheme.primary) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
    )
}