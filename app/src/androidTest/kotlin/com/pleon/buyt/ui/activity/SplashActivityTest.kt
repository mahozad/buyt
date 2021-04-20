package com.pleon.buyt.ui.activity

import androidx.lifecycle.Lifecycle.State.RESUMED
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.uiautomator.UiDevice
import com.pleon.buyt.ui.fragment.PREF_FIRST_TIME_RUN
import com.pleon.buyt.waitFor
import de.mannodermaus.junit5.ActivityScenarioExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

class SplashActivityTest {

    @JvmField
    @RegisterExtension
    val scenarioExtension = ActivityScenarioExtension.launch<SplashActivity>()
    private lateinit var device: UiDevice

    @BeforeEach fun setUp() {
        Intents.init()
    }

    @AfterEach fun tearDown() {
        Intents.release()
    }

    @Test fun activityMustBeLaunched(scenario: ActivityScenario<SplashActivity>) {
        assertThat(scenario.state).isEqualTo(RESUMED)
    }

    @Test fun shouldLaunchIntroActivityForFirstTimeUser(scenario: ActivityScenario<SplashActivity>) {
        scenario.onActivity {
            it.prefs.edit().putBoolean(PREF_FIRST_TIME_RUN, true).apply()
        }
        onView(isRoot()).perform(waitFor(2000))
        intended(hasComponent(IntroActivity::class.qualifiedName))
    }

    @Test fun shouldLaunchMainActivityForNonFirstTimeUser(scenario: ActivityScenario<SplashActivity>) {
        scenario.onActivity {
            it.prefs.edit().putBoolean(PREF_FIRST_TIME_RUN, false).apply()
        }
        onView(isRoot()).perform(waitFor(2000))
        intended(hasComponent(MainActivity::class.qualifiedName))
    }
}
