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
import androidx.transition.ChangeBounds
import androidx.transition.TransitionManager
import com.pleon.buyt.R
import com.pleon.buyt.database.dto.StoreDetail
import com.pleon.buyt.ui.BaseViewHolder
import com.pleon.buyt.ui.adapter.StoresAdapter.StoreHolder
import com.pleon.buyt.util.AnimationUtil.animateIcon
import com.pleon.buyt.util.FormatterUtil.formatPrice
import com.pleon.buyt.util.LineChartBuilder.buildLineChart
import com.pleon.buyt.viewmodel.StoresViewModel
import kotlinx.android.synthetic.main.store_list_row.view.*

class StoresAdapter(private val frag: Fragment, private val viewModel: StoresViewModel)
    : ListAdapter<StoreDetail, StoreHolder>(StoreDiffCallback) {

    object StoreDiffCallback : DiffUtil.ItemCallback<StoreDetail>() {
        override fun areItemsTheSame(oldItem: StoreDetail, newItem: StoreDetail): Boolean {
            return oldItem.store.storeId == newItem.store.storeId
        }

        override fun areContentsTheSame(oldItem: StoreDetail, newItem: StoreDetail): Boolean {
            return oldItem.store.name == newItem.store.name
        }
    }

    private lateinit var recyclerView: RecyclerView
    private var extendedStoreId = 0L

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        this.recyclerView = recyclerView
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StoreHolder {
        val itemView = LayoutInflater.from(parent.context)
                .inflate(R.layout.store_list_row, parent, false)
        itemView.lineChart.setTypeface(getFont(frag.context!!, R.font.vazir_scaled_down)!!)
        return StoreHolder(itemView)
    }

    override fun setHasStableIds(hasStableIds: Boolean) = super.setHasStableIds(true)

    override fun onBindViewHolder(holder: StoreHolder, position: Int) {
        holder.bindStore(currentList[position])
    }

    override fun getItemId(position: Int) = currentList[position].store.storeId

    fun getStore(position: Int) = super.getItem(position).store

    inner class StoreHolder(v: View) : BaseViewHolder(v) {

        init {
            itemView.cardForeground.setOnClickListener { itemView.showChartButton.performClick() }
            itemView.showChartButton.setOnClickListener {
                TransitionManager.beginDelayedTransition(recyclerView, ChangeBounds().setDuration(200))

                itemView.lineChart.visibility = if (itemView.lineChart.visibility == VISIBLE) GONE else VISIBLE
                val isChartShown = (itemView.lineChart.visibility == VISIBLE)
                extendedStoreId = if (isChartShown) getStore(adapterPosition).storeId else 0
                itemView.showChartButton.setImageResource(if (isChartShown) R.drawable.avd_line_chart_on else R.drawable.avd_line_chart_off)
                animateIcon(itemView.showChartButton.drawable)

                if (isChartShown) {
                    // removeObservers() is REQUIRED; MUST be called before the observeForever
                    viewModel.getStoreStats(getStore(adapterPosition)).removeObservers(frag)
                    viewModel.getStoreStats(getStore(adapterPosition)).observeForever { dailyCosts ->
                        if (frag.context == null) return@observeForever // To prevent bug on relaunch
                        // Do NOT use animation in the show(); Causes bug
                        buildLineChart(frag.context!!, itemView.lineChart, dailyCosts).show()
                    }
                    notifyDataSetChanged() // To collapse other extended cards
                }
            }
        }

        fun bindStore(storeDetail: StoreDetail) {
            itemView.storeIcon.setImageResource(storeDetail.store.category.storeImageRes)
            itemView.storeName.text = storeDetail.store.name
            itemView.purchaseCount.text = frag.resources.getQuantityString(R.plurals.store_detail_purchase_count, storeDetail.purchaseCount, storeDetail.purchaseCount)
            itemView.totalSpending.text = frag.resources.getQuantityString(R.plurals.price_with_suffix, storeDetail.totalSpending, formatPrice(storeDetail.totalSpending))
            itemView.circular_reveal.alpha = 0f // for the case of undo of deleted item
            itemView.showChartButton.visibility = if (frag.resources.getInteger(R.integer.layout_columns) == 1) VISIBLE else GONE
            if (storeDetail.store.storeId != extendedStoreId && itemView.lineChart.visibility == VISIBLE) {
                itemView.showChartButton.setImageResource(R.drawable.avd_line_chart_off)
                animateIcon(itemView.showChartButton.drawable)
            } else if (storeDetail.store.storeId == extendedStoreId) {
                itemView.showChartButton.setImageResource(R.drawable.avd_line_chart_on)
                animateIcon(itemView.showChartButton.drawable)
            }
            itemView.lineChart.visibility = if (storeDetail.store.storeId == extendedStoreId) VISIBLE else GONE
        }
    }
}
