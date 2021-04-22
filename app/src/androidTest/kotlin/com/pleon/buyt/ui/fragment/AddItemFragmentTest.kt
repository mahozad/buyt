package com.pleon.buyt.ui.fragment

import androidx.lifecycle.Lifecycle.State.RESUMED
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.espresso.matcher.ViewMatchers.Visibility.VISIBLE
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import androidx.test.uiautomator.UiDevice
import com.pleon.buyt.R
import com.pleon.buyt.database.dao.ItemDao
import com.pleon.buyt.database.dao.StoreDao
import com.pleon.buyt.model.Category
import com.pleon.buyt.model.Coordinates
import com.pleon.buyt.model.Store
import com.pleon.buyt.ui.activity.MainActivity
import com.pleon.buyt.withTextInputLayoutError
import com.pleon.buyt.withTextInputLayoutHint
import de.mannodermaus.junit5.ActivityScenarioExtension
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.CoreMatchers.*
import org.junit.jupiter.api.*
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import org.junit.jupiter.api.extension.RegisterExtension
import org.koin.java.KoinJavaComponent.inject

const val SAMPLE_ITEM_NAME = "item"
const val SAMPLE_PRICE = "123"

/**
 * Note: Espresso suggests to turn off all three types of device animations
 *  in *Settings* 🡲 *Developer Settings* but this resulted
 *  in the tests to not complete and keep running forever.
 */
@Tag("UI")
@TestInstance(PER_CLASS)
class AddItemFragmentTest {

    @JvmField
    @RegisterExtension
    val scenarioExtension = ActivityScenarioExtension.launch<MainActivity>()
    lateinit var device: UiDevice
    private val storeDao by inject(StoreDao::class.java)
    private val itemDao by inject(ItemDao::class.java)

    @BeforeAll
    fun initialize() {
        // Remove all app databases to start tests with no previous data;
        //  could also have used Android Orchestrator which is probably slower
        val context = getInstrumentation().targetContext
        val databases = context.databaseList()
        for (database in databases)
            context.deleteDatabase(database)
    }

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

    @Test fun fillingInNameAndPressingAddButtonShouldAddTheItem() {
        itemDao.deleteAll() // Required
        val initialItemCount = itemDao.getCount()
        onView(withId(R.id.name)).perform(typeText(SAMPLE_ITEM_NAME))
        Espresso.closeSoftKeyboard() // Required
        onView(withId(R.id.fab)).perform(click())
        val newItemCount = itemDao.getCount()
        assertThat(newItemCount).isGreaterThan(initialItemCount)
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

    @Test fun afterClosingFullScreenFragmentNewFragmentShouldNotBeFullScreen() {
        onView(withId(R.id.expandHandle)).perform(click())
        Espresso.pressBack()
        onView(withId(R.id.action_add)).perform(click())
        onView(withId(R.id.description_layout))
                .check(matches(either(not(isDisplayed()))
                        .or(withAlpha(0f))))
        onView(withId(R.id.urgent))
                .check(matches(either(not(isDisplayed()))
                        .or(withAlpha(0f))))
        onView(withId(R.id.bought_group))
                .check(matches(either(not(isDisplayed()))
                        .or(withAlpha(0f))))
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

    @Test fun touchingTheSameUnitButtonTwiceShouldNotDeselectIt() {
        onView(withId(R.id.btn2))
                .perform(click())
                .perform(click())
        onView(withId(R.id.btn2))
                .check(matches(isChecked()))
    }

    /**
     * FIXME: If date picker dialog is shown (at least once)
     *  and a config change happens (e.g. screen rotation),
     *  then the date picker is shown.
     */
    @Test fun dismissedPersianDatePickerShouldNotBeOpenedOnConfigChange() {
        onView(withId(R.id.expandHandle)).perform(click())
        onView(withId(R.id.bought))
                .perform(scrollTo())
                .perform(click())
        onView(withId(R.id.dateEd))
                .perform(scrollTo())
                .perform(click())
        onView(withText(android.R.string.cancel))
                .perform(click())
        device.setOrientationLeft()
        onView(withText(android.R.string.cancel))
                .inRoot(isDialog())
                .check(matches(not(isDisplayed())))
        device.setOrientationNatural()
    }

    @Test fun addingABoughtItemShouldResetTheLayoutAndHideTheBoughtGroup() {
        // Make sure there is at least one store available to choose in the dialog
        val storeName = "Test Store"
        val store = Store(Coordinates(1.0, 2.0), storeName, Category.DRUG)
        storeDao.insert(store)
        Espresso.pressBack() // To ensure store dialog is updated
        onView(withId(R.id.action_add)).perform(click())

        onView(withId(R.id.name))
                .perform(typeText(SAMPLE_ITEM_NAME))
                .perform(closeSoftKeyboard())
        onView(withId(R.id.expandHandle)).perform(click())
        onView(withId(R.id.bought))
                .perform(scrollTo())
                .perform(click())
        onView(withId(R.id.priceEd))
                .perform(typeText(SAMPLE_PRICE))
                .perform(closeSoftKeyboard())
        onView(withText(R.string.menu_title_select_store))
                .perform(click())
        onView(withChild(withText(storeName)))
                .perform(click())
        onView(withText(android.R.string.ok))
                .perform(click())
        onView(withId(R.id.fab))
                .perform(click())

        onView(withId(R.id.name))
                .check(matches(withText("")))
        onView(withId(R.id.quantityEd))
                .check(matches(not(withText(""))))
        onView(withId(R.id.description))
                .check(matches(withText("")))
        onView(withId(R.id.dateEd))
                .check(matches(withText(R.string.input_def_purchase_date)))
        onView(withId(R.id.bought))
                .check(matches(isNotChecked()))
        onView(withId(R.id.bought_group))
                .check(matches(either(not(isDisplayed())).or(withAlpha(0f))))
        onView(allOf(withId(R.id.price_layout), withTextInputLayoutHint(R.string.input_hint_price)))
                .check(matches(either(not(isDisplayed())).or(withAlpha(0f))))
        onView(withId(R.id.date_layout))
                .check(matches(either(not(isDisplayed())).or(withAlpha(0f))))
    }
}
