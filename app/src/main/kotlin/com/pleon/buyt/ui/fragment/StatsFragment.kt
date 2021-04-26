package com.pleon.buyt.ui.fragment

import android.os.Bundle
import android.view.View
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
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
import org.jetbrains.anko.defaultSharedPreferences
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import kotlin.math.log10

private const val PIE_MAX_SLICES = 5

class StatsFragment : BaseFragment() {

    @ColorInt private lateinit var pieSliceColors: IntArray
    private val viewModel by sharedViewModel<StatsViewModel>()

    override fun layout() = R.layout.fragment_stats

    override fun onViewCreated(view: View, savedState: Bundle?) {
        viewModel.stats.observe(viewLifecycleOwner, this::showStats)
        pieSliceColors = resources.getIntArray(R.array.pieChartColors)
        lineChart.setTypeface(getFont(requireContext(), R.font.vazir_wol_v_26_0_2_scaled_uniformly_to_94_percent)!!)
    }

    private fun showStats(stats: Stats) = with(stats) {
        showLineChart(dailyCosts)
        showPieChart(mostPurchasedCategories)

        textView18.text = formatNumber(totalPurchaseCount)
        textView3.text = formatPrice(totalPurchaseCost)
        textView.text = formatPrice(averagePurchaseCost)
        textView6.text = formatPrice(maxPurchaseCost)
        textView7.text = formatPrice(minPurchaseCost)
        textView17.text = storeWithMaxPurchaseCount?.name ?: getString(R.string.no_value)
        textView22.visibility = if (storeWithMaxPurchaseCount == null) INVISIBLE else VISIBLE
        textView22.text = resources.getQuantityString(R.plurals.purchase_count,
                storeWithMaxPurchaseCount?.purchaseCount ?: 0,
                formatNumber(storeWithMaxPurchaseCount?.purchaseCount ?: 0))
        textView9.setText(weekdayWithMaxPurchaseCount?.weekdayNameStringRes ?: R.string.no_value)
        textView20.text = mostPurchasedItem?.name ?: getString(R.string.no_value)
        textView21.visibility = if (mostPurchasedItem == null) INVISIBLE else VISIBLE
        textView21.text = resources.getQuantityString(R.plurals.purchase_count,
                mostPurchasedItem?.purchaseCount ?: 0,
                formatNumber(mostPurchasedItem?.purchaseCount ?: 0))
    }

    private fun showLineChart(dailyCosts: List<DailyCost>) {
        val dotRadius = when {
            dailyCosts.size > Period.EXTENDED.length -> 1f
            dailyCosts.size > Period.MEDIUM.length -> 4f
            dailyCosts.size > Period.NARROW.length -> 6f
            else -> 8f
        }
        buildLineChart(requireContext(), lineChart, dailyCosts, dotsRadius = dotRadius)
                .show(Animation(500))
    }

    private fun showPieChart(categorySums: List<CategorySum>) {
        pieChart.clearData()
        val isLogScale = requireContext().defaultSharedPreferences.getBoolean(PREF_LOG_SCALE, false)
        categorySums.take(PIE_MAX_SLICES - 1).forEachIndexed { index, categorySum ->
            val value = if (isLogScale) log10(categorySum.value + 1.0) else categorySum.value.toDouble()
            val sliceName = getString(Category.valueOf(categorySum.name).nameRes)
            pieChart.addSlice(Slice(sliceName, value, pieSliceColors[index]))
        }
        val other = categorySums.drop(PIE_MAX_SLICES - 1).sumOf(CategorySum::value)
        val value = if (isLogScale) log10(other.toDouble()) else other.toDouble()
        if (other > 0) pieChart.addSlice(Slice(getString(R.string.pie_chart_other), value, pieSliceColors[PIE_MAX_SLICES - 1]))
        pieChart.startAnim(480)
    }
}
