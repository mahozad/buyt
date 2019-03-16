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
import java.lang.Math.cos
import java.util.*

// The ViewModel's role is to provide data to the UI and survive configuration changes.
// Every screen in the app (an activity with all its fragments) has one corresponding viewModel for itself.
// A ViewModel acts as a communication center between the Repository and the UI.
// You can also use a ViewModel to share data between fragments

// Warning: Never pass context into ViewModel instances. Do not store Activity, Fragment, or View instances or their Context in the ViewModel.
// For example, an Activity can be destroyed and created many times during the lifecycle of a ViewModel as the device is rotated.
// If you store a reference to the Activity in the ViewModel, you end up with references that point to the destroyed Activity. This is a memory leak.

/**
 * [ViewModels][androidx.lifecycle.ViewModel] only survive configuration changes and not
 * force-kills. So to survive process stops, implement
 * [AppCompatActivity.onSaveInstanceState] method in your activity/fragment.
 */
class MainViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        private const val EARTH_RADIUS = 6371000.0 // In meters
    }

    enum class State {
        IDLE, FINDING, SELECTING
    }

    private val preferences = getDefaultSharedPreferences(application)
    private val repository = MainRepository(application)
    @Volatile var state = IDLE
    var location: Location? = null
    var isFindingSkipped = false
    var foundStores: MutableList<Store> = ArrayList()
    var shouldCompletePurchase = false
    var shouldAnimateNavIcon = false
    @DrawableRes var storeIcon = 0
    @StringRes var storeTitle = 0

    // TODO: Use paging library architecture component
    val allItems = repository.allItems
    val allStores = repository.getAllStores()

    fun buy(items: Collection<Item>, store: Store, purchaseDate: Date) {
        repository.buy(items, store, purchaseDate)
    }

    fun findNearStores(origin: Coordinates): LiveData<List<Store>> {
        val distInMeters = preferences.getString("distance", "50")!!.toInt()
        val nearStoresDistance = cos(distInMeters / EARTH_RADIUS)
        return repository.findNearStores(origin, nearStoresDistance)
    }

    fun updateItems(items: Collection<Item>) = repository.updateItems(items)

    fun deleteItem(item: Item) = repository.deleteItem(item)

    fun resetFoundStores() = foundStores.clear()

    fun getStoreTitle(): String {
        return if (foundStores.size == 1) foundStores[0].name.toString()
        else getApplication<Application>().getString(storeTitle)
    }
}
