package com.pleon.buyt.ui.dialog

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AppCompatDialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.pleon.buyt.R

class UpgradeSuccessDialogFragment : AppCompatDialogFragment() {

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
        val dialog = MaterialAlertDialogBuilder(context!!, R.style.JustifiedTextDialogStyle)
                .setIcon(R.drawable.ic_premium)
                .setPositiveButton(android.R.string.ok) { _, _ -> /* Dismiss */ }
                .create()

        dialog.setTitle(R.string.dialog_title_congratulate_premium)
        dialog.setMessage(getText(R.string.dialog_message_congratulate_premium))
        dialog.setCancelable(false) // Prevent dialog from getting dismissed on back key pressed
        dialog.setCanceledOnTouchOutside(false)
        return dialog
    }
}
