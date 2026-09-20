package com.elachi.app.ui.pantry

import android.content.Context
import android.os.CountDownTimer
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.elachi.app.data.local.entities.PantryItemEntity
import com.elachi.app.data.local.entities.ShoppingListItemEntity
import com.elachi.app.util.AlarmPlayer
import kotlinx.coroutines.delay

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

    // Lives here (not inside the Kitchen Tools tab) so switching tabs doesn't kill a running timer.
    val timer = remember(context) { KitchenTimerState(context) }

    DisposableEffect(Unit) { onDispose { timer.release() } }

    // Errors and confirmations from the ViewModel
    val message = viewModel.message.value
    LaunchedEffect(message) {
        if (message != null) {
            snackbarHostState.showSnackbar(message)
            viewModel.clearMessage()
        }
    }

    // Silence the alarm after 30s if nobody dismisses the "Time's up" dialog
    LaunchedEffect(timer.showTimeUp) {
        if (timer.showTimeUp) {
            delay(30_000L)
            timer.dismissTimeUp()
        }
    }

    Scaffold(
        // ScreenShell in NavGraph already provides the top bar and handles insets
        contentWindowInsets = WindowInsets(0.dp, 0.dp, 0.dp, 0.dp),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            if (tab != 2) {
                FloatingActionButton(onClick = { showAddDialog = true }) {
                    Icon(
                        Icons.Filled.Add,
                        contentDescription = if (tab == 0) "Add pantry item" else "Add shopping item",
                    )
                }
            }
        },
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            TabRow(selectedTabIndex = tab) {
                Tab(selected = tab == 0, onClick = { tab = 0 }, text = { Text("Your Pantry") })
                Tab(selected = tab == 1, onClick = { tab = 1 }, text = { Text("Shopping List") })
                Tab(selected = tab == 2, onClick = { tab = 2 }, text = { Text("Kitchen Tools") })
            }

            when (tab) {
                0 -> PantryTab(
                    pantry = pantryItems,
                    onEdit = { editingItem = it },
                    onDelete = { viewModel.deletePantryItem(it) },
                )

                1 -> ShoppingTab(
                    list = shoppingList,
                    onToggle = { id, bought -> viewModel.toggleBought(id, bought) },
                    onDelete = { viewModel.deleteShoppingItem(it) },
                    onClearBought = { viewModel.clearBoughtItems() },
                    onClearAll = { viewModel.clearShoppingList() },
                )

                2 -> KitchenToolsPanel(timer)
            }
        }
    }

    if (showAddDialog) {
        if (tab == 0) {
            ItemDialog(
                title = "Add Pantry Item",
                existingNames = pantryItems.map { it.name.trim().lowercase() }.toSet(),
                onDismiss = { showAddDialog = false },
                onSave = { name, qty, unit ->
                    viewModel.addPantryItem(name, qty, unit)
                    showAddDialog = false
                },
            )
        } else {
            ItemDialog(
                title = "Add Shopping Item",
                existingNames = null, // duplicates are merged on the shopping list instead
                onDismiss = { showAddDialog = false },
                onSave = { name, qty, unit ->
                    viewModel.addShoppingItem(name, qty, unit)
                    showAddDialog = false
                },
            )
        }
    }

    editingItem?.let { item ->
        ItemDialog(
            title = "Edit Pantry Item",
            initialName = item.name,
            initialQuantity = formatQty(item.quantity),
            initialUnit = item.unit,
            // the item's own name is allowed; renaming it to another existing item is not
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
            title = { Text("Time's up!") },
            text = { Text("Your timer has finished.") },
            confirmButton = { Button(onClick = { timer.dismissTimeUp() }) { Text("Stop alarm") } },
        )
    }
}

// ======================================================================
// Pantry tab
// ======================================================================

