package com.pleon.buyt.ui.dialog

import android.app.Dialog
import android.os.Bundle
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDialogFragment
import com.pleon.buyt.R

class MessageDialogFragment : AppCompatDialogFragment() {

    override fun onCreateDialog(savedState: Bundle?): Dialog {
        @StringRes val title = arguments!!["TITLE"] as Int
        @StringRes val message = arguments!!["MESSAGE"] as Int

        val inflater = activity!!.layoutInflater
        val view = inflater.inflate(R.layout.fragment_message_dialog, null)
        val textView = view.findViewById<TextView>(R.id.message)
        textView.setText(message)

        return AlertDialog.Builder(activity!!)
                .setView(view)
                .setTitle(getString(title))
                .setPositiveButton(android.R.string.ok) { _, _ -> /* Dismiss */ }
                .create()
                .apply { setCanceledOnTouchOutside(false) }
    }

    companion object {
        fun newInstance(@StringRes title: Int,
                        @StringRes message: Int): MessageDialogFragment {
            val args = Bundle()
            args.putInt("TITLE", title)
            args.putInt("MESSAGE", message)
            return MessageDialogFragment().apply { arguments = args }
        }
    }
}
