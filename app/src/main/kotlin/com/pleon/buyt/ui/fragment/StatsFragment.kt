package com.pleon.buyt.ui.fragment

import android.os.Bundle
import android.view.View
import androidx.annotation.ColorInt
import androidx.core.content.res.ResourcesCompat.getFont
import com.db.chart.animation.Animation
import com.pleon.buyt.R
import com.pleon.buyt.database.dto.CategorySum
import com.pleon.buyt.database.dto.DailyCost
import com.pleon.buyt.database.dto.Stats
import com.pleon.buyt.model.Category
import com.pleon.buyt.ui.PieChartView.Slice
import com.pleon.buyt.util.buildLineChart
import com.pleon.buyt.util.formatNumber
import com.pleon.buyt.util.formatPrice
import com.pleon.buyt.viewmodel.StatsViewModel
import com.pleon.buyt.viewmodel.StatsViewModel.Period
import kotlinx.android.synthetic.main.fragment_stats.*
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

private const val PIE_MAX_SLICES = 5

class StatsFragment : BaseFragment() {

    @ColorInt private lateinit var pieSliceColors: IntArray
    @Volatile private var isStartup = true // Required due to LiveData called twice on startup
    private val viewModel by sharedViewModel<StatsViewModel>()

    override fun layout() = R.layout.fragment_stats

    override fun onViewCreated(view: View, savedState: Bundle?) {
        viewModel.stats.observe(viewLifecycleOwner) { stats ->
            showStats(stats)
            isStartup = false
        }

        pieSliceColors = resources.getIntArray(R.array.pieChartColors)
        lineChart.setTypeface(getFont(requireContext(), R.font.vazir_scaled_down)!!)
    }

    private fun showStats(stats: Stats) = with(stats) {
        showLineChart(dailyCosts)
        showPieChart(mostPurchasedCategories)

        textView18.text = formatNumber(numberOfPurchases)
        textView3.text = formatPrice(totalPurchaseCost)
        textView.text = formatPrice(averagePurchaseCost)
        textView6.text = formatPrice(maxPurchaseCost)
        textView7.text = formatPrice(minPurchaseCost)
        textView17.text = storeWithMaxPurchaseCount?.name ?: getString(R.string.no_value)
        textView9.setText(weekdayNameResWithMaxPurchases ?: R.string.no_value)
    }

    private fun showLineChart(dailyCosts: List<DailyCost>) {
        val dotRadius = when {
            dailyCosts.size > Period.EXTENDED.length -> 1f
            dailyCosts.size > Period.MEDIUM.length -> 4f
            dailyCosts.size > Period.NARROW.length -> 6f
            else -> 8f
        }
        val lineChart = buildLineChart(requireContext(), lineChart, dailyCosts, dotsRadius = dotRadius)
        if (isStartup) lineChart.show() else lineChart.show(Animation(500))
    }

    private fun showPieChart(categorySums: List<CategorySum>) {
        pieChart.clearData()

        categorySums.take(PIE_MAX_SLICES - 1).forEachIndexed { index, categorySum ->
            val sliceName = getString(Category.valueOf(categorySum.name).nameRes)
            pieChart.addSlice(Slice(sliceName, categorySum.value, pieSliceColors[index]))
        }
        val other = categorySums.drop(PIE_MAX_SLICES - 1).sumBy(CategorySum::value)
        if (other > 0) pieChart.addSlice(Slice(getString(R.string.pie_chart_other), other, pieSliceColors[PIE_MAX_SLICES - 1]))

        pieChart.startAnim(if (isStartup) 0 else 480)
    }
}
