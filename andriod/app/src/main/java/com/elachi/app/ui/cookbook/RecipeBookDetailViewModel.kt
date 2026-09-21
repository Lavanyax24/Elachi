package com.elachi.app.ui.cookbook

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elachi.app.data.local.entities.RecipeBookEntity
import com.elachi.app.data.local.entities.RecipeEntity
import com.elachi.app.data.repository.RecipeRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

// This class handles the data for the recipe book detail screen

class RecipeBookDetailViewModel(
    bookId: String,
    recipeRepository: RecipeRepository,
) : ViewModel() {

    val book: StateFlow<RecipeBookEntity?> = recipeRepository.observeBook(bookId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val recipes: StateFlow<List<RecipeEntity>> = recipeRepository.observeRecipesInBook(bookId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}