package com.elachi.app.ui.cookbook

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elachi.app.data.local.entities.RecipeBookEntity
import com.elachi.app.data.local.entities.RecipeEntity
import com.elachi.app.data.repository.RecipeRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// This class handles the data for the cookbook screen
// It also handles the creation, updating, and deletion of recipe books

class CookbookViewModel(
    private val userId: String,
    private val recipeRepository: RecipeRepository,
) : ViewModel() {

    val books: StateFlow<List<RecipeBookEntity>> = recipeRepository.observeBooks(userId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allRecipes: StateFlow<List<RecipeEntity>> = recipeRepository.observeAllRecipes(userId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            recipeRepository.refreshBooksFromNetwork(userId)
            recipeRepository.refreshRecipesFromNetwork(userId)
        }
    }

    fun createBook(name: String, description: String?, icon: String, colour: String, coverImageUrl: String? = null) {
        viewModelScope.launch {
            recipeRepository.createBook(userId, name, description, icon, colour, coverImageUrl)
        }
    }

    fun updateBook(book: RecipeBookEntity, name: String, description: String?, icon: String, colour: String, coverImageUrl: String? = null) {
        viewModelScope.launch {
            recipeRepository.updateBook(book.id, name, description, icon, colour, coverImageUrl ?: book.coverImageUrl)
        }
    }

    fun deleteBook(book: RecipeBookEntity) {
        viewModelScope.launch {
            recipeRepository.deleteBook(book)
        }
    }
}