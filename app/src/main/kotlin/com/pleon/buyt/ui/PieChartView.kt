package com.pleon.buyt.ui

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.graphics.Paint.ANTI_ALIAS_FLAG
import android.graphics.Paint.DITHER_FLAG
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.core.content.ContextCompat.getColor
import androidx.core.content.res.ResourcesCompat
import com.pleon.buyt.R
import com.pleon.buyt.util.formatPercent
import org.jetbrains.anko.dip
import org.jetbrains.anko.sp
import kotlin.math.*

/**
 * Adopted from [https://github.com/luweibin3118/PieChartView]
 */
class PieChartView : View {

    private val mPaint = Paint(ANTI_ALIAS_FLAG or DITHER_FLAG)
    private val mPath = Path()
    private val drawLinePath = Path()
    private val mPathMeasure = PathMeasure()
    private var mWidth: Int = 0
    private var mHeight: Int = 0
    private val pieRectF = RectF()
    private var radius: Int = 0
    private val slices = ArrayList<Slice>()
    private val leftTypeList = ArrayList<Slice>()
    private val rightTypeList = ArrayList<Slice>()
    private var gap = 4f
    private var innerRadius = 80f
    private var offRadius = 0f
    private var offLine = 0f
    private var textAlpha: Int = 0
    private var emptyColor = getColor(context, R.color.chartEmptyColor)
    private var itemTextSize = sp(13.5f)
    private var textPadding = 8
    private val defaultStartAngle = -90
    private val pieCell = 0f
    private var animator: ValueAnimator? = null
    private val startPoint = Point()
    private val centerPoint = Point()
    private val endPoint = Point()
    private val tempPoint = Point()

    constructor(cxt: Context) : super(cxt) {
        mPaint.typeface = ResourcesCompat.getFont(cxt, R.font.vazir_wol_v_26_0_2_scaled_uniformly_to_94_percent)
    }

    constructor(cxt: Context, attrs: AttributeSet?) : super(cxt, attrs) {
        mPaint.typeface = ResourcesCompat.getFont(cxt, R.font.vazir_wol_v_26_0_2_scaled_uniformly_to_94_percent)
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
        animator?.start()
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
        drawPie(canvas)
        if (offRadius == 360f) drawTitle(canvas)
    }

    private fun drawPie(canvas: Canvas) {
        // in order to be able to clear part of canvas (making hole in pie) these are required
        // for more info see [https://stackoverflow.com/q/19947835/8583692]
        val bitmap = Bitmap.createBitmap(canvas.width, canvas.height, Bitmap.Config.ARGB_8888)
        val temp = Canvas(bitmap)
        val transparentPaint = Paint(ANTI_ALIAS_FLAG)
        transparentPaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)

        if (slices.isEmpty()) { // draw empty pie
            mPaint.style = Paint.Style.FILL
            mPaint.color = emptyColor
            temp.drawCircle(mWidth / 2f, mHeight / 2f, radius.toFloat(), mPaint)
        }

        val sum = slices.sumOf(Slice::weight).toFloat()

        var startRadius = defaultStartAngle.toFloat()
        var sumRadius = 0f
        leftTypeList.clear()
        rightTypeList.clear()
        for ((index, slice) in slices.withIndex()) {
            slice.radius = slice.weight.toFloat() / sum * 360f
            val al = 2.0 * PI * ((startRadius + 90) / 360.0)
            tempPoint.set((mWidth / 2 + radius * sin(al)).toInt(), (mHeight / 2 - radius * cos(al)).toInt())

            val angle = 2.0 * PI * ((startRadius + slice.radius / 2) / 360.0)
            val cos = -cos(angle)
            if (cos > 0) leftTypeList.add(slice) else rightTypeList.add(slice)

            sumRadius += abs(slice.radius)
            mPaint.style = Paint.Style.FILL
            mPaint.color = slice.color
            if (sumRadius <= offRadius) {
                temp.drawArc(pieRectF, startRadius, slice.radius, true, mPaint)
            } else {
                temp.drawArc(pieRectF, startRadius, slice.radius - abs(offRadius - sumRadius), true, mPaint)
                break
            }
            startRadius += slice.radius
            // draw transparent separating lines
            if (slices.size > 1 && gap > 0 && pieCell == 0f) {
                transparentPaint.strokeWidth = if (index == 0) gap * 2 else gap
                temp.drawLine(width / 2f, height / 2f, tempPoint.x.toFloat(), tempPoint.y.toFloat(), transparentPaint)
            }
        }

