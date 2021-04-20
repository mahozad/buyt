package com.pleon.buyt.ui.fragment

import androidx.lifecycle.Lifecycle.State.RESUMED
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.espresso.matcher.ViewMatchers.Visibility.VISIBLE
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import androidx.test.uiautomator.UiDevice
import com.pleon.buyt.R
import com.pleon.buyt.ui.activity.MainActivity
import com.pleon.buyt.withTextInputLayoutError
import com.pleon.buyt.withTextInputLayoutHint
import de.mannodermaus.junit5.ActivityScenarioExtension
import org.hamcrest.CoreMatchers.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

const val SAMPLE_ITEM_NAME = "item"

/**
 * Note: Espresso suggests to turn off all three types of device animations
 *  in *Settings* 🡲 *Developer Settings* but this resulted
 *  in the tests to not complete and keep running forever.
 */
@Tag("UI")
class AddItemFragmentTest {

    @JvmField
    @RegisterExtension
    val scenarioExtension = ActivityScenarioExtension.launch<MainActivity>()
    private lateinit var device: UiDevice

    @BeforeEach
    fun setUp(scenario: ActivityScenario<MainActivity>) {
        device = UiDevice.getInstance(getInstrumentation())
        scenario.moveToState(RESUMED)
        // Open the fragment
        onView(withId(R.id.action_add)).perform(click())
    }

    @Test fun expandButtonShouldBeAccessible() {
        onView(withId(R.id.expandHandle))
                .check(matches(isDisplayed()))
                .check(matches(isEnabled()))
    }

    @Test fun nameInputShouldHaveFocus() {
        onView(withId(R.id.name_layout))
                .check(matches(hasFocus()))
    }

    @Test fun quantityInputShouldHaveDefaultValue() {
        onView(withId(R.id.quantityEd))
                .check(matches(withText(R.string.input_def_quantity)))
    }

    @Test fun nameInputActionShouldTransferFocusToQuantityInput() {
        onView(withId(R.id.name))
                .perform(typeText(SAMPLE_ITEM_NAME))
                .perform(pressImeActionButton())
                .check(matches(not(hasFocus())))
        onView(withId(R.id.quantityEd))
                .check(matches(hasFocus()))
    }

    @Test fun quantityInputActionShouldNotThrowException() {
        onView(withId(R.id.name))
                .perform(typeText(SAMPLE_ITEM_NAME))
                .perform(pressImeActionButton())
        onView(withId(R.id.quantityEd))
                .perform(pressImeActionButton())
    }

    @Test fun middleViewsShouldNotBeDisplayed() {
        onView(withId(R.id.description_layout))
                .check(matches(either(not(isDisplayed()))
                        .or(withAlpha(0f))))
        onView(withId(R.id.urgent))
                .check(matches(either(not(isDisplayed()))
                        .or(withAlpha(0f))))
        onView(withId(R.id.bought_group))
                .check(matches(either(not(isDisplayed()))
                        .or(withAlpha(0f))))
        onView(allOf(withId(R.id.price_layout), withTextInputLayoutHint(R.string.input_hint_price)))
                .check(matches(either(not(isDisplayed()))
                        .or(withAlpha(0f))))
        onView(withId(R.id.date_layout))
                .check(matches(either(not(isDisplayed()))
                        .or(withAlpha(0f))))
    }

    @Test fun expandButtonShouldExpandTheFragment() {
        onView(withId(R.id.expandHandle)).perform(click())
        onView(withId(R.id.description_layout)).check(matches(isDisplayed()))
        onView(withId(R.id.urgent)).check(matches(isDisplayed()))
        onView(withId(R.id.bought))
                .perform(scrollTo())
                .check(matches(isDisplayed()))
    }

    @Test fun expandButtonShouldBeInvisibleAfterFragmentIsExpanded() {
        onView(withId(R.id.expandHandle))
                .perform(click())
                .check(matches(withAlpha(0f)))
    }

