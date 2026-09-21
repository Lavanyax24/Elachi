package com.elachi.app.ui.pantry

import android.content.Context
import android.os.CountDownTimer
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.elachi.app.data.local.entities.PantryItemEntity
import com.elachi.app.data.local.entities.ShoppingListItemEntity
import com.elachi.app.ui.tools.CalculatorScreen
import com.elachi.app.ui.tools.UnitConverterScreen
import com.elachi.app.util.AlarmPlayer
import kotlinx.coroutines.delay

// This is the pantry screen where the users can add items to their pantry.

private const val MAX_NAME_LENGTH = 40
private const val MAX_QUANTITY = 100_000.0

private fun formatQty(q: Double): String =
    if (q % 1.0 == 0.0) q.toInt().toString() else q.toString()

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantryScreen(viewModel: PantryViewModel) {
    val context = LocalContext.current
    val pantryItems by viewModel.pantryItems.collectAsState()
    val shoppingList by viewModel.shoppingList.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var editingItem by remember { mutableStateOf<PantryItemEntity?>(null) }
    var tab by remember { mutableIntStateOf(0) }
    val snackbarHostState = remember { SnackbarHostState() }
    val timer = remember(context) { KitchenTimerState(context) }

    DisposableEffect(Unit) { onDispose { timer.release() } }

    val message = viewModel.message.value
    LaunchedEffect(message) {
        if (message != null) {
            snackbarHostState.showSnackbar(message)
            viewModel.clearMessage()
        }
    }

    LaunchedEffect(timer.showTimeUp) {
        if (timer.showTimeUp) {
            delay(30_000L)
            timer.dismissTimeUp()
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0.dp, 0.dp, 0.dp, 0.dp),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            if (tab != 2) {
                FloatingActionButton(
                    onClick = { showAddDialog = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White
                ) {
                    Icon(
                        Icons.Filled.Add,
                        contentDescription = if (tab == 0) "Add pantry" else "Add shopping",
                    )
                }
            }
        },
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize().background(MaterialTheme.colorScheme.background)) {
            TabRow(
                selectedTabIndex = tab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                Tab(selected = tab == 0, onClick = { tab = 0 }, text = { Text("Your Pantry") })
                Tab(selected = tab == 1, onClick = { tab = 1 }, text = { Text("Shopping List") })
                Tab(selected = tab == 2, onClick = { tab = 2 }, text = { Text("Kitchen Tools") })
            }

            when (tab) {
                0 -> PantryTab(pantry = pantryItems, onEdit = { editingItem = it }, onDelete = { viewModel.deletePantryItem(it) })
                1 -> ShoppingTab(list = shoppingList, onToggle = { id, b -> viewModel.toggleBought(id, b) }, onDelete = { viewModel.deleteShoppingItem(it) }, onClearBought = { viewModel.clearBoughtItems() }, onClearAll = { viewModel.clearShoppingList() })
                2 -> KitchenToolsPanel(timer)
            }
        }
    }

    if (showAddDialog) {
        ItemDialog(
            title = if (tab == 0) "Add Pantry Item" else "Add Shopping Item",
            existingNames = if (tab == 0) pantryItems.map { it.name.trim().lowercase() }.toSet() else null,
            onDismiss = { showAddDialog = false },
            onSave = { name, qty, unit ->
                if (tab == 0) viewModel.addPantryItem(name, qty, unit) else viewModel.addShoppingItem(name, qty, unit)
                showAddDialog = false
            }
        )
    }

    editingItem?.let { item ->
        ItemDialog(
            title = "Edit Pantry Item",
            initialName = item.name,
            initialQuantity = formatQty(item.quantity),
            initialUnit = item.unit,
            existingNames = pantryItems.filter { it.id != item.id }.map { it.name.trim().lowercase() }.toSet(),
            onDismiss = { editingItem = null },
            onSave = { name, qty, unit ->
                viewModel.editPantryItem(item, name, qty, unit)
                editingItem = null
            },
        )
    }

    if (timer.showTimeUp) {
        AlertDialog(
            onDismissRequest = { timer.dismissTimeUp() },
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            textContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            title = { Text("Time's up!") },
            text = { Text("Your timer has finished.") },
            confirmButton = { Button(onClick = { timer.dismissTimeUp() }) { Text("Stop alarm") } },
        )
    }
}

