package com.pleon.buyt.ui.activity

import android.content.Intent
import android.graphics.drawable.Animatable
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.view.Menu
import android.view.MenuItem
import android.view.animation.AlphaAnimation
import kotlinx.android.synthetic.main.activity_help.*

class HelpActivity : BaseActivity() {

    override fun layout() = com.pleon.buyt.R.layout.activity_help

    override fun onCreate(savedState: Bundle?) {
        super.onCreate(savedState)
        waveHeader.start()
        animateBrand()
        logo.setOnClickListener {
            logo.setImageResource(com.pleon.buyt.R.drawable.avd_logo)
            (logo.drawable as Animatable).start()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(com.pleon.buyt.R.menu.menu_bottom_help, menu)
        return true
    }

    private fun animateBrand() {
        Handler().postDelayed({ (logo.drawable as Animatable).start() }, 300)
        Handler().postDelayed({
            nameVersion.startAnimation(AlphaAnimation(0F, 1F).apply {
                duration = 300
                nameVersion.alpha = 1F
            })
        }, 500)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()
            com.pleon.buyt.R.id.action_translate -> {
                val uri = Uri.parse("https://pleonco.oneskyapp.com/collaboration/project?id=158739")
                startActivity(Intent(Intent.ACTION_VIEW, uri))
            }
        }
        return true
    }
}
