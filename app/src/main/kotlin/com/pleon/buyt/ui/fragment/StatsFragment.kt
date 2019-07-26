package com.pleon.buyt.ui.fragment

import android.os.Bundle
import android.util.TypedValue
import android.view.View
import androidx.annotation.ColorInt
import androidx.core.content.res.ResourcesCompat.getFont
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

private const val PIE_TOTAL_SLICES = 5

class StatsFragment : BaseFragment() {

    @ColorInt private lateinit var pieSliceColors: IntArray
    @Volatile private var isStartup = true // Required due to LiveData called twice on startup
    private val viewModel by sharedViewModel<StatsViewModel>()

    override fun layout() = R.layout.fragment_stats

    override fun onViewCreated(view: View, savedState: Bundle?) {
        viewModel.stats.observe(viewLifecycleOwner, Observer { stats ->
            showStats(stats)
            isStartup = false
        })

        pieSliceColors = resources.getIntArray(R.array.pieChartColors)
        lineChart.setTypeface(getFont(context!!, R.font.vazir_scaled_down)!!)
        setPieChartBackgroundColor()
    }

    private fun setPieChartBackgroundColor() {
        val typedValue = TypedValue()
        context!!.theme.resolveAttribute(R.attr.pieChartBackgroundColor, typedValue, true)
        val pieBgColor = typedValue.data
        pieChart.setBackGroundColor(pieBgColor)
    }

    private fun showStats(stats: Stats) = with(stats) {
        showLineChart(dailyCosts)
        showPieChart(mostPurchasedCategories)

        textView18.text = formatNumber(numberOfPurchases)
        textView3.text = formatPrice(totalPurchaseCost)
        textView.text = formatPrice(averagePurchaseCost)
        textView6.text = formatPrice(maxPurchaseCost)
        textView7.text = formatPrice(minPurchaseCost)
        textView17.text = storeNameWithMaxPurchaseCount
        if (weekdayNameResWithMaxPurchases != 0) textView9.setText(weekdayNameResWithMaxPurchases)
        else textView9.text = "-"
    }

    private fun showLineChart(dailyCosts: List<DailyCost>) {
        val shouldShowDots = dailyCosts.size <= 30
        val lineChart = buildLineChart(context!!, lineChart, dailyCosts, shouldShowDots)
        if (isStartup) lineChart.show() else lineChart.show(Animation(500))
    }

    private fun showPieChart(pieSlices: List<PieSlice>) {
        pieChart.clearData()

        pieSlices.take(PIE_TOTAL_SLICES - 1).forEachIndexed { index, slice ->
            val sliceName = getString(Category.valueOf(slice.name).nameRes)
            pieChart.addSector(Sector(sliceName, slice.value, pieSliceColors[index]))
        }
        val other = pieSlices.drop(PIE_TOTAL_SLICES - 1).sumBy { it.value }
        if (other > 0) pieChart.addSector(Sector(getString(R.string.pie_chart_other), other, pieSliceColors[PIE_TOTAL_SLICES - 1]))

        pieChart.startAnim(if (isStartup) 0 else 480)
    }
}