@Composable
private fun PantryTab(pantry: List<PantryItemEntity>, onEdit: (PantryItemEntity) -> Unit, onDelete: (String) -> Unit) {
    if (pantry.isEmpty()) {
        EmptyState(title = "Your pantry is empty", body = "Tap + to add what you have at home.")
        return
    }
    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        items(pantry, key = { it.id }) { item ->
            Row(modifier = Modifier.fillMaxWidth().clickable { onEdit(item) }, horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(item.name, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
                    if (item.pendingSync) {
                        Text("Saved locally", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("${formatQty(item.quantity)} ${item.unit}".trim(), color = MaterialTheme.colorScheme.onSurface)
                    IconButton(onClick = { onEdit(item) }) { Icon(Icons.Filled.Edit, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant) }
                    IconButton(onClick = { onDelete(item.id) }) { Icon(Icons.Filled.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) }
                }
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
        }
    }
}

@Composable
private fun ShoppingTab(list: List<ShoppingListItemEntity>, onToggle: (String, Boolean) -> Unit, onDelete: (String) -> Unit, onClearBought: () -> Unit, onClearAll: () -> Unit) {
    if (list.isEmpty()) {
        EmptyState(title = "Your shopping list is empty", body = "Tap + to add an item.")
        return
    }
    val toBuy = list.filter { !it.isBought }
    val bought = list.filter { it.isBought }
    var menuOpen by remember { mutableStateOf(false) }
    var pendingClear by remember { mutableStateOf<ClearAction?>(null) }
    Column(modifier = Modifier.fillMaxSize()) {
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("${toBuy.size} to buy · ${bought.size} bought", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
            Box {
                TextButton(onClick = { menuOpen = true }) {
                    Icon(Icons.Filled.ClearAll, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(6.dp))
                    Text("Clear", color = MaterialTheme.colorScheme.primary)
                }
                DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }, containerColor = MaterialTheme.colorScheme.surface) {
                    DropdownMenuItem(text = { Text("Clear bought items") }, enabled = bought.isNotEmpty(), onClick = { menuOpen = false; pendingClear = ClearAction.BOUGHT })
                    DropdownMenuItem(text = { Text("Clear entire list") }, onClick = { menuOpen = false; pendingClear = ClearAction.ALL })
                }
            }
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
        LazyColumn(contentPadding = PaddingValues(8.dp)) {
            items(toBuy, key = { it.id }) { item -> ShoppingRow(item, onToggle, onDelete) }
            if (bought.isNotEmpty()) {
                item { Text("In the basket · ${bought.size}", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(start = 8.dp, top = 16.dp, bottom = 4.dp)) }
                items(bought, key = { it.id }) { item -> ShoppingRow(item, onToggle, onDelete) }
            }
        }
    }
    pendingClear?.let { action ->
        AlertDialog(
            onDismissRequest = { pendingClear = null },
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            textContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            title = { Text(if (action == ClearAction.BOUGHT) "Clear bought items?" else "Clear entire list?") },
            text = { Text("Are you sure you want to clear these items?") },
            confirmButton = { TextButton(onClick = { if (action == ClearAction.BOUGHT) onClearBought() else onClearAll(); pendingClear = null }) { Text("Clear", color = MaterialTheme.colorScheme.error) } },
            dismissButton = { TextButton(onClick = { pendingClear = null }) { Text("Cancel") } },
        )
    }
}

@Composable
private fun ShoppingRow(item: ShoppingListItemEntity, onToggle: (String, Boolean) -> Unit, onDelete: (String) -> Unit) {
    val textColor = if (item.isBought) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f) else MaterialTheme.colorScheme.onSurface
    val decoration = if (item.isBought) TextDecoration.LineThrough else TextDecoration.None
    Row(modifier = Modifier.fillMaxWidth().clickable { onToggle(item.id, !item.isBought) }, verticalAlignment = Alignment.CenterVertically) {
        Checkbox(checked = item.isBought, onCheckedChange = { onToggle(item.id, it) }, colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary))
        Text(item.name, color = textColor, textDecoration = decoration, modifier = Modifier.weight(1f))
        Text("${formatQty(item.quantity)} ${item.unit}".trim(), color = textColor, textDecoration = decoration)
        IconButton(onClick = { onDelete(item.id) }) { Icon(Icons.Filled.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)) }
    }
}

