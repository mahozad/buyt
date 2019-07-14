package com.pleon.buyt.util

import android.content.Context
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.O
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.core.content.ContextCompat.getSystemService

object VibrationUtil {

    fun vibrate(cxt: Context, duration: Long, intensity: Int) {
        val vibrator = getSystemService(cxt, Vibrator::class.java) as Vibrator
        if (SDK_INT >= O) vibrator.vibrate(VibrationEffect.createOneShot(duration, intensity))
        else vibrator.vibrate(200)
    }
}
