package com.pleon.buyt.ui.fragment

import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.Observer
import com.db.chart.animation.Animation
import com.pleon.buyt.R
import com.pleon.buyt.database.dto.DailyCost
import com.pleon.buyt.database.dto.PieSlice
import com.pleon.buyt.database.dto.Stats
import com.pleon.buyt.model.Category
import com.pleon.buyt.ui.PieChartView.Sector
import com.pleon.buyt.util.FormatterUtil.formatNumber
import com.pleon.buyt.util.FormatterUtil.formatPrice
import com.pleon.buyt.util.LineChartBuilder.buildLineChart
import com.pleon.buyt.viewmodel.StatsViewModel
import kotlinx.android.synthetic.main.fragment_stats.*
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

private const val PIE_CHART_MAX_SLICES = 5

class StatsFragment : BaseFragment() {

    @ColorInt private lateinit var pieSliceColors: IntArray
    @ColorRes private var pieBgColor: Int = 0 // This color varies based on the app theme
    @Volatile private var isStartup = true // Required due to LiveData called twice on startup
    private val viewModel by sharedViewModel<StatsViewModel>()

    override fun layout() = R.layout.fragment_stats

    override fun onViewCreated(view: View, savedState: Bundle?) {
        viewModel.stats.observe(viewLifecycleOwner, Observer { stats ->
            showStats(stats)
            isStartup = false
        })

        pieSliceColors = resources.getIntArray(R.array.pieChartColors)
        val typedValue = TypedValue()
        context!!.theme.resolveAttribute(R.attr.pieChartBackgroundColor, typedValue, true)
        pieBgColor = typedValue.data

        lineChart.setTypeface(ResourcesCompat.getFont(context!!, R.font.vazir_scaled_down)!!)
        pieChart.setSectorGap(4) // gap between slices
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
        val shouldShowDots = dailyCosts.size <= 30
        val lineChart = buildLineChart(context!!, lineChart, dailyCosts, shouldShowDots)
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
                pieChart.addSector(Sector(sliceName, slice.value, pieSliceColors[index]))
            } else other += slice.value
        }

        if (other > 0) pieChart.addSector(Sector(getString(R.string.pie_chart_other), other, pieSliceColors[PIE_CHART_MAX_SLICES - 1]))

        pieChart.startAnim(if (isStartup) 0 else 480)
    }
}
