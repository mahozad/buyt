package com.pleon.buyt.ui.fragment

import android.animation.ValueAnimator
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.os.Bundle
import android.view.*
import android.view.View.*
import android.view.inputmethod.EditorInfo.IME_ACTION_DONE
import android.widget.ArrayAdapter
import android.widget.DatePicker
import android.widget.EditText
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat.*
import androidx.core.widget.CompoundButtonCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputLayout
import com.mohamadamin.persianmaterialdatetimepicker.date.DatePickerDialog
import com.mohamadamin.persianmaterialdatetimepicker.utils.PersianCalendar
import com.pleon.buyt.R
import com.pleon.buyt.isPremium
import com.pleon.buyt.model.Category
import com.pleon.buyt.model.Item
import com.pleon.buyt.model.Item.Quantity.Unit.*
import com.pleon.buyt.ui.NumberInputWatcher
import com.pleon.buyt.ui.dialog.DatePickerDialogFragment
import com.pleon.buyt.ui.dialog.SelectDialogFragment
import com.pleon.buyt.ui.dialog.SelectDialogFragment.SelectDialogRow
import com.pleon.buyt.ui.newAfterTextWatcher
import com.pleon.buyt.util.*
import com.pleon.buyt.viewmodel.AddItemViewModel
import ir.huri.jcal.JalaliCalendar
import kotlinx.android.synthetic.main.fragment_add_item.*
import org.jetbrains.anko.dip
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.*
import java.util.Calendar.DAY_OF_YEAR

/**
 * This fragment requires a Toolbar as it needs to inflate and use a menu item for selection of
 * store category. So the activities using this fragment must have a Toolbar set.
 */
