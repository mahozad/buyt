package com.pleon.buyt.ui.dialog

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS
import androidx.appcompat.app.AppCompatDialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.pleon.buyt.R

// DialogFragment is just another Fragment
class LocationOffDialogFragment : AppCompatDialogFragment() {

    private var callback: Callback? = null

    /**
     * When you override `onCreateDialog`, Android COMPLETELY IGNORES several
     * attributes of the root node of the .xml Layout you inflate. This includes,
     * but probably isn't limited to:
     *  * background_color
     *  * layout_gravity
     *  * layout_width
     *  * layout_height
     *
     * See [this very good explanation](https://stackoverflow.com/a/41495370/8583692)
     *
     * @param savedState
     * @return
     */
    override fun onCreateDialog(savedState: Bundle?): Dialog {

        val dialog = MaterialAlertDialogBuilder(context!!)
                .setIcon(R.drawable.ic_location_off)
                .setPositiveButton(getString(R.string.dialog_action_go_to_settings)) { _, _ -> startActivity(Intent(ACTION_LOCATION_SOURCE_SETTINGS)) }
                .setNegativeButton(getString(R.string.dialog_action_skip)) { _, _ -> callback!!.onEnableLocationDenied() }
                .create()

        // getText is to preserve html formats
        dialog.setTitle(R.string.dialog_title_location_off)
        dialog.setMessage(getText(R.string.dialog_message_location_off))
        dialog.setCancelable(false) // Prevent dialog from getting dismissed on back key pressed
        dialog.setCanceledOnTouchOutside(false)
        return dialog
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is Callback) {
            callback = context
        } else {
            throw RuntimeException("$context must implement $callback")
        }
    }

    override fun onDetach() {
        super.onDetach()
        callback = null
    }

    companion object {
        fun newInstance(): LocationOffDialogFragment {
            return LocationOffDialogFragment()
        }
    }
}
