package com.pleon.buyt.ui.activity

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
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
import com.pleon.buyt.util.animateAlpha
import com.pleon.buyt.util.animateIcon
import com.pleon.buyt.util.localizeDigits
import kotlinx.android.synthetic.main.activity_about.*
import org.jetbrains.anko.startActivity
import org.koin.android.ext.android.inject
import org.mindrot.jbcrypt.BCrypt

const val FLAG_START_UPGRADE = "com.pleon.buyt.flag.START_UPGRADE"
// TODO: for security, generate your payload here for verification.
//  Since this is a SAMPLE, we just use a random string, but on a production app
//  you should carefully generate this.
private const val PAYLOAD = "payload-string"
private const val RC_REQUEST: Int = 62026 // (arbitrary) request code for the purchase flow

class AboutActivity : BaseActivity() {

    private val iabHelper by inject<IabHelper>()
    private val subscriptionRepository by inject<SubscriptionRepository>()

    override fun layout() = R.layout.activity_about

    override fun onCreate(savedState: Bundle?) {
        super.onCreate(savedState)
        waveHeader.start()
        nameVersion.text = getString(R.string.appNameVersion, localizeDigits(BuildConfig.VERSION_NAME))
        logo.setOnClickListener { animateIcon(logo.drawable) }
        upgradePremiumBtn.setOnClickListener { upgradeToPremium() }
        intent.extras?.getBoolean(FLAG_START_UPGRADE)?.let { upgradeToPremium() }
    }

    override fun onStart() {
        super.onStart()
        animateBrandAndUpgradeButton()
    }

    private fun animateBrandAndUpgradeButton() {
        animateAlpha(logo, toAlpha = 1f, duration = 300, startDelay = 300)
        animateAlpha(nameVersion, toAlpha = 1f, duration = 300, startDelay = 500)
        if (!isPremium) {
            upgradePremiumBtn.visibility = android.view.View.VISIBLE
            animateAlpha(upgradePremiumBtn, toAlpha = 1f, duration = 300, startDelay = 1000)
        }
    }

    private fun upgradeToPremium() = try {
        iabHelper.flagEndAsync() // To prevent error when previous purchases abandoned
        iabHelper.launchPurchaseFlow(this, SKU_PREMIUM, RC_REQUEST, onPurchaseResult(), PAYLOAD)
    } catch (e: Exception) {
        BillingErrorDialogFragment().show(supportFragmentManager, "BILLING-DIALOG")
    }

    private fun onPurchaseResult() = { result: IabResult, info: Purchase ->
        if (result.isSuccess) {
            lifecycleScope.launchWhenStarted {
                isPremium = true
                val subscription = Subscription(BCrypt.hashpw("PREMIUM", BCrypt.gensalt()))
                subscriptionRepository.insertSubscription(subscription)
                UpgradeSuccessDialogFragment().show(supportFragmentManager, "UPG_DIALOG")
                animateAlpha(upgradePremiumBtn, toAlpha = 0f, duration = 200, startDelay = 300)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_bottom_about, menu)
        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (data != null && !iabHelper.handleActivityResult(requestCode, resultCode, data)) {
            // Not handled, so handle it ourselves (here's where you'd perform
            // any handling of activity results not related to in-app billing...
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_view_app_info -> MaterialAlertDialogBuilder(this)
                    .setView(R.layout.dialog_app_info)
                    .setPositiveButton(android.R.string.ok) { _, _ -> /* Dismiss */ }
                    .setCancelable(false)
                    .create()
                    .show()
            R.id.action_show_tutorial -> startActivity<IntroActivity>()
            android.R.id.home -> finish()
        }
        return true
    }
}
