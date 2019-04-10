package com.pleon.buyt.ui;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.Point;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;

/**
 * Adopted from [https://github.com/luweibin3118/PieChartView]
 */
public class PieChartView extends View {

    private Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
    private Path mPath = new Path(), drawLinePath = new Path();
    private PathMeasure mPathMeasure = new PathMeasure();
    private Canvas mCanvas;
    private int width, height;
    private RectF pieRectF = new RectF(), tempRectF = new RectF();
    private int radius;
    private List<Slice> sliceList = new ArrayList<>(), leftTypeList = new ArrayList<>(), rightTypeList = new ArrayList<>();
    private List<Point> itemPoints = new ArrayList<>();
    private int cell = 0;
    private float innerRadius = 0;
    private float offRadius = 0, offLine;
    private int textAlpha;
    private Point firstPoint;
    private int backGroundColor = 0xffffffff;
    private int itemTextSize = 30, textPadding = 8;
    private int defaultStartAngle = -90;
    private float pieCell;
    private ValueAnimator animator;
    private long animDuration = 1000;
    private Point startPoint = new Point();
    private Point centerPoint = new Point();
    private Point endPoint = new Point();
    private Point tempPoint = new Point();

    public PieChartView(Context context) {
        super(context);
    }

    public PieChartView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

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

