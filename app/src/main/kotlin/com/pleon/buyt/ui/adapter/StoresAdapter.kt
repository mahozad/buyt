package com.pleon.buyt.ui.adapter

import android.graphics.Paint
import android.graphics.drawable.Animatable
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.core.content.ContextCompat
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

class StoresAdapter @Inject constructor(private val cxt: StoresFragment) : Adapter<StoreHolder>() {

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
        viewModel = ViewModelProviders.of(cxt, viewModelFactory).get(StoresViewModel::class.java)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StoreHolder {
        val itemView = LayoutInflater.from(parent.context)
                .inflate(R.layout.store_list_row, parent, false)
        itemView.lineChart.setTypeface(ResourcesCompat.getFont(cxt.context!!, R.font.vazir_scaled_down)!!)
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
                if (itemView.chart_group.visibility == GONE) {
                    extendedStoreId = getStore(adapterPosition).storeId
                    itemView.chart_group.visibility = VISIBLE
                    itemView.showChartButton.setImageResource(R.drawable.avd_line_chart_on)
                } else {
                    extendedStoreId = 0
                    itemView.chart_group.visibility = GONE
                    itemView.showChartButton.setImageResource(R.drawable.avd_line_chart_off)
                }
                (itemView.showChartButton.drawable as Animatable).start()

                if (itemView.chart_group.visibility == VISIBLE) {
                    viewModel.getStoreStats(getStore(adapterPosition)).removeObservers(cxt) // This MUST be before the observeForever
                    viewModel.getStoreStats(getStore(adapterPosition)).observeForever { t ->

                        if (cxt.context == null) return@observeForever // To prevent bug on relaunch

                        itemView.lineChart.reset()

                        val dataSet = LineSet()
                        for (dailyCost in t) dataSet.addPoint(dailyCost.date, dailyCost.totalCost.toFloat())

                        dataSet.setDotsColor(ContextCompat.getColor(cxt.context!!, R.color.colorPrimary))
                        dataSet.setDotsRadius(2.5f)
                        dataSet.color = ContextCompat.getColor(cxt.context!!, R.color.colorPrimaryDark)
                        dataSet.thickness = 2.5f

                        itemView.lineChart.setLabelsFormat(DecimalFormat(cxt.getString(R.string.currency_format)))

                        val colors = cxt.resources.getIntArray(R.array.lineChartGradient)
                        val steps = floatArrayOf(0.0f, 0.2f, 0.5f, 1.0f)
                        dataSet.setGradientFill(colors, steps)
                        itemView.lineChart.addData(dataSet)
                        val paint = Paint()
                        paint.color = ContextCompat.getColor(cxt.context!!, R.color.chartGridColor)
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
            itemView.purchaseCount.text = cxt.resources.getQuantityString(R.plurals.store_detail_purchase_count, storeDetail.purchaseCount, storeDetail.purchaseCount)
            itemView.totalSpending.text = cxt.resources.getQuantityString(R.plurals.price_with_suffix, storeDetail.totalSpending, formatPrice(storeDetail.totalSpending))
            itemView.circular_reveal.alpha = 0f // for the case of undo of deleted item
            itemView.showChartButton.visibility = if (frag.shouldShowChartButton()) VISIBLE else GONE
            if (storeDetail.store.storeId != extendedStoreId && itemView.chart_group.visibility == VISIBLE) {
                itemView.showChartButton.setImageResource(R.drawable.avd_line_chart_off)
                (itemView.showChartButton.drawable as Animatable).start()
            }
            itemView.chart_group.visibility = if (storeDetail.store.storeId == extendedStoreId) VISIBLE else GONE
        }
    }
}
