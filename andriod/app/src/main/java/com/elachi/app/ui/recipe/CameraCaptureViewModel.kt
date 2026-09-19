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
                            title = aiResult.title.trim(),
                            ingredients = aiResult.ingredients
                                .map { ingredient ->
                                    DraftIngredient(
                                        name = ingredient.name.trim(),
                                        quantity = ingredient.quantity
                                            .toString()
                                            .removeSuffix(".0"),
                                        unit = ingredient.unit.trim(),
                                    )
                                }
                                .ifEmpty {
                                    listOf(DraftIngredient())
                                },
                            steps = aiResult.steps
                                .map { it.trim() }
                                .filter { it.isNotBlank() }
                                .ifEmpty {
                                    listOf("")
                                },
                            method = aiResult.method.trim(),
                            servings = aiResult.servings
                                .takeIf { it > 0 }
                                ?.toString()
                                .orEmpty(),
                            cookTimeMinutes =
                                aiResult.cookTimeMinutes
                                    .takeIf { it > 0 }
                                    ?.toString()
                                    .orEmpty(),
                        ),
                    )
                }
                .onFailure { exception ->
                    Log.w(
                        "RecipeOCR",
                        "AI parsing failed. Using local parser.",
                        exception,
                    )

                    val localResult =
                        RecipeTextParser.parse(cleanedLines)

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
                            method = "",
                            servings = "",
                            cookTimeMinutes = "",
                        ),
                    )
                }
        }
    }
}