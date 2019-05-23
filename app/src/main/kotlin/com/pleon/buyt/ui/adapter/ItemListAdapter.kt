package com.pleon.buyt.ui.adapter

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.MotionEvent.ACTION_DOWN
import android.view.View
import android.view.View.*
import android.view.ViewGroup
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.transition.ChangeBounds
import androidx.transition.TransitionManager.beginDelayedTransition
import com.pleon.buyt.R
import com.pleon.buyt.model.Item
import com.pleon.buyt.ui.BaseViewHolder
import com.pleon.buyt.ui.NumberInputWatcher
import com.pleon.buyt.ui.adapter.ItemListAdapter.ItemHolder
import kotlinx.android.synthetic.main.item_list_row.view.*
import java.lang.Long.parseLong

class ItemListAdapter(private val context: Context, private val itemTouchHelper: ItemTouchHelper) : Adapter<ItemHolder>() {

    var items = listOf<Item>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }
    val selectedItems = mutableSetOf<Item>()
    private lateinit var recyclerView: RecyclerView
    private var dragModeEnabled = false
    private var selectionModeEnabled = false

    init {
        // setHasStableIds is an optimization hint that you give to the RecyclerView
        // and tell it "when I provide a ViewHolder, its id is unique and will not change."
        setHasStableIds(true)
    }

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
        // if (this.items != null) {
        holder.bindItem(items[position])
        // } else: case of data not being ready yet; set a placeholder or something
    }

    /**
     * setHasStableIds() should also be set (in e.g. constructor). This is an optimization hint that you
     * give to the RecyclerView and tell it "when I provide a ViewHolder, its id is unique and won't change."
     */
    override fun getItemId(position: Int) = items[position].itemId

    override fun getItemCount() = items.size

    fun getItem(position: Int) = items[position]

    fun clearSelectedItems() = selectedItems.clear()

    fun toggleDragMode() {
        if (items.isNotEmpty()) { // toggle only if there is any item
            dragModeEnabled = !dragModeEnabled
            notifyDataSetChanged()
        }
    }

    fun togglePriceInput(enabled: Boolean) {
        selectionModeEnabled = enabled
        notifyDataSetChanged()
    }

    // Adapter (and RecyclerView) work with ViewHolders instead of direct Views.
    inner class ItemHolder(view: View) : BaseViewHolder(view) {

        init {
            val suffix = context.getString(R.string.input_suffix_price)
            itemView.price.addTextChangedListener(NumberInputWatcher(itemView.price_layout, itemView.price, suffix))
            itemView.price.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) = onPriceChanged()
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            })
            itemView.dragButton.setOnTouchListener { _, event -> onDragHandleTouch(event) }
            itemView.cardForeground.setOnClickListener { onCardClick() }
            itemView.selectCheckBox.setOnCheckedChangeListener { _, isChecked -> onItemSelected(isChecked) }
        }

        fun bindItem(item: Item) {
            itemView.categoryIcon.setImageResource(item.category.imageRes)
            itemView.item_name.text = item.name
            itemView.description.text = item.description
            itemView.item_quantity.text = context.getString(R.string.item_quantity,
                    item.quantity.value, context.getString(item.quantity.unit.nameRes))
            itemView.urgentIcon.visibility = if (item.isUrgent) VISIBLE else INVISIBLE
            itemView.selectCheckBox.isChecked = selectedItems.contains(item)
            itemView.description.visibility = if (item.description.isNullOrEmpty()) GONE else VISIBLE
            itemView.circular_reveal.alpha = 0f // for the case of undo of deleted item
            itemView.price.setText(if (item.totalPrice != 0L) item.totalPrice.toString() else "")

            if (selectionModeEnabled) {
                itemView.selectCheckBox.visibility = VISIBLE
                itemView.dragButton.visibility = INVISIBLE
            } else {
                itemView.selectCheckBox.visibility = INVISIBLE
                itemView.price_container.visibility = GONE
                itemView.dragButton.visibility = if (dragModeEnabled) VISIBLE else INVISIBLE
            }
        }

        private fun onDragHandleTouch(event: MotionEvent): Boolean {
            if (event.actionMasked == ACTION_DOWN) {
                // disabled in clearView() method of the touch helper
                if (dragModeEnabled) itemView.cardForeground.isDragged = true
                itemTouchHelper.startDrag(this)
            }
            return false
        }

        private fun onCardClick() {
            if (selectionModeEnabled) itemView.selectCheckBox.performClick()
        }

        fun onPriceChanged() {
            val priceString = itemView.price.text.toString().replace("[^\\d]".toRegex(), "")
            if (priceString.isNotEmpty()) {
                val price = parseLong(priceString)
                val item = items[adapterPosition]
                item.totalPrice = price
            }
        }

        private fun onItemSelected(checked: Boolean) {
            beginDelayedTransition(recyclerView, ChangeBounds().setDuration(200))
            if (checked) {
                itemView.price_container.visibility = VISIBLE
                selectedItems.add(items[adapterPosition])
            } else {
                itemView.price_container.visibility = GONE
                selectedItems.remove(items[adapterPosition])
                val item = items[adapterPosition]
                item.totalPrice = 0
                itemView.price.text?.clear()
            }
        }
    }
}
