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
import com.pleon.buyt.BuildConfig
import com.pleon.buyt.R
import com.pleon.buyt.SKU_PREMIUM
import com.pleon.buyt.billing.IabHelper
import com.pleon.buyt.billing.IabResult
import com.pleon.buyt.billing.Purchase
import com.pleon.buyt.isPremium
import com.pleon.buyt.model.Subscription
import com.pleon.buyt.repository.SubscriptionRepository
import com.pleon.buyt.ui.dialog.BillingErrorDialogFragment
import com.pleon.buyt.ui.dialog.UpgradeSuccessDialogFragment
import kotlinx.android.synthetic.main.activity_help.*
import org.jetbrains.anko.startActivity
import org.koin.android.ext.android.inject
import org.mindrot.jbcrypt.BCrypt
import java.util.*

const val EXTRA_SHOULD_START_UPGRADE = "com.pleon.buyt.extra.SHOULD_START_UPGRADE"
private const val TRANSLATION_PAGE_URL = "https://pleonco.oneskyapp.com/collaboration/project?id=158739"

// (arbitrary) request code for the purchase flow
private const val RC_REQUEST: Int = 62026

class HelpActivity : BaseActivity() {

    private val iabHelper by inject<IabHelper>()
    private val subscriptionRepository by inject<SubscriptionRepository>()

    override fun layout() = R.layout.activity_help

    override fun onCreate(savedState: Bundle?) {
        super.onCreate(savedState)
        waveHeader.start()
        nameVersion.text = getString(R.string.appNameVersion, getLocalizedAppVersion())
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

    private fun getLocalizedAppVersion() = if (Locale.getDefault().language == "fa") {
        BuildConfig.VERSION_NAME
                .replace("alpha", "آلفا").replace("beta", "بتا")
                .replace('0', '۰').replace('1', '۱')
                .replace('2', '۲').replace('3', '۳')
                .replace('4', '۴').replace('5', '۵')
                .replace('6', '۶').replace('7', '۷')
                .replace('8', '۸').replace('9', '۹')
    } else {
        BuildConfig.VERSION_NAME
    }

    private fun startPurchase() {
        /* TODO: for security, generate your payload here for verification.
         *  Since this is a SAMPLE, we just use a random string, but on a production app
         *  you should carefully generate this. */
        iabHelper.launchPurchaseFlow(this, SKU_PREMIUM, RC_REQUEST,
                onPurchaseResult(), "payload-string")
    }

    private fun onPurchaseResult() = { result: IabResult, info: Purchase ->
        if (result.isSuccess) {
            isPremium = true
            val subscription = Subscription(BCrypt.hashpw("PREMIUM", BCrypt.gensalt()))
            subscriptionRepository.insertSubscription(subscription)
            UpgradeSuccessDialogFragment().show(supportFragmentManager, "UPG_DIALOG")
            Handler().postDelayed({ upgradePremiumBtn.visibility = GONE }, 300)
        }
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
            android.R.id.home -> finish()
            R.id.action_translate -> openTranslationPage()
            R.id.action_show_tutorial ->
                startActivity<IntroActivity>(EXTRA_LAUNCH_MAIN_ACTIVITY to false)
        }
        return true
    }

    private fun openTranslationPage() {
        val uri = Uri.parse(TRANSLATION_PAGE_URL)
        startActivity(Intent(Intent.ACTION_VIEW, uri))
    }
}
