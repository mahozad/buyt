package com.pleon.buyt.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.pleon.buyt.R
import com.pleon.buyt.model.PurchaseDetail
import com.pleon.buyt.ui.BaseViewHolder
import com.pleon.buyt.ui.DateHeaderDecoration.StickyHeaderInterface
import com.pleon.buyt.ui.adapter.PurchaseDetailAdapter.ItemTypes.DATE
import com.pleon.buyt.ui.adapter.PurchaseDetailAdapter.ItemTypes.ITEM
import ir.huri.jcal.JalaliCalendar
import kotlinx.android.synthetic.main.date_header.view.*
import kotlinx.android.synthetic.main.purchase_detail.view.*
import org.jetbrains.anko.configuration
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

class PurchaseDetailAdapter(private val cxt: Context) : Adapter<ViewHolder>(), StickyHeaderInterface {

    private val priceFormat = DecimalFormat("#,###")
    private val dateFormat = SimpleDateFormat("MM/dd/yyyy", Locale.US)

    enum class ItemTypes { ITEM, DATE }

    var items = listOf<Any>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    /**
     * Note that unlike in ListView adapters, types don't have to be contiguous
     */
    override fun getItemViewType(pos: Int) = if (items[pos] is Date) DATE.ordinal else ITEM.ordinal

    override fun getItemCount() = items.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            ITEM.ordinal -> PurchaseHolder(inflater.inflate(R.layout.purchase_detail, parent, false))
            else -> DateHolder(inflater.inflate(R.layout.date_header, parent, false))
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        when (holder.itemViewType) {
            ITEM.ordinal -> (holder as PurchaseHolder).bindItem(items[position] as PurchaseDetail)
            else -> (holder as DateHolder).bindItem(items[position] as Date)
        }
    }

    override fun getHeaderPositionForItem(itemPosition: Int): Int {
        var itemPosition = itemPosition
        var headerPosition = 0
        do {
            if (this.isHeader(itemPosition)) {
                headerPosition = itemPosition
                break
            }
            itemPosition -= 1
        } while (itemPosition >= 0)
        return headerPosition
    }

    override fun bindHeaderData(header: View, headerPosition: Int) {
        header.headerText.text = formatDate(items[headerPosition] as Date)
    }

    override fun getHeaderLayout(headerPosition: Int) = R.layout.date_header

    override fun isHeader(itemPosition: Int) = items[itemPosition] is Date

    private fun formatDate(date: Date): String {
        return if (cxt.configuration.locale.displayName.contains("فارسی")) {
            val jalaliCalendar = JalaliCalendar(date)
            String.format(cxt.configuration.locale, "%s %d %s %d",
                    jalaliCalendar.dayOfWeekString, jalaliCalendar.day,
                    jalaliCalendar.monthString, jalaliCalendar.year)
        } else dateFormat.format(date)
    }

    inner class PurchaseHolder(view: View) : BaseViewHolder(view) {
        fun bindItem(purchaseDetail: PurchaseDetail) {
            itemView.storeName.text = purchaseDetail.store[0].name
            itemView.itemDetails.adapter = ItemDetailAdapter(cxt).apply {
                items = purchaseDetail.item
            }
            var totalCost = 0L
            for (item in purchaseDetail.item) totalCost += item.totalPrice
            itemView.totalCost.text = cxt.getString(R.string.purchase_detail_price, priceFormat.format(totalCost))
        }

    }

    inner class DateHolder(view: View) : BaseViewHolder(view) {
        fun bindItem(date: Date) {
            itemView.headerText.text = formatDate(date)
        }
    }
}
