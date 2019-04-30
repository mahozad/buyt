package com.pleon.buyt.ui

import android.view.View
import com.google.android.material.snackbar.BaseTransientBottomBar.BaseCallback
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.snackbar.Snackbar.LENGTH_LONG
import com.pleon.buyt.R

fun showSnackbar(container: View, message: Int, length: Int, action: Int = 0) {
    val snackbar = Snackbar.make(container, message, length)
    if (action != 0) snackbar.setAction(action) { /* to dismiss on click */ }
    snackbar.show()
}

fun showUndoSnackbar(container: View, message: String, onUndo: () -> Unit, onDismiss: () -> Unit) {
    val snackbar = Snackbar.make(container, message, LENGTH_LONG)

    snackbar.setAction(R.string.snackbar_action_undo) { onUndo() }
    snackbar.addCallback(object : BaseCallback<Snackbar>() {
        override fun onDismissed(bar: Snackbar?, event: Int) {
            // If dismiss wasn't because of "UNDO" then call listener
            if (event != DISMISS_EVENT_ACTION) onDismiss()
        }
    })

    snackbar.show()
}
