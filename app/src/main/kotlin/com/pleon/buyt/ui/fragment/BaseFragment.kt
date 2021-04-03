package com.pleon.buyt.ui.fragment

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import org.koin.android.ext.android.inject

abstract class BaseFragment : Fragment() {

    protected val prefs: SharedPreferences by inject()

    @LayoutRes abstract fun layout(): Int

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedState: Bundle?): View {
        return inflater.inflate(layout(), container, false)
    }
}