@Composable
private fun PantryTab(
    pantry: List<PantryItemEntity>,
    onEdit: (PantryItemEntity) -> Unit,
    onDelete: (String) -> Unit,
) {
    if (pantry.isEmpty()) {
        EmptyState(
            title = "Your pantry is empty",
            body = "Tap + to add what you have at home. It's used to suggest recipes and work out what you need to buy.",
        )
        return
    }

    LazyColumn(
        contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 88.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        items(pantry, key = { it.id }) { item ->
            Row(
                modifier = Modifier.fillMaxWidth().clickable { onEdit(item) },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(item.name, fontWeight = FontWeight.Medium)
                    if (item.pendingSync) {
                        Text(
                            "Saved on this phone · not synced yet",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("${formatQty(item.quantity)} ${item.unit}".trim())
                    IconButton(onClick = { onEdit(item) }) {
                        Icon(Icons.Filled.Edit, contentDescription = "Edit ${item.name}")
                    }
                    IconButton(onClick = { onDelete(item.id) }) {
                        Icon(Icons.Filled.Delete, contentDescription = "Remove ${item.name}")
                    }
                }
            }
            HorizontalDivider()
        }
    }
}

// ======================================================================
// Shopping list tab
// ======================================================================

private enum class ClearAction { BOUGHT, ALL }

@Composable
private fun ShoppingTab(
    list: List<ShoppingListItemEntity>,
    onToggle: (String, Boolean) -> Unit,
    onDelete: (String) -> Unit,
    onClearBought: () -> Unit,
    onClearAll: () -> Unit,
) {
    if (list.isEmpty()) {
        EmptyState(
            title = "Your shopping list is empty",
            body = "Tap + to add an item, or use \"Add missing to list\" on a recipe.",
        )
        return
    }

    // The ViewModel already sorts bought items last; this just splits them for the header.
    val toBuy = list.filter { !it.isBought }
    val bought = list.filter { it.isBought }

    var menuOpen by remember { mutableStateOf(false) }
    var pendingClear by remember { mutableStateOf<ClearAction?>(null) }

    Column(modifier = Modifier.fillMaxSize()) {
        // Summary + Clear menu
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 8.dp, top = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                "${toBuy.size} to buy · ${bought.size} bought",
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.weight(1f),
            )
            Box {
                TextButton(onClick = { menuOpen = true }) {
                    Icon(Icons.Filled.ClearAll, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text("Clear")
                }
                DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                    DropdownMenuItem(
                        text = { Text("Clear bought items") },
                        enabled = bought.isNotEmpty(),
                        onClick = {
                            menuOpen = false
                            pendingClear = ClearAction.BOUGHT
                        },
                    )
                    DropdownMenuItem(
                        text = { Text("Clear entire list") },
                        onClick = {
                            menuOpen = false
                            pendingClear = ClearAction.ALL
                        },
                    )
                }
            }
        }
        HorizontalDivider()

        LazyColumn(
            contentPadding = PaddingValues(start = 8.dp, top = 8.dp, end = 8.dp, bottom = 88.dp),
        ) {
            items(toBuy, key = { it.id }) { item ->
                ShoppingRow(item, onToggle, onDelete, Modifier.animateItem())
            }

            if (bought.isNotEmpty()) {
                item(key = "bought_header") {
                    Text(
                        "In the basket · ${bought.size}",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 8.dp, top = 16.dp, bottom = 4.dp),
                    )
                }
                items(bought, key = { it.id }) { item ->
                    ShoppingRow(item, onToggle, onDelete, Modifier.animateItem())
                }
            }
        }
    }

    pendingClear?.let { action ->
        val count = if (action == ClearAction.BOUGHT) bought.size else list.size
        val noun = if (count == 1) "item" else "items"
        AlertDialog(
            onDismissRequest = { pendingClear = null },
            title = { Text(if (action == ClearAction.BOUGHT) "Clear bought items?" else "Clear entire list?") },
            text = {
                Text(
                    if (action == ClearAction.BOUGHT) "This removes the $count ticked $noun from your list."
                    else "This removes all $count $noun, including ones you haven't bought yet.",
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (action == ClearAction.BOUGHT) onClearBought() else onClearAll()
                        pendingClear = null
                    },
                ) { Text("Clear", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { pendingClear = null }) { Text("Cancel") } },
        )
    }
}