    public void startAnim() {
        animator = ValueAnimator.ofFloat(0, 360f * 2);
        animator.setDuration(animDuration);
        animator.setInterpolator(new LinearInterpolator());
        animator.addUpdateListener(animation -> {
            float value = (float) animation.getAnimatedValue();
            if (value < 360f) {
                offRadius = value;
                offLine = 0;
                textAlpha = 0;
            } else if (value >= 360f) {
                offRadius = 360f;
                offLine = (value - 360f) / 360f;
                if (offLine > 0.5f) {
                    textAlpha = (int) (255 * ((offLine - 0.5f) / 0.5f));
                } else {
                    textAlpha = 0;
                }
            } else if (value == 360f * 2) {
                offRadius = 360f;
                offLine = 1.0f;
                textAlpha = 255;
            }
            postInvalidate();
        });
        animator.start();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldWid, int oldHei) {
        super.onSizeChanged(w, h, oldWid, oldHei);
        this.width = w;
        this.height = h;
        radius = (int) (Math.min(width, height) / 3.2);
        pieRectF.set(width / 2 - radius, height / 2 - radius, width / 2 + radius, height / 2 + radius);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        try {
            mCanvas = canvas;
            drawPie();
            if (offRadius == 360f) {
                drawTitle();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void drawTitle() {
        resetPaint();
        float startRadius = defaultStartAngle;
        int count = rightTypeList.size();
        int h;
        if (count > 1) {
            h = (radius * 2) / (count - 1);
        } else {
            h = radius;
        }
        for (int i = 0; i < count; i++) {
            mPath.reset();
            Slice slice = rightTypeList.get(i);
            double angle = 2 * Math.PI * ((startRadius + slice.radius / 2) / 360d);
            int x = (int) (width / 2 + radius * Math.cos(angle));
            int y = (int) (height / 2 + radius * Math.sin(angle));
            startPoint.set(x, y);
            centerPoint.set((int) (width / 2 + radius * 1.2f), height / 2 - radius + h * (i));
            endPoint.set((int) (width * 0.98f), centerPoint.y);
            mPath.moveTo(startPoint.x, startPoint.y);
            mPath.lineTo(centerPoint.x, centerPoint.y);
            mPath.lineTo(endPoint.x, endPoint.y);
            resetPaint();
            mPaint.setStrokeWidth(2);
            mPaint.setColor(slice.color);
            mPaint.setStyle(Paint.Style.STROKE);
            mPathMeasure.setPath(mPath, false);
            drawLinePath.reset();
            mPathMeasure.getSegment(0, mPathMeasure.getLength() * offLine, drawLinePath, true);
            mCanvas.drawPath(drawLinePath, mPaint);
            startRadius += slice.radius;

            if (textAlpha > 0) {
                mPaint.setTextSize(itemTextSize);
                mPaint.setStyle(Paint.Style.FILL);
                mPaint.setTextAlign(Paint.Align.CENTER);
                mPaint.setAlpha(textAlpha);
                mCanvas.drawText(slice.type, centerPoint.x + (endPoint.x - centerPoint.x) / 2,
                        centerPoint.y - textPadding, mPaint);
                mPaint.setTextSize(itemTextSize * 0.85f);
                mCanvas.drawText(slice.getPercent(), centerPoint.x + (endPoint.x - centerPoint.x) / 2,
                        centerPoint.y + (itemTextSize + textPadding) * 0.8f, mPaint);
            }
        }

        count = leftTypeList.size();
        if (count > 1) {
            h = (radius * 2) / (count - 1);
        } else {
            h = radius;
        }

        for (int i = 0; i < count; i++) {
            mPath.reset();
            Slice slice = leftTypeList.get(i);
            double angle = 2 * Math.PI * ((startRadius + slice.radius / 2) / 360d);
            int x = (int) (width / 2 + radius * Math.cos(angle));
            int y = (int) (height / 2 + radius * Math.sin(angle));
            startPoint.set(x, y);
            centerPoint.set((int) (width / 2 - radius * 1.2f), height / 2 - radius + h * (count - 1 - i));
            endPoint.set((int) (width * 0.02f), centerPoint.y);
            mPath.moveTo(startPoint.x, startPoint.y);
            mPath.lineTo(centerPoint.x, centerPoint.y);
            mPath.lineTo(endPoint.x, endPoint.y);
            resetPaint();
            mPaint.setStrokeWidth(2);
            mPaint.setColor(slice.color);
            mPaint.setAntiAlias(true);
            mPaint.setDither(true);
            mPaint.setStyle(Paint.Style.STROKE);
            mPathMeasure.setPath(mPath, false);
            drawLinePath.reset();
            mPathMeasure.getSegment(0, mPathMeasure.getLength() * offLine, drawLinePath, true);
            mCanvas.drawPath(drawLinePath, mPaint);
            startRadius += slice.radius;

            if (textAlpha > 0) {
                mPaint.setTextSize(itemTextSize);
                mPaint.setStyle(Paint.Style.FILL);
                mPaint.setTextAlign(Paint.Align.CENTER);
                mPaint.setAlpha(textAlpha);
                mCanvas.drawText(slice.type, centerPoint.x + (endPoint.x - centerPoint.x) / 2,
                        centerPoint.y - textPadding, mPaint);
                mPaint.setTextSize(itemTextSize * 0.85f);
                mCanvas.drawText(slice.getPercent(), centerPoint.x + (endPoint.x - centerPoint.x) / 2,
                        centerPoint.y + (itemTextSize + textPadding) * 0.8f, mPaint);
            }
        }

        if (textAlpha == 1f) {
            sliceList.clear();
            leftTypeList.clear();
            rightTypeList.clear();
            itemPoints.clear();
        }
    }

    private void drawPie() {
        if (mCanvas == null) {
            return;
        }
        mCanvas.drawColor(backGroundColor);
        mPaint.setStyle(Paint.Style.FILL);
        int sum = 0;
        for (Slice slice : sliceList) {
            sum += slice.widget;
        }
        float a = 360f / sum;
        float startRadius = defaultStartAngle;
        float sumRadius = 0;
        leftTypeList.clear();
        rightTypeList.clear();
        itemPoints.clear();
        for (Slice slice : sliceList) {
            slice.radius = slice.widget * a;
            double al = 2 * Math.PI * ((startRadius + 90) / 360d);
            tempPoint.set((int) (width / 2 + radius * Math.sin(al)),
                    (int) (height / 2 - radius * Math.cos(al)));
            if (cell > 0) {
                if (startRadius == defaultStartAngle) {
                    firstPoint = tempPoint;
                }
            }

            double angle = 2 * Math.PI * ((startRadius + slice.radius / 2) / 360d);
            double sin = -Math.sin(angle);
            double cos = -Math.cos(angle);
            if (cos > 0) {
                leftTypeList.add(slice);
            } else {
                rightTypeList.add(slice);
            }
            sumRadius += Math.abs(slice.radius);
            mPaint.setStyle(Paint.Style.FILL);
            mPaint.setColor(slice.color);
            if (pieCell > 0) {
                if (sumRadius <= offRadius) {
                    tempRectF.set(pieRectF.left - (float) (pieCell * cos), pieRectF.top - (float) (pieCell * sin),
                            pieRectF.right - (float) (pieCell * cos), pieRectF.bottom - (float) (pieCell * sin));
                    mCanvas.drawArc(tempRectF, startRadius, slice.radius, true, mPaint);
                } else {
                    mCanvas.drawArc(tempRectF, startRadius, slice.radius - (Math.abs(offRadius - sumRadius)), true, mPaint);
                    break;
                }
            } else {
                if (sumRadius <= offRadius) {
                    mCanvas.drawArc(pieRectF, startRadius, slice.radius, true, mPaint);
                } else {
                    mCanvas.drawArc(pieRectF, startRadius, slice.radius - (Math.abs(offRadius - sumRadius)), true, mPaint);
                    break;
                }

            }
            startRadius += slice.radius;
            if (cell > 0 && pieCell == 0) {
                mPaint.setColor(backGroundColor);
                mPaint.setStrokeWidth(cell);
                mCanvas.drawLine(getWidth() / 2, getHeight() / 2, tempPoint.x, tempPoint.y, mPaint);
            }
        }
        if (cell > 0 && firstPoint != null && pieCell == 0) {
            mPaint.setColor(backGroundColor);
            mPaint.setStrokeWidth(cell);
            mCanvas.drawLine(getWidth() / 2, getHeight() / 2, firstPoint.x, firstPoint.y, mPaint);
        }
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(backGroundColor);
        if (innerRadius > 0 && pieCell == 0) {
            mCanvas.drawCircle(width / 2, height / 2, radius * innerRadius, mPaint);
        }
    }

    public void resetPaint() {
        mPaint.reset();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setAlpha(256);
    }

    public void clearData() {
        sliceList.clear();
    }

    public void addSector(Slice slice) {
        sliceList.add(slice);
    }

    /**
     * Set the gap between slices.
     */
    public void setCell(int cell) {
        this.cell = cell;
    }

    public void setInnerRadius(float innerRadius) {
        if (innerRadius > 1f) innerRadius = 1f;
        else if (innerRadius < 0) innerRadius = 0;

        this.innerRadius = innerRadius;
    }

    public void setBackGroundColor(int backGroundColor) {
        this.backGroundColor = backGroundColor;
    }

    public void setItemTextSize(int itemTextSize) {
        this.itemTextSize = itemTextSize;
    }

    /**
     * Sets vertical padding between the text and its horizontal line
     */
    public void setTextPadding(int textPadding) {
        this.textPadding = textPadding;
    }

    public void setAnimDuration(long animDuration) {
        this.animDuration = animDuration;
    }

    public static class Slice {

        private static final DecimalFormat formatter = new DecimalFormat("0.0%");
        String type;
        int widget;
        int color;
        float radius;

        public Slice(String type, int widget, int color) {
            this.type = type;
            this.widget = widget;
            this.color = color;
        }

        String getPercent() {
            return formatter.format(radius / 360f);
        }
    }
}
