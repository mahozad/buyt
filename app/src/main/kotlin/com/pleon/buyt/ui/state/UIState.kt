package com.pleon.buyt.ui.state

import com.pleon.buyt.ui.activity.MainActivity

lateinit var activity: MainActivity

interface UIState {

    fun event(event: Event)

}