@Composable
private fun ShoppingRow(
    item: ShoppingListItemEntity,
    onToggle: (String, Boolean) -> Unit,
    onDelete: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    // Ticked = scratched off and faded
    val textColor =
        if (item.isBought) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f)
        else MaterialTheme.colorScheme.onSurface
    val decoration = if (item.isBought) TextDecoration.LineThrough else TextDecoration.None

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onToggle(item.id, !item.isBought) },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Checkbox(
            checked = item.isBought,
            onCheckedChange = { onToggle(item.id, it) },
        )
        Text(
            item.name,
            color = textColor,
            textDecoration = decoration,
            modifier = Modifier.weight(1f),
        )
        Text(
            "${formatQty(item.quantity)} ${item.unit}".trim(),
            color = textColor,
            textDecoration = decoration,
        )
        IconButton(onClick = { onDelete(item.id) }) {
            Icon(Icons.Filled.Delete, contentDescription = "Remove ${item.name}")
        }
    }
}

@Composable
private fun EmptyState(title: String, body: String) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(title, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
        Spacer(Modifier.height(8.dp))
        Text(
            body,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

// ======================================================================
// Add item dialog (with validation)
// ======================================================================

private val UNIT_OPTIONS = listOf("g", "kg", "ml", "L", "cups", "tbsp", "tsp", "pcs", "oz", "lb")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UnitDropdown(
    selected: String,
    options: List<String>,
    onSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier,
    ) {
        OutlinedTextField(
            value = selected,
            onValueChange = {},
            readOnly = true,
            label = { Text("Unit") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor(),
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onSelected(option)
                        expanded = false
                    },
                )
            }
        }
    }
}

/**
 * Used for adding and editing. Pass the initial* values to pre-fill it when editing.
 * @param existingNames lower-cased names already present; pass null to skip the duplicate check.
 */
@Composable
private fun ItemDialog(
    title: String,
    existingNames: Set<String>?,
    onDismiss: () -> Unit,
    onSave: (String, Double, String) -> Unit,
    initialName: String = "",
    initialQuantity: String = "",
    initialUnit: String = "g",
) {
    var name by remember { mutableStateOf(initialName) }
    var quantity by remember { mutableStateOf(initialQuantity) }
    var unit by remember { mutableStateOf(initialUnit.ifBlank { "g" }) }
    // Make sure an unusual unit (e.g. "cup" from a recipe) still appears in the list
    val unitOptions = remember(initialUnit) {
        if (initialUnit.isBlank() || initialUnit in UNIT_OPTIONS) UNIT_OPTIONS else listOf(initialUnit) + UNIT_OPTIONS
    }
    var submitted by remember { mutableStateOf(false) } // only show errors after the first Save tap

    val cleanName = name.trim().replace(Regex("\\s+"), " ")
    val parsedQuantity = quantity.replace(',', '.').toDoubleOrNull()

    val nameError: String? = when {
        cleanName.isEmpty() -> "Enter an item name"
        cleanName.none { it.isLetter() } -> "The name must contain letters"
        existingNames != null && cleanName.lowercase() in existingNames ->
            "\"$cleanName\" is already in your pantry"
        else -> null
    }
    val quantityError: String? = when {
        quantity.isBlank() -> "Enter a quantity"
        parsedQuantity == null -> "Enter a valid number"
        parsedQuantity <= 0.0 -> "Must be more than 0"
        parsedQuantity > MAX_QUANTITY -> "That looks too large"
        else -> null
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(modifier = Modifier.imePadding()) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it.take(MAX_NAME_LENGTH) },
                    label = { Text("Item name (e.g. Flour)") },
                    singleLine = true,
                    isError = submitted && nameError != null,
                    supportingText = { if (submitted && nameError != null) Text(nameError) },
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.Top) {
                    OutlinedTextField(
                        value = quantity,
                        onValueChange = { input ->
                            quantity = input.filter { it.isDigit() || it == '.' || it == ',' }.take(9)
                        },
                        label = { Text("Quantity") },
                        singleLine = true,
                        isError = submitted && quantityError != null,
                        supportingText = { if (submitted && quantityError != null) Text(quantityError) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f),
                    )
                    Spacer(Modifier.width(8.dp))
                    UnitDropdown(
                        selected = unit,
                        options = unitOptions,
                        onSelected = { unit = it },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    submitted = true
                    if (nameError == null && quantityError == null && parsedQuantity != null) {
                        onSave(cleanName, parsedQuantity, unit.trim())
                    }
                },
            ) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

