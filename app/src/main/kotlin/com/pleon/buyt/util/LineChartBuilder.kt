package com.pleon.buyt.util

import android.content.Context
import android.graphics.Paint
import androidx.core.content.ContextCompat.getColor
import com.db.chart.model.LineSet
import com.db.chart.renderer.AxisRenderer.LabelPosition.NONE
import com.db.chart.view.LineChartView
import com.pleon.buyt.R
import java.text.DecimalFormat

fun buildLineChart(cxt: Context, chartView: LineChartView, data: List<DataSet>,
                   hasDots: Boolean = true, dotsRadius: Float = 3f, thickness: Float = 3f,
                   isSmooth: Boolean = false, isDashed: Boolean = false,
                   gridRows: Int = 3, gridCols: Int = 0): LineChartView {

    chartView.reset()

    val dataSet = LineSet()
    var isEmpty = true
    for (datum in data) {
        dataSet.addPoint(datum.getLabel(), datum.getValue())
        if (datum.getValue() > 0) isEmpty = false
    }

    val fillColors = cxt.resources.getIntArray(R.array.lineChartGradient)
    val gridPaint = Paint().apply { color = getColor(cxt, R.color.chartGridColor) }

    dataSet.setColor(getColor(cxt, if (isEmpty) R.color.chartEmptyColor else R.color.colorPrimaryDark))
            .setSmooth(isSmooth)
            .setThickness(thickness)
            .setGradientFill(fillColors, floatArrayOf(0.0f, 0.2f, 0.5f, 1.0f))

    if (hasDots) {
        dataSet.setDotsColor(getColor(cxt, if (isEmpty) R.color.chartEmptyColor else R.color.colorPrimary))
        dataSet.setDotsRadius(dotsRadius)
    }

    if (isDashed) dataSet.setDashed(floatArrayOf(0f, 2.5f, 4.9f, 10f, 15f))

    chartView.setLabelsFormat(DecimalFormat(cxt.getString(R.string.currency_format)))
            .setGrid(gridRows, gridCols, gridPaint)
            .setXLabels(NONE)
            .addData(dataSet)

    return chartView
}

interface DataSet {
    fun getLabel(): String
    fun getValue(): Float
}
