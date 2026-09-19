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
import com.elachi.app.ui.common.ElachiTopBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantryScreen(viewModel: PantryViewModel) {
    val pantryItems by viewModel.pantryItems.collectAsState()
    val shoppingList by viewModel.shoppingList.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var tab by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            ElachiTopBar(
                title = "Pantry & Tools",
                actions = {
                    // The add button only makes sense on the Pantry and Shopping List tabs
                    if (tab != 2) {
                        IconButton(onClick = { showAddDialog = true }) {
                            Icon(
                                Icons.Filled.Add,
                                contentDescription = if (tab == 0) "Add pantry item" else "Add shopping item",
                            )
                        }
                    }
                }
            )
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
                    contentPadding = PaddingValues(16.dp),
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
    // Reuses the same CountDownTimer pattern as Cook Mode's per-step timer.
    // Redesigned in commit 5a.4.
    var secondsLeft by remember { mutableIntStateOf(0) }
    var timer by remember { mutableStateOf<android.os.CountDownTimer?>(null) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(
            String.format("%02d:%02d", secondsLeft / 60, secondsLeft % 60),
            style = MaterialTheme.typography.headlineMedium,
        )
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(1, 5, 10, 15, 30).forEach { minutes ->
                OutlinedButton(onClick = {
                    timer?.cancel()
                    secondsLeft = minutes * 60
                    timer = object : android.os.CountDownTimer(minutes * 60_000L, 1000L) {
                        override fun onTick(millisUntilFinished: Long) {
                            secondsLeft = (millisUntilFinished / 1000).toInt()
                        }

                        override fun onFinish() {
                            secondsLeft = 0
                        }
                    }.start()
                }) { Text("${minutes}m") }
            }
        }
    }
}