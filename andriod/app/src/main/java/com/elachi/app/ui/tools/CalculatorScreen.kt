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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elachi.app.ui.theme.ElachiGreen

@Composable
fun CalculatorScreen() {
    // The full expression being typed, e.g. "7 + 5 + 4"
    var expression by remember { mutableStateOf("") }
    // The current number being typed
    var currentInput by remember { mutableStateOf("0") }
    // The result shown after "=" was pressed
    var result by remember { mutableStateOf<String?>(null) }
    // True when we just pressed an operator or =, so the next digit starts a fresh number
    var startFresh by remember { mutableStateOf(true) }

    fun formatNumber(value: Double): String {
        return if (value == value.toLong().toDouble()) {
            value.toLong().toString()
        } else {
            "%.4f".format(value).trimEnd('0').trimEnd('.')
        }
    }

    fun onDigit(digit: String) {
        // If we just pressed = and start typing a new number, clear everything
        if (result != null) {
            expression = ""
            result = null
        }
        currentInput = if (startFresh || currentInput == "0") digit else currentInput + digit
        startFresh = false
    }

    fun onDecimal() {
        if (result != null) {
            expression = ""
            result = null
        }
        if (startFresh) {
            currentInput = "0."
            startFresh = false
        } else if (!currentInput.contains(".")) {
            currentInput += "."
        }
    }

    fun onOperator(op: String) {
        if (result != null) {
            // Reuse the previous result as the start of a new expression
            expression = "$result $op "
            currentInput = result!!
            result = null
            startFresh = true
            return
        }
        if (!startFresh) {
            // Finish the current number in the expression
            expression += currentInput + " $op "
        } else {
            // Replace the trailing operator (user changed their mind)
            expression = expression.trimEnd().dropLast(1).trimEnd() + " $op "
        }
        startFresh = true
    }

    fun onEquals() {
        if (result != null) return
        val fullExpr = if (!startFresh) expression + currentInput else expression.trimEnd()
        if (fullExpr.isBlank()) return
        val evaluated = evaluateExpression(fullExpr)
        if (evaluated != null) {
            expression = fullExpr
            result = formatNumber(evaluated)
            currentInput = result!!
            startFresh = true
        }
    }

    fun onClear() {
        expression = ""
        currentInput = "0"
        result = null
        startFresh = true
    }

    fun onBackspace() {
        if (result != null) {
            onClear()
            return
        }
        currentInput = if (currentInput.length > 1) currentInput.dropLast(1) else "0"
    }

    fun onPercent() {
        val current = currentInput.toDoubleOrNull() ?: 0.0
        currentInput = formatNumber(current / 100)
        startFresh = false
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

        // Display — shows the full expression on top, current number big at the bottom
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF1C1C19),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier.padding(20.dp).fillMaxWidth(),
                horizontalAlignment = Alignment.End,
            ) {
                // Small top line — shows the expression so far
                val topLine = when {
                    result != null -> "$expression ="
                    expression.isNotEmpty() -> expression + if (startFresh) "" else currentInput
                    else -> ""
                }
                Text(
                    text = topLine.ifBlank { " " },
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White.copy(alpha = 0.55f),
                    textAlign = TextAlign.End,
                    maxLines = 2,
                )
                Spacer(Modifier.height(8.dp))
                // Big bottom line — shows the result if = was pressed, otherwise the current number
                Text(
                    text = result ?: currentInput,
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontSize = 44.sp,
                        fontWeight = FontWeight.Medium,
                    ),
                    color = Color.White,
                    maxLines = 1,
                    textAlign = TextAlign.End,
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
                                    "%" -> onPercent()
                                    "+", "-", "×", "÷" -> onOperator(key)
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

/**
 * Evaluates a simple expression like "7 + 5 + 4" or "500 × 10" respecting
 * operator precedence (× and ÷ before + and -). Supports only the four
 * basic operators and decimal numbers. Returns null if the expression
 * can't be parsed.
 */
private fun evaluateExpression(expr: String): Double? {
    return try {
        // Tokenize: split into numbers and operators
        val tokens = mutableListOf<String>()
        val cleaned = expr.replace(" ", "")
        var i = 0
        while (i < cleaned.length) {
            val c = cleaned[i]
            when {
                c.isDigit() || c == '.' -> {
                    val start = i
                    while (i < cleaned.length && (cleaned[i].isDigit() || cleaned[i] == '.')) i++
                    tokens += cleaned.substring(start, i)
                }
                c in "+-×÷" -> {
                    tokens += c.toString()
                    i++
                }
                else -> return null
            }
        }

        if (tokens.isEmpty()) return null

        // First pass: × and ÷
        val pass1 = mutableListOf<String>()
        var idx = 0
        while (idx < tokens.size) {
            val token = tokens[idx]
            if (token == "×" || token == "÷") {
                val left = pass1.removeLastOrNull()?.toDoubleOrNull() ?: return null
                val right = tokens.getOrNull(idx + 1)?.toDoubleOrNull() ?: return null
                val value = if (token == "×") left * right else {
                    if (right == 0.0) return null
                    left / right
                }
                pass1 += value.toString()
                idx += 2
            } else {
                pass1 += token
                idx++
            }
        }

        // Second pass: + and -
        if (pass1.isEmpty()) return null
        var result = pass1[0].toDoubleOrNull() ?: return null
        var j = 1
        while (j < pass1.size) {
            val op = pass1[j]
            val right = pass1.getOrNull(j + 1)?.toDoubleOrNull() ?: return null
            result = when (op) {
                "+" -> result + right
                "-" -> result - right
                else -> return null
            }
            j += 2
        }
        result
    } catch (e: Exception) {
        null
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