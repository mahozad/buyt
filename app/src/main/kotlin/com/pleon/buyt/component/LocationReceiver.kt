package com.pleon.buyt.component

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.location.Location
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocationReceiver @Inject constructor() : BroadcastReceiver() {

    private val location = MutableLiveData<Location>()

    override fun onReceive(cxt: Context?, intent: Intent) {
        location.value = intent.getParcelableExtra(EXTRA_LOCATION)
    }

    fun getLocation(): LiveData<Location> = location
}
