package com.pleon.buyt.ui.activity

import android.content.Intent
import android.content.Intent.ACTION_VIEW
import android.net.Uri.parse
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

const val FLAG_START_UPGRADE = "com.pleon.buyt.flag.START_UPGRADE"
private const val TRANSLATION_PAGE_URL = "https://pleonco.oneskyapp.com/collaboration/project?id=158739"
// TODO: for security, generate your payload here for verification.
//  Since this is a SAMPLE, we just use a random string, but on a production app
//  you should carefully generate this.
private const val PAYLOAD = "payload-string"
private const val RC_REQUEST: Int = 62026 // (arbitrary) request code for the purchase flow

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
        animateBrandAndUpgradeButton()
        intent.extras?.let { /* extras is not null */ upgradeToPremium() }
    }

    private fun animateBrandAndUpgradeButton() {
        animateIcon(logo.drawable, startDelay = 300)
        animateAlpha(nameVersion, toAlpha = 1f, duration = 300, startDelay = 500)
        if (!isPremium) animateAlpha(upgradePremiumBtn, toAlpha = 1f, duration = 300, startDelay = 1000)
    }

    private fun upgradeToPremium() = try {
        iabHelper.flagEndAsync() // To prevent error when previous purchases abandoned
        iabHelper.launchPurchaseFlow(this, SKU_PREMIUM, RC_REQUEST, onPurchaseResult(), PAYLOAD)
    } catch (e: Exception) {
        BillingErrorDialogFragment().show(supportFragmentManager, "BILLING-DIALOG")
    }

    private fun onPurchaseResult() = { result: IabResult, info: Purchase ->
        if (result.isSuccess) {
            isPremium = true
            val subscription = Subscription(BCrypt.hashpw("PREMIUM", BCrypt.gensalt()))
            subscriptionRepository.insertSubscription(subscription)
            UpgradeSuccessDialogFragment().show(supportFragmentManager, "UPG_DIALOG")
            animateAlpha(upgradePremiumBtn, toAlpha = 0f, duration = 200, startDelay = 300)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_bottom_help, menu)
        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (!iabHelper.handleActivityResult(requestCode, resultCode, data)) {
            // Not handled, so handle it ourselves (here's where you'd perform
            // any handling of activity results not related to in-app billing...
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_translate -> startActivity(Intent(ACTION_VIEW, parse(TRANSLATION_PAGE_URL)))
            R.id.action_show_tutorial -> startActivity<IntroActivity>()
            android.R.id.home -> finish()
        }
        return true
    }
}
