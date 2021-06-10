package com.pleon.buyt.viewmodel

import android.app.Application
import android.content.SharedPreferences
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pleon.buyt.R
import com.pleon.buyt.model.Coordinates
import com.pleon.buyt.model.Item
import com.pleon.buyt.model.Store
import com.pleon.buyt.repository.MainRepository
import com.pleon.buyt.ui.fragment.PREF_SEARCH_DISTANCE
import com.pleon.buyt.ui.fragment.PREF_SEARCH_DIST_DEF
import com.pleon.buyt.ui.state.IdleState
import com.pleon.buyt.ui.state.State
import com.pleon.buyt.util.formatNumber
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.*
import kotlin.math.cos

const val FREE_BUY_LIMIT = 5
private const val EARTH_RADIUS = 6_371_000.0 // In meters

/**
 * Every screen in the app (an activity with all its fragments) has one corresponding viewModel for itself.
 * A ViewModel acts as a communication center between the Repository and the UI.
 * You can also use a ViewModel to share data between fragments.
 *
 * [ViewModels][androidx.lifecycle.ViewModel] only survive configuration changes and not
 * force-kills. So to survive process stops, implement
 * [AppCompatActivity.onSaveInstanceState] method in your activity/fragment.
 */
class MainViewModel(private val app: Application, private val repository: MainRepository,
                    private val prefs: SharedPreferences, initialState: State)
    : AndroidViewModel(app) {

    var state: State = initialState
    var location: Location? = null
    var foundStores = mutableListOf<Store>()
    var shouldCompletePurchase = false
    var shouldAnimateNavIcon = false
    var isFindingSkipped = false
    val items = repository.items.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    // OR
    /* fun getItems(): StateFlow<List<Item>> {
        return repository.getItems()
            .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    } */
    // OR
    /* fun getItems(): StateFlow<List<Item>> {
        val itemsFlow = MutableStateFlow(emptyList<Item>())
        viewModelScope.launch {
            repository.getItems().collect {
                itemsFlow.value = it
            }
        }
        return itemsFlow
    } */
    // OR
    /* fun getItemsLiveData() = liveData {
        repository.getItems().collect { emit(it) }
    } */

    suspend fun getAllStores() = repository.getAllStores()

    suspend fun getPurchaseCountInPeriod(period: Int = 7) = repository.getPurchaseCountInPeriod(period)

    suspend fun findNearStores(origin: Coordinates): List<Store> {
        val searchDistInMeters = prefs.getString(PREF_SEARCH_DISTANCE, PREF_SEARCH_DIST_DEF)!!.toInt()
        val searchDist = cos(searchDistInMeters / EARTH_RADIUS)
        return repository.findNearStores(origin, searchDist)
    }

    fun buy(
        items: Collection<Item>,
        store: Store,
        date: Date
    ) = viewModelScope.launch {
        repository.buy(items, store, date)
    }

    fun updateItems(items: Collection<Item>) = viewModelScope.launch {
        repository.updateItems(items)
    }

    fun flagItemForDeletion(item: Item) {
        item.isFlaggedForDeletion = true
        updateItems(listOf(item))
    }

    fun deleteItem(item: Item) = viewModelScope.launch { repository.deleteItem(item) }

    fun restoreDeletedItem(item: Item) {
        item.isFlaggedForDeletion = false
        updateItems(listOf(item))
    }

    fun shiftToIdleState() {
        state = IdleState
        foundStores.clear()
        isFindingSkipped = false // ???
        shouldAnimateNavIcon = false // ???
        shouldCompletePurchase = false // ???
    }

    fun getStoreIcon() = when {
        foundStores.size == 0 -> R.drawable.ic_store
        isFindingSkipped -> R.drawable.ic_store
        else -> R.drawable.ic_store_add
    }

    fun getStoreTitle(): String = when {
        foundStores.size == 0 -> app.getString(R.string.menu_text_new_store_found)
        foundStores.size == 1 -> foundStores.first().name
        isFindingSkipped -> app.getString(R.string.menu_text_multiple_stores_saved, formatNumber(foundStores.size))
        else -> app.getString(R.string.menu_text_multiple_stores_found, formatNumber(foundStores.size))
    }
}