// ======================================================================
// Kitchen tools: timer
// ======================================================================

/** Holds the timer so it survives tab switches. Must be created and used on the main thread. */
@Stable
private class KitchenTimerState(context: Context) {
    private val alarm = AlarmPlayer(context)
    private var countDown: CountDownTimer? = null

    var minutesInput by mutableStateOf("")
    var secondsInput by mutableStateOf("")

    var remainingMs by mutableLongStateOf(0L)
    var isRunning by mutableStateOf(false)
    var hasStarted by mutableStateOf(false) // true from Start until Reset / alarm dismissed
    var showTimeUp by mutableStateOf(false)

    fun start(totalSeconds: Int) {
        if (totalSeconds <= 0) return
        remainingMs = totalSeconds * 1000L
        hasStarted = true
        runCountdown(remainingMs)
    }

    fun resume() {
        if (hasStarted && !isRunning && remainingMs > 0L) runCountdown(remainingMs)
    }

    fun pause() {
        countDown?.cancel()
        isRunning = false
    }

    fun reset() {
        countDown?.cancel()
        alarm.stop()
        remainingMs = 0L
        isRunning = false
        hasStarted = false
        showTimeUp = false
    }

    fun dismissTimeUp() {
        alarm.stop()
        showTimeUp = false
        hasStarted = false
    }

    fun release() {
        countDown?.cancel()
        alarm.stop()
    }

    private fun runCountdown(ms: Long) {
        countDown?.cancel()
        countDown = object : CountDownTimer(ms, 250L) {
            override fun onTick(millisUntilFinished: Long) {
                remainingMs = millisUntilFinished
            }

            override fun onFinish() {
                remainingMs = 0L
                isRunning = false
                alarm.play()
                showTimeUp = true
            }
        }.start()
        isRunning = true
    }
}

@Composable
private fun KitchenToolsPanel(timer: KitchenTimerState) {
    val inputTotalSeconds =
        (timer.minutesInput.toIntOrNull() ?: 0) * 60 + (timer.secondsInput.toIntOrNull() ?: 0)
    val displaySeconds =
        if (timer.hasStarted) ((timer.remainingMs + 999) / 1000).toInt() else inputTotalSeconds

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
    ) {
        Text(
            text = String.format("%02d:%02d", displaySeconds / 60, displaySeconds % 60),
            style = MaterialTheme.typography.displayMedium,
            fontWeight = FontWeight.Bold,
        )

        Spacer(Modifier.height(16.dp))

        if (!timer.hasStarted) {
            // Quick timers start straight away
            Text("Quick timers", style = MaterialTheme.typography.labelLarge)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(1, 5, 10, 15).forEach { minutes ->
                    OutlinedButton(onClick = { timer.start(minutes * 60) }) { Text("${minutes}m") }
                }
            }

            Spacer(Modifier.height(20.dp))

            // Custom time
            Text("Or set a custom time", style = MaterialTheme.typography.labelLarge)
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = timer.minutesInput,
                    onValueChange = { new -> timer.minutesInput = new.filter { it.isDigit() }.take(3) },
                    label = { Text("Min") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.width(96.dp),
                )
                Text(
                    ":",
                    style = MaterialTheme.typography.headlineMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 8.dp),
                )
                OutlinedTextField(
                    value = timer.secondsInput,
                    onValueChange = { new ->
                        val digits = new.filter { it.isDigit() }.take(2)
                        timer.secondsInput = if ((digits.toIntOrNull() ?: 0) > 59) "59" else digits
                    },
                    label = { Text("Sec") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.width(96.dp),
                )
            }

            Spacer(Modifier.height(20.dp))

            Button(
                onClick = { timer.start(inputTotalSeconds) },
                enabled = inputTotalSeconds > 0,
            ) { Text("Start") }
        } else {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (timer.isRunning) {
                    Button(onClick = { timer.pause() }) { Text("Pause") }
                } else {
                    Button(onClick = { timer.resume() }) { Text("Resume") }
                }
                OutlinedButton(onClick = { timer.reset() }) { Text("Reset") }
            }
        }
    }
}