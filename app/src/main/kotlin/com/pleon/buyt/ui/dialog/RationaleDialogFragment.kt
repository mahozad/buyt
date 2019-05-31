package com.pleon.buyt.ui.dialog

import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS
import androidx.appcompat.app.AppCompatDialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.pleon.buyt.R

// DialogFragment is just another Fragment
class RationaleDialogFragment : AppCompatDialogFragment() {

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
                .setPositiveButton(getString(R.string.dialog_action_go_to_settings)) { _, _ ->
                    val intent = Intent(ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri = Uri.fromParts("package", activity!!.packageName, null)
                    intent.data = uri
                    startActivity(intent)
                }
                .setNegativeButton(getString(R.string.dialog_action_skip)) { _, _ -> /* To dismiss on click */ }
                .create()

        dialog.setTitle(R.string.dialog_title_location_permission)
        dialog.setMessage(getText(R.string.dialog_message_location_permission))
        dialog.setCancelable(false) // Prevent dialog from getting dismissed on back key pressed
        dialog.setCanceledOnTouchOutside(false)
        return dialog
    }

    companion object {
        fun newInstance(): RationaleDialogFragment {
            return RationaleDialogFragment()
        }
    }
}
