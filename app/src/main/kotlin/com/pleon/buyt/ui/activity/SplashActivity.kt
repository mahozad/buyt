package com.pleon.buyt.ui.activity

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import com.pleon.buyt.R
import com.pleon.buyt.ui.fragment.PREF_FIRST_TIME_RUN
import com.pleon.buyt.util.animateIcon
import kotlinx.android.synthetic.main.activity_help.*

class SplashActivity : BaseActivity() {

    override fun layout() = R.layout.activity_splash

    override fun onCreate(savedState: Bundle?) {
        super.onCreate(savedState)
        animateIcon(logo.drawable)
        scheduleProperActivity()
    }

    private fun scheduleProperActivity() {
        Handler().postDelayed({
            val isFirstTimeRun = prefs.getBoolean(PREF_FIRST_TIME_RUN, true)
            val activity = if (isFirstTimeRun) IntroActivity::class.java else MainActivity::class.java
            startActivity(Intent(this, activity).putExtra(FLAG_START_MAIN, true))
            finish()
        }, 900)
    }

    override fun onBackPressed() { /* Do nothing */ }
}
