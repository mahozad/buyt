package com.pleon.buyt.ui.activity

import android.graphics.Color
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout.LayoutParams
import android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.graphics.ColorUtils.blendARGB
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.google.android.material.animation.ArgbEvaluatorCompat
import com.pleon.buyt.R
import com.pleon.buyt.ui.adapter.IntroPageAdapter
import com.pleon.buyt.ui.fragment.PREF_FIRST_TIME_RUN
import com.pleon.buyt.util.animateIcon
import kotlinx.android.synthetic.main.activity_intro.*
import kotlinx.android.synthetic.main.fragment_intro_1.view.*
import org.jetbrains.anko.dimen
import org.jetbrains.anko.dip
import org.jetbrains.anko.startActivity
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf
import java.util.*
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

const val FLAG_START_MAIN = "com.pleon.buyt.flag.LAUNCH_MAIN_ACTIVITY"

class IntroActivity : BaseActivity() {

    private val argbEvaluator by inject<ArgbEvaluatorCompat>()
    private val adapter by inject<IntroPageAdapter> {
        parametersOf(this@IntroActivity)
    }
    private lateinit var indicators: Array<ImageView>
    private lateinit var pageColors: IntArray
    private var lastPage = 0

    override fun layout() = R.layout.activity_intro

    override fun onCreate(savedState: Bundle?) {
        super.onCreate(savedState)
        pageColors = resources.getIntArray(R.array.introPageColors)
        setupViewPager()
        createIndicators()
        setupParallaxEffect()
        backButton.setOnClickListener { viewPager.currentItem-- }
        nextButton.setOnClickListener { if (isLastPage()) finishIntro() else viewPager.currentItem++ }
    }

    private fun setupViewPager() {
        viewPager.adapter = adapter
        viewPager.registerOnPageChangeCallback(object : OnPageChangeCallback() {
            override fun onPageScrolled(pos: Int, off: Float, offPx: Int) = onPageScroll(pos, off)
            override fun onPageSelected(pos: Int) = onPageSelect(pos)
        })
    }

    private fun onPageScroll(position: Int, offset: Float) {
        updatePageAndStatusBarColor(position, offset)
        updateBackButtonPlacement(position, offset)
        updateIndicators(position, offset)
    }

    private fun onPageSelect(position: Int) {
        animateNextButtonIconIfNeeded(position)
        lastPage = position // should be the last statement
    }

    private fun isLastPage() = viewPager.currentItem == adapter.itemCount - 1

    private fun finishIntro() {
        prefs.edit().putBoolean(PREF_FIRST_TIME_RUN, false).apply()
        intent.extras?.getBoolean(FLAG_START_MAIN)?.let { startActivity<MainActivity>() }
        finish()
    }

    private fun createIndicators() {
        indicators = Array(adapter.itemCount) {
            val indicator = ImageView(this).apply { alpha = 0.2f }
            indicator.setImageResource(R.drawable.shape_page_indicator)
            indicator.layoutParams = LayoutParams(WRAP_CONTENT, WRAP_CONTENT).apply {
                setMargins(dip(8), dip(8), dip(8), dip(8))
            }
            indicatorContainer.addView(indicator)
            return@Array indicator
        }
    }

    private fun setupParallaxEffect() = viewPager.setPageTransformer { page, position ->
        val adjustedPosition = if (Locale.getDefault().language == "fa") -position else position
        val translateX = adjustedPosition * page.width / 2
        if (abs(position) <= 1) page.img.translationX = translateX
    }

    private fun updatePageAndStatusBarColor(position: Int, offset: Float) {
        val startColor = pageColors[position]
        val endColor = pageColors[min(position + 1, adapter.itemCount - 1)]
        val color = argbEvaluator.evaluate(offset, startColor, endColor)
        parentLayout.setBackgroundColor(color)
        window.statusBarColor = blendARGB(color, Color.BLACK, 0.3f)
    }

    private fun updateBackButtonPlacement(position: Int, offset: Float) {
        val params = backButton.layoutParams as ConstraintLayout.LayoutParams
        val finalPos = dimen(R.dimen.intro_btn_margin_bottom)
        val bottomMargin = if (position < 1) (offset * finalPos).toInt() else finalPos
        params.setMargins(params.leftMargin, params.topMargin, params.rightMargin, bottomMargin)
        backButton.layoutParams = params
    }

    private fun updateIndicators(position: Int, offset: Float) {
        indicators[position].alpha = max(1 - offset, 0.2f)
        if (position < adapter.itemCount - 1) indicators[position + 1].alpha = max(0.2f, offset)
    }

    private fun animateNextButtonIconIfNeeded(position: Int) {
        if (position == adapter.itemCount - 1) {
            nextButton.setImageResource(R.drawable.avd_next_done)
            animateIcon(nextButton.drawable)
        } else if (position < adapter.itemCount - 1 && lastPage == adapter.itemCount - 1) {
            nextButton.setImageResource(R.drawable.avd_done_next)
            animateIcon(nextButton.drawable)
        }
    }
}
