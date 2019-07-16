package com.pleon.buyt.ui

import android.animation.ValueAnimator
import android.content.Context
import android.content.res.Resources
import android.graphics.*
import android.graphics.Paint.ANTI_ALIAS_FLAG
import android.graphics.Paint.DITHER_FLAG
import android.util.AttributeSet
import android.util.TypedValue.COMPLEX_UNIT_DIP
import android.util.TypedValue.applyDimension
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.core.content.res.ResourcesCompat
import com.pleon.buyt.R
import com.pleon.buyt.util.FormatterUtil.formatPercent
import kotlin.math.*

/**
 * Adopted from [https://github.com/luweibin3118/PieChartView]
 */
class PieChartView : View {

    private val mPaint = Paint(ANTI_ALIAS_FLAG or DITHER_FLAG)
    private val mPath = Path()
    private val drawLinePath = Path()
    private val mPathMeasure = PathMeasure()
    private var mCanvas: Canvas? = null
    private var mWidth: Int = 0
    private var mHeight: Int = 0
    private val pieRectF = RectF()
    private var radius: Int = 0
    private val sectors = ArrayList<Sector>()
    private val leftTypeList = ArrayList<Sector>()
    private val rightTypeList = ArrayList<Sector>()
    private val itemPoints = ArrayList<Point>()
    private var gap = 0f
    private var innerRadius = 0f
    private var offRadius = 0f
    private var offLine = 0f
    private var textAlpha: Int = 0
    private var firstPoint: Point? = null
    private var backGroundColor = Color.TRANSPARENT
    private var itemTextSize = 30
    private var textPadding = 8
    private val defaultStartAngle = -90
    private val pieCell = 0f
    private var animator: ValueAnimator? = null
    private val startPoint = Point()
    private val centerPoint = Point()
    private val endPoint = Point()
    private val tempPoint = Point()

    constructor(cxt: Context) : super(cxt) {
        mPaint.typeface = ResourcesCompat.getFont(cxt, R.font.vazir_scaled_down)
    }

    constructor(cxt: Context, attrs: AttributeSet?) : super(cxt, attrs) {
        mPaint.typeface = ResourcesCompat.getFont(cxt, R.font.vazir_scaled_down)
    }

    fun startAnim(duration: Long) {
        animator = ValueAnimator.ofFloat(0f, 360f * 2)
        animator!!.duration = duration
        animator!!.interpolator = LinearInterpolator()
        animator!!.addUpdateListener { animation ->
            val value = animation.animatedValue as Float
            if (value < 360f) {
                offRadius = value
                offLine = 0f
                textAlpha = 0
            } else if (value >= 360f) {
                offRadius = 360f
                offLine = (value - 360f) / 360f
                textAlpha = if (offLine > 0.5f) (255 * ((offLine - 0.5f) / 0.5f)).toInt() else 0
            } else if (value == 360f * 2) {
                offRadius = 360f
                offLine = 1.0f
                textAlpha = 255
            }
            postInvalidate()
        }
        animator!!.start()
    }

    override fun onSizeChanged(w: Int, h: Int, oldWid: Int, oldHei: Int) {
        super.onSizeChanged(w, h, oldWid, oldHei)
        mWidth = w
        mHeight = h
        radius = (min(mWidth, mHeight) / 3.2).toInt()
        pieRectF.set((mWidth / 2 - radius).toFloat(), (mHeight / 2 - radius).toFloat(), (mWidth / 2 + radius).toFloat(), (mHeight / 2 + radius).toFloat())
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        mCanvas = canvas
        drawPie()
        if (offRadius == 360f) drawTitle()
    }

    private fun drawPie() {
        mCanvas?.drawColor(backGroundColor)
        mPaint.style = Paint.Style.FILL

        var sum = 0
        for (slice in sectors) sum += slice.widget
        val a = 360f / sum

        var startRadius = defaultStartAngle.toFloat()
        var sumRadius = 0f
        leftTypeList.clear()
        rightTypeList.clear()
        itemPoints.clear()
        for ((index, slice) in sectors.withIndex()) {
            slice.radius = slice.widget * a
            val al = 2.0 * PI * ((startRadius + 90) / 360.0)
            tempPoint.set((mWidth / 2 + radius * sin(al)).toInt(), (mHeight / 2 - radius * cos(al)).toInt())

            if (gap > 0 && startRadius == defaultStartAngle.toFloat()) firstPoint = tempPoint

            val angle = 2.0 * PI * ((startRadius + slice.radius / 2) / 360.0)
            val cos = -cos(angle)
            if (cos > 0) leftTypeList.add(slice) else rightTypeList.add(slice)

            sumRadius += abs(slice.radius)
            mPaint.style = Paint.Style.FILL
            mPaint.color = slice.color
            if (sumRadius <= offRadius) {
                mCanvas!!.drawArc(pieRectF, startRadius, slice.radius, true, mPaint)
            } else {
                mCanvas!!.drawArc(pieRectF, startRadius, slice.radius - abs(offRadius - sumRadius), true, mPaint)
                break
            }
            startRadius += slice.radius
            if (gap > 0 && pieCell == 0f) {
                mPaint.color = backGroundColor
                mPaint.strokeWidth = if (index == 0) gap + 4 else gap
                mCanvas!!.drawLine(width / 2f, height / 2f, tempPoint.x.toFloat(), tempPoint.y.toFloat(), mPaint)
            }
        }
        mPaint.style = Paint.Style.FILL
        mPaint.color = backGroundColor
        if (innerRadius > 0 && pieCell == 0f) {
            mCanvas!!.drawCircle(mWidth / 2f, mHeight / 2f, radius * innerRadius, mPaint)
        }
    }

