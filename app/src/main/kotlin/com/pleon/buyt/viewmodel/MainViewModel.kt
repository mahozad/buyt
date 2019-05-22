package com.pleon.buyt.viewmodel

import android.app.Application
import android.content.SharedPreferences
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.pleon.buyt.R
import com.pleon.buyt.model.Coordinates
import com.pleon.buyt.model.Item
import com.pleon.buyt.model.Store
import com.pleon.buyt.repository.MainRepository
import com.pleon.buyt.ui.fragment.PREF_SEARCH_DIST
import com.pleon.buyt.ui.fragment.PREF_SEARCH_DIST_DEF
import com.pleon.buyt.viewmodel.MainViewModel.State.IDLE
import java.text.NumberFormat
import java.util.*
import javax.inject.Inject
import kotlin.math.cos

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
class MainViewModel @Inject constructor(app: Application,
                                        private val repository: MainRepository,
                                        private val prefs: SharedPreferences)
    : AndroidViewModel(app) {

    enum class State {
        IDLE, FINDING, SELECTING
    }

    @Volatile var state = IDLE
    var location: Location? = null
    var isFindingSkipped = false
    var foundStores = mutableListOf<Store>()
    var shouldCompletePurchase = false
    var shouldAnimateNavIcon = false
    var isAddingItem = false

    // TODO: Use paging architecture component library
    val allItems = repository.allItems
    val allStores get() = repository.getAllStores()

    fun findNearStores(origin: Coordinates): LiveData<List<Store>> {
        val searchDistInMeters = prefs.getString(PREF_SEARCH_DIST, PREF_SEARCH_DIST_DEF)!!.toInt()
        val searchDist = cos(searchDistInMeters / EARTH_RADIUS)
        return repository.findNearStores(origin, searchDist)
    }

    fun buy(items: Collection<Item>, store: Store, date: Date) = repository.buy(items, store, date)

    fun updateItems(items: Collection<Item>) = repository.updateItems(items)

    fun flagItemForDeletion(item: Item) {
        item.isFlaggedForDeletion = true
        updateItems(listOf(item))
    }

    fun deleteItem(item: Item) = repository.deleteItem(item)

    fun restoreDeletedItem(item: Item) {
        item.isFlaggedForDeletion = false
        updateItems(listOf(item))
    }

    fun resetFoundStores() = foundStores.clear()

    fun getStoreIcon() = if (foundStores.size != 1) R.drawable.ic_store
    else foundStores[0].category.storeImageRes

    fun getStoreTitle(): String = when (foundStores.size) {
        0 -> getApplication<Application>().getString(R.string.menu_text_new_store_found)
        1 -> foundStores[0].name
        else -> NumberFormat.getInstance().format(foundStores.size)
    }
}