@Composable
private fun EmptyState(title: String, body: String) {
    Column(modifier = Modifier.fillMaxSize().padding(32.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface, textAlign = TextAlign.Center)
        Spacer(Modifier.height(8.dp))
        Text(body, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
    }
}

private val UNIT_OPTIONS = listOf("g", "kg", "ml", "L", "cups", "tbsp", "tsp", "pcs", "oz", "lb")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UnitDropdown(selected: String, options: List<String>, onSelected: (String) -> Unit, modifier: Modifier = Modifier) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }, modifier = modifier) {
        OutlinedTextField(value = selected, onValueChange = {}, readOnly = true, label = { Text("Unit") }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }, modifier = Modifier.menuAnchor(), colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = MaterialTheme.colorScheme.surface, unfocusedContainerColor = MaterialTheme.colorScheme.surface))
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }, containerColor = MaterialTheme.colorScheme.surface) {
            options.forEach { option -> DropdownMenuItem(text = { Text(option) }, onClick = { onSelected(option); expanded = false }) }
        }
    }
}

@Composable
private fun ItemDialog(title: String, existingNames: Set<String>?, onDismiss: () -> Unit, onSave: (String, Double, String) -> Unit, initialName: String = "", initialQuantity: String = "", initialUnit: String = "g") {
    var name by remember { mutableStateOf(initialName) }
    var quantity by remember { mutableStateOf(initialQuantity) }
    var unit by remember { mutableStateOf(initialUnit.ifBlank { "g" }) }
    val unitOptions = remember(initialUnit) { if (initialUnit.isBlank() || initialUnit in UNIT_OPTIONS) UNIT_OPTIONS else listOf(initialUnit) + UNIT_OPTIONS }
    var submitted by remember { mutableStateOf(false) }
    val cleanName = name.trim().replace(Regex("\\s+"), " ")
    val parsedQuantity = quantity.replace(',', '.').toDoubleOrNull()
    val nameError = if (cleanName.isEmpty()) "Enter name" else if (existingNames != null && cleanName.lowercase() in existingNames) "Already exists" else null
    val quantityError = if (quantity.isBlank() || parsedQuantity == null || parsedQuantity <= 0.0) "Invalid qty" else null

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        titleContentColor = MaterialTheme.colorScheme.onSurface,
        textContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        title = { Text(title) },
        text = {
            Column(modifier = Modifier.imePadding()) {
                OutlinedTextField(value = name, onValueChange = { name = it.take(MAX_NAME_LENGTH) }, label = { Text("Item name") }, singleLine = true, isError = submitted && nameError != null, supportingText = { if (submitted && nameError != null) Text(nameError) }, keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words), modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                Row {
                    OutlinedTextField(value = quantity, onValueChange = { quantity = it.filter { c -> c.isDigit() || c == '.' || c == ',' }.take(9) }, label = { Text("Qty") }, singleLine = true, isError = submitted && quantityError != null, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
                    Spacer(Modifier.width(8.dp))
                    UnitDropdown(selected = unit, options = unitOptions, onSelected = { unit = it }, modifier = Modifier.weight(1f))
                }
            }
        },
        confirmButton = { TextButton(onClick = { submitted = true; if (nameError == null && quantityError == null && parsedQuantity != null) onSave(cleanName, parsedQuantity, unit) }) { Text("Save", color = MaterialTheme.colorScheme.primary) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant) } },
    )
}

