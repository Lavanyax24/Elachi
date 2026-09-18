package com.elachi.app.ui.recipe

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elachi.app.data.local.entities.RecipeBookEntity
import com.elachi.app.data.repository.RecipeRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class DraftIngredient(var name: String = "", var quantity: String = "", var unit: String = "")

class AddRecipeViewModel(
    private val userId: String,
    initialBookId: String,
    private val recipeRepository: RecipeRepository,
) : ViewModel() {

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

    // Preset option lists backing the dropdowns, matching the demo's
    // ManualEntryScreen. Each field stays a free text value underneath
    // (category.value etc.), so picking a preset or typing a custom one
    // through CustomAddRow both just set the same state.
    val categoryOptions = listOf("Breakfast", "Lunch", "Dinner", "Dessert", "Snack", "Appetizer")
    val cuisineOptions = listOf("Italian", "Indian", "Mexican", "Asian", "Mediterranean", "American", "Moroccan")
    val foodTypeOptions = listOf("Vegetarian", "Vegan", "Non-Vegetarian", "Pescatarian", "Gluten-Free")
    val difficultyOptions = listOf("Easy", "Medium", "Hard")
    val methodOptions = listOf("Stovetop", "Oven", "Grill", "Air Fryer", "Slow Cooker", "Pressure Cooker", "Microwave", "No-Cook")

    val allergenOptions = listOf("Nuts", "Dairy", "Gluten", "Soy", "Eggs", "Shellfish", "Fish", "Peanuts", "Sesame")
    val selectedAllergens = mutableStateListOf<String>()

    val ingredients = mutableStateListOf(DraftIngredient())
    val steps = mutableStateListOf("")

    var photoUri = mutableStateOf<android.net.Uri?>(null)
    var imageUrl = mutableStateOf<String?>(null)
    var isUploadingPhoto = mutableStateOf(false)

    var isSaving = mutableStateOf(false)
    var savedRecipeId = mutableStateOf<String?>(null)
    var errorMessage = mutableStateOf<String?>(null)

    fun addIngredientRow() { ingredients.add(DraftIngredient()) }
    fun removeIngredientRow(index: Int) { if (ingredients.size > 1) ingredients.removeAt(index) }

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
        if (parsedTitle.isNotBlank()) title.value = parsedTitle
        if (parsedIngredients.isNotEmpty()) {
            ingredients.clear()
            ingredients.addAll(parsedIngredients)
        }
        if (parsedSteps.isNotEmpty()) {
            steps.clear()
            steps.addAll(parsedSteps)
        }
        if (parsedMethod.isNotBlank()) method.value = parsedMethod
        if (parsedServings.isNotBlank()) servings.value = parsedServings
        if (parsedCookTimeMinutes.isNotBlank()) cookTimeMinutes.value = parsedCookTimeMinutes
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
        val cleanedSteps = steps.filter { it.isNotBlank() }
        if (cleanedSteps.isEmpty()) {
            errorMessage.value = "Add at least one step."
            return
        }

        isSaving.value = true
        errorMessage.value = null
        viewModelScope.launch {
            val id = recipeRepository.createRecipe(
                userId = userId,
                bookId = selectedBookId.value,
                title = title.value,
                category = category.value,
                cuisine = cuisine.value.ifBlank { "Other" },
                foodType = foodType.value.ifBlank { "Other" },
                difficulty = difficulty.value,
                servings = servings.value.toIntOrNull() ?: 1,
                cookTimeMinutes = cookTimeMinutes.value.toIntOrNull() ?: 0,
                method = method.value,
                ingredients = cleanedIngredients.map {
                    it.name to ((it.quantity.toDoubleOrNull() ?: 0.0) to it.unit)
                },
                steps = cleanedSteps,
                allergens = selectedAllergens.toList(),
                isPrivate = isPrivate.value,
                imageUrl = imageUrl.value,
            )
            isSaving.value = false
            savedRecipeId.value = id
        }
    }
}