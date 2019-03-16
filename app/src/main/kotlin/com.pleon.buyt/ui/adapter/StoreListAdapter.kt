package com.pleon.buyt.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView.Adapter
import com.pleon.buyt.R
import com.pleon.buyt.model.Store
import com.pleon.buyt.ui.BaseViewHolder
import kotlinx.android.synthetic.main.store_list_row.view.*

class StoreListAdapter(private val context: Context) : Adapter<StoreListAdapter.StoreHolder>() {

    private var stores: List<Store>? = null

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
        // keep this method as lightweight as possible as it is called for every row
        if (stores != null) {
            holder.bindStore(stores!![position])
        } // else: case of data not being ready yet; set a placeholder or something
    }

    override fun getItemCount(): Int {
        return if (stores == null) 0 else stores!!.size
    }

    // setHasStableIds() should also be set (in e.g. constructor). This is an optimization hint that you
    // give to the RecyclerView and tell it "when I provide a ViewHolder, its id is unique and won't change."
    override fun getItemId(position: Int): Long {
        return stores!![position].storeId
    }

    fun setStores(stores: List<Store>) {
        this.stores = stores
        notifyDataSetChanged()
    }

    fun getStore(position: Int): Store {
        return stores!![position]
    }

    // Adapter (and RecyclerView) work with ViewHolders instead of direct Views.
    inner class StoreHolder(view: View) : BaseViewHolder(view) {

        fun bindStore(store: Store) {
            itemView.storeIcon.setImageResource(store.category!!.storeImageRes)
            itemView.storeName.text = store.name
            itemView.circular_reveal.alpha = 0f // for the case of undo of deleted item
        }
    }
}
