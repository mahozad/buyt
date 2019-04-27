package com.pleon.buyt.ui.fragment

import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.db.chart.animation.Animation
import com.db.chart.model.LineSet
import com.db.chart.renderer.AxisRenderer.LabelPosition.NONE
import com.pleon.buyt.R
import com.pleon.buyt.database.dto.DailyCost
import com.pleon.buyt.database.dto.PieSlice
import com.pleon.buyt.database.dto.Statistics
import com.pleon.buyt.model.Category
import com.pleon.buyt.ui.PieChartView.Slice
import com.pleon.buyt.viewmodel.StatisticsViewModel
import kotlinx.android.synthetic.main.fragment_stats.*
import java.text.DecimalFormat

private const val PIE_CHART_MAX_SLICES = 5

class StatsFragment : Fragment(R.layout.fragment_stats) {

    @ColorRes private var pieBgColor: Int = 0 // This color varies based on the app theme
    private lateinit var viewModel: StatisticsViewModel
    private val priceFormat = DecimalFormat("#,###")
    private val pieSliceColors = intArrayOf(0xffC1B435.toInt(), 0xff2DA579.toInt(),
            0xff2D71A5.toInt(), 0xffB53145.toInt(), 0xff909090.toInt())
//    private val pieSliceColors = intArrayOf(0xffC19835.toInt(), 0xffABBA33.toInt(),
//            0xff2DA579.toInt(), 0xff2D38A5.toInt(), 0xffA62D98.toInt())

    override fun onViewCreated(view: View, savedState: Bundle?) {
        viewModel = ViewModelProviders.of(activity!!).get(StatisticsViewModel::class.java)

        val typedValue = TypedValue()
        context!!.theme.resolveAttribute(R.attr.pieChartBackgroundColor, typedValue, true)
        pieBgColor = typedValue.data
        pieChart.setCell(4) // gap between slices
        pieChart.setInnerRadius(0.6f)
        pieChart.setBackGroundColor(pieBgColor)
        pieChart.setItemTextSize(13.5f)
        pieChart.setAnimDuration(480)
    }

    fun showStats(stats: Statistics) {
        showLineChart(stats.dailyCosts!!)
        showPieChart(stats.mostPurchasedCategories)

        textView3.text = priceFormat.format(stats.totalPurchaseCost)
        textView.text = priceFormat.format(stats.averagePurchaseCost)
        textView18.text = priceFormat.format(stats.numberOfPurchases)
        textView6.text = priceFormat.format(stats.maxPurchaseCost)
        textView7.text = priceFormat.format(stats.minPurchaseCost)
        textView17.text = stats.storeNameWithMaxPurchaseCount

        if (stats.weekdayNameResWithMaxPurchases != 0)
            textView9.setText(stats.weekdayNameResWithMaxPurchases)
        else textView9.text = "-"
    }

    private fun showLineChart(dailyCosts: List<DailyCost>) {
        chart.reset()

        var totalCosts = 0L

        val dataSet = LineSet()
        for (dailyCost in dailyCosts) {
            dataSet.addPoint(dailyCost.date, dailyCost.totalCost.toFloat())
            totalCosts += dailyCost.totalCost
        }

        if (dailyCosts.size <= 20) {
            dataSet.setDotsColor(ContextCompat.getColor(context!!,
                    if (totalCosts == 0L) R.color.chartEmptyColor else R.color.colorPrimary))
            dataSet.setDotsRadius(3f)
        }
        dataSet.isSmooth = false // TODO: Add an option in settings for the user to toggle this
        dataSet.color = ContextCompat.getColor(context!!,
                if (totalCosts == 0L) R.color.chartEmptyColor else R.color.colorPrimaryDark)
        dataSet.thickness = 2.5f

        val moneyFormat = DecimalFormat(getString(R.string.currency_format))
        chart.setLabelsFormat(moneyFormat)

        val colors = resources.getIntArray(R.array.lineChartGradient)
        val steps = floatArrayOf(0.0f, 0.5f, 1.0f)
        dataSet.setGradientFill(colors, steps)
        chart.addData(dataSet)
        chart.setXLabels(NONE)
        chart.show(Animation(500))
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

        pieChart.startAnim()
    }
}
