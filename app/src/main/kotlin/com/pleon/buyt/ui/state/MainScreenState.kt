package com.pleon.buyt.ui.state

import com.pleon.buyt.ui.activity.MainActivity

lateinit var activity: MainActivity

// State Design Pattern
interface MainScreenState {

    fun event(event: Event)

}
