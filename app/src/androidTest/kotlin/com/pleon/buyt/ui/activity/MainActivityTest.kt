package com.pleon.buyt.ui.activity

import androidx.lifecycle.Lifecycle.State.RESUMED
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import androidx.test.uiautomator.UiDevice
import de.mannodermaus.junit5.ActivityScenarioExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

@Tag("UI")
/**
 * Espresso is aware that it needs to wait with assertions and view interactions
 * until an activity is launched and displayed on the screen.
 *
 * Note: Espresso suggests to turn off all three types of device animations
 *  in *Settings* 🡲 *Developer Settings* but this resulted
 *  in the tests to not complete and keep running forever.
 *
 * You can also create tests graphically from Run -> Record Espresso Test
 *
 * Sometimes your UI test passes ten times, then breaks on the eleventh attempt
 * for some mysterious reason. It’s called flakiness.
 * The most popular reason for flakiness is the instability of the UI
 * tests libraries, such as Espresso and UI Automator.
 */
class MainActivityTest {

    @JvmField
    @RegisterExtension
    // JUnit 5 alternative of ActivityScenarioRule in JUnit 4;
    // Automatically closes scenario after each test;
    // Each test is supplied with a new scenario instance
    val scenarioExtension = ActivityScenarioExtension.launch<MainActivity>()
    lateinit var scenario: ActivityScenario<MainActivity>
    lateinit var device: UiDevice

    @BeforeEach
    fun setUp() {
        device = UiDevice.getInstance(getInstrumentation())
        scenario = scenarioExtension.scenario
        scenario.moveToState(RESUMED)
    }

    @Test fun activityMustBeLaunched() {
        scenario.onActivity { /* get its fields or ... */ }
        assertThat(scenario.state).isEqualTo(RESUMED)
    }

    /**
     * Alternative way for getting the scenario.
     * Define it as a [parameter][scenario] of the function and it will
     * be automatically injected by the [scenario extension][scenarioExtension]
     * defined at the top.
     *
     * Using this approach, the [scenario] property is not needed.
     */
    @Test fun activityMustBeLaunchedAlt(scenario: ActivityScenario<MainActivity>) {
        assertThat(scenario.state).isEqualTo(RESUMED)
    }
}
