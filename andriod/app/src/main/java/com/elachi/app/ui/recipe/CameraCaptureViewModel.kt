package com.elachi.app.ui.recipe

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elachi.app.data.repository.RecipeRepository
import com.elachi.app.util.RecipeTextParser
import kotlinx.coroutines.launch

data class OcrResult(
    val title: String,
    val ingredients: List<DraftIngredient>,
    val steps: List<String>,
    val method: String,
    val servings: String,
    val cookTimeMinutes: String,
)

class CameraCaptureViewModel(
    private val recipeRepository: RecipeRepository,
) : ViewModel() {

    fun processRecognizedText(
        rawLines: List<String>,
        onResult: (OcrResult) -> Unit,
    ) {
        val cleanedLines = rawLines
            .map { it.trim() }
            .filter { it.isNotBlank() }

        if (cleanedLines.isEmpty()) {
            onResult(
                OcrResult(
                    title = "",
                    ingredients = listOf(DraftIngredient()),
                    steps = listOf(""),
                    method = "",
                    servings = "",
                    cookTimeMinutes = "",
                ),
            )

            return
        }

        /*
         * Always parse locally as well. The local result supplies any fields
         * omitted by the backend and is also the offline fallback.
         */
        val localResult = RecipeTextParser.parse(cleanedLines)
        val combinedRawText = cleanedLines.joinToString("\n")

        viewModelScope.launch {
            recipeRepository
                .parseRecipeTextViaAi(combinedRawText)
                .onSuccess { aiResult ->
                    Log.d(
                        "RecipeOCR",
                        "Recipe text was structured by the AI backend.",
                    )

                    onResult(
                        OcrResult(
                            title = aiResult.title
                                .trim()
                                .ifBlank { localResult.title },
                            ingredients = aiResult.ingredients
                                .map { ingredient ->
                                    DraftIngredient(
                                        name = ingredient.name.trim(),
                                        quantity = ingredient.quantity
                                            .toString()
                                            .removeSuffix(".0"),
                                        unit = RecipeTextParser.normalizeUnit(
                                            ingredient.unit,
                                        ),
                                    )
                                }
                                .filter { it.name.isNotBlank() }
                                .ifEmpty {
                                    localResult.ingredients.map {
                                            ingredient ->
                                        DraftIngredient(
                                            name = ingredient.name,
                                            quantity = ingredient.quantity,
                                            unit = RecipeTextParser
                                                .normalizeUnit(
                                                    ingredient.unit,
                                                ),
                                        )
                                    }
                                },
                            steps = RecipeTextParser.normalizeSteps(
                                rawSteps = aiResult.steps,
                                mergeUnnumberedParagraph = false,
                            )
                                .ifEmpty {
                                    localResult.steps
                                },
                            method = RecipeTextParser.normalizeMethod(
                                aiResult.method,
                            ).ifBlank { localResult.method },
                            servings = aiResult.servings
                                .takeIf { it > 0 }
                                ?.toString()
                                ?: localResult.servings,
                            cookTimeMinutes =
                                aiResult.cookTimeMinutes
                                    .takeIf { it > 0 }
                                    ?.toString()
                                    ?: localResult.cookTimeMinutes,
                        ),
                    )
                }
                .onFailure { exception ->
                    Log.w(
                        "RecipeOCR",
                        "AI parsing failed. Using local parser.",
                        exception,
                    )

                    onResult(
                        OcrResult(
                            title = localResult.title,
                            ingredients = localResult.ingredients
                                .map { ingredient ->
                                    DraftIngredient(
                                        name = ingredient.name,
                                        quantity = ingredient.quantity,
                                        unit = ingredient.unit,
                                    )
                                }
                                .ifEmpty {
                                    listOf(DraftIngredient())
                                },
                            steps = localResult.steps
                                .filter { it.isNotBlank() }
                                .ifEmpty {
                                    listOf("")
                                },
                            method = localResult.method,
                            servings = localResult.servings,
                            cookTimeMinutes =
                                localResult.cookTimeMinutes,
                        ),
                    )
                }
        }
    }
}
