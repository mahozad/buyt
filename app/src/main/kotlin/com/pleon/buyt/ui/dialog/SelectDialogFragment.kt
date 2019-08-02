package com.pleon.buyt.ui.dialog

import android.app.Dialog
import android.content.DialogInterface.BUTTON_POSITIVE
import android.os.Bundle
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.pleon.buyt.R
import com.pleon.buyt.ui.adapter.SelectionListAdapter
import java.io.Serializable

// DialogFragment is just another Fragment
class SelectDialogFragment : AppCompatDialogFragment(), SelectionListAdapter.Callback {

    class SelectDialogRow(val name: String, val imgRes: Int) : Serializable

    interface Callback {
        fun onSelected(index: Int)
    }

    private lateinit var dialog: AlertDialog

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
        val inflater = activity!!.layoutInflater
        val customView = inflater.inflate(R.layout.fragment_selection_list, null)

        val adapter = SelectionListAdapter(this)
        adapter.setList(arguments!!.getSerializable("LIST") as List<SelectDialogRow>)

        val storeRecyclerView = customView.findViewById<RecyclerView>(R.id.storeList)
        storeRecyclerView.adapter = adapter
        // remove RecyclerView blinking animation
        (storeRecyclerView.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false

        dialog = AlertDialog.Builder(activity!!)
                .setView(customView).setTitle(getString(arguments!!.getInt("TITLE")))
                .setPositiveButton(android.R.string.ok) { _, _ ->
                    val selectedIndex = adapter.selectedIndex
                    callback!!.onSelected(selectedIndex)
                }
                .setNegativeButton(android.R.string.cancel) { d, which ->
                    // cancel
                }.create()

        dialog.setCanceledOnTouchOutside(false)
        // Disable OK button by default (the button can be get only after the dialog is shown)
        dialog.setOnShowListener { d -> (d as AlertDialog).getButton(BUTTON_POSITIVE).isEnabled = false }

        return dialog
    }

    override fun onStoreClick() {
        dialog.getButton(BUTTON_POSITIVE).isEnabled = true
    }

    // @Override
    // public void onAttach(Context context) {
    //     super.onAttach(context);
    //     if (context instanceof Callback) {
    //         callback = (Callback) context;
    //     } else {
    //         throw new RuntimeException(context.toString() + " must implement Callback");
    //     }
    // }

    override fun onDetach() {
        super.onDetach()
        callback = null
    }

    companion object {
        private var callback: Callback? = null

        fun newInstance(callback: Callback, @StringRes title: Int,
                        list: List<SelectDialogRow>): SelectDialogFragment {
            /* FIXME: callback should be set in the onAttach() method, but because the context passed
             *  to it is the containing activity and not the containing fragment, we passed it here */
            Companion.callback = callback

            val fragment = SelectDialogFragment()
            val args = Bundle()
            args.putSerializable("LIST", list as Serializable)
            fragment.arguments = args
            args.putInt("TITLE", title)

            return fragment
        }
    }
}
