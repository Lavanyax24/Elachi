package com.elachi.app.util


data class DraftIngredient(var name: String = "", var quantity: String = "", var unit: String = "")

data class ParsedRecipe(
    val title: String,
    val ingredients: List<DraftIngredient>,
    val steps: List<String>,
)


object RecipeTextParser {

    private val quantityUnitRegex = Regex(
        """^([\d\s./]+)\s*(gram|grams|g|kilogram|kilograms|kg|milliliter|milliliters|ml|liter|liters|l|teaspoon|teaspoons|tsp|tablespoon|tablespoons|tbsp|cup|cups|ounce|ounces|oz|pound|pounds|lb|lbs|pinch|pinches|clove|cloves|slice|slices|can|cans|package|pkg|handful)?\s+(.+)$""",
        RegexOption.IGNORE_CASE,
    )
    private val stepHeadingRegex = Regex("""^\s*(instructions?|method|steps?|directions?|prep|cooking)\s*:?\s*$""", RegexOption.IGNORE_CASE)
    private val ingredientHeadingRegex = Regex("""^\s*ingredients?\s*:?\s*$""", RegexOption.IGNORE_CASE)
    private val numberedLineRegex = Regex("""^\s*(\d+)[.)]\s*(.+)$""")
    private val fractionMap = mapOf(
        '½' to "1/2", '⅓' to "1/3", '⅔' to "2/3", '¼' to "1/4", '¾' to "3/4",
        '⅛' to "1/8", '⅜' to "3/8", '⅝' to "5/8", '⅞' to "7/8"
    )

    fun parse(rawLines: List<String>): ParsedRecipe {
        val lines = rawLines.map { it.trim() }.filter { it.isNotBlank() }
        if (lines.isEmpty()) return ParsedRecipe("", emptyList(), emptyList())

        var title = ""
        val potentialTitle = lines.firstOrNull { line ->
            line.length in 3..60 &&
                    !ingredientHeadingRegex.matches(line) &&
                    !stepHeadingRegex.matches(line) &&
                    quantityUnitRegex.matchEntire(line) == null
        }
        if (potentialTitle != null) {
            title = potentialTitle
        }

        val ingredients = mutableListOf<DraftIngredient>()
        val steps = mutableListOf<String>()

        var mode = Mode.UNKNOWN
        for (line in lines) {
            val normalizedLine = line.normalizeFractions()

            when {
                normalizedLine == title -> continue
                ingredientHeadingRegex.matches(normalizedLine) -> { mode = Mode.INGREDIENTS; continue }
                stepHeadingRegex.matches(normalizedLine) -> { mode = Mode.STEPS; continue }
            }

            val qtyMatch = quantityUnitRegex.matchEntire(normalizedLine)
            val numberedMatch = numberedLineRegex.matchEntire(normalizedLine)

            when {
                numberedMatch != null -> {
                    steps += numberedMatch.groupValues[2].trim()
                    mode = Mode.STEPS
                }
                qtyMatch != null -> {
                    val (qty, unit, name) = qtyMatch.destructured
                    ingredients += DraftIngredient(
                        name = name.trim().removePrefix("of ").trim(),
                        quantity = parseQuantity(qty.trim()).toString(),
                        unit = unit.trim().lowercase(),
                    )
                    if (mode == Mode.UNKNOWN) mode = Mode.INGREDIENTS
                }
                mode == Mode.STEPS -> {
                    if (normalizedLine.length > 5) steps += normalizedLine
                }
                mode == Mode.INGREDIENTS -> {
                    ingredients += DraftIngredient(name = normalizedLine, quantity = "", unit = "")
                }
                else -> {
                    if (normalizedLine.length > 50 || normalizedLine.endsWith(".")) {
                        steps += normalizedLine
                    } else {
                        ingredients += DraftIngredient(name = normalizedLine, quantity = "", unit = "")
                    }
                }
            }
        }

        return ParsedRecipe(
            title = title,
            ingredients = ingredients.ifEmpty { listOf(DraftIngredient()) },
            steps = steps.ifEmpty { listOf("") },
        )
    }

    private fun String.normalizeFractions(): String {
        var res = this
        fractionMap.forEach { (char, value) ->
            res = res.replace(char.toString(), value.toString())
        }
        return res
    }

    private fun parseQuantity(text: String): Double {
        return try {
            if (text.contains("/")) {
                val parts = text.split(" ").filter { it.isNotBlank() }
                if (parts.size == 2) {
                    val whole = parts[0].toDoubleOrNull() ?: 0.0
                    val fractionParts = parts[1].split("/")
                    val fraction = fractionParts[0].toDouble() / fractionParts[1].toDouble()
                    whole + fraction
                } else {
                    val fractionParts = text.split("/")
                    fractionParts[0].toDouble() / fractionParts[1].toDouble()
                }
            } else {
                text.replace(" ", "").toDouble()
            }
        } catch (e: Exception) {
            0.0
        }
    }

    private enum class Mode { UNKNOWN, INGREDIENTS, STEPS }
}

