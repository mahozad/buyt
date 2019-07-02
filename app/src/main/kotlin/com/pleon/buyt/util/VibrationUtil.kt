package com.pleon.buyt.util

import android.app.Application
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.core.content.ContextCompat.getSystemService
import javax.inject.Inject

class VibrationUtil @Inject constructor(private val app: Application) {

    fun vibrate(duration: Long, intensity: Int) {
        val vibrator = getSystemService(app, Vibrator::class.java) as Vibrator
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(duration, intensity))
        } else {
            vibrator.vibrate(200)
        }
    }
}
