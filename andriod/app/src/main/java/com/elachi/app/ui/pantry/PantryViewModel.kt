package com.elachi.app.ui.pantry

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elachi.app.data.local.entities.PantryItemEntity
import com.elachi.app.data.local.entities.ShoppingListItemEntity
import com.elachi.app.data.repository.AchievementRepository
import com.elachi.app.data.repository.PantryRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PantryViewModel(
    private val userId: String,
    private val pantryRepository: PantryRepository,
    private val achievementRepository: AchievementRepository,
) : ViewModel() {

    val pantryItems: StateFlow<List<PantryItemEntity>> =
        pantryRepository.observePantry(userId)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyList(),
            )

    val shoppingList: StateFlow<List<ShoppingListItemEntity>> =
        pantryRepository.observeShoppingList(userId)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyList(),
            )

    fun addPantryItem(
        name: String,
        quantity: Double,
        unit: String,
    ) = viewModelScope.launch {
        pantryRepository.addPantryItem(
            userId = userId,
            name = name,
            quantity = quantity,
            unit = unit,
        )

        achievementRepository.onPantryItemAdded(
            totalPantryItems = pantryItems.value.size + 1,
        )
    }

    fun deletePantryItem(id: String) = viewModelScope.launch {
        pantryRepository.deletePantryItem(id)
    }

    fun addShoppingItem(
        name: String,
        quantity: Double,
        unit: String,
    ) = viewModelScope.launch {
        pantryRepository.addShoppingItem(
            userId = userId,
            name = name,
            quantity = quantity,
            unit = unit,
        )
    }

    fun toggleBought(
        id: String,
        bought: Boolean,
    ) = viewModelScope.launch {
        pantryRepository.setShoppingItemBought(id, bought)
    }

    fun deleteShoppingItem(id: String) = viewModelScope.launch {
        pantryRepository.deleteShoppingItem(id)
    }
}