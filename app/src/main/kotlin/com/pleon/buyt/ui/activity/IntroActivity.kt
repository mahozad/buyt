package com.pleon.buyt.ui.activity

import android.graphics.drawable.Animatable
import android.os.Bundle
import android.util.TypedValue
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.davidmedenjak.pagerindicator.ViewPager2PagerListener
import com.google.android.material.animation.ArgbEvaluatorCompat
import com.pleon.buyt.R
import com.pleon.buyt.ui.adapter.IntroPagerAdapter
import kotlinx.android.synthetic.main.activity_intro.*
import kotlin.math.min

class IntroActivity : BaseActivity() {

    var pagerLastPage = 0

    override fun layout() = R.layout.activity_intro

    override fun onCreate(savedState: Bundle?) {
        super.onCreate(savedState)

        val colors = intArrayOf(
                resources.getColor(R.color.bottomBarDarkBgColor),
                resources.getColor(R.color.chartEmptyColor),
                resources.getColor(R.color.colorAccent)
        )

        val adapter = IntroPagerAdapter(supportFragmentManager, lifecycle)
        viewPager.adapter = adapter
        indicator.setPager(ViewPager2PagerListener(viewPager))

        viewPager.registerOnPageChangeCallback(object : OnPageChangeCallback() {
            private val argbEvaluator = ArgbEvaluatorCompat()

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

                val params = backButton.layoutParams as ConstraintLayout.LayoutParams
                val finalPos = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 84F, resources.displayMetrics).toInt()
                val margin = if (position < 1) (positionOffset * finalPos).toInt() else finalPos
                params.setMargins(params.leftMargin, params.topMargin, params.rightMargin, margin)
                backButton.layoutParams = params

                if (position <= viewPager.childCount && position < colors.size - 1) {
                    viewPager.setBackgroundColor(argbEvaluator.evaluate(positionOffset, colors[position], colors[position + 1]) as Int)
                } else {
                    viewPager.setBackgroundColor(colors[colors.size - 1])
                }
            }

            override fun onPageSelected(position: Int) {
                if (position == adapter.itemCount - 1) {
                    nextButtonIcon.setImageResource(R.drawable.avd_next_done)
                    (nextButtonIcon.drawable as Animatable).start()
                } else if (position == adapter.itemCount - 2 && pagerLastPage == adapter.itemCount - 1) {
                    nextButtonIcon.setImageResource(R.drawable.avd_done_next)
                    (nextButtonIcon.drawable as Animatable).start()
                }
                pagerLastPage = position
            }
        })


        backButton.setOnClickListener { viewPager.currentItem = viewPager.currentItem - 1 }
        nextButtonIcon.setOnClickListener {
            viewPager.currentItem = min(viewPager.currentItem + 1, adapter.itemCount - 1)
        }


        /* addSlide(SimpleSlide.Builder()
                .title(R.string.dialog_title_select_lang)
                .description("dyfghjlgkk;jbhkl")
                .image(R.drawable.ic_sigma)
                .background(R.color.colorPrimary)
                .backgroundDark(R.color.colorPrimaryDark)
                .scrollable(false)
                // .permission(Manifest.permission.CAMERA)
                .buttonCtaLabel("111") // label for permission button
                .build())
        */
    }

//    override fun onBackPressed() {}
}
