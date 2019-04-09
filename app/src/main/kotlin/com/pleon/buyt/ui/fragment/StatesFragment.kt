package com.pleon.buyt.ui.fragment

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_TIME_TICK
import android.content.IntentFilter
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.db.chart.animation.Animation
import com.db.chart.model.LineSet
import com.db.chart.renderer.AxisRenderer.LabelPosition.NONE
import com.pleon.buyt.R
import com.pleon.buyt.database.DailyCost
import com.pleon.buyt.database.PieSlice
import com.pleon.buyt.model.Category
import com.pleon.buyt.ui.PieChartView.Slice
import com.pleon.buyt.viewmodel.StatisticsViewModel
import kotlinx.android.synthetic.main.fragment_states.*
import java.text.DecimalFormat
import java.util.*

private const val PIE_CHART_MAX_SLICES = 5

class StatesFragment : Fragment() {

    val period get() = viewModel.period
    @ColorRes private var pieBgColor: Int = 0 // This color varies based on the app theme
    private lateinit var viewModel: StatisticsViewModel
    private val priceFormat = DecimalFormat("#,###")
    private val pieSliceColors = intArrayOf(0xffC1B435.toInt(), 0xff2DA579.toInt(),
            0xff2D71A5.toInt(), 0xffB53145.toInt(), 0xff909090.toInt())
//    private val pieSliceColors = intArrayOf(0xffC19835.toInt(), 0xffABBA33.toInt(),
//            0xff2DA579.toInt(), 0xff2D38A5.toInt(), 0xffA62D98.toInt())

    var filter: Category?
        get() = viewModel.filter
        set(filter) {
            viewModel.filter = filter
            showStatistics()
        }

    // Update the statistics when date changes (e.g. time changes from 23:59 to 00:00)
    private var today = Date()
    private val timeReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (Date().date != today.date) {
                showStatistics()
                today = Date()
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_states, container, false)
    }

    override fun onViewCreated(view: View, savedState: Bundle?) {
        viewModel = ViewModelProviders.of(this).get(StatisticsViewModel::class.java)
        activity?.registerReceiver(timeReceiver, IntentFilter(ACTION_TIME_TICK))

        val typedValue = TypedValue()
        context!!.theme.resolveAttribute(R.attr.pieChartBackgroundColor, typedValue, true)
        pieBgColor = typedValue.data
        pieChart.setCell(4) // gap between slices
        pieChart.setInnerRadius(0.6f)
        pieChart.setBackGroundColor(pieBgColor)
        pieChart.setItemTextSize(21) // FIXME: text size isn't consistent across different devices
        pieChart.setAnimDuration(500)

        showStatistics()
    }

    private fun showStatistics() {
        viewModel.statistics.observe(viewLifecycleOwner, Observer { statistics ->
            showLineChart(statistics.dailyCosts!!)
            showPieChart(statistics.mostPurchasedCategories)

            textView3.text = priceFormat.format(statistics.totalPurchaseCost)
            textView.text = priceFormat.format(statistics.averagePurchaseCost)
            textView18.text = priceFormat.format(statistics.numberOfPurchases)
            textView6.text = priceFormat.format(statistics.maxPurchaseCost)
            textView7.text = priceFormat.format(statistics.minPurchaseCost)

            if (statistics.weekdayNameResWithMaxPurchases != 0) textView9.setText(statistics.weekdayNameResWithMaxPurchases)
            else textView9.text = "-"

            textView17.text = statistics.storeNameWithMaxPurchaseCount
        })
    }

    private fun showLineChart(dailyCosts: List<DailyCost>) {
        chart.reset()

        val dataSet = LineSet()
        for (dailyCost in dailyCosts) {
            dataSet.addPoint(dailyCost.date, dailyCost.totalCost.toFloat())
        }

        if (viewModel.period.length <= 20) {
            dataSet.setDotsColor(ContextCompat.getColor(context!!, R.color.colorPrimary))
            dataSet.setDotsRadius(3f)
        }
        dataSet.isSmooth = false // TODO: Add an option in settings for the user to toggle this
        dataSet.color = ContextCompat.getColor(context!!, R.color.colorPrimaryDark)
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

        var other = 0
        for ((index, slice) in pieSlices.withIndex()) {
            if (index < PIE_CHART_MAX_SLICES - 1) {
                val sliceName = getString(Category.valueOf(slice.name).nameRes)
                pieChart.addSector(Slice(sliceName, slice.value, pieSliceColors[index]))
            } else {
                other += slice.value
            }
        }
        if (other > 0) {
            pieChart.addSector(Slice(getString(R.string.pie_chart_other), other, pieSliceColors[PIE_CHART_MAX_SLICES - 1]))
        }

        pieChart.startAnim()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        activity?.unregisterReceiver(timeReceiver)
    }

    fun togglePeriod() = viewModel.togglePeriod().also { showStatistics() }
}
