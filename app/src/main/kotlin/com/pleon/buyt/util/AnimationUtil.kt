package com.pleon.buyt.util

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

/**
 * See this answer [https://stackoverflow.com/a/49431260/8583692] for why we are doing this!
 */
fun animateIconInfinitely(icon: Drawable, startDelay: Long = 0, repeatDelay: Long = 0) {
    registerAnimationCallback(icon, object : AnimationCallback() {
        private val handler = Handler(Looper.getMainLooper())
        override fun onAnimationEnd(icon: Drawable) {
            handler.post { animateIcon(icon, repeatDelay) }
        }
    })
    animateIcon(icon, startDelay)
}

fun animateIcon(icon: Drawable, startDelay: Long = 0) {
    fun start() = (icon as Animatable).start()
    if (startDelay <= 0) start() else Handler().postDelayed({ start() }, startDelay)
}

fun animateAlpha(view: View, toAlpha: Float, duration: Long = 200, startDelay: Long = 0) {
    view.animate().alpha(toAlpha)
            .setDuration(duration)
            .setStartDelay(startDelay)
            .setInterpolator(FastOutSlowInInterpolator())
            .withStartAction { if (toAlpha == 1f) view.visibility = VISIBLE }
            .withEndAction { if (toAlpha == 0f) view.visibility = GONE }
            .start()
}
