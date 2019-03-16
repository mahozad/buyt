package com.pleon.buyt.ui.adapter

import android.content.Context
import android.graphics.drawable.Animatable
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
import butterknife.OnCheckedChanged
import butterknife.OnClick
import butterknife.OnTextChanged
import butterknife.OnTouch
import com.pleon.buyt.R
import com.pleon.buyt.model.Item
import com.pleon.buyt.ui.BaseViewHolder
import com.pleon.buyt.ui.NumberInputWatcher
import com.pleon.buyt.ui.adapter.ItemListAdapter.ItemHolder
import kotlinx.android.synthetic.main.item_list_row.view.*
import java.lang.Long.parseLong
import java.text.NumberFormat
import java.util.*

class ItemListAdapter(private val context: Context, private val itemTouchHelper: ItemTouchHelper) : Adapter<ItemHolder>() {

    var items: List<Item>? = null
        set(items) {
            field = items
            notifyDataSetChanged()
        }
    private var recyclerView: RecyclerView? = null
    private var dragModeEnabled = false
    private var selectionModeEnabled = false
    val selectedItems = HashSet<Item>()
    private val numberFormat: NumberFormat = NumberFormat.getInstance()

    init {
        // setHasStableIds is an optimization hint that you give to the RecyclerView
        // and tell it "when I provide a ViewHolder, its id is unique and will not change."
        setHasStableIds(true)
    }

    /**
     * Gets a reference of the enclosing RecyclerView.
     *
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

    override fun onBindViewHolder(holder: ItemHolder, position: Int) {
        // keep this method as lightweight as possible as it is called for every row
        if (this.items != null) {
            holder.bindItem(this.items!![position])
        } // else: case of data not being ready yet; set a placeholder or something
    }

    override fun getItemCount(): Int {
        return if (this.items == null) 0 else this.items!!.size
    }

    // setHasStableIds() should also be set (in e.g. constructor). This is an optimization hint that you
    // give to the RecyclerView and tell it "when I provide a ViewHolder, its id is unique and won't change."
    override fun getItemId(position: Int): Long {
        return this.items!![position].itemId
    }

    fun getItem(position: Int): Item {
        return this.items!![position]
    }

    fun getSelectedItems(): Set<Item> {
        return selectedItems
    }

    fun clearSelectedItems() {
        selectedItems.clear()
    }

    fun toggleEditMode() {
        if (this.items!!.isNotEmpty()) { // toggle only if there is any item
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

        var delAnimating: Boolean = false

        init {
            val suffix = context.getString(R.string.input_suffix_price)
            itemView.price!!.addTextChangedListener(NumberInputWatcher(itemView.price_layout, itemView.price, suffix))
        }

        fun bindItem(item: Item) {
            itemView.categoryIcon.setImageResource(item.category!!.imageRes)
            itemView.item_name.text = item.name
            itemView.description.text = item.description
            itemView.item_quantity.text = numberFormat.format(item.quantity!!.quantity) + " " + context.getString(item.quantity!!.unit!!.nameRes)
            itemView.urgentIcon.visibility = if (item.isUrgent) VISIBLE else INVISIBLE
            itemView.selectCheckBox.isChecked = selectedItems.contains(item)
            itemView.description.visibility = if (selectionModeEnabled || !item.isExpanded) GONE else VISIBLE
            itemView.circular_reveal.alpha = 0f // for the case of undo of deleted item
            itemView.price.setText(if (item.totalPrice != 0L) item.totalPrice.toString() else "")

            if (selectionModeEnabled) {
                itemView.selectCheckBox.visibility = VISIBLE
                itemView.expandDragButton.visibility = INVISIBLE
            } else {
                itemView.selectCheckBox.visibility = INVISIBLE
                itemView.price_container.visibility = GONE
                if (dragModeEnabled) {
                    itemView.expandDragButton.setImageResource(R.drawable.ic_drag_handle)
                    itemView.expandDragButton.visibility = VISIBLE
                } else if (item.description != null) {
                    itemView.expandDragButton.setImageResource(if (item.isExpanded) R.drawable.avd_collapse else R.drawable.avd_expand)
                    itemView.expandDragButton.visibility = VISIBLE
                } else {
                    itemView.expandDragButton.visibility = INVISIBLE
                }
            }
        }

        @OnTouch(R.id.expandDragButton)
        fun onDragHandleTouch(event: MotionEvent): Boolean {
            if (event.actionMasked == ACTION_DOWN) {
                if (dragModeEnabled) {
                    itemView.cardForeground!!.isDragged = true // disabled in clearView() method of the touch helper
                }
                itemTouchHelper.startDrag(this)
            }
            return false
        }

        @OnClick(R.id.expandDragButton)
        fun onExpandToggle() {
            val item = items!![adapterPosition]

            itemView.expandDragButton!!.setImageResource(if (item.isExpanded) R.drawable.avd_collapse else R.drawable.avd_expand)
            (itemView.expandDragButton!!.drawable as Animatable).start()

            beginDelayedTransition(recyclerView!!, ChangeBounds().setDuration(200))

            itemView.description!!.visibility = if (itemView.description!!.visibility == GONE) VISIBLE else GONE
            item.isExpanded = itemView.description!!.visibility == VISIBLE
        }

        @OnClick(R.id.cardForeground)
        fun onCardClick() {
            if (selectionModeEnabled) {
                itemView.selectCheckBox!!.performClick() // delegate to chBx listener
            } else if (!itemView.description!!.text.toString().isEmpty()) {
                itemView.expandDragButton!!.post { itemView.expandDragButton!!.performClick() }
            }
        }

        @OnTextChanged(R.id.price)
        fun onPriceChanged() {
            val priceString = itemView.price!!.text.toString().replace("[^\\d]".toRegex(), "")
            if (!priceString.isEmpty()) {
                val price = parseLong(priceString)
                val item = items!![adapterPosition]
                item.totalPrice = price
            }
        }

        @OnCheckedChanged(R.id.selectCheckBox)
        fun onItemSelected(checked: Boolean) {
            beginDelayedTransition(recyclerView!!, ChangeBounds().setDuration(200))
            if (checked) {
                itemView.price_container.visibility = VISIBLE
                selectedItems.add(items!![adapterPosition])
            } else {
                itemView.price_container.visibility = GONE
                selectedItems.remove(items!![adapterPosition])
                val item = items!![adapterPosition]
                item.totalPrice = 0
                itemView.price!!.text!!.clear()
            }
        }
    }
}
