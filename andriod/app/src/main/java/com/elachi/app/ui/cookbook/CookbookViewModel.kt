package com.elachi.app.ui.cookbook

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elachi.app.data.local.entities.RecipeBookEntity
import com.elachi.app.data.repository.RecipeRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CookbookViewModel(
    private val userId: String,
    private val recipeRepository: RecipeRepository,
) : ViewModel() {

    val books: StateFlow<List<RecipeBookEntity>> = recipeRepository.observeBooks(userId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        // Pull the latest books from the backend so a fresh install shows them
        viewModelScope.launch {
            recipeRepository.refreshBooksFromNetwork(userId)
        }
    }
}