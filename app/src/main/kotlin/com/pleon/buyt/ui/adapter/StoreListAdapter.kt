package com.pleon.buyt.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView.Adapter
import com.pleon.buyt.R
import com.pleon.buyt.model.Store
import com.pleon.buyt.ui.BaseViewHolder
import kotlinx.android.synthetic.main.store_list_row.view.*

class StoreListAdapter : Adapter<StoreListAdapter.StoreHolder>() {

    var stores = listOf<Store>()
        set(stores) {
            field = stores
            notifyDataSetChanged()
        }

    init {
        // setHasStableIds is an optimization hint that you give to the RecyclerView
        // and tell it "when I provide a ViewHolder, its id is unique and will not change."
        setHasStableIds(true)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StoreHolder {
        val itemView = LayoutInflater.from(parent.context)
                .inflate(R.layout.store_list_row, parent, false)
        return StoreHolder(itemView)
    }

    override fun onBindViewHolder(holder: StoreHolder, position: Int) {
        // if (stores != null) {
        holder.bindStore(stores[position])
        // } else: case of data not being ready yet; set a placeholder or something
    }

    override fun getItemCount() = stores.size

    override fun getItemId(position: Int) = stores[position].storeId

    fun getStore(position: Int) = stores[position]

    // Adapter (and RecyclerView) work with ViewHolders instead of direct Views.
    inner class StoreHolder(view: View) : BaseViewHolder(view) {
        fun bindStore(store: Store) {
            itemView.storeIcon.setImageResource(store.category.storeImageRes)
            itemView.storeName.text = store.name
            itemView.circular_reveal.alpha = 0f // for the case of undo of deleted item
        }
    }
}
