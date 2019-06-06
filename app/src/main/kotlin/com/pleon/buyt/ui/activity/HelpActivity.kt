package com.pleon.buyt.ui.activity

import android.content.Intent
import android.graphics.drawable.Animatable
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.view.Menu
import android.view.MenuItem
import android.view.View.GONE
import android.view.View.VISIBLE
import com.pleon.buyt.R
import com.pleon.buyt.SKU_PREMIUM
import com.pleon.buyt.billing.IabHelper
import com.pleon.buyt.isPremium
import com.pleon.buyt.repository.SubscriptionRepository
import com.pleon.buyt.ui.dialog.BillingErrorDialogFragment
import kotlinx.android.synthetic.main.activity_help.*
import javax.inject.Inject

const val EXTRA_SHOULD_START_UPGRADE = "com.pleon.buyt.extra.SHOULD_START_UPGRADE"
private const val TRANSLATION_PAGE_URL = "https://pleonco.oneskyapp.com/collaboration/project?id=158739"

// (arbitrary) request code for the purchase flow
private const val RC_REQUEST: Int = 62026

class HelpActivity : BaseActivity() {

    @Inject internal lateinit var iabHelper: IabHelper
    @Inject internal lateinit var subscriptionRepository: SubscriptionRepository

    override fun layout() = R.layout.activity_help

    override fun onCreate(savedState: Bundle?) {
        super.onCreate(savedState)
        waveHeader.start()
        animateViews()
        logo.setOnClickListener {
            logo.setImageResource(R.drawable.avd_logo)
            (logo.drawable as Animatable).start()
        }

        // TODO: Encrypt premium attribute in preferences to prevent the user from hacking it.
        //  See [https://developer.android.com/jetpack/androidx/releases/security]
        //  and [https://developer.android.com/topic/security/data]

        upgradePremiumBtn.visibility = if (isPremium) GONE else VISIBLE
        upgradePremiumBtn.setOnClickListener {
            iabHelper.flagEndAsync() // To prevent error when previous purchases abandoned
            try {
                startPurchase()
            } catch (e: Exception) {
                BillingErrorDialogFragment().show(supportFragmentManager, "BILLING-DIALOG")
            }
        }

        // performClick() does not work if the click listener has not been set
        if (intent.getBooleanExtra(EXTRA_SHOULD_START_UPGRADE, false))
            Handler().postDelayed({ upgradePremiumBtn.performClick() }, 500)
    }

    private fun startPurchase() {
        /* TODO: for security, generate your payload here for verification.
         *  Since this is a SAMPLE, we just use a random string, but on a production app
         *  you should carefully generate this. */
        iabHelper.launchPurchaseFlow(
                this,
                SKU_PREMIUM,
                RC_REQUEST,
                { result, _ ->
                    if (result.isSuccess) {
                        subscriptionRepository.insertSubscription()
                        isPremium = true // Upgrade the app to premium
                        upgradePremiumBtn.visibility = GONE
                    }
                },
                "payload-string"
        )
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_bottom_help, menu)
        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        // Pass on the activity result to the billing helper for handling
        if (!iabHelper.handleActivityResult(requestCode, resultCode, data)) {
            // Not handled, so handle it ourselves (here's where you'd perform any handling of
            // activity results not related to in-app billing...
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun animateViews() {
        Handler().postDelayed({ (logo.drawable as Animatable).start() }, 300)
        Handler().postDelayed({ nameVersion.animate().alpha(1f).duration = 300 }, 500)
        Handler().postDelayed({ upgradePremiumBtn.animate().alpha(1f).duration = 300 }, 1000)
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
