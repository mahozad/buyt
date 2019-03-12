package com.pleon.buyt.ui.activity

import android.graphics.drawable.Animatable
import android.os.Bundle
import android.os.Handler
import android.view.Menu
import android.view.MenuItem
import android.view.animation.AlphaAnimation
import kotlinx.android.synthetic.main.activity_help.*

class HelpActivity : BaseActivity() {

    override fun layoutResource() = com.pleon.buyt.R.layout.activity_help

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        logo.setOnClickListener {
            logo.setImageResource(com.pleon.buyt.R.drawable.avd_logo)
            (logo.drawable as Animatable).start()
        }
        waveHeader.start()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(com.pleon.buyt.R.menu.menu_bottom_help, menu)
        return true
    }

    override fun onResume() {
        super.onResume()
        Handler().postDelayed({ (logo.drawable as Animatable).start() }, 300)
        Handler().postDelayed({
            textView10.startAnimation(AlphaAnimation(0F, 1F).apply {
                duration = 300
                textView10.alpha = 1F
            })
        }, 400)
        Handler().postDelayed({
            textView11.startAnimation(AlphaAnimation(0F, 1F).apply {
                duration = 300
                textView11.alpha = 1F
            })
        }, 800)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()
        }
        return true
    }
}