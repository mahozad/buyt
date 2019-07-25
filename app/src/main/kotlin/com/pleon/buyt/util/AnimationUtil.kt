package com.pleon.buyt.util

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.graphics.drawable.Animatable
import android.graphics.drawable.Drawable
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.vectordrawable.graphics.drawable.Animatable2Compat.AnimationCallback
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat.registerAnimationCallback

object AnimationUtil {

    /**
     * See this answer [https://stackoverflow.com/a/49431260/8583692] for why we are doing this!
     */
    fun animateIconInfinitely(icon: Drawable) {
        registerAnimationCallback(icon, object : AnimationCallback() {
            private val handler = Handler(Looper.getMainLooper())
            override fun onAnimationEnd(icon: Drawable) {
                handler.post { animateIcon(icon) }
            }
        })
        animateIcon(icon)
    }

    fun animateIcon(icon: Drawable, startDelay: Long = 0) {
        if (startDelay > 0) Handler().postDelayed({ (icon as Animatable).start() }, startDelay)
        else (icon as Animatable).start()
    }

    fun animateAlpha(view: View, toAlpha: Float, duration: Long = 200, startDelay: Long = 0) {
        if (toAlpha == 1f) view.visibility = VISIBLE
        view.animate()
                .alpha(toAlpha)
                .setInterpolator(FastOutSlowInInterpolator())
                .setDuration(duration)
                .setStartDelay(startDelay)
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator?) {
                        if (toAlpha == 0f) view.visibility = GONE
                    }
                })
                .start()
    }
}
