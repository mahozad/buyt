package com.pleon.buyt

import android.content.Context
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import com.pleon.buyt.BuildConfig.APPLICATION_ID
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

private const val LAUNCH_TIMEOUT = 5000L

class BuytApplicationUITest {

    private lateinit var device: UiDevice

    @BeforeEach
    fun setUp() {
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        device.pressHome()
        waitForLauncher()
    }

    private fun waitForLauncher() {
        val launcherPackage = device.launcherPackageName
        val launcherObject = By.pkg(launcherPackage).depth(0)
        device.wait(Until.hasObject(launcherObject), LAUNCH_TIMEOUT)
    }

    @Test fun appShouldLaunchSuccessfully() {
        // Launch the app
        val context = ApplicationProvider.getApplicationContext<Context>()
        val intent = context.packageManager.getLaunchIntentForPackage(APPLICATION_ID)
        intent?.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK) // Clear any previous instances
        context.startActivity(intent)

        // Wait for the app to appear
        val appObject = By.pkg(APPLICATION_ID).depth(0)
        device.wait(Until.hasObject(appObject), LAUNCH_TIMEOUT)
    }
}
