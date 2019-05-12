package com.pleon.buyt.ui.activity

import android.graphics.Color
import android.graphics.drawable.Animatable
import android.os.Bundle
import android.util.TypedValue
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.graphics.ColorUtils
import androidx.preference.PreferenceManager.getDefaultSharedPreferences
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.google.android.material.animation.ArgbEvaluatorCompat
import com.pleon.buyt.R
import com.pleon.buyt.ui.adapter.IntroPageAdapter
import com.pleon.buyt.ui.fragment.PREF_NEWBIE
import kotlinx.android.synthetic.main.activity_intro.*
import kotlinx.android.synthetic.main.fragment_intro_1.view.*
import kotlin.math.max
import kotlin.math.min

class IntroActivity : BaseActivity() {

    var lastPage = 0
    var lastOffset = 0f
    lateinit var dots: Array<ImageView>

    override fun layout() = R.layout.activity_intro

    override fun onCreate(savedState: Bundle?) {
        super.onCreate(savedState)

        dots = arrayOf(dot1, dot2, dot3)
        val colors = resources.getIntArray(R.array.introPageColors)

        val adapter = IntroPageAdapter(this)
        viewPager.adapter = adapter
        viewPager.offscreenPageLimit = 2 // to cache pages for smooth scrolling

        viewPager.registerOnPageChangeCallback(object : OnPageChangeCallback() {
            private val argbEvaluator = ArgbEvaluatorCompat()

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

                val params = backButton.layoutParams as ConstraintLayout.LayoutParams
                val finalPos = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 84F, resources.displayMetrics).toInt()
                val margin = if (position < 1) (positionOffset * finalPos).toInt() else finalPos
                params.setMargins(params.leftMargin, params.topMargin, params.rightMargin, margin)
                backButton.layoutParams = params

                val color = argbEvaluator.evaluate(positionOffset, colors[position], colors[min(position + 1, adapter.itemCount - 1)])
                parentLayout.setBackgroundColor(color)
                window.statusBarColor = ColorUtils.blendARGB(color, Color.BLACK, 0.3f)

                if (positionOffset >= lastOffset) {
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
                } else if (position < adapter.itemCount - 1 && lastPage == adapter.itemCount - 1) {
                    nextButton.setImageResource(R.drawable.avd_done_next)
                    (nextButton.drawable as Animatable).start()
                }
                lastPage = position
            }
        })

        // For parallax effect
        viewPager.setPageTransformer { page, pos ->
            if (pos >= -1 && pos <= 1) page.img.translationX = -pos * page.width / 2
            else page.alpha = 1f
        }

        backButton.setOnClickListener { viewPager.currentItem = viewPager.currentItem - 1 }
        nextButton.setOnClickListener {
            if (viewPager.currentItem == adapter.itemCount - 1) {
                getDefaultSharedPreferences(this).edit().putBoolean(PREF_NEWBIE, false).apply()
                finish()
            }
            viewPager.currentItem = min(viewPager.currentItem + 1, adapter.itemCount - 1)
        }

        for ((index, dot) in dots.withIndex()) {
            dot.setOnClickListener { viewPager.currentItem = index }
        }
    }

    override fun onBackPressed() {/* Do nothing */
    }
}
