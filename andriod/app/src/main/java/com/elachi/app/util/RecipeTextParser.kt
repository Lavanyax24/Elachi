package com.elachi.app.util

import java.util.Locale

data class DraftIngredient(
    var name: String = "",
    var quantity: String = "",
    var unit: String = "",
)

data class ParsedRecipe(
    val title: String,
    val ingredients: List<DraftIngredient>,
    val steps: List<String>,
    val method: String = "",
    val servings: String = "",
    val cookTimeMinutes: String = "",
)

/** Converts ML Kit OCR text into fields used by AddRecipeScreen. */
object RecipeTextParser {

    private const val QUANTITY_PATTERN =
        "(?:\\d+(?:[.,]\\d+)?(?:\\s+\\d+\\s*/\\s*\\d+)?|\\d+\\s*/\\s*\\d+)"

    private const val UNIT_PATTERN =
        "(?:milligrams?|mg|kilograms?|kgs?|kg|grams?|gr|g|millilit(?:er|re)s?|ml|" +
                "lit(?:er|re)s?|l|teaspoons?|tsp|tablespoons?|tbsp|cups?|" +
                "fluid\\s+ounces?|fl\\s*oz|ounces?|oz|pounds?|lbs?|lb|" +
                "pinches?|pieces?|slices?|cloves?|cans?|packets?|packages?|pkgs?|" +
                "bunch(?:es)?|handfuls?)"

    private val ingredientWithUnitRegex = Regex(
        "^($QUANTITY_PATTERN)\\s*($UNIT_PATTERN)\\.?\\s*(?:of\\s+)?(.+)$",
        RegexOption.IGNORE_CASE,
    )
    private val ingredientWithoutUnitRegex = Regex(
        "^($QUANTITY_PATTERN)\\s+(.+)$",
        RegexOption.IGNORE_CASE,
    )
    private val ingredientHeadingRegex = Regex(
        "^\\s*ingredients?\\s*:?\\s*(.*)$",
        RegexOption.IGNORE_CASE,
    )
    private val stepHeadingRegex = Regex(
        "^\\s*(?:instructions?|directions?|steps?|preparation|procedure)\\s*:?\\s*(.*)$",
        RegexOption.IGNORE_CASE,
    )
    private val methodHeadingRegex = Regex(
        "^\\s*method\\s*:?\\s*(.*)$",
        RegexOption.IGNORE_CASE,
    )
    private val servingsRegex = Regex(
        "(?:serves?|servings?|yield)\\s*:?\\s*(\\d+)",
        RegexOption.IGNORE_CASE,
    )
    private val trailingServingsRegex = Regex(
        "^(\\d+)\\s+(?:servings?|portions?)$",
        RegexOption.IGNORE_CASE,
    )
    private val cookTimeRegex = Regex(
        "(?:cook(?:ing)?|total)\\s*time\\s*:?\\s*(\\d+)\\s*(hours?|hrs?|hr|h|minutes?|mins?|min|m)?",
        RegexOption.IGNORE_CASE,
    )
    private val prepTimeRegex = Regex(
        "prep(?:aration)?\\s*time\\s*:?\\s*(\\d+)\\s*(hours?|hrs?|hr|h|minutes?|mins?|min|m)?",
        RegexOption.IGNORE_CASE,
    )
    private val listMarkerRegex = Regex("^\\s*(?:[-•*‣▪◦]|\\d+[.)])\\s+")
    private val standaloneStepNumberRegex = Regex("^\\s*\\d+[.)]?\\s*$")
    // Require a marker for inline numbers so an instruction such as
    // "180 C for 20 minutes" is not mistaken for step 180.
    private val numberedStepRegex = Regex("^\\s*\\d+[.)]\\s*(.+)$")
    private val bulletStepRegex = Regex("^\\s*[-•*‣▪◦]\\s+(.+)$")

    private val fractionMap = mapOf(
        '½' to "1/2",
        '⅓' to "1/3",
        '⅔' to "2/3",
        '¼' to "1/4",
        '¾' to "3/4",
        '⅛' to "1/8",
        '⅜' to "3/8",
        '⅝' to "5/8",
        '⅞' to "7/8",
    )

