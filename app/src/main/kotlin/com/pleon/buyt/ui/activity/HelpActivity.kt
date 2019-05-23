package com.pleon.buyt.ui.activity

import android.content.Intent
import android.graphics.drawable.Animatable
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.view.Menu
import android.view.MenuItem
import com.pleon.buyt.R
import kotlinx.android.synthetic.main.activity_help.*

private const val TRANSLATION_PAGE_URL = "https://pleonco.oneskyapp.com/collaboration/project?id=158739"

class HelpActivity : BaseActivity() {

    override fun layout() = R.layout.activity_help

    override fun onCreate(savedState: Bundle?) {
        super.onCreate(savedState)
        waveHeader.start()
        animateBrand()
        logo.setOnClickListener {
            logo.setImageResource(R.drawable.avd_logo)
            (logo.drawable as Animatable).start()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_bottom_help, menu)
        return true
    }

    private fun animateBrand() {
        Handler().postDelayed({ (logo.drawable as Animatable).start() }, 300)
        Handler().postDelayed({ nameVersion.animate().alpha(1f).duration = 300 }, 500)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_show_tutorial -> showTutorial()
            R.id.action_translate -> openTranslationPage()
            android.R.id.home -> finish()
        }
        return true
    }

    private fun showTutorial() = startActivity(Intent(this, IntroActivity::class.java))

    private fun openTranslationPage() {
        val uri = Uri.parse(TRANSLATION_PAGE_URL)
        startActivity(Intent(Intent.ACTION_VIEW, uri))
    }
}
