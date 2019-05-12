package com.pleon.buyt.ui.activity

import android.graphics.Color
import android.graphics.drawable.Animatable
import android.os.Bundle
import android.util.TypedValue.COMPLEX_UNIT_DIP
import android.util.TypedValue.applyDimension
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.graphics.ColorUtils
import androidx.preference.PreferenceManager.getDefaultSharedPreferences
import androidx.viewpager2.adapter.FragmentStateAdapter
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

    private lateinit var adapter: FragmentStateAdapter
    private lateinit var colors: IntArray
    private lateinit var dots: Array<ImageView>
    private val argbEvaluator = ArgbEvaluatorCompat()
    private var lastPage = 0
    private var lastOffset = 0f

    override fun layout() = R.layout.activity_intro

    override fun onCreate(savedState: Bundle?) {
        super.onCreate(savedState)

        adapter = IntroPageAdapter(this).also { viewPager.adapter = it }
        colors = resources.getIntArray(R.array.introPageColors)
        dots = arrayOf(dot1, dot2, dot3)

        viewPager.offscreenPageLimit = 2 // to cache pages for smooth scrolling
        setupParallaxEffect()
        for ((i, dot) in dots.withIndex()) dot.setOnClickListener { viewPager.currentItem = i }

        viewPager.registerOnPageChangeCallback(object : OnPageChangeCallback() {

            override fun onPageScrolled(position: Int, offset: Float, offsetPx: Int) {
                updatePageAndStatusBarColor(position, offset)
                updateBackButtonPlacement(position, offset)
                updateDots(position, offset)
            }

            override fun onPageSelected(position: Int) {
                animateNextButtonIconIfNeeded(position)
                lastPage = position // should be the last statement
            }
        })

        backButton.setOnClickListener { viewPager.currentItem = viewPager.currentItem - 1 }
        nextButton.setOnClickListener {
            if (viewPager.currentItem == adapter.itemCount - 1) {
                getDefaultSharedPreferences(this).edit().putBoolean(PREF_NEWBIE, false).apply()
                finish()
            }
            viewPager.currentItem = min(viewPager.currentItem + 1, adapter.itemCount - 1)
        }
    }

    override fun onBackPressed() {}

    private fun setupParallaxEffect() {
        viewPager.setPageTransformer { page, pos ->
            if (pos >= -1 && pos <= 1) page.img.translationX = -pos * page.width / 2
            else page.alpha = 1.0f
        }
    }

    private fun updatePageAndStatusBarColor(position: Int, offset: Float) {
        val startColor = colors[position]
        val endColor = colors[min(position + 1, adapter.itemCount - 1)]
        val color = argbEvaluator.evaluate(offset, startColor, endColor)
        parentLayout.setBackgroundColor(color)
        window.statusBarColor = ColorUtils.blendARGB(color, Color.BLACK, 0.3f)
    }

    private fun updateBackButtonPlacement(position: Int, offset: Float) {
        val params = backButton.layoutParams as ConstraintLayout.LayoutParams
        val finalPos = applyDimension(COMPLEX_UNIT_DIP, 84F, resources.displayMetrics).toInt()
        val bottomMargin = if (position < 1) (offset * finalPos).toInt() else finalPos
        params.setMargins(params.leftMargin, params.topMargin, params.rightMargin, bottomMargin)
        backButton.layoutParams = params
    }

    private fun updateDots(position: Int, offset: Float) {
        if (offset >= lastOffset) {
            dots[position].alpha = max(1 - offset, 0.2f)
            if (position < adapter.itemCount - 1) dots[position + 1].alpha = max(0.2f, offset)
        } else {
            dots[position].alpha = max(offset, 0.2f)
            if (position > 0) dots[position - 1].alpha = max(1 - offset, 0.2f)
        }
    }

    private fun animateNextButtonIconIfNeeded(position: Int) {
        if (position == adapter.itemCount - 1) {
            nextButton.setImageResource(R.drawable.avd_next_done)
            (nextButton.drawable as Animatable).start()
        } else if (position < adapter.itemCount - 1 && lastPage == adapter.itemCount - 1) {
            nextButton.setImageResource(R.drawable.avd_done_next)
            (nextButton.drawable as Animatable).start()
        }
    }
}
