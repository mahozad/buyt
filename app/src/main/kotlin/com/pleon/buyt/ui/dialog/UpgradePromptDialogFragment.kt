package com.pleon.buyt.ui.dialog

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatDialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.pleon.buyt.R
import com.pleon.buyt.ui.activity.EXTRA_SHOULD_START_UPGRADE
import com.pleon.buyt.ui.activity.HelpActivity

class UpgradePromptDialogFragment(private val message: CharSequence) : AppCompatDialogFragment() {

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
                .setIcon(R.drawable.ic_premium)
                .setPositiveButton(R.string.dialog_action_upgrade_to_premium) { _, _ ->
                    val intent = Intent(context, HelpActivity::class.java)
                    intent.putExtra(EXTRA_SHOULD_START_UPGRADE, true)
                    startActivity(intent)
                }
                .setNegativeButton(android.R.string.cancel) { _, _ -> /* To dismiss on click */ }
                .create()

        dialog.setTitle(R.string.dialog_title_upgrade_to_premium)
        dialog.setMessage(message)
        dialog.setCancelable(false) // Prevent dialog from getting dismissed on back key pressed
        dialog.setCanceledOnTouchOutside(false)
        return dialog
    }
}
