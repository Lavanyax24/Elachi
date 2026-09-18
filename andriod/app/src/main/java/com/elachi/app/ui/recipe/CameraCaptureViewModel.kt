package com.elachi.app.ui.recipe

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

/**
 * Owns the "what do we do with the raw OCR text" decision for both Camera
 * capture and Screenshot import.
 *
 * Primary path: send the raw text to the backend's AI-powered structuring
 * endpoint (Cohere, POST /api/recipes/parse-text).
 * Fallback path: if the AI call fails (offline, Render cold start, Cohere
 * rate limit), silently fall back to the on-device RecipeTextParser.
 */
class CameraCaptureViewModel(
    private val recipeRepository: RecipeRepository,
) : ViewModel() {

    fun processRecognizedText(
        rawLines: List<String>,
        onResult: (OcrResult) -> Unit,
    ) {
        val rawText = rawLines.joinToString("\n")

        viewModelScope.launch {
            recipeRepository.parseRecipeTextViaAi(rawText)
                .onSuccess { ai ->
                    onResult(
                        OcrResult(
                            title = ai.title,
                            ingredients = ai.ingredients
                                .map {
                                    DraftIngredient(
                                        it.name,
                                        it.quantity.toString(),
                                        it.unit,
                                    )
                                }
                                .ifEmpty { listOf(DraftIngredient()) },
                            steps = ai.steps.ifEmpty { listOf("") },
                            method = ai.method,
                            servings = if (ai.servings > 0) ai.servings.toString() else "",
                            cookTimeMinutes = if (ai.cookTimeMinutes > 0) {
                                ai.cookTimeMinutes.toString()
                            } else {
                                ""
                            },
                        ),
                    )
                }
                .onFailure {
                    val local = RecipeTextParser.parse(rawLines)

                    onResult(
                        OcrResult(
                            title = local.title,
                            ingredients = local.ingredients
                                .map {
                                    DraftIngredient(
                                        it.name,
                                        it.quantity,
                                        it.unit,
                                    )
                                }
                                .ifEmpty { listOf(DraftIngredient()) },
                            steps = local.steps.ifEmpty { listOf("") },
                            method = "",
                            servings = "",
                            cookTimeMinutes = "",
                        ),
                    )
                }
        }
    }
}