class AddItemFragment : BaseFragment(), DatePickerDialog.OnDateSetListener,
        SelectDialogFragment.Callback, android.app.DatePickerDialog.OnDateSetListener {

    interface FullScreen {
        val rootViewHeight: Int
        val fragmentContainerView: View
    }

    var isScrollLocked = true
    // These colors vary based on the app theme
    @ColorRes private var colorError = 0
    @ColorRes private var colorSurface = 0
    @ColorRes private var colorOnSurface = 0
    @ColorRes private var colorUnfocused = 0
    @ColorRes private var colorUnfocusedBorder = 0
    private val viewModel by viewModel<AddItemViewModel>()
    private val isUrgent get() = urgent.isChecked
    private val isBought get() = bought.isChecked
    private val defaultUnitButtonId = R.id.btn1
    private var selectCategoryTxvi: TextView? = null
    private lateinit var unitBtns: Array<MaterialButton>
    private lateinit var nameCats: Map<String, Category>

    override fun layout() = R.layout.fragment_add_item

    /**
     * Your fragments can contribute menu items to the activity's Options Menu (and, consequently,
     * the app bar) by implementing onCreateOptionsMenu(). In order for this method to receive calls,
     * however, setHasOptionsMenu() must be called during onCreate(), to indicate that the fragment
     * would like to add items to the Options Menu. Otherwise, the fragment doesn't receive a call
     * to onCreateOptionsMenu(). Any items that you then add to the Options Menu from the fragment
     * are appended to the existing menu items.
     *
     * Note: Although your fragment receives an on-item-selected callback for each menu item it adds,
     * the activity is first to receive the respective callback when the user selects a menu item.
     * If the activity's implementation of the on-item-selected callback does not handle the
     * selected item, then the event is passed to the fragment's callback.
     *
     * @param view
     * @param savedState
     * @return
     */
    override fun onViewCreated(view: View, savedState: Bundle?) {
        unitBtns = arrayOf(btn1, btn2, btn3)
        colorError = context.resolveThemeColorRes(R.attr.colorError)
        colorSurface = context.resolveThemeColorRes(R.attr.colorSurface)
        colorOnSurface = context.resolveThemeColorRes(R.attr.colorOnSurface)
        colorUnfocused = context.resolveThemeColorRes(R.attr.unitUnfocusedColor)
        colorUnfocusedBorder = context.resolveThemeColorRes(R.attr.unitBorderUnfocusedColor)
        setHasOptionsMenu(true) // for onCreateOptionsMenu() to be called
        setColorOfAllUnits(colorUnfocused) // because sometimes the color is not right
        setHeightOfAllUnits()
        setupItemNameAutoComplete()
        setupBoughtButton()
        setupListeners()
        scrollView.isVerticalScrollBarEnabled = false
        animateIconInfinitely(expandHandle.drawable)
    }

    /**
     * As said [here](https://github.com/material-components/material-components-android/blob/15daf58d53a4a363af32fd427304f495bb57331c/lib/java/com/google/android/material/textfield/res/values/styles.xml#L176)
     * and also [here](https://github.com/material-components/material-components-android/blob/15daf58d53a4a363af32fd427304f495bb57331c/lib/java/com/google/android/material/textfield/res/values/styles.xml#L198)
     * the height of the dense text input layout is 54dp (accounting for line height etc.) therefore
     * it adds top and bottom padding to [text field][com.google.android.material.textfield.TextInputEditText]
     * to match material design specs.
     * Also see [the java class](ALso see https://github.com/material-components/material-components-android/blob/15daf58d53a4a363af32fd427304f495bb57331c/lib/java/com/google/android/material/textfield/TextInputEditText.java#L183)
     * for more details about text field height.
     *
     * We could also have specified sizes in the layout file which was almost identical and as good
     * as the sizes we are setting here. Checkout the previous commit before this comment was added
     * to see the xml layout approach.
     */
    private fun setHeightOfAllUnits() {
        quantityEd.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED)
        // To fix quantityEd::measure causing the direction to be always LTR (why?)
        val isRtl = resources.getBoolean(R.bool.isRtl)
        if (isRtl) quantityEd.textDirection = TEXT_DIRECTION_RTL
        val correction = requireContext().dip(11.2f)
        for (button in unitBtns) {
            val params = button.layoutParams
            params.height = quantityEd.measuredHeight + correction
            button.layoutParams = params
        }
    }

    override fun onResume() {
        super.onResume()
        // Fix the bug with the fragment when the device was in landscape mode and
        // one of bottom inputs like price was focused and a config change happened
        scrollView.post { scrollView.scrollTo(0, 0) }
        name.requestFocus()
    }

    private fun setupBoughtButton() = bought.apply {
        // Reverse position of button icon in RTL layouts
        if (Locale.getDefault().language == "fa") layoutDirection = LAYOUT_DIRECTION_RTL
        if (!isPremium) {
            isEnabled = false
            iconTint = getColorStateList(context, R.color.unit_btn_unfocused_color)
            setTextColor(getColor(context, R.color.unit_btn_unfocused_color))
            setText(R.string.checkbox_purchased_disabled)
        }
    }

    private fun setupListeners() {
        priceEd.addTextChangedListener(NumberInputWatcher(price_layout, priceEd, getString(R.string.input_suffix_price)))
        quantityEd.addTextChangedListener(NumberInputWatcher(quantity_layout, quantityEd))
        dateEd.setOnClickListener(this::onDateClicked)
        dateEd.setOnFocusChangeListener(this::onDateGainedFocus)
        scrollView.setOnTouchListener(this::onScrolled)
        expandHandle.setOnTouchListener(this::expandToFullScreen)
        urgent.setOnCheckedChangeListener(this::onUrgentToggled)
        bought.setOnClickListener(this::onBoughtToggled)
        quantityEd.setOnFocusChangeListener(this::onQuantityFocusChanged)
        quantityEd.setOnEditorActionListener { _, actionId, _ -> onQuantityDone(actionId) }
        name.addTextChangedListener(newAfterTextWatcher(this::onNameChanged))
        quantityEd.addTextChangedListener(newAfterTextWatcher(this::onQuantityChanged))
        description.addTextChangedListener(newAfterTextWatcher(this::onDescriptionChanged))
    }

    private fun onScrolled(view: View, motionEvent: MotionEvent): Boolean {
        scrollView.isVerticalScrollBarEnabled = !isScrollLocked
        return isScrollLocked
    }

    private fun onQuantityDone(actionId: Int): Boolean {
        if (actionId == IME_ACTION_DONE) onDonePressed()
        return true // consume the event
    }

    private fun expandToFullScreen(view: View, event: MotionEvent): Boolean {
        expandHandle.isEnabled = false // So user's subsequent touches has no effect
        if (activity is FullScreen) {
            animateAlpha(expandHandle, toAlpha = 0f)
            animateAlpha(description_layout, toAlpha = 1f, startDelay = 200)
            animateAlpha(urgent, toAlpha = 1f, startDelay = 300)
            animateAlpha(bought, toAlpha = 1f, startDelay = 400)
            animateAlpha(dividerStart, toAlpha = 1f, startDelay = 400)
            animateAlpha(dividerEnd, toAlpha = 1f, startDelay = 400)
            expandHeight(activity as FullScreen)
        }
        return true // consume the event
    }

    /**
     * Note the android:fillViewport="true" on the scroll view
     */
    private fun expandHeight(activity: FullScreen) {
        isScrollLocked = false
        val containerView = activity.fragmentContainerView
        val animator = ValueAnimator.ofInt(containerView.measuredHeight, activity.rootViewHeight)
        animator.setDuration(300).addUpdateListener {
            containerView.layoutParams.height = it.animatedValue as Int
            containerView.layoutParams = containerView.layoutParams
        }
        animator.start()
    }

    private fun setupItemNameAutoComplete() {
        viewModel.itemNameCats.observe(viewLifecycleOwner) { nameCats ->
            this.nameCats = nameCats
            val adapter = ArrayAdapter(
                    requireContext(),
                    android.R.layout.simple_dropdown_item_1line,
                    nameCats.keys.toList())
            name.setAdapter(adapter)
        }
    }

    /**
     * For this method to be called, it is required that setHasOptionsMenu() has been set.
     *
     * Note that the containing activity must have a Toolbar set so this fragment can inflate and
     * use its own menu item.
     *
     * @param menu
     * @param inflater
     */
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_add_item, menu)

        selectCategoryTxvi = menu.findItem(R.id.action_item_category).actionView.findViewById(R.id.select_store)

        if (viewModel.store != null) {
            selectCategoryTxvi!!.text = viewModel.store!!.name
            selectCategoryTxvi!!.setCompoundDrawablesRelativeWithIntrinsicBounds(
                    viewModel.store!!.category.storeImageRes, 0, 0, 0
            )
        } else if (bought.isChecked) {
            selectCategoryTxvi!!.setText(R.string.menu_title_select_store)
            selectCategoryTxvi!!.setCompoundDrawablesRelativeWithIntrinsicBounds(
                    R.drawable.ic_store, 0, 0, 0
            )
        }

        // Setting up "Choose category" action because it has custom layout
        val menuItem = menu.findItem(R.id.action_item_category)
        menuItem.actionView.setOnClickListener { onOptionsItemSelected(menuItem) }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_item_category) {
            val selectionList = ArrayList<SelectDialogRow>() // dialog requires ArrayList
            val dialogTitle: Int
            if (isBought) {
                dialogTitle = R.string.dialog_title_select_store
                if (viewModel.storeList.isEmpty()) {
                    MaterialAlertDialogBuilder(requireContext(), R.style.JustifiedTextDialogStyle)
                            .setTitle(dialogTitle)
                            .setIcon(R.drawable.ic_about)
                            .setMessage(R.string.dialog_message_no_store_available)
                            .setPositiveButton(android.R.string.ok) { _, _ -> /* Dismiss */ }
                            .show()
                    return true
                } else {
                    for (store in viewModel.storeList)
                        selectionList.add(SelectDialogRow(store.name, store.category.storeImageRes))
                }
            } else {
                for (category in Category.values())
                    selectionList.add(SelectDialogRow(getString(category.nameRes), category.imageRes))
                dialogTitle = R.string.dialog_title_select_cat
            }
            val selectDialog = SelectDialogFragment.newInstance(this, dialogTitle, selectionList)
            selectDialog.show(requireActivity().supportFragmentManager, "SELECT_DIALOG")
        }
        return true
    }

    private fun price(): Long = try {
        priceEd.text?.toNumber() ?: 0
    } catch (e: NumberFormatException) {
        0
    }

    private fun quantity(): Item.Quantity {
        val quantity = quantityEd.text?.toNumber() ?: viewModel.defaultQuantity
        val unit = when (btnGrp.checkedButtonId) {
            R.id.btn1 -> UNIT
            R.id.btn2 -> KILOGRAM
            else -> GRAM
        }
        return Item.Quantity(quantity, unit)
    }

    /**
     * DatePickerDialog.show() for persian calendar is passed getActivity().getFragmentManager()
     * because it requires android.app.FragmentManager instead of androidx version.
     *
     * DatePickerDialogFragment.show() is passed getChildFragmentManager() can get the parent (this)
     * fragment and set it as the callback.
     */
    private fun onDateClicked(view: View) {
        if (Locale.getDefault().language == "fa") {
            val persianCal = PersianCalendar()
            val datePicker = DatePickerDialog.newInstance(this,
                    persianCal.persianYear,
                    persianCal.persianMonth,
                    persianCal.persianDay
            )
            setDatePickerTheme(datePicker)
            val selectableDays = arrayOfNulls<PersianCalendar>(10)
            for (i in selectableDays.indices) {
                val selectableDay = PersianCalendar()
                selectableDay.addPersianDate(DAY_OF_YEAR, -i)
                selectableDays[i] = selectableDay
            }
            datePicker.selectableDays = selectableDays

            datePicker.retainInstance = true
            datePicker.show(requireActivity().fragmentManager, "DATE_PICKER")
        } else {
            DatePickerDialogFragment().show(childFragmentManager, "DATE_PICKER")
        }
    }

    /**
     * To change date picker colors see colors.xml file.
     */
    private fun setDatePickerTheme(datePicker: DatePickerDialog) {
        val theme = prefs.getString(PREF_THEME, DEFAULT_THEME_NAME)
        val uiMode =
                resources.configuration.uiMode and
                Configuration.UI_MODE_NIGHT_MASK
        val isInDarkMode = uiMode != Configuration.UI_MODE_NIGHT_NO
        datePicker.isThemeDark =
                (theme == PREF_THEME_DARK) ||
                (theme == PREF_THEME_AUTO && isInDarkMode)
    }

    /**
     * Calls the onClick method of the date field.
     *
     * This method is required because if focusable is set to true (and that is required
     * because of the textField border to change color), then the first click on the text view
     * will not open the date picker dialog because if the field does not have focus the first
     * click on it only requests focus.
     *
     * @param focused if the view is focused
     */
    private fun onDateGainedFocus(view: View, focused: Boolean) {
        if (focused) onDateClicked(view)
    }

    // On result of Persian date picker

    /**
     * Because we want all the dates in the database to be in same format, we convert the given
     * persian date to Gregorian.
     *
     * This way all of our dates in the database are uniformed and if needed, we can format them
     * however we want at runtime; for example to show date in Persian format we can do this:<br></br>
     * `Locale locale = new Locale("fa_IR@calendar=persian");`<br></br>
     * `DateFormat.getDateInstance(DateFormat.LONG, locale).format(date)`
     *
     * @param view
     * @param year
     * @param month
     * @param day
     */
    override fun onDateSet(view: DatePickerDialog, year: Int, month: Int, day: Int) {
        // PersianCalendar cal = new PersianCalendar();
        // cal.set(year, month, day);
        val jalaliCalendar = JalaliCalendar(year, month + 1, day)
        viewModel.purchaseDate = jalaliCalendar.toGregorian().time
        val formattedDate = formatDate(viewModel.purchaseDate)
        dateEd.setText(formattedDate)
    }

    // On result of Gregorian date picker
    override fun onDateSet(view: DatePicker, year: Int, month: Int, dayOfMonth: Int) {
        val cal = Calendar.getInstance()
        cal.set(year, month, dayOfMonth)
        viewModel.purchaseDate = cal.time
        val formattedDate = formatDate(viewModel.purchaseDate)
        dateEd.setText(formattedDate)
    }

    private fun onUrgentToggled(view: View, isChecked: Boolean) {
        urgent.setButtonDrawable(if (isChecked) R.drawable.avd_urgent_check else R.drawable.avd_urgent_uncheck)
        animateIcon(CompoundButtonCompat.getButtonDrawable(urgent)!!)
    }

    private fun onBoughtToggled(view: View) {
        val drawableId = if (isBought) R.drawable.avd_expand else R.drawable.avd_collapse
        val drawable = getDrawable(requireContext(), drawableId)
        bought.icon = drawable
        animateIcon(drawable!!)
        if (selectCategoryTxvi != null) { // to fix bug on config change
            selectCategoryTxvi?.text = getString(if (isBought) R.string.menu_title_select_store else viewModel.category.nameRes)
            selectCategoryTxvi?.setTextColor(getColor(requireContext(), colorOnSurface))
            val icon = if (isBought) R.drawable.avd_store_error else viewModel.defaultInitialItemCategory.imageRes
            selectCategoryTxvi?.setCompoundDrawablesRelativeWithIntrinsicBounds(
                    icon, 0, 0, 0
            )
        }
        bought_group.visibility = if (isBought) VISIBLE else GONE
        price_layout.error = null
        if (!isBought) viewModel.store = null
    }

    private fun onQuantityFocusChanged(view: View, hasFocus: Boolean) {
        var color = colorError
        if (hasFocus && quantity_layout.error == null) color = R.color.colorPrimary
        else if (!hasFocus && quantity_layout.error == null) color = colorUnfocused

        setColorOfAllUnits(color)
    }

    private fun setColorOfAllUnits(color: Int) {
        if (color == R.color.colorPrimary && !quantityEd.hasFocus()) return
        for (btn in unitBtns) {
            btn.setStrokeColorResource(if (color == colorUnfocused) colorUnfocusedBorder else color)

            val states = arrayOf(
                    intArrayOf(android.R.attr.state_checked), // checked
                    intArrayOf(android.R.attr.state_checked * -1) // unchecked
            )
            val colors = intArrayOf(getColor(requireContext(), color), colorSurface)
            btn.backgroundTintList = ColorStateList(states, colors)
        }
    }

    private fun onNameChanged() {
        val itemName = name.text.toString()
        if (itemName.isNotEmpty()) {
            name_layout.error = null // clear error if exists
            setCounterEnabledIfInputLong(name_layout)
            // For when item name is not empty and a config change happens
            if (!::nameCats.isInitialized) return
            if (!isBought) {
                nameCats[itemName]?.let {
                    viewModel.category = it
                    selectCategoryTxvi?.setCompoundDrawablesRelativeWithIntrinsicBounds(
                            it.imageRes, 0, 0, 0
                    )
                    selectCategoryTxvi?.setText(it.nameRes)
                }
            }
        }
    }

    private fun onQuantityChanged() {
        if (quantityEd.text.toString().isNotEmpty()) { // to prevent error with config change
            quantity_layout.error = null // clear error if exists
        }
        setColorOfAllUnits(R.color.colorPrimary)
    }

    private fun onDescriptionChanged() = setCounterEnabledIfInputLong(description_layout)

    private fun setCounterEnabledIfInputLong(layout: TextInputLayout) {
        val inputLength = layout.editText!!.text.toString().length
        layout.isCounterEnabled = inputLength > layout.counterMaxLength * 0.66
    }

    fun onDonePressed() {
        if (validateFields()) {
            val itemName = name.text.toString()
            val item = Item(itemName, quantity(), viewModel.category, isUrgent, isBought)
            if (!isEmpty(description)) item.description = description.text.toString()
            if (isBought) item.totalPrice = price()
            viewModel.addItem(item, isBought)
            viewModel.resetValues()
            resetFields()
        }
    }

    private fun resetFields() {
        name.text.clear()
        name_layout.isCounterEnabled = false
        name.requestFocus()
        quantityEd.setText(getString(R.string.input_def_quantity))
        btnGrp.check(defaultUnitButtonId)
        setColorOfAllUnits(colorUnfocused)
        description.text?.clear()
        if (isUrgent) { // Animate only if was checked
            urgent.setButtonDrawable(R.drawable.avd_urgent_uncheck)
            animateIcon(CompoundButtonCompat.getButtonDrawable(urgent)!!)
        }
        if (isBought) { // Animate only if was expanded
            with(getDrawable(requireContext(), R.drawable.avd_collapse)) {
                bought.icon = this
                animateIcon(this!!)
            }
        }
        urgent.isChecked = false
        bought.isChecked = false
        bought_group.visibility = GONE
        priceEd.text?.clear()
        dateEd.setText(getString(R.string.input_def_purchase_date))
        selectCategoryTxvi?.setCompoundDrawablesRelativeWithIntrinsicBounds(
                viewModel.defaultInitialItemCategory.imageRes, 0, 0, 0
        )
        selectCategoryTxvi?.text = getString(viewModel.defaultInitialItemCategory.nameRes)
        selectCategoryTxvi?.setTextColor(getColor(requireContext(), colorOnSurface))
    }

    override fun onSelected(index: Int) {
        val name: String
        val imageRes: Int
        if (isBought) {
            viewModel.store = viewModel.storeList[index]
            name = viewModel.store!!.name
            imageRes = viewModel.store!!.category.storeImageRes
        } else {
            val category = Category.values()[index]
            name = getString(category.nameRes)
            imageRes = category.imageRes
            viewModel.category = category
        }
        selectCategoryTxvi?.setCompoundDrawablesRelativeWithIntrinsicBounds(
                imageRes, 0, 0, 0
        )
        selectCategoryTxvi?.setTextColor(getColor(requireContext(), colorOnSurface))
        selectCategoryTxvi?.text = name
    }

    private fun validateFields(): Boolean {
        var validated = true

        if (isEmpty(name)) {
            name_layout.error = getString(R.string.input_error_name)
            validated = false
        }
        if (isEmpty(quantityEd)) {
            quantity_layout.error = getString(R.string.input_error_quantity)
            setColorOfAllUnits(colorError)
            validated = false
        }
        if (bought.isChecked && (isEmpty(priceEd) || price() == 0L)) {
            price_layout.error = getString(R.string.input_error_price)
            validated = false
        }
        if (isBought && viewModel.store == null) {
            selectCategoryTxvi?.setTextColor(getColor(requireContext(), colorError))
            selectCategoryTxvi?.setCompoundDrawablesRelativeWithIntrinsicBounds(
                    R.drawable.avd_store_error, 0, 0, 0
            )
            animateIcon(selectCategoryTxvi!!.compoundDrawablesRelative[0])
            validated = false
        }

        return validated
    }

    private fun isEmpty(et: EditText) = et.text.toString().trim { it <= ' ' }.isEmpty()
}
