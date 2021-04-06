package com.pleon.buyt.ui.activity

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import androidx.core.content.ContextCompat.getColor
import androidx.core.graphics.ColorUtils
import com.pleon.buyt.R
import com.pleon.buyt.ui.fragment.PREF_FIRST_TIME_RUN
import com.pleon.buyt.util.animateIcon
import kotlinx.android.synthetic.main.activity_help.logo
import kotlinx.android.synthetic.main.activity_splash.*

class SplashActivity : BaseActivity() {

    override fun layout() = R.layout.activity_splash

    override fun onCreate(savedState: Bundle?) {
        super.onCreate(savedState)
        val screenBgColor = getColor(this, R.color.splashScreenBgColor)
        window.statusBarColor = ColorUtils.blendARGB(screenBgColor, Color.BLACK, 0.3f)
        animateIcon(logo.drawable)
        animateIcon(ripple.drawable, 200)
        scheduleProperActivity()
    }

    private fun scheduleProperActivity() {
        Handler().postDelayed({
            val isFirstTimeRun = prefs.getBoolean(PREF_FIRST_TIME_RUN, true)
            val activity = if (isFirstTimeRun) IntroActivity::class.java else MainActivity::class.java
            startActivity(Intent(this, activity).putExtra(FLAG_START_MAIN, true))
            finish()
        }, 1200)
    }

    override fun onBackPressed() { /* Do nothing */ }
}
