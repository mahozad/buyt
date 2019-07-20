package com.pleon.buyt.ui.state

import android.location.Location
import android.os.Bundle
import com.pleon.buyt.model.Store

sealed class Event {
    object FabClicked : Event()
    object HomeClicked : Event()
    object BackClicked : Event()
    object FindingSkipped : Event()
    object OptionsMenuCreated : Event()
    object LocationPermissionGranted : Event()
    class StoresFound(val stores: List<Store>) : Event()
    class StoreCreated(val store: Store) : Event()
    class StoreSelected(val storeIndex: Int) : Event()
    class LocationFound(val location: Location) : Event()
    class ItemListChanged(val isListEmpty:Boolean) : Event()
    class SaveInstanceCalled(val outState: Bundle) : Event()
    class RestoreInstanceCalled(val savedState: Bundle) : Event()
}
