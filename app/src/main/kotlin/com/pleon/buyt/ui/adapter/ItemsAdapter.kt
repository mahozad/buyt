package com.pleon.buyt.ui.adapter

import android.app.Application
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.MotionEvent.ACTION_DOWN
import android.view.View
import android.view.View.*
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.ChangeBounds
import androidx.transition.TransitionManager.beginDelayedTransition
import com.pleon.buyt.R
import com.pleon.buyt.model.Item
import com.pleon.buyt.ui.BaseViewHolder
import com.pleon.buyt.ui.NumberInputWatcher
import com.pleon.buyt.ui.adapter.ItemsAdapter.ItemHolder
import com.pleon.buyt.ui.newAfterTextWatcher
import com.pleon.buyt.util.removeNonDigitChars
import kotlinx.android.synthetic.main.item_list_row.view.*
import java.lang.Long.parseLong

class ItemsAdapter(private val app: Application) : ListAdapter<Item, ItemHolder>(ItemDiffCallback) {

    object ItemDiffCallback : DiffUtil.ItemCallback<Item>() {
        override fun areItemsTheSame(oldItem: Item, newItem: Item) = oldItem.itemId == newItem.itemId
        override fun areContentsTheSame(oldItem: Item, newItem: Item) = with(newItem) {
            name == oldItem.name &&
                    quantity == oldItem.quantity &&
                    description == oldItem.description &&
                    isUrgent == oldItem.isUrgent
        }
    }

    val selectedItems = mutableSetOf<Item>()
    lateinit var touchHelper: ItemTouchHelper
    private lateinit var recyclerView: RecyclerView
    private var dragModeEnabled = false
    private var selectionModeEnabled = false

    /**
     * setHasStableIds is an optimization hint that you give to the RecyclerView
     * and tell it "when I provide a ViewHolder, its id is unique and will not change."
     */
    override fun setHasStableIds(hasStableIds: Boolean) = super.setHasStableIds(true)

    /**
     * setHasStableIds() should also be set (in e.g. constructor). This is an optimization hint that you
     * give to the RecyclerView and tell it "when I provide a ViewHolder, its id is unique and won't change."
     */
    override fun getItemId(position: Int) = currentList[position].itemId

    /**
     * Gets a reference of the enclosing RecyclerView.
     *
     * Note that if the adapter is assigned to multiple RecyclerViews, then only one
     * of them is assigned to the filed because every time the adapter is attached to a new
     * RecyclerView, this method is called and therefore the field is overwritten.
     *
     * @param recyclerView the enclosing RecyclerView
     */
    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        this.recyclerView = recyclerView
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHolder {
        val itemView = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_list_row, parent, false)
        return ItemHolder(itemView)
    }

    /**
     * keep this method as lightweight as possible as it is called for every row
     */
    override fun onBindViewHolder(holder: ItemHolder, position: Int) {
        holder.bindItem(currentList[position])
    }

    public override fun getItem(position: Int): Item = super.getItem(position)

    fun clearSelectedItems() = selectedItems.clear()

    fun toggleDragMode() {
        if (currentList.isNotEmpty()) { // toggle only if there is any item
            dragModeEnabled = !dragModeEnabled
            notifyDataSetChanged()
        }
    }

    fun togglePriceInput(isEnabled: Boolean) {
        selectionModeEnabled = isEnabled
        notifyDataSetChanged()
    }

    // Adapter (and RecyclerView) work with ViewHolders instead of direct Views.
    inner class ItemHolder(view: View) : BaseViewHolder(view) {

        init {
            val suffix = app.getString(R.string.input_suffix_price)
            itemView.price.addTextChangedListener(NumberInputWatcher(itemView.price_layout, itemView.price, suffix))
            itemView.price.addTextChangedListener(newAfterTextWatcher(this::onPriceChanged))
            itemView.dragButton.setOnTouchListener(this::onDragHandleTouch)
            itemView.cardForeground.setOnClickListener(this::onCardClick)
            itemView.selectCheckBox.setOnCheckedChangeListener(this::onItemSelected)
        }

        fun bindItem(item: Item) {
            itemView.categoryIcon.setImageResource(item.category.imageRes)
            itemView.item_name.text = item.name
            itemView.description.text = item.description
            itemView.description.visibility = if (item.description.isNullOrEmpty()) GONE else VISIBLE
            itemView.item_quantity.text = app.getString(R.string.item_quantity,
                    item.quantity.value, app.getString(item.quantity.unit.nameRes))
            itemView.urgentIcon.visibility = if (item.isUrgent) VISIBLE else INVISIBLE
            itemView.selectCheckBox.isChecked = item in selectedItems
            itemView.selectCheckBox.visibility = if (selectionModeEnabled) VISIBLE else INVISIBLE
            itemView.circular_reveal.alpha = 0f // for the case of undo of deleted item
            itemView.price.setText(if (item.totalPrice != 0L) item.totalPrice.toString() else "")
            itemView.dragButton.visibility = INVISIBLE
            if (dragModeEnabled && !selectionModeEnabled) itemView.dragButton.visibility = VISIBLE

            if (selectionModeEnabled && item in selectedItems) {
                itemView.price_container.visibility = VISIBLE
            } else {
                itemView.price_container.visibility = GONE
            }
        }

        private fun onDragHandleTouch(view: View, event: MotionEvent): Boolean {
            if (event.actionMasked == ACTION_DOWN) {
                // disabled in clearView() method of the touch helper
                if (dragModeEnabled) itemView.cardForeground.isDragged = true
                touchHelper.startDrag(this)
            }
            return false
        }

        private fun onCardClick(view: View) {
            if (selectionModeEnabled) itemView.selectCheckBox.performClick()
        }

        private fun onPriceChanged() {
            val priceString = itemView.price.text!!.removeNonDigitChars()
            if (priceString.isNotEmpty()) {
                val price = parseLong(priceString)
                currentList[adapterPosition].totalPrice = price
            }
        }

        private fun onItemSelected(view: View, isChecked: Boolean) {
            beginDelayedTransition(recyclerView, ChangeBounds().setDuration(200))
            if (isChecked) {
                itemView.price_container.visibility = VISIBLE
                selectedItems.add(currentList[adapterPosition])
            } else {
                itemView.price_container.visibility = GONE
                currentList[adapterPosition].totalPrice = 0
                selectedItems.remove(currentList[adapterPosition])
                itemView.price.text?.clear()
            }
        }
    }
}
