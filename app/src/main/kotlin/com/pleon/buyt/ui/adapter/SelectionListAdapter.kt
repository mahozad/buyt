package com.pleon.buyt.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.RadioButton
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.pleon.buyt.R
import com.pleon.buyt.ui.adapter.SelectionListAdapter.StoreHolder
import com.pleon.buyt.ui.dialog.SelectDialogFragment.SelectDialogRow
import kotlinx.android.synthetic.main.selection_list_row.view.*

class SelectionListAdapter(private val callback: Callback) : Adapter<StoreHolder>() {

    interface Callback {
        fun onItemClicked()
    }

    var selectedIndex = -1
    private var list: List<SelectDialogRow>? = null

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

                callback.onItemClicked()

                notifyItemChanged(selectedIndex)
                selectedIndex = holder.adapterPosition
                notifyItemChanged(selectedIndex)
            }
            holder.itemView.storeRadioButton.setOnClickListener(clickListener)
            holder.view.setOnClickListener(clickListener)
        } // else: case of data not being ready yet; set a placeholder or something
    }

    override fun getItemCount() = list?.size ?: 0

    fun setList(list: List<SelectDialogRow>) {
        this.list = list
        notifyDataSetChanged()
    }

    // Adapter (and RecyclerView) works with ViewHolders instead of direct Views.
    inner class StoreHolder(val view: View) : ViewHolder(view) {
        fun bindRow(selection: SelectDialogRow) {
            itemView.storeName.text = selection.name
            itemView.storeIcon.setImageResource(selection.imgRes)
            itemView.storeRadioButton.isChecked = (adapterPosition == selectedIndex)
        }
    }
}