    fun parse(rawLines: List<String>): ParsedRecipe {
        val lines = rawLines
            .flatMap { it.lines() }
            .map { it.cleanOcrSpacing() }
            .filter { it.isNotBlank() }

        if (lines.isEmpty()) {
            return ParsedRecipe(
                title = "",
                ingredients = listOf(DraftIngredient()),
                steps = listOf(""),
            )
        }

        val titleIndex = lines.indexOfFirst { isPossibleTitle(it) }
        val title = titleIndex
            .takeIf { it >= 0 }
            ?.let { lines[it].removeListMarker().trim(' ', ':', '-') }
            .orEmpty()

        val ingredients = mutableListOf<DraftIngredient>()
        val steps = mutableListOf<String>()

        var method = ""
        var servings = ""
        var cookTimeMinutes = ""
        var mode = Mode.UNKNOWN

        lines.forEachIndexed { index, originalLine ->
            if (index == titleIndex) return@forEachIndexed

            val line = originalLine.normalizeFractions()

            servingsRegex.find(line)?.let { match ->
                servings = match.groupValues[1]
                return@forEachIndexed
            }
            trailingServingsRegex.matchEntire(line)?.let { match ->
                servings = match.groupValues[1]
                return@forEachIndexed
            }
            cookTimeRegex.find(line)?.let { match ->
                cookTimeMinutes = durationToMinutes(
                    amount = match.groupValues[1],
                    unit = match.groupValues[2],
                )
                return@forEachIndexed
            }
            if (prepTimeRegex.containsMatchIn(line)) return@forEachIndexed

            ingredientHeadingRegex.matchEntire(line)?.let { match ->
                mode = Mode.INGREDIENTS
                val content = match.groupValues[1].trim()
                if (content.isNotBlank()) addIngredient(content, ingredients)
                return@forEachIndexed
            }
            stepHeadingRegex.matchEntire(line)?.let { match ->
                mode = Mode.STEPS
                val content = match.groupValues[1].trim()
                if (content.isNotBlank()) steps += content
                return@forEachIndexed
            }
            methodHeadingRegex.matchEntire(line)?.let { match ->
                val content = match.groupValues[1].trim()
                if (content.isBlank()) {
                    mode = Mode.STEPS
                } else {
                    val detectedMethod = normalizeMethod(content)
                    if (detectedMethod.isNotBlank()) {
                        method = detectedMethod
                    } else {
                        mode = Mode.STEPS
                        steps += content.removeListMarker()
                    }
                }
                return@forEachIndexed
            }

            // Preserve step markers here. normalizeSteps() needs them to tell
            // where one OCR paragraph ends and the next numbered step begins.
            val cleanedLine = if (mode == Mode.STEPS) {
                line.trim()
            } else {
                line.removeListMarker().trim()
            }
            if (cleanedLine.isBlank()) return@forEachIndexed

            when (mode) {
                Mode.INGREDIENTS -> addIngredient(cleanedLine, ingredients)
                Mode.STEPS -> steps += cleanedLine
                Mode.UNKNOWN -> {
                    val ingredient = parseIngredient(cleanedLine)
                    when {
                        ingredient != null -> ingredients += ingredient
                        looksLikeInstruction(cleanedLine) -> steps += cleanedLine
                        else -> ingredients += DraftIngredient(name = cleanedLine)
                    }
                }
            }
        }

        if (method.isBlank()) {
            method = normalizeMethod(steps.joinToString(" "))
        }

        return ParsedRecipe(
            title = title,
            ingredients = ingredients
                .filter { it.name.isNotBlank() }
                .ifEmpty { listOf(DraftIngredient()) },
            steps = normalizeSteps(
                rawSteps = steps,
                mergeUnnumberedParagraph = true,
            )
                .ifEmpty { listOf("") },
            method = method,
            servings = servings,
            cookTimeMinutes = cookTimeMinutes,
        )
    }

    /** Returns a value that exactly matches the unit dropdown. */
    fun normalizeUnit(rawUnit: String): String {
        val unit = rawUnit
            .trim()
            .lowercase(Locale.ROOT)
            .replace(".", "")
            .replace(Regex("\\s+"), " ")

        return when (unit) {
            "", "none", "no unit", "unit", "n/a" -> ""
            "milligram", "milligrams", "mg" -> "mg"
            "gram", "grams", "gr", "g" -> "g"
            "kilogram", "kilograms", "kgs", "kg" -> "kg"
            "milliliter", "milliliters", "millilitre", "millilitres", "ml" -> "ml"
            "liter", "liters", "litre", "litres", "l" -> "L"
            "teaspoon", "teaspoons", "tsp" -> "tsp"
            "tablespoon", "tablespoons", "tbsp" -> "tbsp"
            "cup" -> "cup"
            "cups" -> "cups"
            "fluid ounce", "fluid ounces", "fl oz", "ounce", "ounces", "oz" -> "oz"
            "pound", "pounds", "lbs", "lb" -> "lb"
            "pinch", "pinches" -> "pinch"
            "piece" -> "piece"
            "pieces" -> "pieces"
            "slice" -> "slice"
            "slices" -> "slices"
            "clove" -> "clove"
            "cloves" -> "cloves"
            "can", "cans" -> "can"
            "packet", "packets", "package", "packages", "pkg", "pkgs" -> "packet"
            "bunch", "bunches" -> "bunch"
            "handful", "handfuls" -> "handful"
            "to taste", "as needed" -> "to taste"
            else -> rawUnit.trim()
        }
    }

