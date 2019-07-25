package com.pleon.buyt.ui.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
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
import com.pleon.buyt.util.AnimationUtil.animateAlpha
import com.pleon.buyt.util.AnimationUtil.animateIcon
import com.pleon.buyt.util.TextUtil.localizeDigits
import kotlinx.android.synthetic.main.activity_help.*
import org.jetbrains.anko.startActivity
import org.koin.android.ext.android.inject
import org.mindrot.jbcrypt.BCrypt

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
        nameVersion.text = getString(R.string.appNameVersion, localizeDigits(BuildConfig.VERSION_NAME))
        logo.setOnClickListener { animateIcon(logo.drawable) }
        upgradePremiumBtn.setOnClickListener { upgradeToPremium() }
        animateViews()
        if (intent.getBooleanExtra(EXTRA_SHOULD_START_UPGRADE, false)) upgradeToPremium()
    }

    private fun animateViews() {
        animateIcon(logo.drawable, startDelay = 300)
        animateAlpha(nameVersion, toAlpha = 1f, duration = 300, startDelay = 500)
        if (!isPremium) animateAlpha(upgradePremiumBtn, toAlpha = 1f, duration = 300, startDelay = 1000)
    }

    private fun upgradeToPremium() = try {
        iabHelper.flagEndAsync() // To prevent error when previous purchases abandoned
        startPurchase()
    } catch (e: Exception) {
        BillingErrorDialogFragment().show(supportFragmentManager, "BILLING-DIALOG")
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
            animateAlpha(upgradePremiumBtn, 0f, duration = 200, startDelay = 300)
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
