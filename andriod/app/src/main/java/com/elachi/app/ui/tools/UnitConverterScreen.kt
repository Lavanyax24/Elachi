package com.elachi.app.ui.tools

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

private val UNITS_BY_CATEGORY = mapOf(
    "Weight" to listOf("g", "kg", "oz", "lb"),
    "Volume" to listOf("ml", "l", "cups", "tbsp", "tsp", "fl oz"),
    "Temperature" to listOf("°C", "°F"),
)

private val TO_BASE_FACTOR = mapOf(
    "g" to 1.0, "kg" to 1000.0, "oz" to 28.3495, "lb" to 453.592,
    "ml" to 1.0, "l" to 1000.0, "cups" to 240.0, "tbsp" to 15.0,
    "tsp" to 5.0, "fl oz" to 29.5735,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnitConverterScreen() {
    var category by remember { mutableStateOf("Weight") }
    var fromUnit by remember { mutableStateOf("g") }
    var toUnit by remember { mutableStateOf("kg") }
    var value by remember { mutableStateOf("") }
    var fromExpanded by remember { mutableStateOf(false) }
    var toExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(category) {
        val units = UNITS_BY_CATEGORY[category] ?: listOf("g")
        fromUnit = units.first()
        toUnit = units.getOrElse(1) { units.first() }
    }

    val converted: Double? = value.toDoubleOrNull()?.let { input ->
        if (category == "Temperature") {
            when {
                fromUnit == "°C" && toUnit == "°F" -> input * 9 / 5 + 32
                fromUnit == "°F" && toUnit == "°C" -> (input - 32) * 5 / 9
                else -> input
            }
        } else {
            val base = input * (TO_BASE_FACTOR[fromUnit] ?: 1.0)
            base / (TO_BASE_FACTOR[toUnit] ?: 1.0)
        }
    }

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Category", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurface)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            UNITS_BY_CATEGORY.keys.forEach { cat ->
                FilterChip(
                    selected = category == cat,
                    onClick = { category = cat },
                    label = { Text(cat) },
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = MaterialTheme.colorScheme.primary, selectedLabelColor = androidx.compose.ui.graphics.Color.White)
                )
            }
        }

        OutlinedTextField(
            value = value,
            onValueChange = { value = it },
            label = { Text("Enter value") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = MaterialTheme.colorScheme.surface, unfocusedContainerColor = MaterialTheme.colorScheme.surface)
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ExposedDropdownMenuBox(expanded = fromExpanded, onExpandedChange = { fromExpanded = it }, modifier = Modifier.weight(1f)) {
                OutlinedTextField(
                    value = fromUnit, onValueChange = {}, readOnly = true, label = { Text("From") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(fromExpanded) },
                    modifier = Modifier.menuAnchor(androidx.compose.material3.MenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = MaterialTheme.colorScheme.surface, unfocusedContainerColor = MaterialTheme.colorScheme.surface)
                )
                ExposedDropdownMenu(expanded = fromExpanded, onDismissRequest = { fromExpanded = false }, containerColor = MaterialTheme.colorScheme.surface) {
                    (UNITS_BY_CATEGORY[category] ?: emptyList()).forEach { u -> DropdownMenuItem(text = { Text(u) }, onClick = { fromUnit = u; fromExpanded = false }) }
                }
            }
            ExposedDropdownMenuBox(expanded = toExpanded, onExpandedChange = { toExpanded = it }, modifier = Modifier.weight(1f)) {
                OutlinedTextField(
                    value = toUnit, onValueChange = {}, readOnly = true, label = { Text("To") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(toExpanded) },
                    modifier = Modifier.menuAnchor(androidx.compose.material3.MenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = MaterialTheme.colorScheme.surface, unfocusedContainerColor = MaterialTheme.colorScheme.surface)
                )
                ExposedDropdownMenu(expanded = toExpanded, onDismissRequest = { toExpanded = false }, containerColor = MaterialTheme.colorScheme.surface) {
                    (UNITS_BY_CATEGORY[category] ?: emptyList()).forEach { u -> DropdownMenuItem(text = { Text(u) }, onClick = { toUnit = u; toExpanded = false }) }
                }
            }
        }

        Surface(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("Result", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(6.dp))
                Text(text = converted?.let { "${"%.2f".format(it)} $toUnit" } ?: "—", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}
