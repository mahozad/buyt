package com.pleon.buyt.util

import androidx.recyclerview.widget.ItemTouchHelper
import com.pleon.buyt.ui.TouchHelperCallback

object ReflectionUtil {

    fun extractTouchHelperCallback(touchHelper: ItemTouchHelper): TouchHelperCallback {
        val callbackField = ItemTouchHelper::class.java.getDeclaredField("mCallback")
        callbackField.isAccessible = true
        return callbackField[touchHelper] as TouchHelperCallback
    }
}
