package com.elachi.app.ui.recipe

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elachi.app.data.local.entities.RecipeBookEntity
import com.elachi.app.data.repository.RecipeRepository
import com.elachi.app.util.RecipeTextParser
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class DraftIngredient(
    val name: String = "",
    val quantity: String = "",
    val unit: String = "",
)

class AddRecipeViewModel(
    private val userId: String,
    initialBookId: String,
    private val recipeRepository: RecipeRepository,
    private val editingRecipeId: String? = null,
) : ViewModel() {

    val isEditMode: Boolean = editingRecipeId != null

    // Every recipe must belong to a Recipe Book. This is the user's full
    // book list so the form can offer a real picker (matching the demo's
    // "Recipe Book" dropdown) instead of being locked to whichever book the
    // Add button happened to be tapped from.
    val availableBooks: StateFlow<List<RecipeBookEntity>> = recipeRepository.observeBooks(userId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    var selectedBookId = mutableStateOf(initialBookId)

    var title = mutableStateOf("")
    var category = mutableStateOf("Dinner")
    var cuisine = mutableStateOf("Italian")
    var foodType = mutableStateOf("Vegetarian")
    var difficulty = mutableStateOf("Easy")
    var servings = mutableStateOf("4")
    var cookTimeMinutes = mutableStateOf("30")
    var method = mutableStateOf("Stovetop")
    var isPrivate = mutableStateOf(true)

    // Preset option lists backing the optional-detail dropdowns.
    val categoryOptions = listOf("Breakfast", "Lunch", "Dinner", "Dessert", "Snack", "Appetizer")
    val cuisineOptions = listOf("Italian", "Indian", "Mexican", "Asian", "Mediterranean", "American", "Moroccan")
    val foodTypeOptions = listOf("Vegetarian", "Vegan", "Non-Vegetarian", "Pescatarian", "Gluten-Free")
    val difficultyOptions = listOf("Easy", "Medium", "Hard")
    val methodOptions = listOf("Stovetop", "Oven", "Grill", "Air Fryer", "Slow Cooker", "Pressure Cooker", "Microwave", "No-Cook")
    val ingredientUnitOptions = listOf(
        "mg",
        "g",
        "kg",
        "ml",
        "L",
        "tsp",
        "tbsp",
        "cup",
        "cups",
        "pinch",
        "piece",
        "pieces",
        "slice",
        "slices",
        "clove",
        "cloves",
        "can",
        "packet",
        "bunch",
        "handful",
        "to taste",
        "oz",
        "lb",
        "None",
    )
    val allergenOptions = mutableStateListOf("Nuts", "Dairy", "Gluten", "Soy", "Eggs", "Shellfish", "Fish", "Peanuts", "Sesame")
    val selectedAllergens = mutableStateListOf<String>()

    val ingredients = mutableStateListOf(DraftIngredient())
    val steps = mutableStateListOf("")

    var photoUri = mutableStateOf<android.net.Uri?>(null)
    var imageUrl = mutableStateOf<String?>(null)
    var isUploadingPhoto = mutableStateOf(false)

    var isSaving = mutableStateOf(false)
    var isLoading = mutableStateOf(isEditMode)
    var savedRecipeId = mutableStateOf<String?>(null)
    var errorMessage = mutableStateOf<String?>(null)

    init {
        if (editingRecipeId != null) {
            loadRecipeForEditing(editingRecipeId)
        }
    }

    private fun loadRecipeForEditing(recipeId: String) {
        viewModelScope.launch {
            try {
                val recipe = recipeRepository.getRecipeOnce(recipeId)
                    ?: throw IllegalArgumentException("Recipe not found.")

                if (recipe.ownerId != userId) {
                    throw IllegalStateException("You cannot edit another user's recipe.")
                }

                val savedIngredients = recipeRepository.getIngredientsOnce(recipeId)
                val savedSteps = recipeRepository.getStepsOnce(recipeId)

                selectedBookId.value = recipe.bookId
                title.value = recipe.title
                category.value = recipe.category
                cuisine.value = recipe.cuisine
                foodType.value = recipe.foodType
                difficulty.value = recipe.difficulty
                servings.value = recipe.servings.toString()
                cookTimeMinutes.value = recipe.cookTimeMinutes.toString()
                method.value = recipe.method
                isPrivate.value = recipe.isPrivate
                imageUrl.value = recipe.imageUrl

                selectedAllergens.clear()
                selectedAllergens.addAll(
                    recipe.allergensCsv
                        .split(",")
                        .map { it.trim() }
                        .filter { it.isNotBlank() },
                )

                ingredients.clear()
                ingredients.addAll(
                    savedIngredients.map {
                        DraftIngredient(
                            name = it.name,
                            quantity = it.quantity.toString(),
                            unit = it.unit,
                        )
                    }.ifEmpty { listOf(DraftIngredient()) },
                )

                steps.clear()
                steps.addAll(
                    savedSteps
                        .sortedBy { it.order }
                        .map { it.instruction }
                        .ifEmpty { listOf("") },
                )
            } catch (exception: Exception) {
                errorMessage.value = exception.message ?: "The recipe could not be loaded."
            } finally {
                isLoading.value = false
            }
        }
    }

    fun addIngredientRow() { ingredients.add(DraftIngredient()) }
    fun removeIngredientRow(index: Int) { if (ingredients.size > 1) ingredients.removeAt(index) }
    fun updateIngredient(index: Int, ingredient: DraftIngredient) {
        ingredients[index] = ingredient
    }

    fun addStepRow() { steps.add("") }
    fun removeStepRow(index: Int) { if (steps.size > 1) steps.removeAt(index) }

    fun uploadPhoto(context: android.content.Context, uri: android.net.Uri) {
        photoUri.value = uri
        isUploadingPhoto.value = true
        viewModelScope.launch {
            com.elachi.app.data.remote.SupabaseStorageClient.uploadImage(context, uri, folder = "recipe-photos")
                .onSuccess { url -> imageUrl.value = url }
                .onFailure { e -> errorMessage.value = e.message }
            isUploadingPhoto.value = false
        }
    }

    fun toggleAllergen(allergen: String) {
        if (selectedAllergens.contains(allergen)) selectedAllergens.remove(allergen) else selectedAllergens.add(allergen)
    }

    /** Called by the OCR/screenshot flow to pre-fill the form before the user
     * reviews it (FR-2.2 / FR-2.3). method/servings/cookTimeMinutes come from
     * the AI-powered parser and are blank when the local fallback parser was
     * used instead, in which case the form just keeps its existing defaults. */
    fun prefillFromParsedRecipe(
        parsedTitle: String,
        parsedIngredients: List<DraftIngredient>,
        parsedSteps: List<String>,
        parsedMethod: String = "",
        parsedServings: String = "",
        parsedCookTimeMinutes: String = "",
    ) {
        if (parsedTitle.isNotBlank()) {
            title.value = parsedTitle.trim()
        }

        val cleanedIngredients = parsedIngredients
            .map { ingredient ->
                ingredient.copy(
                    name = ingredient.name.trim(),
                    quantity = ingredient.quantity.trim(),
                    unit = RecipeTextParser.normalizeUnit(
                        ingredient.unit,
                    ),
                )
            }
            .filter { it.name.isNotBlank() }

        if (cleanedIngredients.isNotEmpty()) {
            ingredients.clear()
            ingredients.addAll(cleanedIngredients)
        }

        val cleanedSteps = RecipeTextParser.normalizeSteps(
            rawSteps = parsedSteps,
            mergeUnnumberedParagraph = false,
        )

        if (cleanedSteps.isNotEmpty()) {
            steps.clear()
            steps.addAll(cleanedSteps)
        }

        val normalizedMethod =
            RecipeTextParser.normalizeMethod(parsedMethod)
        if (normalizedMethod.isNotBlank()) {
            method.value = normalizedMethod
        }

        parsedServings
            .trim()
            .toIntOrNull()
            ?.takeIf { it > 0 }
            ?.let { servings.value = it.toString() }

        parsedCookTimeMinutes
            .trim()
            .toIntOrNull()
            ?.takeIf { it > 0 }
            ?.let { cookTimeMinutes.value = it.toString() }
    }

    fun save() {
        if (selectedBookId.value.isBlank()) {
            errorMessage.value = "Please choose a Recipe Book."
            return
        }
        if (title.value.isBlank()) {
            errorMessage.value = "Please give your recipe a title."
            return
        }
        val cleanedIngredients = ingredients.filter { it.name.isNotBlank() }
        if (cleanedIngredients.isEmpty()) {
            errorMessage.value = "Add at least one ingredient."
            return
        }
        val cleanedSteps = RecipeTextParser.normalizeSteps(
            rawSteps = steps.toList(),
            mergeUnnumberedParagraph = false,
        )
        if (cleanedSteps.isEmpty()) {
            errorMessage.value = "Add at least one step."
            return
        }

        isSaving.value = true
        errorMessage.value = null
        viewModelScope.launch {
            try {
                val recipeIngredients = cleanedIngredients.map {
                    it.name.trim() to
                            ((it.quantity.toDoubleOrNull() ?: 0.0) to it.unit.trim())
                }
                val recipeSteps = cleanedSteps.map { it.trim() }

                val id = if (editingRecipeId == null) {
                    recipeRepository.createRecipe(
                        userId = userId,
                        bookId = selectedBookId.value,
                        title = title.value.trim(),
                        category = category.value,
                        cuisine = cuisine.value.ifBlank { "Other" },
                        foodType = foodType.value.ifBlank { "Other" },
                        difficulty = difficulty.value,
                        servings = servings.value.toIntOrNull() ?: 1,
                        cookTimeMinutes = cookTimeMinutes.value.toIntOrNull() ?: 0,
                        method = method.value,
                        ingredients = recipeIngredients,
                        steps = recipeSteps,
                        allergens = selectedAllergens.toList(),
                        isPrivate = isPrivate.value,
                        imageUrl = imageUrl.value,
                    )
                } else {
                    recipeRepository.updateRecipe(
                        recipeId = editingRecipeId,
                        userId = userId,
                        bookId = selectedBookId.value,
                        title = title.value.trim(),
                        category = category.value,
                        cuisine = cuisine.value.ifBlank { "Other" },
                        foodType = foodType.value.ifBlank { "Other" },
                        difficulty = difficulty.value,
                        servings = servings.value.toIntOrNull() ?: 1,
                        cookTimeMinutes = cookTimeMinutes.value.toIntOrNull() ?: 0,
                        method = method.value,
                        ingredients = recipeIngredients,
                        steps = recipeSteps,
                        allergens = selectedAllergens.toList(),
                        isPrivate = isPrivate.value,
                        imageUrl = imageUrl.value,
                    )
                    editingRecipeId
                }
                savedRecipeId.value = id
            } catch (e: Exception) {
                errorMessage.value = "The recipe could not be saved. Please try again."
            } finally {
                isSaving.value = false
            }
        }
    }

    fun addCustomAllergen(allergenName: String) {
        val cleanedName = allergenName
            .trim()
            .replace(Regex("\\s+"), " ")

        if (cleanedName.isBlank()) {
            return
        }

        /*
         * Find an existing allergy without treating uppercase and
         * lowercase versions as different allergies.
         */
        val existingAllergen = allergenOptions.firstOrNull {
            it.equals(cleanedName, ignoreCase = true)
        }

        val allergenToSelect = if (existingAllergen != null) {
            existingAllergen
        } else {
            val formattedName = cleanedName.replaceFirstChar { firstCharacter ->
                if (firstCharacter.isLowerCase()) {
                    firstCharacter.titlecase()
                } else {
                    firstCharacter.toString()
                }
            }

            allergenOptions.add(formattedName)
            formattedName
        }

        /*
         * Automatically select the newly created allergy for the recipe.
         */
        if (
            selectedAllergens.none {
                it.equals(allergenToSelect, ignoreCase = true)
            }
        ) {
            selectedAllergens.add(allergenToSelect)
        }
    }

}