    private fun drawTitle() {
        resetPaint()

        var startRadius = defaultStartAngle.toFloat()
        var count = rightTypeList.size
        var h = if (count > 1) radius * 2 / (count - 1) else radius

        for (i in 0 until count) {
            mPath.reset()
            val slice = rightTypeList[i]
            val angle = 2.0 * PI * ((startRadius + slice.radius / 2) / 360.0)
            val x = (mWidth / 2 + radius * cos(angle)).toInt()
            val y = (mHeight / 2 + radius * sin(angle)).toInt()
            startPoint.set(x, y)
            centerPoint.set((mWidth / 2 + radius * 1.2f).toInt(), mHeight / 2 - radius + h * i)
            endPoint.set((mWidth * 0.98f).toInt(), centerPoint.y)
            mPath.moveTo(startPoint.x.toFloat(), startPoint.y.toFloat())
            mPath.lineTo(centerPoint.x.toFloat(), centerPoint.y.toFloat())
            mPath.lineTo(endPoint.x.toFloat(), endPoint.y.toFloat())
            resetPaint()
            mPaint.strokeWidth = 2f
            mPaint.color = slice.color
            mPaint.style = Paint.Style.STROKE
            mPathMeasure.setPath(mPath, false)
            drawLinePath.reset()
            mPathMeasure.getSegment(0f, mPathMeasure.length * offLine, drawLinePath, true)
            mCanvas!!.drawPath(drawLinePath, mPaint)
            startRadius += slice.radius

            if (textAlpha > 0) {
                mPaint.textSize = itemTextSize.toFloat()
                mPaint.style = Paint.Style.FILL
                mPaint.textAlign = Paint.Align.CENTER
                mPaint.alpha = textAlpha
                mCanvas!!.drawText(slice.type, (centerPoint.x + (endPoint.x - centerPoint.x) / 2).toFloat(),
                        (centerPoint.y - textPadding).toFloat(), mPaint)
                mPaint.textSize = itemTextSize * 0.85f
                mCanvas!!.drawText(slice.percent, (centerPoint.x + (endPoint.x - centerPoint.x) / 2).toFloat(),
                        centerPoint.y + (itemTextSize + textPadding) * 0.8f, mPaint)
            }
        }

        count = leftTypeList.size
        h = if (count > 1) radius * 2 / (count - 1) else radius

        for (i in 0 until count) {
            mPath.reset()
            val slice = leftTypeList[i]
            val angle = 2.0 * PI * ((startRadius + slice.radius / 2) / 360.0)
            val x = (mWidth / 2 + radius * cos(angle)).toInt()
            val y = (mHeight / 2 + radius * sin(angle)).toInt()
            startPoint.set(x, y)
            centerPoint.set((mWidth / 2 - radius * 1.2f).toInt(), mHeight / 2 - radius + h * (count - 1 - i))
            endPoint.set((mWidth * 0.02f).toInt(), centerPoint.y)
            mPath.moveTo(startPoint.x.toFloat(), startPoint.y.toFloat())
            mPath.lineTo(centerPoint.x.toFloat(), centerPoint.y.toFloat())
            mPath.lineTo(endPoint.x.toFloat(), endPoint.y.toFloat())
            resetPaint()
            mPaint.strokeWidth = 2f
            mPaint.color = slice.color
            mPaint.isAntiAlias = true
            mPaint.isDither = true
            mPaint.style = Paint.Style.STROKE
            mPathMeasure.setPath(mPath, false)
            drawLinePath.reset()
            mPathMeasure.getSegment(0f, mPathMeasure.length * offLine, drawLinePath, true)
            mCanvas!!.drawPath(drawLinePath, mPaint)
            startRadius += slice.radius

            if (textAlpha > 0) {
                mPaint.textSize = itemTextSize.toFloat()
                mPaint.style = Paint.Style.FILL
                mPaint.textAlign = Paint.Align.CENTER
                mPaint.alpha = textAlpha
                mCanvas!!.drawText(slice.type, (centerPoint.x + (endPoint.x - centerPoint.x) / 2).toFloat(),
                        (centerPoint.y - textPadding).toFloat(), mPaint)
                mPaint.textSize = itemTextSize * 0.85f
                mCanvas!!.drawText(slice.percent, (centerPoint.x + (endPoint.x - centerPoint.x) / 2).toFloat(),
                        centerPoint.y + (itemTextSize + textPadding) * 0.8f, mPaint)
            }
        }

        if (textAlpha.toFloat() == 1f) {
            sectors.clear()
            leftTypeList.clear()
            rightTypeList.clear()
            itemPoints.clear()
        }
    }

    fun resetPaint() {
        mPaint.reset()
        mPaint.isAntiAlias = true
        mPaint.isDither = true
        mPaint.alpha = 256
    }

    fun clearData() = sectors.clear()

    fun addSector(sector: Sector) = sectors.add(sector)

    fun setSectorGap(gap: Int) {
        this.gap = gap.toFloat()
    }

    fun setInnerRadius(innerRadius: Float) {
        this.innerRadius = innerRadius.coerceIn(0f, 1f)
    }

    fun setBackGroundColor(backGroundColor: Int) {
        this.backGroundColor = backGroundColor
    }

    fun setItemTextSize(itemTextSize: Float) {
        val displayMetrics = Resources.getSystem().displayMetrics
        val textSize = applyDimension(COMPLEX_UNIT_DIP, itemTextSize, displayMetrics)
        this.itemTextSize = textSize.toInt()
    }

    /**
     * Sets vertical padding between the text and its horizontal line
     */
    @Suppress("unused")
    fun setTextPadding(textPadding: Int) {
        this.textPadding = textPadding
    }

    class Sector(internal var type: String, internal var widget: Int, internal var color: Int) {
        internal var radius: Float = 0f
        internal val percent: String get() = formatPercent(radius / 360f)
    }
}
