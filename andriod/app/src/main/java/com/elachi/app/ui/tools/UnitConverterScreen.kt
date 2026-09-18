package com.elachi.app.ui.tools

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.elachi.app.ui.theme.ElachiGreen

private val UNITS_BY_CATEGORY = mapOf(
    "Weight" to listOf("g", "kg", "oz", "lb"),
    "Volume" to listOf("ml", "l", "cups", "tbsp", "tsp", "fl oz"),
    "Temperature" to listOf("°C", "°F"),
)

// Factor to convert each unit to its base (grams for weight, ml for volume)
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

    // When category changes, reset units to that category's first two options
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            "Unit Converter",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            color = ElachiGreen,
        )

        // Category chips
        Text("Category", style = MaterialTheme.typography.labelLarge, color = Color(0xFF45483E))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            UNITS_BY_CATEGORY.keys.forEach { cat ->
                FilterChip(
                    selected = category == cat,
                    onClick = { category = cat },
                    label = { Text(cat) },
                )
            }
        }

        // Input value
        OutlinedTextField(
            value = value,
            onValueChange = { value = it },
            label = { Text("Enter value") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
        )

        // From / To dropdowns
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ExposedDropdownMenuBox(
                expanded = fromExpanded,
                onExpandedChange = { fromExpanded = it },
                modifier = Modifier.weight(1f),
            ) {
                OutlinedTextField(
                    value = fromUnit,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("From") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(fromExpanded) },
                    modifier = Modifier.menuAnchor(
                        androidx.compose.material3.MenuAnchorType.PrimaryNotEditable,
                    ).fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                )
                ExposedDropdownMenu(
                    expanded = fromExpanded,
                    onDismissRequest = { fromExpanded = false },
                ) {
                    (UNITS_BY_CATEGORY[category] ?: emptyList()).forEach { u ->
                        DropdownMenuItem(
                            text = { Text(u) },
                            onClick = { fromUnit = u; fromExpanded = false },
                        )
                    }
                }
            }

            ExposedDropdownMenuBox(
                expanded = toExpanded,
                onExpandedChange = { toExpanded = it },
                modifier = Modifier.weight(1f),
            ) {
                OutlinedTextField(
                    value = toUnit,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("To") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(toExpanded) },
                    modifier = Modifier.menuAnchor(
                        androidx.compose.material3.MenuAnchorType.PrimaryNotEditable,
                    ).fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                )
                ExposedDropdownMenu(
                    expanded = toExpanded,
                    onDismissRequest = { toExpanded = false },
                ) {
                    (UNITS_BY_CATEGORY[category] ?: emptyList()).forEach { u ->
                        DropdownMenuItem(
                            text = { Text(u) },
                            onClick = { toUnit = u; toExpanded = false },
                        )
                    }
                }
            }
        }

        // Result card
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = ElachiGreen.copy(alpha = 0.08f),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    "Result",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color(0xFF45483E),
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = converted?.let { "${"%.2f".format(it)} $toUnit" } ?: "—",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = ElachiGreen,
                )
            }
        }

        Spacer(Modifier.height(24.dp))
    }
}