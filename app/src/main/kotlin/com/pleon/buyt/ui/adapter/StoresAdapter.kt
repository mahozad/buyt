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
import com.db.chart.renderer.AxisRenderer.LabelPosition.NONE
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

                itemView.lineChart.visibility = if (itemView.lineChart.visibility == VISIBLE) GONE else VISIBLE
                val isChartShown = (itemView.lineChart.visibility == VISIBLE)
                extendedStoreId = if (isChartShown) getStore(adapterPosition).storeId else 0
                itemView.showChartButton.setImageResource(if (isChartShown) R.drawable.avd_line_chart_on else R.drawable.avd_line_chart_off)
                (itemView.showChartButton.drawable as Animatable).start()

                if (isChartShown) {
                    // REQUIRED; MUST be called before the observeForever
                    viewModel.getStoreStats(getStore(adapterPosition)).removeObservers(frag)

                    viewModel.getStoreStats(getStore(adapterPosition)).observeForever { dailyCosts ->
                        if (frag.context == null) return@observeForever // To prevent bug on relaunch

                        itemView.lineChart.reset()

                        val dataSet = LineSet()
                        for (dc in dailyCosts) dataSet.addPoint(dc.date, dc.totalCost.toFloat())

                        val gradientColors = frag.resources.getIntArray(R.array.lineChartGradient)
                        val gridPaint = Paint().apply { color = getColor(frag.context!!, R.color.chartGridColor) }

                        dataSet.setColor(getColor(frag.context!!, R.color.colorPrimaryDark))
                                .setDotsColor(getColor(frag.context!!, R.color.colorPrimary))
                                .setThickness(3f)
                                .setDotsRadius(2.5f)
                                .setGradientFill(gradientColors, floatArrayOf(0.0f, 0.2f, 0.5f, 1.0f))

                        itemView.lineChart.setLabelsFormat(DecimalFormat(frag.getString(R.string.currency_format)))
                                .setGrid(3, 0, gridPaint)
                                .setXLabels(NONE)
                                .addData(dataSet)

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
            if (storeDetail.store.storeId != extendedStoreId && itemView.lineChart.visibility == VISIBLE) {
                itemView.showChartButton.setImageResource(R.drawable.avd_line_chart_off)
                (itemView.showChartButton.drawable as Animatable).start()
            } else if (storeDetail.store.storeId == extendedStoreId) {
                itemView.showChartButton.setImageResource(R.drawable.avd_line_chart_on)
                (itemView.showChartButton.drawable as Animatable).start()
            }
            itemView.lineChart.visibility = if (storeDetail.store.storeId == extendedStoreId) VISIBLE else GONE
        }
    }
}
