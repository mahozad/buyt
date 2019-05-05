package com.pleon.buyt.ui.activity

import android.graphics.drawable.Animatable
import android.os.Bundle
import android.util.TypedValue
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.google.android.material.animation.ArgbEvaluatorCompat
import com.pleon.buyt.R
import com.pleon.buyt.ui.adapter.IntroPagerAdapter
import kotlinx.android.synthetic.main.activity_intro.*
import kotlin.math.max
import kotlin.math.min

class IntroActivity : BaseActivity() {

    var pagerLastPage = 0
    var pagerLastOffset = 0f
    lateinit var dots: Array<ImageView>

    override fun layout() = R.layout.activity_intro

    override fun onCreate(savedState: Bundle?) {
        super.onCreate(savedState)

        dots = arrayOf(dot1, dot2, dot3)
        val colors = resources.getIntArray(R.array.introColors)

        val adapter = IntroPagerAdapter(supportFragmentManager, lifecycle)
        viewPager.adapter = adapter

        viewPager.registerOnPageChangeCallback(object : OnPageChangeCallback() {
            private val argbEvaluator = ArgbEvaluatorCompat()

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

                val params = backButton.layoutParams as ConstraintLayout.LayoutParams
                val finalPos = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 84F, resources.displayMetrics).toInt()
                val margin = if (position < 1) (positionOffset * finalPos).toInt() else finalPos
                params.setMargins(params.leftMargin, params.topMargin, params.rightMargin, margin)
                backButton.layoutParams = params

                if (position <= viewPager.childCount && position < colors.size - 1) {
                    parentLayout.setBackgroundColor(argbEvaluator.evaluate(positionOffset, colors[position], colors[position + 1]) as Int)
                } else {
                    parentLayout.setBackgroundColor(colors[colors.size - 1])
                }

                if (positionOffset >= pagerLastOffset) {
                    dots[position].alpha = max(1 - positionOffset, 0.2f)
                    if (position < adapter.itemCount - 1) dots[position + 1].alpha = max(0.2f, positionOffset)
                } else {
                    dots[position].alpha = max(positionOffset, 0.2f)
                    if (position > 0) dots[position - 1].alpha = max(1 - positionOffset, 0.2f)
                }
            }

            override fun onPageSelected(position: Int) {
                if (position == adapter.itemCount - 1) {
                    nextButton.setImageResource(R.drawable.avd_next_done)
                    (nextButton.drawable as Animatable).start()
                } else if (position == adapter.itemCount - 2 && pagerLastPage == adapter.itemCount - 1) {
                    nextButton.setImageResource(R.drawable.avd_done_next)
                    (nextButton.drawable as Animatable).start()
                }
                pagerLastPage = position
            }
        })

        backButton.setOnClickListener { viewPager.currentItem = viewPager.currentItem - 1 }
        nextButton.setOnClickListener {
            if (viewPager.currentItem == adapter.itemCount - 1) finish()
            viewPager.currentItem = min(viewPager.currentItem + 1, adapter.itemCount - 1)
        }

        for ((index, dot) in dots.withIndex()) {
            dot.setOnClickListener { viewPager.currentItem = index }
        }
    }

    // override fun onBackPressed() {}
}
