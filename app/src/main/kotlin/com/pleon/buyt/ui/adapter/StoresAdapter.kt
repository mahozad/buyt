package com.pleon.buyt.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat.getFont
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.pleon.buyt.R
import com.pleon.buyt.database.dto.StoreDetail
import com.pleon.buyt.ui.BaseViewHolder
import com.pleon.buyt.ui.adapter.StoresAdapter.StoreHolder
import com.pleon.buyt.util.animateIcon
import com.pleon.buyt.util.buildLineChart
import com.pleon.buyt.util.formatPrice
import kotlinx.android.synthetic.main.store_list_row.view.*

class StoresAdapter(private val frag: Fragment) : ListAdapter<StoreDetail, StoreHolder>(StoreDiffCallback) {

    object StoreDiffCallback : DiffUtil.ItemCallback<StoreDetail>() {
        override fun areItemsTheSame(oldItem: StoreDetail, newItem: StoreDetail): Boolean {
            return oldItem.brief.store.storeId == newItem.brief.store.storeId
        }

        override fun areContentsTheSame(oldItem: StoreDetail, newItem: StoreDetail): Boolean {
            return oldItem.brief.store.name == newItem.brief.store.name &&
                    oldItem.brief.purchaseCount == newItem.brief.purchaseCount &&
                    oldItem.brief.totalSpending == newItem.brief.totalSpending
        }
    }

    private lateinit var recyclerView: RecyclerView

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        this.recyclerView = recyclerView
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StoreHolder {
        val itemView = LayoutInflater.from(parent.context)
                .inflate(R.layout.store_list_row, parent, false)
        itemView.lineChart.setTypeface(getFont(frag.requireContext(), R.font.vazir_wol_v_26_0_2_scaled_uniformly_to_94_percent)!!)
        return StoreHolder(itemView)
    }

    override fun setHasStableIds(hasStableIds: Boolean) = super.setHasStableIds(true)

    override fun getItemId(position: Int) = currentList[position].brief.store.storeId

    override fun onBindViewHolder(holder: StoreHolder, position: Int) {
        holder.bindStore(currentList[position])
    }

    fun getStore(position: Int) = super.getItem(position).brief.store

    private var expandedPosition = -1
    private var previousExpandedPosition = -1

    inner class StoreHolder(v: View) : BaseViewHolder(v) {

        fun bindStore(storeDetail: StoreDetail) {
            itemView.storeIcon.setImageResource(storeDetail.brief.store.category.storeImageRes)
            itemView.storeName.text = storeDetail.brief.store.name
            itemView.purchaseCount.text = frag.resources.getQuantityString(R.plurals.store_detail_purchase_count, storeDetail.brief.purchaseCount, storeDetail.brief.purchaseCount)
            itemView.totalSpending.text = frag.resources.getQuantityString(R.plurals.price_with_suffix, storeDetail.brief.totalSpending, formatPrice(storeDetail.brief.totalSpending))
            itemView.circular_reveal.alpha = 0f // for the case of undo of deleted item

            // FIXME: WHY?
            itemView.showChartButton.visibility = if (frag.resources.getInteger(R.integer.layout_columns) == 1) VISIBLE else GONE

            val isExpanded = (layoutPosition == expandedPosition)
            itemView.lineChart.visibility = if (isExpanded) VISIBLE else GONE
            if (isExpanded) {
                itemView.showChartButton.setImageResource(R.drawable.avd_line_chart_on)
                animateIcon(itemView.showChartButton.drawable)
                buildLineChart(frag.requireContext(), itemView.lineChart, storeDetail.dailyCosts, dotsRadius = 4f).show()
            } else if (layoutPosition == previousExpandedPosition) {
                itemView.showChartButton.setImageResource(R.drawable.avd_line_chart_off)
                animateIcon(itemView.showChartButton.drawable)
            } else {
                itemView.showChartButton.setImageResource(R.drawable.ic_line_chart)
            }

            if (isExpanded) previousExpandedPosition = layoutPosition
            itemView.setOnClickListener {
                expandedPosition = if (isExpanded) -1 else layoutPosition
                notifyItemChanged(previousExpandedPosition) // To collapse it
                notifyItemChanged(layoutPosition)
            }
        }
    }
}