    /** Maps OCR/backend wording to one of the method dropdown values. */
    fun normalizeMethod(rawMethod: String): String {
        val value = rawMethod.lowercase(Locale.ROOT)

        return when {
            "air fryer" in value || "air-fry" in value || "air fry" in value -> "Air Fryer"
            "slow cooker" in value || "crockpot" in value || "crock pot" in value -> "Slow Cooker"
            "pressure cooker" in value || "instant pot" in value -> "Pressure Cooker"
            "microwave" in value -> "Microwave"
            "no-cook" in value || "no cook" in value -> "No-Cook"
            "grill" in value || "barbecue" in value || "bbq" in value -> "Grill"
            "oven" in value || "bake" in value || "roast" in value || "preheat" in value -> "Oven"
            "stovetop" in value || "stove top" in value || "fry" in value ||
                    "sauté" in value || "saute" in value || "simmer" in value ||
                    "boil" in value || "saucepan" in value || "skillet" in value -> "Stovetop"
            else -> ""
        }
    }

    /**
     * Removes OCR step numbers and joins wrapped lines into their correct step.
     *
     * Example input:
     * 1
     * Gather all ingredients.
     * 2
     * Sift flour, baking powder,
     * sugar and salt together.
     *
     * becomes two complete steps without the standalone 1 and 2 values.
     */
    fun normalizeSteps(
        rawSteps: List<String>,
        mergeUnnumberedParagraph: Boolean = false,
    ): List<String> {
        val lines = rawSteps
            .flatMap { it.lines() }
            .map { it.cleanOcrSpacing() }
            .filter { it.isNotBlank() }

        if (lines.isEmpty()) return emptyList()

        val hasNumberedSteps = lines.any { line ->
            standaloneStepNumberRegex.matches(line) ||
                    numberedStepRegex.matches(line)
        }
        val hasBulletSteps = lines.any { bulletStepRegex.matches(it) }

        if (!hasNumberedSteps && !hasBulletSteps) {
            return if (mergeUnnumberedParagraph) {
                listOf(joinStepLines(lines))
            } else {
                lines
            }
        }

        val completedSteps = mutableListOf<String>()
        val currentStepLines = mutableListOf<String>()

        fun finishCurrentStep() {
            if (currentStepLines.isNotEmpty()) {
                val completeStep = joinStepLines(currentStepLines)
                if (completeStep.isNotBlank()) completedSteps += completeStep
                currentStepLines.clear()
            }
        }

        lines.forEach { line ->
            when {
                standaloneStepNumberRegex.matches(line) -> {
                    // A standalone number is a boundary, not instruction text.
                    finishCurrentStep()
                }

                numberedStepRegex.matches(line) -> {
                    finishCurrentStep()
                    val instruction = numberedStepRegex
                        .matchEntire(line)
                        ?.groupValues
                        ?.get(1)
                        .orEmpty()
                        .trim()
                    if (instruction.isNotBlank()) {
                        currentStepLines += instruction
                    }
                }

                bulletStepRegex.matches(line) -> {
                    finishCurrentStep()
                    val instruction = bulletStepRegex
                        .matchEntire(line)
                        ?.groupValues
                        ?.get(1)
                        .orEmpty()
                        .trim()
                    if (instruction.isNotBlank()) {
                        currentStepLines += instruction
                    }
                }

                else -> {
                    // This is another visual line belonging to the same step.
                    currentStepLines += line
                }
            }
        }

        finishCurrentStep()
        return completedSteps
    }

    private fun addIngredient(
        line: String,
        destination: MutableList<DraftIngredient>,
    ) {
        destination += parseIngredient(line)
            ?: DraftIngredient(name = line.removeListMarker().trim())
    }

