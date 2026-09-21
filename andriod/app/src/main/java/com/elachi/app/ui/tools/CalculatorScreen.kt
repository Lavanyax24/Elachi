package com.elachi.app.ui.tools

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CalculatorScreen() {
    var expression by remember { mutableStateOf("") }
    var currentInput by remember { mutableStateOf("0") }
    var result by remember { mutableStateOf<String?>(null) }
    var startFresh by remember { mutableStateOf(true) }

    fun formatNumber(value: Double): String = if (value == value.toLong().toDouble()) value.toLong().toString() else "%.4f".format(value).trimEnd('0').trimEnd('.')

    fun onDigit(digit: String) {
        if (result != null) { expression = ""; result = null }
        currentInput = if (startFresh || currentInput == "0") digit else currentInput + digit
        startFresh = false
    }

    fun onDecimal() {
        if (result != null) { expression = ""; result = null }
        if (startFresh) { currentInput = "0."; startFresh = false } else if (!currentInput.contains(".")) currentInput += "."
    }

    fun onOperator(op: String) {
        if (result != null) { expression = "$result $op "; currentInput = result!!; result = null; startFresh = true; return }
        if (!startFresh) expression += currentInput + " $op " else expression = expression.trimEnd().dropLast(1).trimEnd() + " $op "
        startFresh = true
    }

    fun onEquals() {
        if (result != null) return
        val fullExpr = if (!startFresh) expression + currentInput else expression.trimEnd()
        if (fullExpr.isBlank()) return
        val evaluated = evaluateExpression(fullExpr)
        if (evaluated != null) { expression = fullExpr; result = formatNumber(evaluated); currentInput = result!!; startFresh = true }
    }

    fun onClear() { expression = ""; currentInput = "0"; result = null; startFresh = true }
    fun onBackspace() { if (result != null) onClear() else currentInput = if (currentInput.length > 1) currentInput.dropLast(1) else "0" }
    fun onPercent() { currentInput = formatNumber((currentInput.toDoubleOrNull() ?: 0.0) / 100); startFresh = false }

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Surface(shape = RoundedCornerShape(16.dp), color = Color(0xFF1C1C19), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(20.dp).fillMaxWidth(), horizontalAlignment = Alignment.End) {
                val topLine = when { result != null -> "$expression ="; expression.isNotEmpty() -> expression + if (startFresh) "" else currentInput; else -> "" }
                Text(text = topLine.ifBlank { " " }, style = MaterialTheme.typography.titleMedium, color = Color.White.copy(alpha = 0.55f), textAlign = TextAlign.End, maxLines = 2)
                Spacer(Modifier.height(8.dp))
                Text(text = result ?: currentInput, style = MaterialTheme.typography.headlineLarge.copy(fontSize = 44.sp, fontWeight = FontWeight.Medium), color = Color.White, maxLines = 1, textAlign = TextAlign.End)
            }
        }
        Spacer(Modifier.height(4.dp))
        val keypad = listOf(listOf("C", "⌫", "%", "÷"), listOf("7", "8", "9", "×"), listOf("4", "5", "6", "-"), listOf("1", "2", "3", "+"), listOf("0", ".", "", "="))
        keypad.forEach { row ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { key -> if (key.isBlank()) Spacer(modifier = Modifier.weight(1f)) else CalculatorKey(label = key, modifier = Modifier.weight(1f), onClick = { when (key) { "C" -> onClear(); "⌫" -> onBackspace(); "%" -> onPercent(); "+", "-", "×", "÷" -> onOperator(key); "=" -> onEquals(); "." -> onDecimal(); else -> onDigit(key) } }) }
            }
        }
    }
}

private fun evaluateExpression(expr: String): Double? {
    return try {
        val tokens = mutableListOf<String>(); val cleaned = expr.replace(" ", ""); var i = 0
        while (i < cleaned.length) { val c = cleaned[i]; when { c.isDigit() || c == '.' -> { val s = i; while (i < cleaned.length && (cleaned[i].isDigit() || cleaned[i] == '.')) i++; tokens += cleaned.substring(s, i) }; c in "+-×÷" -> { tokens += c.toString(); i++ }; else -> return null } }
        if (tokens.isEmpty()) return null
        val p1 = mutableListOf<String>(); var idx = 0
        while (idx < tokens.size) { val t = tokens[idx]; if (t == "×" || t == "÷") { val l = p1.removeLastOrNull()?.toDoubleOrNull() ?: return null; val r = tokens.getOrNull(idx + 1)?.toDoubleOrNull() ?: return null; p1 += (if (t == "×") l * r else if (r == 0.0) return null else l / r).toString(); idx += 2 } else { p1 += t; idx++ } }
        if (p1.isEmpty()) return null; var res = p1[0].toDoubleOrNull() ?: return null; var j = 1
        while (j < p1.size) { val op = p1[j]; val r = p1.getOrNull(j + 1)?.toDoubleOrNull() ?: return null; res = when (op) { "+" -> res + r; "-" -> res - r; else -> return null }; j += 2 }
        res
    } catch (e: Exception) { null }
}

@Composable
private fun CalculatorKey(label: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val isOp = label in listOf("+", "-", "×", "÷", "=")
    val isFn = label in listOf("C", "⌫", "%")
    val container = when { isOp -> MaterialTheme.colorScheme.primary; isFn -> MaterialTheme.colorScheme.surfaceVariant; else -> MaterialTheme.colorScheme.surface }
    val content = when { isOp -> Color.White; isFn -> MaterialTheme.colorScheme.onSurfaceVariant; else -> MaterialTheme.colorScheme.onSurface }
    OutlinedButton(
        onClick = onClick, 
        modifier = modifier.height(64.dp), 
        shape = RoundedCornerShape(16.dp), 
        colors = ButtonDefaults.outlinedButtonColors(containerColor = container),
        border = androidx.compose.foundation.BorderStroke(1.dp, if (isOp) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
    ) {
        Text(label, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, fontSize = 22.sp), color = content)
    }
}
