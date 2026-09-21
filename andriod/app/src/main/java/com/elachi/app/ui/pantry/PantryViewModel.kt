package com.elachi.app.ui.pantry

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elachi.app.data.local.entities.PantryItemEntity
import com.elachi.app.data.local.entities.ShoppingListItemEntity
import com.elachi.app.data.repository.AchievementRepository
import com.elachi.app.data.repository.PantryRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PantryViewModel(
    private val userId: String,
    private val pantryRepository: PantryRepository,
    private val achievementRepository: AchievementRepository,
) : ViewModel() {

    val message = mutableStateOf<String?>(null)

    fun clearMessage() {
        message.value = null
    }

    val pantryItems: StateFlow<List<PantryItemEntity>> =
        pantryRepository.observePantry(userId)
            .catch { e ->
                Log.e(TAG, "observePantry failed", e)
                message.value = "Couldn't load your pantry."
                emit(emptyList())
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyList(),
            )

    val shoppingList: StateFlow<List<ShoppingListItemEntity>> =
        pantryRepository.observeShoppingList(userId)
            .map { list -> list.sortedBy { it.isBought } }
            .catch { e ->
                Log.e(TAG, "observeShoppingList failed", e)
                message.value = "Couldn't load your shopping list."
                emit(emptyList())
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyList(),
            )

    init {
        launchSafely(errorMessage = null) {
            pantryRepository.cleanUpShoppingDuplicates(userId)
            pantryRepository.retryPendingPantrySync(userId)
        }
    }

    private fun launchSafely(errorMessage: String?, block: suspend () -> Unit): Job =
        viewModelScope.launch {
            try {
                block()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, errorMessage ?: "background task failed", e)
                if (errorMessage != null) message.value = errorMessage
            }
        }

    // ---------------- Pantry ----------------

    fun addPantryItem(name: String, quantity: Double, unit: String): Job =
        launchSafely("Couldn't save \"$name\". Please try again.") {
            val synced = pantryRepository.addPantryItem(userId, name, quantity, unit)
            if (!synced) {
                message.value = "Saved on your phone. We'll sync \"$name\" when you're back online."
            }

            try {
                achievementRepository.onPantryItemAdded(totalPantryItems = pantryItems.value.size + 1)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "onPantryItemAdded failed", e)
            }
        }

    fun editPantryItem(original: PantryItemEntity, name: String, quantity: Double, unit: String): Job =
        launchSafely("Couldn't update \"$name\". Please try again.") {
            val synced = pantryRepository.updatePantryItem(original, name, quantity, unit)
            message.value =
                if (synced) "Updated $name"
                else "Updated on your phone. We'll sync \"$name\" when you're back online."
        }

    fun deletePantryItem(id: String): Job =
        launchSafely("Couldn't remove that item. Please try again.") {
            val synced = pantryRepository.deletePantryItem(id)
            if (!synced) message.value = "Removed from your phone, but the server couldn't be updated."
        }

    // ---------------- Shopping list ----------------

    fun addShoppingItem(name: String, quantity: Double, unit: String): Job =
        launchSafely("Couldn't add \"$name\" to your list. Please try again.") {
            val result = pantryRepository.addShoppingItem(userId, name, quantity, unit)
            if (result.merged) {
                val qty = if (result.quantity % 1.0 == 0.0) result.quantity.toInt().toString() else result.quantity.toString()
                message.value = "${result.name} was already on your list. Now $qty ${result.unit}".trim()
            }
        }

    fun toggleBought(id: String, bought: Boolean): Job =
        launchSafely("Couldn't update that item. Please try again.") {
            pantryRepository.setShoppingItemBought(id, bought)
        }

    fun deleteShoppingItem(id: String): Job =
        launchSafely("Couldn't remove that item. Please try again.") {
            pantryRepository.deleteShoppingItem(id)
        }

    fun clearBoughtItems(): Job =
        launchSafely("Couldn't clear the bought items. Please try again.") {
            pantryRepository.clearBoughtShoppingItems(userId)
            message.value = "Bought items cleared"
        }

    fun clearShoppingList(): Job =
        launchSafely("Couldn't clear your shopping list. Please try again.") {
            pantryRepository.clearShoppingList(userId)
            message.value = "Shopping list cleared"
        }

    private companion object {
        const val TAG = "PantryVM"
    }
}