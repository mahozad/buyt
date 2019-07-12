package com.pleon.buyt.ui.adapter

import android.graphics.Paint
import android.graphics.drawable.Animatable
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.core.content.ContextCompat.getColor
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.transition.ChangeBounds
import androidx.transition.TransitionManager
import com.db.chart.model.LineSet
import com.db.chart.renderer.AxisRenderer
import com.pleon.buyt.R
import com.pleon.buyt.database.dto.StoreDetail
import com.pleon.buyt.ui.BaseViewHolder
import com.pleon.buyt.ui.adapter.StoresAdapter.StoreHolder
import com.pleon.buyt.ui.fragment.StoresFragment
import com.pleon.buyt.util.FormatterUtil.formatPrice
import com.pleon.buyt.viewmodel.StoresViewModel
import com.pleon.buyt.viewmodel.ViewModelFactory
import kotlinx.android.synthetic.main.store_list_row.view.*
import java.text.DecimalFormat
import javax.inject.Inject

class StoresAdapter @Inject constructor(private val frag: StoresFragment) : Adapter<StoreHolder>() {

    @Inject internal lateinit var viewModelFactory: ViewModelFactory<StoresViewModel>
    private lateinit var recyclerView: RecyclerView
    private lateinit var viewModel: StoresViewModel
    private var extendedStoreId = 0L

    var storeDetails = listOf<StoreDetail>()
        set(storeDetails) {
            field = storeDetails
            notifyDataSetChanged()
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
        viewModel = ViewModelProviders.of(frag, viewModelFactory).get(StoresViewModel::class.java)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StoreHolder {
        val itemView = LayoutInflater.from(parent.context)
                .inflate(R.layout.store_list_row, parent, false)
        itemView.lineChart.setTypeface(ResourcesCompat.getFont(frag.context!!, R.font.vazir_scaled_down)!!)
        itemView.lineChart.setLabelsFormat(DecimalFormat(frag.getString(R.string.currency_format)))
        return StoreHolder(itemView)
    }

    override fun setHasStableIds(hasStableIds: Boolean) {
        super.setHasStableIds(true)
    }

    override fun onBindViewHolder(holder: StoreHolder, position: Int) {
        holder.bindStore(storeDetails[position])
    }

    override fun getItemCount() = storeDetails.size

    override fun getItemId(position: Int) = storeDetails[position].store.storeId

    fun getStore(position: Int) = storeDetails[position].store

    inner class StoreHolder(v: View) : BaseViewHolder(v) {

        init {
            itemView.showChartButton.setOnClickListener {
                TransitionManager.beginDelayedTransition(recyclerView, ChangeBounds().setDuration(200))

                val isChartShown = itemView.chart_group.visibility == VISIBLE
                extendedStoreId = if (isChartShown) 0 else getStore(adapterPosition).storeId
                itemView.chart_group.visibility = if (isChartShown) GONE else VISIBLE
                itemView.showChartButton.setImageResource(if (isChartShown) R.drawable.avd_line_chart_off else R.drawable.avd_line_chart_on)
                (itemView.showChartButton.drawable as Animatable).start()

                if (itemView.chart_group.visibility == VISIBLE) {
                    // REQUIRED; MUST be called before the observeForever
                    viewModel.getStoreStats(getStore(adapterPosition)).removeObservers(frag)

                    viewModel.getStoreStats(getStore(adapterPosition)).observeForever { dailyCosts ->
                        if (frag.context == null) return@observeForever // To prevent bug on relaunch

                        itemView.lineChart.reset()

                        val dataSet = LineSet()
                        for (dc in dailyCosts) dataSet.addPoint(dc.date, dc.totalCost.toFloat())
                        dataSet.setDotsColor(getColor(frag.context!!, R.color.colorPrimary))
                        dataSet.setDotsRadius(2.6f)
                        dataSet.color = getColor(frag.context!!, R.color.colorPrimaryDark)
                        dataSet.thickness = 2.5f
                        val colors = frag.resources.getIntArray(R.array.lineChartGradient)
                        dataSet.setGradientFill(colors, floatArrayOf(0.0f, 0.2f, 0.5f, 1.0f))
                        itemView.lineChart.addData(dataSet)

                        val paint = Paint().apply { color = getColor(frag.context!!, R.color.chartGridColor) }
                        itemView.lineChart.setGrid(3, 0, paint)
                        itemView.lineChart.setXLabels(AxisRenderer.LabelPosition.NONE)
                        itemView.lineChart.show() // Do NOT use animation; causes bug
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
            itemView.showChartButton.visibility = if (frag.shouldShowChartButton()) VISIBLE else GONE
            if (storeDetail.store.storeId != extendedStoreId && itemView.chart_group.visibility == VISIBLE) {
                itemView.showChartButton.setImageResource(R.drawable.avd_line_chart_off)
                (itemView.showChartButton.drawable as Animatable).start()
            } else if (storeDetail.store.storeId == extendedStoreId) {
                itemView.showChartButton.setImageResource(R.drawable.avd_line_chart_on)
                (itemView.showChartButton.drawable as Animatable).start()
            }
            itemView.chart_group.visibility = if (storeDetail.store.storeId == extendedStoreId) VISIBLE else GONE
        }
    }
}
