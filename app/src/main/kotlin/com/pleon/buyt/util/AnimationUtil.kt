package com.pleon.buyt.util

import android.graphics.drawable.Animatable
import android.graphics.drawable.Drawable
import android.os.Handler
import android.os.Looper
import android.view.View
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
                handler.post { (icon as Animatable).start() }
            }
        })

        (icon as Animatable).start()
    }

    fun animateAlpha(view: View, alphaValue: Float, duration: Long = 200) {
        view.animate()
                .alpha(alphaValue)
                .setInterpolator(FastOutSlowInInterpolator())
                .setDuration(duration)
                .start()
    }
}
