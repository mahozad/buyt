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
import com.pleon.buyt.R
import com.pleon.buyt.model.Category
import com.pleon.buyt.model.DailyCost
import com.pleon.buyt.viewmodel.StatisticsViewModel
import kotlinx.android.synthetic.main.fragment_states.*
import java.text.DecimalFormat
import java.util.*

class StatesFragment : Fragment() {

    val period get() = viewModel.period
    private lateinit var viewModel: StatisticsViewModel

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
        chartCaption.text = getString(R.string.chart_caption, viewModel.period.length)
        showStatistics()
    }

    private fun showStatistics() {
        viewModel.statistics.observe(viewLifecycleOwner, Observer { statistics ->
            showGraph(statistics.dailyCosts!!)

            textView3.text = statistics.totalPurchaseCost.toString()
            textView.text = statistics.averagePurchaseCost.toString()
            if (statistics.mostPurchasedCategoryName != 0) {
                textView13.setText(statistics.mostPurchasedCategoryName)
            }
            textView18.text = statistics.numberOfPurchases.toString()
            textView6.text = statistics.maxPurchaseCost.toString()
            textView7.text = statistics.minPurchaseCost.toString()
            textView9.setText(statistics.weekdayNameResWithMaxPurchases)
            textView17.text = statistics.storeNameWithMaxPurchaseCount
        })
    }

    private fun showGraph(dailyCosts: List<DailyCost>) {
        chart.reset()

        val dataSet = LineSet()
        for (dailyCost in dailyCosts) {
            dataSet.addPoint(dailyCost.date, dailyCost.totalCost.toFloat())
        }

        if (viewModel.period.length <= 20) {
            dataSet.setDotsColor(ContextCompat.getColor(context!!, R.color.colorPrimary))
            dataSet.setDotsRadius(3f)
        }
        dataSet.isSmooth = false // TODO: Add an options in settings for the user to toggle this
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

    override fun onDestroyView() {
        super.onDestroyView()
        activity?.unregisterReceiver(timeReceiver)
    }

    fun togglePeriod() {
        viewModel.togglePeriod()
        val caption = getString(R.string.chart_caption, viewModel.period.length)
        chartCaption.text = caption
        showStatistics()
    }
}