        // inner hole
        temp.drawCircle(mWidth / 2f, mHeight / 2f, innerRadius, transparentPaint)

        canvas.drawBitmap(bitmap, 0f, 0f, mPaint)
    }

    private fun drawTitle(canvas: Canvas) {
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
            mPaint.strokeWidth = dip(1.85f).toFloat()
            mPaint.color = slice.color
            mPaint.style = Paint.Style.STROKE
            mPathMeasure.setPath(mPath, false)
            drawLinePath.reset()
            mPathMeasure.getSegment(0f, mPathMeasure.length * offLine, drawLinePath, true)
            canvas.drawPath(drawLinePath, mPaint)
            startRadius += slice.radius

            if (textAlpha > 0) {
                mPaint.textSize = itemTextSize.toFloat()
                mPaint.style = Paint.Style.FILL
                mPaint.textAlign = Paint.Align.CENTER
                mPaint.alpha = textAlpha
                canvas.drawText(slice.type, (centerPoint.x + (endPoint.x - centerPoint.x) / 2).toFloat(),
                        centerPoint.y - (textPadding * 2.4f), mPaint)
                mPaint.textSize = itemTextSize * 0.85f
                canvas.drawText(slice.percent, (centerPoint.x + (endPoint.x - centerPoint.x) / 2).toFloat(),
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
            mPaint.strokeWidth = dip(1.85f).toFloat()
            mPaint.color = slice.color
            mPaint.isAntiAlias = true
            mPaint.isDither = true
            mPaint.style = Paint.Style.STROKE
            mPathMeasure.setPath(mPath, false)
            drawLinePath.reset()
            mPathMeasure.getSegment(0f, mPathMeasure.length * offLine, drawLinePath, true)
            canvas.drawPath(drawLinePath, mPaint)
            startRadius += slice.radius

            if (textAlpha > 0) {
                mPaint.textSize = itemTextSize.toFloat()
                mPaint.style = Paint.Style.FILL
                mPaint.textAlign = Paint.Align.CENTER
                mPaint.alpha = textAlpha
                canvas.drawText(slice.type, (centerPoint.x + (endPoint.x - centerPoint.x) / 2).toFloat(),
                        centerPoint.y - (textPadding * 2.4f), mPaint)
                mPaint.textSize = itemTextSize * 0.85f
                canvas.drawText(slice.percent, (centerPoint.x + (endPoint.x - centerPoint.x) / 2).toFloat(),
                        centerPoint.y + (itemTextSize + textPadding) * 0.8f, mPaint)
            }
        }

        if (textAlpha.toFloat() == 1f) {
            slices.clear()
            leftTypeList.clear()
            rightTypeList.clear()
        }
    }

    private fun resetPaint() {
        mPaint.reset()
        mPaint.isAntiAlias = true
        mPaint.isDither = true
        mPaint.alpha = 256
    }

    fun clearData() = slices.clear()

    fun addSlice(slice: Slice) = slices.add(slice)

    fun setSliceGap(gap: Int) {
        this.gap = gap.toFloat()
    }

    fun setInnerRadius(innerRadius: Float) {
        this.innerRadius = innerRadius.coerceIn(0f, 1f)
    }

    fun setItemTextSize(itemTextSize: Float) {
        this.itemTextSize = sp(itemTextSize)
    }

    /**
     * Sets vertical padding between the text and its horizontal line
     */
    @Suppress("unused")
    fun setTextPadding(textPadding: Int) {
        this.textPadding = textPadding
    }

    class Slice(var type: String, var weight: Double, var color: Int) {
        var radius: Float = 0f
        val percent: String get() = formatPercent(radius / 360f)
    }
}
