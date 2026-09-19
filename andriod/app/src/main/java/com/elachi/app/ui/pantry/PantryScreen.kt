package com.elachi.app.ui.pantry

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import android.media.Ringtone
import android.media.RingtoneManager
import android.os.CountDownTimer
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantryScreen(viewModel: PantryViewModel) {
    val pantryItems by viewModel.pantryItems.collectAsState()
    val shoppingList by viewModel.shoppingList.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var tab by remember { mutableIntStateOf(0) }

    Scaffold(
        // ScreenShell in NavGraph already provides the top bar and handles insets
        contentWindowInsets = WindowInsets(0.dp, 0.dp, 0.dp, 0.dp),
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
                0 -> LazyColumn(
                    contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 88.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    items(pantryItems, key = { it.id }) { item ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(item.name, fontWeight = FontWeight.Medium)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("${item.quantity} ${item.unit}")
                                IconButton(onClick = { viewModel.deletePantryItem(item.id) }) {
                                    Icon(Icons.Filled.Delete, contentDescription = "Remove")
                                }
                            }
                        }
                        HorizontalDivider()
                    }
                }

                1 -> LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    items(shoppingList, key = { it.id }) { item ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(
                                    checked = item.isBought,
                                    onCheckedChange = { viewModel.toggleBought(item.id, it) },
                                )
                                Text(item.name)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("${item.quantity} ${item.unit}")
                                IconButton(onClick = { viewModel.deleteShoppingItem(item.id) }) {
                                    Icon(Icons.Filled.Delete, contentDescription = "Remove")
                                }
                            }
                        }
                    }
                }

                2 -> KitchenToolsPanel()
            }
        }



        if (showAddDialog) {
            if (tab == 0) {
                AddItemDialog(
                    title = "Add Pantry Item",
                    onDismiss = { showAddDialog = false },
                    onAdd = { name, qty, unit ->
                        viewModel.addPantryItem(name, qty, unit)
                        showAddDialog = false
                    },
                )
            } else {
                AddItemDialog(
                    title = "Add Shopping Item",
                    onDismiss = { showAddDialog = false },
                    onAdd = { name, qty, unit ->
                        viewModel.addShoppingItem(name, qty, unit)
                        showAddDialog = false
                    },
                )
            }
        }
    }
}

private val UNIT_OPTIONS = listOf("g", "kg", "ml", "L", "cups", "tbsp", "tsp", "pcs", "oz", "lb")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UnitDropdown(
    selected: String,
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
            UNIT_OPTIONS.forEach { option ->
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

@Composable
private fun AddItemDialog(
    title: String,
    onDismiss: () -> Unit,
    onAdd: (String, Double, String) -> Unit,
) {
    var name by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf("g") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(modifier = Modifier.imePadding()) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Item name (e.g. Flour)") },
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(8.dp))
                Row {
                    OutlinedTextField(
                        value = quantity,
                        onValueChange = { quantity = it },
                        label = { Text("Quantity") },
                        modifier = Modifier.weight(1f),
                    )
                    Spacer(Modifier.width(8.dp))
                    // Replaced with a dropdown in commit 5a.3
                    UnitDropdown(
                        selected = unit,
                        onSelected = { unit = it },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onAdd(name.trim(), quantity.toDoubleOrNull() ?: 0.0, unit.trim()) },
                enabled = name.isNotBlank(),
            ) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

/**
 * Kitchen Tools now only contains the timer.
 * The unit converter and calculator moved to Phase 5b (Saa'diyah).
 */
@Composable
private fun KitchenToolsPanel() {
    Column(modifier = Modifier.padding(16.dp)) {
        StandaloneTimer()
    }
}

@Composable
private fun StandaloneTimer() {
    val context = LocalContext.current

    // What the user types
    var minutesInput by remember { mutableStateOf("") }
    var secondsInput by remember { mutableStateOf("") }

    // Timer state
    var remainingMs by remember { mutableLongStateOf(0L) }
    var isRunning by remember { mutableStateOf(false) }
    var hasStarted by remember { mutableStateOf(false) }   // true once Start pressed, until Reset
    var isFinished by remember { mutableStateOf(false) }

    var timer by remember { mutableStateOf<CountDownTimer?>(null) }
    var ringtone by remember { mutableStateOf<Ringtone?>(null) }

    fun stopSound() {
        ringtone?.stop()
        ringtone = null
    }

    fun playSound() {
        stopSound()
        val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        ringtone = RingtoneManager.getRingtone(context, uri)?.also { it.play() }
    }

    fun startCountdown(fromMs: Long) {
        timer?.cancel()
        timer = object : CountDownTimer(fromMs, 250L) {
            override fun onTick(millisUntilFinished: Long) {
                remainingMs = millisUntilFinished
            }

            override fun onFinish() {
                remainingMs = 0L
                isRunning = false
                isFinished = true
                playSound()
            }
        }.start()
        isRunning = true
    }

    // Stop everything if the user leaves this screen
    DisposableEffect(Unit) {
        onDispose {
            timer?.cancel()
            ringtone?.stop()
        }
    }

    val inputTotalSeconds =
        (minutesInput.toIntOrNull() ?: 0) * 60 + (secondsInput.toIntOrNull() ?: 0)

    // Before starting, show what will be counted down; afterwards show the live value
    val displaySeconds =
        if (hasStarted) ((remainingMs + 999) / 1000).toInt() else inputTotalSeconds

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth(),
    ) {
        // MM:SS display
        Text(
            text = String.format("%02d:%02d", displaySeconds / 60, displaySeconds % 60),
            style = MaterialTheme.typography.displayMedium,
            fontWeight = FontWeight.Bold,
        )
        if (isFinished) {
            Text(
                "Time's up!",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
            )
        }

        Spacer(Modifier.height(20.dp))

        // Custom minutes + seconds inputs (locked while the timer is active)
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = minutesInput,
                onValueChange = { new ->
                    minutesInput = new.filter { it.isDigit() }.take(3)
                },
                label = { Text("Min") },
                singleLine = true,
                enabled = !hasStarted,
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
                value = secondsInput,
                onValueChange = { new ->
                    val digits = new.filter { it.isDigit() }.take(2)
                    // Keep seconds within 0–59
                    secondsInput = if ((digits.toIntOrNull() ?: 0) > 59) "59" else digits
                },
                label = { Text("Sec") },
                singleLine = true,
                enabled = !hasStarted,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.width(96.dp),
            )
        }

        Spacer(Modifier.height(20.dp))

        // Start / Pause / Reset
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = {
                    if (!hasStarted) {
                        remainingMs = inputTotalSeconds * 1000L
                        hasStarted = true
                    }
                    startCountdown(remainingMs)
                },
                enabled = !isRunning && !isFinished && (hasStarted || inputTotalSeconds > 0),
            ) { Text(if (hasStarted) "Resume" else "Start") }

            OutlinedButton(
                onClick = {
                    timer?.cancel()
                    isRunning = false
                },
                enabled = isRunning,
            ) { Text("Pause") }

            OutlinedButton(
                onClick = {
                    timer?.cancel()
                    stopSound()
                    remainingMs = 0L
                    isRunning = false
                    isFinished = false
                    hasStarted = false
                },
                enabled = hasStarted,
            ) { Text("Reset") }
        }
    }
}