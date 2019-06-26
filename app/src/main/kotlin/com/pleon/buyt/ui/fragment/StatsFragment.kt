package com.pleon.buyt.ui.fragment

import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat.getColor
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders.of
import com.db.chart.animation.Animation
import com.db.chart.model.LineSet
import com.db.chart.renderer.AxisRenderer.LabelPosition.NONE
import com.pleon.buyt.R
import com.pleon.buyt.database.dto.DailyCost
import com.pleon.buyt.database.dto.PieSlice
import com.pleon.buyt.database.dto.Stats
import com.pleon.buyt.model.Category
import com.pleon.buyt.ui.PieChartView.Slice
import com.pleon.buyt.util.FormatterUtil.formatNumber
import com.pleon.buyt.util.FormatterUtil.formatPrice
import com.pleon.buyt.viewmodel.StatsViewModel
import com.pleon.buyt.viewmodel.ViewModelFactory
import kotlinx.android.synthetic.main.fragment_stats.*
import java.text.DecimalFormat
import javax.inject.Inject

private const val PIE_CHART_MAX_SLICES = 5

class StatsFragment : BaseFragment() {

    @Inject internal lateinit var viewModelFactory: ViewModelFactory<StatsViewModel>
    @ColorInt private lateinit var pieSliceColors: IntArray
    @ColorRes private var pieBgColor: Int = 0 // This color varies based on the app theme
    @Volatile private var isStartup = true // Required due to LiveData called twice on startup
    private lateinit var viewModel: StatsViewModel

    override fun layout() = R.layout.fragment_stats

    override fun onViewCreated(view: View, savedState: Bundle?) {
        viewModel = of(activity!!, viewModelFactory).get(StatsViewModel::class.java)
        viewModel.stats.observe(viewLifecycleOwner, Observer { stats ->
            showStats(stats)
            isStartup = false
        })

        pieSliceColors = resources.getIntArray(R.array.pieChartColors)
        val typedValue = TypedValue()
        context!!.theme.resolveAttribute(R.attr.pieChartBackgroundColor, typedValue, true)
        pieBgColor = typedValue.data

        lineChart.setTypeface(ResourcesCompat.getFont(context!!, R.font.vazir_scaled_down)!!)
        pieChart.setCell(4) // gap between slices
        pieChart.setInnerRadius(0.6f)
        pieChart.setBackGroundColor(pieBgColor)
        pieChart.setItemTextSize(13.5f)
    }

    private fun showStats(stats: Stats) {
        showLineChart(stats.dailyCosts)
        showPieChart(stats.mostPurchasedCategories)

        textView18.text = formatNumber(stats.numberOfPurchases)
        textView3.text = formatPrice(stats.totalPurchaseCost)
        textView.text = formatPrice(stats.averagePurchaseCost)
        textView6.text = formatPrice(stats.maxPurchaseCost)
        textView7.text = formatPrice(stats.minPurchaseCost)
        textView17.text = stats.storeNameWithMaxPurchaseCount

        if (stats.weekdayNameResWithMaxPurchases != 0)
            textView9.setText(stats.weekdayNameResWithMaxPurchases)
        else textView9.text = "-"
    }

    private fun showLineChart(dailyCosts: List<DailyCost>) {
        lineChart.reset()

        var totalExpenses = 0L

        val dataSet = LineSet()
        for (dailyCost in dailyCosts) {
            dataSet.addPoint(dailyCost.date, dailyCost.totalCost.toFloat())
            totalExpenses += dailyCost.totalCost
        }

        if (dailyCosts.size <= 30) {
            dataSet.setDotsColor(getColor(context!!,
                    if (totalExpenses == 0L) R.color.chartEmptyColor else R.color.colorPrimary))
            dataSet.setDotsRadius(3f)
        }
        dataSet.isSmooth = false
        dataSet.color = getColor(context!!,
                if (totalExpenses == 0L) R.color.chartEmptyColor else R.color.colorPrimaryDark)
        dataSet.thickness = 2.5f

        lineChart.setLabelsFormat(DecimalFormat(getString(R.string.currency_format)))

        val colors = resources.getIntArray(R.array.lineChartGradient)
        val steps = floatArrayOf(0.0f, 0.2f, 0.5f, 1.0f)
        dataSet.setGradientFill(colors, steps)
        lineChart.addData(dataSet)
        lineChart.setXLabels(NONE)
        if (isStartup) lineChart.show() else lineChart.show(Animation(500))
    }

    private fun showPieChart(pieSlices: List<PieSlice>) {
        pieChart.clearData()

        // If filter is set then the whole chart will be one category so better to show empty hint
        pieChart.visibility = if (pieSlices.isEmpty()) GONE else VISIBLE
        emptyHint.visibility = if (pieSlices.isEmpty()) VISIBLE else GONE

        var other = 0
        for ((index, slice) in pieSlices.withIndex()) {
            if (index < PIE_CHART_MAX_SLICES - 1) {
                val sliceName = getString(Category.valueOf(slice.name).nameRes)
                pieChart.addSector(Slice(sliceName, slice.value, pieSliceColors[index]))
            } else other += slice.value
        }
        if (other > 0) {
            pieChart.addSector(Slice(getString(R.string.pie_chart_other), other, pieSliceColors[PIE_CHART_MAX_SLICES - 1]))
        }

        pieChart.startAnim(if (isStartup) 0 else 480)
    }
}
