package com.pleon.buyt.ui.fragment

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_TIME_TICK
import android.content.IntentFilter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.db.chart.animation.Animation
import com.db.chart.model.LineSet
import com.db.chart.renderer.AxisRenderer.LabelPosition.NONE
import com.pleon.buyt.database.DailyCost
import com.pleon.buyt.database.PieSlice
import com.pleon.buyt.model.Category
import com.pleon.buyt.ui.PieChartView
import com.pleon.buyt.viewmodel.StatisticsViewModel
import kotlinx.android.synthetic.main.fragment_states.*
import java.text.DecimalFormat
import java.util.*


class StatesFragment : Fragment() {

    val period get() = viewModel.period
    private lateinit var viewModel: StatisticsViewModel
    private val priceFormat = DecimalFormat("#,###")
    private val pieColors = intArrayOf(0xff2DA579.toInt(), 0xffC1B435.toInt(),
            0xffC15335.toInt(), 0xff2D71A5.toInt(), 0xff999999.toInt())

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
        return inflater.inflate(com.pleon.buyt.R.layout.fragment_states, container, false)
    }

    override fun onViewCreated(view: View, savedState: Bundle?) {
        viewModel = ViewModelProviders.of(this).get(StatisticsViewModel::class.java)
        activity?.registerReceiver(timeReceiver, IntentFilter(ACTION_TIME_TICK))
        chartCaption.text = getString(com.pleon.buyt.R.string.chart_caption, viewModel.period.length)
        showStatistics()
    }

    private fun showStatistics() {
        viewModel.statistics.observe(viewLifecycleOwner, Observer { statistics ->
            showLineChart(statistics.dailyCosts!!)
            showPieChart(statistics.mostPurchasedCategories)

            textView3.text = priceFormat.format(statistics.totalPurchaseCost)
            textView.text = priceFormat.format(statistics.averagePurchaseCost)

            if (statistics.mostPurchasedCategoryName != 0) textView13.setText(statistics.mostPurchasedCategoryName)
            else textView13.text = "-"

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
            dataSet.setDotsColor(ContextCompat.getColor(context!!, com.pleon.buyt.R.color.colorPrimary))
            dataSet.setDotsRadius(3f)
        }
        dataSet.isSmooth = false // TODO: Add an options in settings for the user to toggle this
        dataSet.color = ContextCompat.getColor(context!!, com.pleon.buyt.R.color.colorPrimaryDark)
        dataSet.thickness = 2.5f

        val moneyFormat = DecimalFormat(getString(com.pleon.buyt.R.string.currency_format))
        chart.setLabelsFormat(moneyFormat)

        val colors = resources.getIntArray(com.pleon.buyt.R.array.lineChartGradient)
        val steps = floatArrayOf(0.0f, 0.5f, 1.0f)
        dataSet.setGradientFill(colors, steps)
        chart.addData(dataSet)
        chart.setXLabels(NONE)
        chart.show(Animation(500))
    }

    private fun showPieChart(pieSlices: List<PieSlice>) {
        for ((index, slice) in pieSlices.withIndex()) {
            pieChart.addItemType(PieChartView.ItemType(getString(Category.valueOf(slice.name).nameRes), slice.value, pieColors[index]))
        }

        pieChart.setAnimDuration(500)
        pieChart.setCell(4) // gap between slices
        pieChart.setInnerRadius(0.7f)
        pieChart.setBackGroundColor(0xff2E362F.toInt())
        pieChart.setItemTextSize(21) // FIXME: size isn't consistent across devices
        pieChart.startAnim()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        activity?.unregisterReceiver(timeReceiver)
    }

    fun togglePeriod() {
        viewModel.togglePeriod()
        chartCaption.text = getString(com.pleon.buyt.R.string.chart_caption, viewModel.period.length)
        showStatistics()
    }
}