@Stable
private class KitchenTimerState(context: Context) {
    private val alarm = AlarmPlayer(context)
    private var countDown: CountDownTimer? = null
    var minutesInput by mutableStateOf(""); var secondsInput by mutableStateOf("")
    var remainingMs by mutableLongStateOf(0L); var isRunning by mutableStateOf(false)
    var hasStarted by mutableStateOf(false); var showTimeUp by mutableStateOf(false)
    fun start(totalSeconds: Int) { if (totalSeconds <= 0) return; remainingMs = totalSeconds * 1000L; hasStarted = true; runCountdown(remainingMs) }
    fun resume() { if (hasStarted && !isRunning && remainingMs > 0L) runCountdown(remainingMs) }
    fun pause() { countDown?.cancel(); isRunning = false }
    fun reset() { countDown?.cancel(); alarm.stop(); remainingMs = 0L; isRunning = false; hasStarted = false; showTimeUp = false }
    fun dismissTimeUp() { alarm.stop(); showTimeUp = false; hasStarted = false }
    fun release() { countDown?.cancel(); alarm.stop() }
    private fun runCountdown(ms: Long) {
        countDown?.cancel()
        countDown = object : CountDownTimer(ms, 250L) {
            override fun onTick(m: Long) { remainingMs = m }
            override fun onFinish() { remainingMs = 0L; isRunning = false; alarm.play(); showTimeUp = true }
        }.start()
        isRunning = true
    }
}

@Composable
private fun KitchenToolsPanel(timer: KitchenTimerState) {
    var subTab by remember { mutableIntStateOf(0) }
    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        TabRow(
            selectedTabIndex = subTab,
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            divider = {},
            indicator = { tabPositions -> if (subTab < tabPositions.size) TabRowDefaults.SecondaryIndicator(modifier = Modifier.tabIndicatorOffset(tabPositions[subTab]), color = MaterialTheme.colorScheme.primary) }
        ) {
            Tab(selected = subTab == 0, onClick = { subTab = 0 }, text = { Text("Timer") })
            Tab(selected = subTab == 1, onClick = { subTab = 1 }, text = { Text("Calculator") })
            Tab(selected = subTab == 2, onClick = { subTab = 2 }, text = { Text("Converter") })
        }
        when (subTab) {
            0 -> TimerTab(timer)
            1 -> CalculatorScreen()
            2 -> UnitConverterScreen()
        }
    }
}

@Composable
private fun TimerTab(timer: KitchenTimerState) {
    val inputTotalSeconds = (timer.minutesInput.toIntOrNull() ?: 0) * 60 + (timer.secondsInput.toIntOrNull() ?: 0)
    val displaySeconds = if (timer.hasStarted) ((timer.remainingMs + 999) / 1000).toInt() else inputTotalSeconds
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
        Text(text = String.format("%02d:%02d", displaySeconds / 60, displaySeconds % 60), style = MaterialTheme.typography.displayMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        Spacer(Modifier.height(16.dp))
        if (!timer.hasStarted) {
            Text("Quick timers", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurface)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(1, 5, 10, 15).forEach { m -> OutlinedButton(onClick = { timer.start(m * 60) }, colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary)) { Text("${m}m") } }
            }
            Spacer(Modifier.height(20.dp))
            Text("Or set a custom time", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurface)
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(value = timer.minutesInput, onValueChange = { timer.minutesInput = it.filter { c -> c.isDigit() }.take(3) }, label = { Text("Min") }, singleLine = true, modifier = Modifier.width(96.dp))
                Text(":", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.padding(horizontal = 8.dp))
                OutlinedTextField(value = timer.secondsInput, onValueChange = { val d = it.filter { c -> c.isDigit() }.take(2); timer.secondsInput = if ((d.toIntOrNull() ?: 0) > 59) "59" else d }, label = { Text("Sec") }, singleLine = true, modifier = Modifier.width(96.dp))
            }
            Spacer(Modifier.height(20.dp))
            Button(onClick = { timer.start(inputTotalSeconds) }, enabled = inputTotalSeconds > 0, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)) { Text("Start", color = Color.White) }
        } else {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (timer.isRunning) Button(onClick = { timer.pause() }) { Text("Pause") } else Button(onClick = { timer.resume() }) { Text("Resume") }
                OutlinedButton(onClick = { timer.reset() }) { Text("Reset") }
            }
        }
    }
}

private enum class ClearAction { BOUGHT, ALL }