    @Test fun clickingOnBoughtButtonShouldDisplayBoughtLayout() {
        onView(withId(R.id.expandHandle)).perform(click())
        onView(withId(R.id.bought))
                .perform(scrollTo())
                .perform(click())
        onView(allOf(withId(R.id.price_layout), withTextInputLayoutHint(R.string.input_hint_price)))
                .perform(scrollTo())
                .check(matches(isDisplayed()))
        onView(withId(R.id.date_layout))
                .perform(scrollTo())
                .check(matches(isDisplayed()))
    }

    @Test fun fragmentShouldBeDisplayedOnConfigChange() {
        device.setOrientationLeft()
        onView(withId(R.id.fragContainer)).check(matches(isDisplayed()))
        onView(withId(R.id.name)).check(matches(isDisplayed()))
        onView(withId(R.id.quantityEd)).check(matches(isDisplayed()))
        device.setOrientationNatural()
    }

    @Test fun middleViewsShouldNotBeDisplayedOnConfigChange() {
        device.setOrientationLeft()
        onView(withId(R.id.description_layout))
                .check(matches(either(not(isDisplayed()))
                        .or(withAlpha(0f))))
        onView(withId(R.id.urgent))
                .check(matches(either(not(isDisplayed()))
                        .or(withAlpha(0f))))
        onView(withId(R.id.bought_group))
                .check(matches(either(not(isDisplayed()))
                        .or(withAlpha(0f))))
        onView(allOf(withId(R.id.price_layout), withTextInputLayoutHint(R.string.input_hint_price)))
                .check(matches(either(not(isDisplayed()))
                        .or(withAlpha(0f))))
        onView(withId(R.id.date_layout))
                .check(matches(either(not(isDisplayed()))
                        .or(withAlpha(0f))))
        device.setOrientationNatural()
    }

    @Test fun fullScreenFragmentShouldBeFullScreenOnConfigChange() {
        onView(withId(R.id.expandHandle)).perform(click())
        device.setOrientationLeft()
        onView(withId(R.id.description_layout))
                .check(matches(withEffectiveVisibility(VISIBLE)))
                .check(matches(withAlpha(1f)))
        onView(withId(R.id.urgent))
                .check(matches(withEffectiveVisibility(VISIBLE)))
                .check(matches(withAlpha(1f)))
        device.setOrientationNatural()
    }

    @Test fun displayedMiddleViewsShouldBeDisplayedOnConfigChange() {
        onView(withId(R.id.expandHandle)).perform(click())
        onView(withId(R.id.bought))
                .perform(scrollTo())
                .perform(click())
        device.setOrientationLeft()
        onView(allOf(withId(R.id.price_layout), withTextInputLayoutHint(R.string.input_hint_price)))
                .check(matches(withEffectiveVisibility(VISIBLE)))
                .check(matches(isDisplayed()))
                .check(matches(withAlpha(1f)))
        onView(withId(R.id.date_layout))
                .check(matches(withEffectiveVisibility(VISIBLE)))
                .check(matches(isDisplayed()))
                .check(matches(withAlpha(1f)))
        device.setOrientationNatural()
    }

    @Test fun shouldDisplayErrorMessageForEmptyName() {
        onView(withId(R.id.fab)).perform(click())
        onView(withId(R.id.name_layout))
                .check(matches(withTextInputLayoutError(R.string.input_error_name)))
    }

    @Test fun shouldDisplayErrorMessageForBothEmptyNameAndEmptyQuantity() {
        onView(withId(R.id.quantityEd)).perform(clearText())
        onView(withId(R.id.fab)).perform(click())
        onView(withId(R.id.name_layout))
                .check(matches(withTextInputLayoutError(R.string.input_error_name)))
        onView(withId(R.id.quantity_layout))
                .check(matches(withTextInputLayoutError(R.string.input_error_quantity)))
    }

    @Test fun clickingOnDateInputShouldOpenDatePickerDialog() {
        onView(withId(R.id.expandHandle)).perform(click())
        onView(withId(R.id.bought))
                .perform(scrollTo())
                .perform(click())
        onView(withId(R.id.dateEd))
                .perform(scrollTo())
                .perform(click())
        onView(withText(android.R.string.cancel))
                .inRoot(isDialog())
                .check(matches(isDisplayed()))
    }

}
