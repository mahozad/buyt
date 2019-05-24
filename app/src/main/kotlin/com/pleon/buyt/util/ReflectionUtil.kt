package com.pleon.buyt.util

import androidx.recyclerview.widget.ItemTouchHelper
import com.pleon.buyt.ui.TouchHelperCallback
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReflectionUtil @Inject constructor() {

    fun extractCallback(touchHelper: ItemTouchHelper): TouchHelperCallback {
        val callbackField = ItemTouchHelper::class.java.getDeclaredField("mCallback")
        callbackField.isAccessible = true
        return callbackField[touchHelper] as TouchHelperCallback
    }
}
