package com.pleon.buyt.ui.dialog

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS
import androidx.appcompat.app.AppCompatDialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.pleon.buyt.R
import com.pleon.buyt.ui.dialog.LocationOffDialogFragment.RationalType.LOCATION_PERMISSION_DENIED

// DialogFragment is just another Fragment
class LocationOffDialogFragment : AppCompatDialogFragment() {

    companion object {
        fun newInstance(rationalType: RationalType): LocationOffDialogFragment {
            val fragment = LocationOffDialogFragment()
            val args = Bundle()
            args.putSerializable("RATIONAL", rationalType)
            fragment.arguments = args
            return fragment
        }
    }

    interface LocationEnableListener {
        fun onEnableLocationDenied()
    }

    enum class RationalType {
        LOCATION_OFF, LOCATION_PERMISSION_DENIED
    }

    private var callback: LocationEnableListener? = null

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
        val dialog = MaterialAlertDialogBuilder(requireContext(), R.style.JustifiedTextDialogStyle)
                .setIcon(R.drawable.ic_location_off)
                .setPositiveButton(R.string.dialog_action_go_to_settings) { _, _ -> onPositiveButtonClick() }
                .setNegativeButton(R.string.dialog_action_skip) { _, _ -> callback!!.onEnableLocationDenied() }
                .create()

        dialog.setTitle(if (getRationalType() == LOCATION_PERMISSION_DENIED) R.string.dialog_title_location_permission else R.string.dialog_title_location_off)
        dialog.setMessage(getText(
                if (getRationalType() == LOCATION_PERMISSION_DENIED) R.string.dialog_message_location_permission
                else R.string.dialog_message_location_off)
        )
        dialog.setCancelable(false) // Prevent dialog from getting dismissed on back key pressed
        dialog.setCanceledOnTouchOutside(false)
        return dialog
    }

    private fun onPositiveButtonClick() {
        if (getRationalType() == LOCATION_PERMISSION_DENIED) {
            val intent = Intent(ACTION_APPLICATION_DETAILS_SETTINGS)
            val uri = Uri.fromParts("package", requireActivity().packageName, null)
            intent.data = uri
            startActivity(intent)
        } else {
            startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
        }
    }

    private fun getRationalType() = requireArguments().getSerializable("RATIONAL") as RationalType

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is LocationEnableListener) callback = context
        else throw RuntimeException("$context must implement $callback")
    }

    override fun onDetach() {
        super.onDetach()
        callback = null
    }
}
