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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elachi.app.ui.theme.ElachiGreen

@Composable
fun CalculatorScreen() {
    var display by remember { mutableStateOf("0") }
    var pendingOp by remember { mutableStateOf<Char?>(null) }
    var storedValue by remember { mutableStateOf(0.0) }
    var waitingForOperand by remember { mutableStateOf(false) }

    // formatNumber MUST be defined above applyOp, because applyOp calls it.
    fun formatNumber(value: Double): String {
        return if (value == value.toLong().toDouble()) {
            value.toLong().toString()
        } else {
            "%.4f".format(value).trimEnd('0').trimEnd('.')
        }
    }

    fun applyOp() {
        val current = display.toDoubleOrNull() ?: 0.0
        val result = when (pendingOp) {
            '+' -> storedValue + current
            '-' -> storedValue - current
            '×' -> storedValue * current
            '÷' -> if (current != 0.0) storedValue / current else 0.0
            else -> current
        }
        display = formatNumber(result)
        storedValue = result
    }

    fun onDigit(digit: String) {
        display = if (waitingForOperand || display == "0") digit else display + digit
        waitingForOperand = false
    }

    fun onOperator(op: Char) {
        if (pendingOp != null && !waitingForOperand) {
            applyOp()
        } else {
            storedValue = display.toDoubleOrNull() ?: 0.0
        }
        pendingOp = op
        waitingForOperand = true
    }

    fun onEquals() {
        if (pendingOp != null) {
            applyOp()
            pendingOp = null
            waitingForOperand = true
        }
    }

    fun onClear() {
        display = "0"
        storedValue = 0.0
        pendingOp = null
        waitingForOperand = false
    }

    fun onDecimal() {
        if (waitingForOperand) {
            display = "0."
            waitingForOperand = false
        } else if (!display.contains(".")) {
            display += "."
        }
    }

    fun onBackspace() {
        if (display.length > 1) {
            display = display.dropLast(1)
        } else {
            display = "0"
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            "Calculator",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            color = ElachiGreen,
        )

        // Display
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF1C1C19),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier.padding(20.dp).fillMaxWidth(),
                horizontalAlignment = Alignment.End,
            ) {
                Text(
                    display,
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontSize = 44.sp,
                        fontWeight = FontWeight.Medium,
                    ),
                    color = Color.White,
                    maxLines = 1,
                )
            }
        }

        Spacer(Modifier.height(4.dp))

        // Keypad
        val keypad = listOf(
            listOf("C", "⌫", "%", "÷"),
            listOf("7", "8", "9", "×"),
            listOf("4", "5", "6", "-"),
            listOf("1", "2", "3", "+"),
            listOf("0", ".", "", "="),
        )

        keypad.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                row.forEach { key ->
                    if (key.isBlank()) {
                        Spacer(modifier = Modifier.weight(1f))
                    } else {
                        CalculatorKey(
                            label = key,
                            modifier = Modifier.weight(1f),
                            onClick = {
                                when (key) {
                                    "C" -> onClear()
                                    "⌫" -> onBackspace()
                                    "%" -> {
                                        val current = display.toDoubleOrNull() ?: 0.0
                                        display = formatNumber(current / 100)
                                    }
                                    "+", "-", "×", "÷" -> onOperator(key[0])
                                    "=" -> onEquals()
                                    "." -> onDecimal()
                                    else -> onDigit(key)
                                }
                            },
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun CalculatorKey(
    label: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val isOperator = label in listOf("+", "-", "×", "÷", "=")
    val isFunction = label in listOf("C", "⌫", "%")

    val container = when {
        isOperator -> ElachiGreen
        isFunction -> Color(0xFFF0EDE9)
        else -> Color.White
    }
    val content = when {
        isOperator -> Color.White
        isFunction -> Color(0xFF45483E)
        else -> Color(0xFF1C1C19)
    }

    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(64.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.outlinedButtonColors(containerColor = container),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = if (isOperator) ElachiGreen else Color(0xFFE5E2DD),
        ),
    ) {
        Text(
            label,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, fontSize = 22.sp),
            color = content,
        )
    }
}