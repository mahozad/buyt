package com.pleon.buyt.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.RadioButton
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.pleon.buyt.R
import com.pleon.buyt.ui.adapter.SelectionListAdapter.StoreHolder
import com.pleon.buyt.ui.dialog.SelectionDialogRow
import kotlinx.android.synthetic.main.selection_list_row.view.*

class SelectionListAdapter(private val mContext: Context, private val callback: Callback) : Adapter<StoreHolder>() {

    interface Callback {
        fun onStoreClick()
    }

    private var list: List<SelectionDialogRow>? = null
    private var callbackNotified = false
    var selectedIndex = -1
        private set

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StoreHolder {
        val itemView = LayoutInflater.from(parent.context)
                .inflate(R.layout.selection_list_row, parent, false)
        return StoreHolder(itemView)
    }

    override fun onBindViewHolder(holder: StoreHolder, position: Int) {
        if (list != null) {
            // TODO: which callback method is the best for setting these listeners? (e.g. onCreate or...?)
            holder.bindRow(list!![position])
            val clickListener = OnClickListener {
                // To show radio button animation
                if (it !is RadioButton) holder.itemView.storeRadioButton.performClick()

                if (!callbackNotified) callback.onStoreClick()

                callbackNotified = true

                notifyItemChanged(selectedIndex)
                selectedIndex = position
                notifyItemChanged(selectedIndex)
            }
            holder.itemView.storeRadioButton.setOnClickListener(clickListener)
            holder.view.setOnClickListener(clickListener)
        } // else: case of data not being ready yet; set a placeholder or something
    }

    override fun getItemCount() = if (list == null) 0 else list!!.size

    fun setList(list: List<SelectionDialogRow>) {
        this.list = list
        notifyDataSetChanged()
    }

    // Adapter (and RecyclerView) works with ViewHolders instead of direct Views.
    inner class StoreHolder internal constructor(internal val view: View) : ViewHolder(view) {
        fun bindRow(selection: SelectionDialogRow) {
            itemView.storeName.text = selection.name
            itemView.storeIcon.setImageResource(selection.image)
            itemView.storeRadioButton.isChecked = position == selectedIndex
        }
    }
}
