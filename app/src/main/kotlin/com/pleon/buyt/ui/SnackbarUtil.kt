package com.pleon.buyt.ui

import android.view.View
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.snackbar.Snackbar.LENGTH_LONG
import com.google.android.material.snackbar.Snackbar.make
import com.pleon.buyt.R

fun showUndoSnackbar(snbContainer: View, message: String,
                     onUndo: () -> Unit, onDismiss: () -> Unit) {
    val snackbar = make(snbContainer, message, LENGTH_LONG)

    snackbar.setAction(R.string.snackbar_action_undo) { onUndo() }
    snackbar.addCallback(object : BaseTransientBottomBar.BaseCallback<Snackbar>() {
        override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
            // If dismiss wasn't because of "UNDO" then delete the store from database
            if (event != DISMISS_EVENT_ACTION) onDismiss()
        }
    })

    snackbar.show()
}
