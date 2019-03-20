package com.pleon.buyt.viewmodel

import android.app.Application
import android.location.Location
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.preference.PreferenceManager.getDefaultSharedPreferences
import com.pleon.buyt.database.repository.MainRepository
import com.pleon.buyt.model.Coordinates
import com.pleon.buyt.model.Item
import com.pleon.buyt.model.Store
import com.pleon.buyt.viewmodel.MainViewModel.State.IDLE
import java.util.*
import kotlin.math.cos

private const val EARTH_RADIUS = 6_371_000.0 // In meters

// Every screen in the app (an activity with all its fragments) has one corresponding viewModel for itself.
// A ViewModel acts as a communication center between the Repository and the UI.
// You can also use a ViewModel to share data between fragments.

/**
 * [ViewModels][androidx.lifecycle.ViewModel] only survive configuration changes and not
 * force-kills. So to survive process stops, implement
 * [AppCompatActivity.onSaveInstanceState] method in your activity/fragment.
 */
class MainViewModel(application: Application) : AndroidViewModel(application) {

    enum class State {
        IDLE, FINDING, SELECTING
    }

    private val preferences = getDefaultSharedPreferences(application)
    private val repository = MainRepository(application)
    @Volatile var state = IDLE
    var location: Location? = null
    var isFindingSkipped = false
    var foundStores = mutableListOf<Store>()
    var shouldCompletePurchase = false
    var shouldAnimateNavIcon = false
    @DrawableRes var storeIcon = 0
    @StringRes var storeTitle = 0

    // TODO: Use paging library architecture component
    val allItems = repository.allItems
    val allStores get() = repository.getAllStores()

    fun buy(items: Collection<Item>, store: Store, purchaseDate: Date) =
            repository.buy(items, store, purchaseDate)

    fun findNearStores(origin: Coordinates): LiveData<List<Store>> {
        val distInMeters = preferences.getString("distance", "50")!!.toInt()
        val nearStoresDistance = cos(distInMeters / EARTH_RADIUS)
        return repository.findNearStores(origin, nearStoresDistance)
    }

    fun updateItems(items: Collection<Item>) = repository.updateItems(items)

    fun deleteItem(item: Item) = repository.deleteItem(item)

    fun resetFoundStores() = foundStores.clear()

    fun getStoreTitle(): String {
        return if (foundStores.size == 1) foundStores[0].name
        else getApplication<Application>().getString(storeTitle)
    }
}