    private fun parseIngredient(line: String): DraftIngredient? {
        val cleanedLine = line.removeListMarker().removePrefix("-").trim()

        ingredientWithUnitRegex.matchEntire(cleanedLine)?.let { match ->
            return DraftIngredient(
                name = cleanIngredientName(match.groupValues[3]),
                quantity = parseQuantity(match.groupValues[1]),
                unit = normalizeUnit(match.groupValues[2]),
            )
        }
        ingredientWithoutUnitRegex.matchEntire(cleanedLine)?.let { match ->
            return DraftIngredient(
                name = cleanIngredientName(match.groupValues[2]),
                quantity = parseQuantity(match.groupValues[1]),
                unit = "",
            )
        }
        if (cleanedLine.endsWith("to taste", ignoreCase = true)) {
            return DraftIngredient(
                name = cleanedLine
                    .dropLast("to taste".length)
                    .trim(' ', ',', '-'),
                unit = "to taste",
            )
        }
        return null
    }

    private fun cleanIngredientName(value: String): String = value
        .trim()
        .removePrefix("of ")
        .trim(' ', '-', ':')

    private fun isPossibleTitle(line: String): Boolean {
        val cleaned = line.removeListMarker().trim()
        return cleaned.length in 3..80 &&
                !ingredientHeadingRegex.matches(cleaned) &&
                !stepHeadingRegex.matches(cleaned) &&
                !methodHeadingRegex.matches(cleaned) &&
                !servingsRegex.containsMatchIn(cleaned) &&
                !cookTimeRegex.containsMatchIn(cleaned) &&
                !prepTimeRegex.containsMatchIn(cleaned) &&
                parseIngredient(cleaned) == null &&
                !looksLikeInstruction(cleaned)
    }

    private fun looksLikeInstruction(line: String): Boolean {
        val lower = line.lowercase(Locale.ROOT)
        val verbs = listOf(
            "add ", "bake ", "beat ", "blend ", "boil ", "chill ",
            "combine ", "cook ", "cut ", "fold ", "fry ", "grill ",
            "heat ", "mix ", "place ", "pour ", "preheat ", "roast ",
            "serve ", "simmer ", "stir ", "whisk ",
        )
        return verbs.any { lower.startsWith(it) } ||
                line.endsWith(".") || line.length > 90
    }

    private fun String.removeListMarker(): String = replace(listMarkerRegex, "")

    private fun joinStepLines(lines: List<String>): String {
        val result = StringBuilder()

        lines.forEach { rawLine ->
            val line = rawLine.trim()
            if (line.isBlank()) return@forEach

            when {
                result.isEmpty() -> result.append(line)
                result.endsWith("-") -> {
                    result.deleteCharAt(result.lastIndex)
                    result.append(line)
                }
                else -> result.append(' ').append(line)
            }
        }

        return result.toString()
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    private fun String.cleanOcrSpacing(): String =
        replace('\u00A0', ' ')
            .replace(Regex("[ \\t]+"), " ")
            .trim()

    private fun String.normalizeFractions(): String {
        val result = StringBuilder()
        forEach { character ->
            val fraction = fractionMap[character]
            if (fraction != null) {
                if (result.isNotEmpty() && result.last().isDigit()) result.append(' ')
                result.append(fraction)
            } else {
                result.append(character)
            }
        }
        return result.toString()
    }

    private fun parseQuantity(rawQuantity: String): String {
        val quantity = rawQuantity
            .replace(',', '.')
            .replace(Regex("\\s*/\\s*"), "/")
            .trim()
        val parts = quantity.split(Regex("\\s+")).filter { it.isNotBlank() }
        val value = when {
            parts.size == 2 && parts[1].contains('/') ->
                (parts[0].toDoubleOrNull() ?: 0.0) + parseFraction(parts[1])
            quantity.contains('/') -> parseFraction(quantity)
            else -> quantity.toDoubleOrNull() ?: 0.0
        }
        return formatNumber(value)
    }

    private fun parseFraction(value: String): Double {
        val parts = value.split('/')
        if (parts.size != 2) return 0.0
        val numerator = parts[0].toDoubleOrNull() ?: return 0.0
        val denominator = parts[1].toDoubleOrNull() ?: return 0.0
        return if (denominator == 0.0) 0.0 else numerator / denominator
    }

    private fun formatNumber(value: Double): String =
        if (value % 1.0 == 0.0) {
            value.toInt().toString()
        } else {
            String.format(Locale.US, "%.3f", value)
                .trimEnd('0')
                .trimEnd('.')
        }

    private fun durationToMinutes(amount: String, unit: String): String {
        val number = amount.toIntOrNull() ?: return ""
        val normalizedUnit = unit.lowercase(Locale.ROOT)
        return if (normalizedUnit.startsWith("h")) {
            (number * 60).toString()
        } else {
            number.toString()
        }
    }

    private enum class Mode {
        UNKNOWN,
        INGREDIENTS,
        STEPS,
    }
}
