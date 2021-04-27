package com.pleon.buyt.util

import android.content.Context
import android.graphics.Paint
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat.getColor
import com.db.chart.model.LineSet
import com.db.chart.renderer.AxisRenderer.LabelPosition.INSIDE
import com.db.chart.renderer.AxisRenderer.LabelPosition.NONE
import com.db.chart.view.LineChartView
import com.pleon.buyt.R
import com.pleon.buyt.ui.fragment.PREF_LOG_SCALE
import org.jetbrains.anko.defaultSharedPreferences
import java.text.DecimalFormat
import kotlin.math.log10

fun buildLineChart(cxt: Context,
                   chartView: LineChartView,
                   data: List<DataSet>,
                   hasDots: Boolean = true,
                   dotsRadius: Float = 8f,
                   thickness: Float = 4f,
                   isSmooth: Boolean = false,
                   isDashed: Boolean = false,
                   gridRows: Int = 3,
                   gridCols: Int = 0): LineChartView {

    chartView.reset()
    val isLogScale = cxt.defaultSharedPreferences.getBoolean(PREF_LOG_SCALE, false)

    val dataSet = LineSet()
    var isEmpty = true

    val maxValue = data.maxOf { it.getValue() }
    val adjustedData = data.map {
        val label = it.getLabel()
        val value = when {
            maxValue < 1_000 -> it.getValue()
            maxValue < 1_000_000 -> it.getValue() / 1_000
            maxValue < 1_000_000_000 -> it.getValue() / 1_000_000
            else -> it.getValue() / 1_000_000_000
        }
        return@map Pair(label, value)
    }
    @StringRes val formatStringRes = when {
        maxValue < 1_000 -> R.string.currency_format_ones
        maxValue < 1_000_000 -> R.string.currency_format_thousands
        else -> R.string.currency_format_millions_and_billions
    }
    val formatter = DecimalFormat(cxt.getString(formatStringRes))

    for (datum in adjustedData) {
        val value = if (isLogScale) log10(datum.second + 1) else datum.second
        dataSet.addPoint(datum.first, value)
        if (datum.second > 0) isEmpty = false
    }

    val fillColors = cxt.resources.getIntArray(R.array.lineChartGradient)
    val gridPaint = Paint().apply { color = getColor(cxt, R.color.chartGridColor) }

    dataSet.setColor(getColor(cxt, if (isEmpty) R.color.chartEmptyColor else R.color.lineChartLineColor))
            .setSmooth(isSmooth)
            .setThickness(thickness)
            .setGradientFill(fillColors, floatArrayOf(0.0f, 0.2f, 0.5f, 1.0f))

    if (hasDots) {
        dataSet.setDotsStrokeColor(getColor(cxt,
                if (isEmpty) R.color.chartEmptyColor
                else R.color.colorPrimary)
        )
        dataSet.setDotsColor(
                if (isEmpty) getColor(cxt, R.color.chartEmptyColor)
                else cxt.resolveThemeColorVal(R.attr.colorSurface)
        )
        dataSet.setDotsStrokeThickness(thickness)
        dataSet.setDotsRadius(dotsRadius)
    }

    if (isDashed) dataSet.setDashed(floatArrayOf(0f, 2.5f, 4.9f, 10f, 15f))

    chartView.setLabelsFormat(formatter)
            .setGrid(gridRows, gridCols, gridPaint)
            .setXLabels(NONE)
            .setYLabels(if (isLogScale) NONE else INSIDE)
            .addData(dataSet)

    return chartView
}

interface DataSet {
    fun getLabel(): String
    fun getValue(): Float
}
