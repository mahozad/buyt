package com.pleon.buyt.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import dagger.android.support.DaggerFragment

abstract class BaseFragment : DaggerFragment() {

    abstract fun layout(): Int

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedState: Bundle?): View {
        return inflater.inflate(layout(), container, false)
    }
}
