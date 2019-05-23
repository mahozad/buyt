package com.pleon.buyt.ui

import android.animation.ValueAnimator
import android.content.Context
import android.content.res.Resources
import android.graphics.*
import android.util.AttributeSet
import android.util.TypedValue.COMPLEX_UNIT_DIP
import android.util.TypedValue.applyDimension
import android.view.View
import android.view.animation.LinearInterpolator
import com.pleon.buyt.util.FormatterUtil.formatPercent
import java.util.*

/**
 * Adopted from [https://github.com/luweibin3118/PieChartView]
 */
class PieChartView : View {

    private val mPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.DITHER_FLAG)
    private val mPath = Path()
    private val drawLinePath = Path()
    private val mPathMeasure = PathMeasure()
    private var mCanvas: Canvas? = null
    private var mWidth: Int = 0
    private var mHeight: Int = 0
    private val pieRectF = RectF()
    private val tempRectF = RectF()
    private var radius: Int = 0
    private val sliceList = ArrayList<Slice>()
    private val leftTypeList = ArrayList<Slice>()
    private val rightTypeList = ArrayList<Slice>()
    private val itemPoints = ArrayList<Point>()
    private var cell = 0
    private var innerRadius = 0f
    private var offRadius = 0f
    private var offLine: Float = 0.toFloat()
    private var textAlpha: Int = 0
    private var firstPoint: Point? = null
    private var backGroundColor = -0x1
    private var itemTextSize = 30
    private var textPadding = 8
    private val defaultStartAngle = -90
    private val pieCell: Float = 0.toFloat()
    private var animator: ValueAnimator? = null
    private var animDuration: Long = 1000
    private val startPoint = Point()
    private val centerPoint = Point()
    private val endPoint = Point()
    private val tempPoint = Point()

    constructor(context: Context) : super(context) {}

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {}

    //    @Override
    //    protected void onAttachedToWindow() {
    //        super.onAttachedToWindow();
    //        startAnim();
    //    }

    //    @Override
    //    protected void onDetachedFromWindow() {
    //        super.onDetachedFromWindow();
    //        if (animator != null) animator.cancel();
    //    }

    fun startAnim() {
        animator = ValueAnimator.ofFloat(0f, 360f * 2)
        animator!!.duration = animDuration
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
                if (offLine > 0.5f) {
                    textAlpha = (255 * ((offLine - 0.5f) / 0.5f)).toInt()
                } else {
                    textAlpha = 0
                }
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
        this.mWidth = w
        this.mHeight = h
        radius = (Math.min(mWidth, mHeight) / 3.2).toInt()
        pieRectF.set((mWidth / 2 - radius).toFloat(), (mHeight / 2 - radius).toFloat(), (mWidth / 2 + radius).toFloat(), (mHeight / 2 + radius).toFloat())
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        try {
            mCanvas = canvas
            drawPie()
            if (offRadius == 360f) {
                drawTitle()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    private fun drawTitle() {
        resetPaint()
        var startRadius = defaultStartAngle.toFloat()
        var count = rightTypeList.size
        var h: Int
        if (count > 1) {
            h = radius * 2 / (count - 1)
        } else {
            h = radius
        }
        for (i in 0 until count) {
            mPath.reset()
            val slice = rightTypeList[i]
            val angle = 2.0 * Math.PI * ((startRadius + slice.radius / 2) / 360.0)
            val x = (mWidth / 2 + radius * Math.cos(angle)).toInt()
            val y = (mHeight / 2 + radius * Math.sin(angle)).toInt()
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
        if (count > 1) {
            h = radius * 2 / (count - 1)
        } else {
            h = radius
        }

        for (i in 0 until count) {
            mPath.reset()
            val slice = leftTypeList[i]
            val angle = 2.0 * Math.PI * ((startRadius + slice.radius / 2) / 360.0)
            val x = (mWidth / 2 + radius * Math.cos(angle)).toInt()
            val y = (mHeight / 2 + radius * Math.sin(angle)).toInt()
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
            sliceList.clear()
            leftTypeList.clear()
            rightTypeList.clear()
            itemPoints.clear()
        }
    }

    private fun drawPie() {
        if (mCanvas == null) {
            return
        }
        mCanvas!!.drawColor(backGroundColor)
        mPaint.style = Paint.Style.FILL
        var sum = 0
        for (slice in sliceList) {
            sum += slice.widget
        }
        val a = 360f / sum
        var startRadius = defaultStartAngle.toFloat()
        var sumRadius = 0f
        leftTypeList.clear()
        rightTypeList.clear()
        itemPoints.clear()
        for (slice in sliceList) {
            slice.radius = slice.widget * a
            val al = 2.0 * Math.PI * ((startRadius + 90) / 360.0)
            tempPoint.set((mWidth / 2 + radius * Math.sin(al)).toInt(),
                    (mHeight / 2 - radius * Math.cos(al)).toInt())
            if (cell > 0) {
                if (startRadius == defaultStartAngle.toFloat()) {
                    firstPoint = tempPoint
                }
            }

            val angle = 2.0 * Math.PI * ((startRadius + slice.radius / 2) / 360.0)
            val sin = -Math.sin(angle)
            val cos = -Math.cos(angle)
            if (cos > 0) {
                leftTypeList.add(slice)
            } else {
                rightTypeList.add(slice)
            }
            sumRadius += Math.abs(slice.radius)
            mPaint.style = Paint.Style.FILL
            mPaint.color = slice.color
            if (pieCell > 0) {
                if (sumRadius <= offRadius) {
                    tempRectF.set(pieRectF.left - (pieCell * cos).toFloat(), pieRectF.top - (pieCell * sin).toFloat(),
                            pieRectF.right - (pieCell * cos).toFloat(), pieRectF.bottom - (pieCell * sin).toFloat())
                    mCanvas!!.drawArc(tempRectF, startRadius, slice.radius, true, mPaint)
                } else {
                    mCanvas!!.drawArc(tempRectF, startRadius, slice.radius - Math.abs(offRadius - sumRadius), true, mPaint)
                    break
                }
            } else {
                if (sumRadius <= offRadius) {
                    mCanvas!!.drawArc(pieRectF, startRadius, slice.radius, true, mPaint)
                } else {
                    mCanvas!!.drawArc(pieRectF, startRadius, slice.radius - Math.abs(offRadius - sumRadius), true, mPaint)
                    break
                }

            }
            startRadius += slice.radius
            if (cell > 0 && pieCell == 0f) {
                mPaint.color = backGroundColor
                mPaint.strokeWidth = cell.toFloat()
                mCanvas!!.drawLine((getWidth() / 2).toFloat(), (getHeight() / 2).toFloat(), tempPoint.x.toFloat(), tempPoint.y.toFloat(), mPaint)
            }
        }
        if (cell > 0 && firstPoint != null && pieCell == 0f) {
            mPaint.color = backGroundColor
            mPaint.strokeWidth = cell.toFloat()
            mCanvas!!.drawLine((getWidth() / 2).toFloat(), (getHeight() / 2).toFloat(), firstPoint!!.x.toFloat(), firstPoint!!.y.toFloat(), mPaint)
        }
        mPaint.style = Paint.Style.FILL
        mPaint.color = backGroundColor
        if (innerRadius > 0 && pieCell == 0f) {
            mCanvas!!.drawCircle((mWidth / 2).toFloat(), (mHeight / 2).toFloat(), radius * innerRadius, mPaint)
        }
    }

    fun resetPaint() {
        mPaint.reset()
        mPaint.isAntiAlias = true
        mPaint.isDither = true
        mPaint.alpha = 256
    }

    fun clearData() {
        sliceList.clear()
    }

    fun addSector(slice: Slice) {
        sliceList.add(slice)
    }

    /**
     * Set the gap between slices.
     */
    fun setCell(cell: Int) {
        this.cell = cell
    }

    fun setInnerRadius(innerRadius: Float) {
        var innerRadius = innerRadius
        if (innerRadius > 1f)
            innerRadius = 1f
        else if (innerRadius < 0) innerRadius = 0f

        this.innerRadius = innerRadius
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
    fun setTextPadding(textPadding: Int) {
        this.textPadding = textPadding
    }

    fun setAnimDuration(animDuration: Long) {
        this.animDuration = animDuration
    }

    class Slice(internal var type: String, internal var widget: Int, internal var color: Int) {

        internal var radius: Float = 0f
        internal val percent: String get() = formatPercent(radius / 360f)
    }
}
