package com.pleon.buyt.ui.fragment

import android.content.res.ColorStateList
import android.os.Bundle
import android.text.Editable
import android.util.TypedValue
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.View.*
import android.view.inputmethod.EditorInfo.IME_ACTION_DONE
import android.widget.ArrayAdapter
import android.widget.DatePicker
import android.widget.EditText
import android.widget.TextView
import androidx.annotation.AttrRes
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat.getColor
import androidx.core.content.ContextCompat.getColorStateList
import androidx.core.widget.CompoundButtonCompat
import androidx.lifecycle.Observer
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputLayout
import com.mohamadamin.persianmaterialdatetimepicker.date.DatePickerDialog
import com.mohamadamin.persianmaterialdatetimepicker.utils.PersianCalendar
import com.pleon.buyt.R
import com.pleon.buyt.isPremium
import com.pleon.buyt.model.Category
import com.pleon.buyt.model.Item
import com.pleon.buyt.model.Item.Quantity.Unit.*
import com.pleon.buyt.ui.NumberInputWatcher
import com.pleon.buyt.ui.TextWatcherAdapter
import com.pleon.buyt.ui.dialog.DatePickerDialogFragment
import com.pleon.buyt.ui.dialog.SelectDialogFragment
import com.pleon.buyt.ui.dialog.SelectDialogFragment.SelectDialogRow
import com.pleon.buyt.util.AnimationUtil.animateAlpha
import com.pleon.buyt.util.AnimationUtil.animateIcon
import com.pleon.buyt.util.AnimationUtil.animateIconInfinitely
import com.pleon.buyt.viewmodel.AddItemViewModel
import ir.huri.jcal.JalaliCalendar
import kotlinx.android.synthetic.main.fragment_add_item.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.text.SimpleDateFormat
import java.util.*

/**
 * This fragment requires a Toolbar as it needs to inflate and use a menu item for selection of
 * store category. So the activities using this fragment must have a Toolbar set.
 */
class AddItemFragment : BaseFragment(), DatePickerDialog.OnDateSetListener,
        SelectDialogFragment.Callback, android.app.DatePickerDialog.OnDateSetListener {

    interface FullScreen {
        fun expandToFullScreen(fragmentRootView: View)
    }

    var isScrollLocked = true
    // These colors vary based on the app theme
    @ColorRes private var colorError = 0
    @ColorRes private var colorSurface = 0
    @ColorRes private var colorOnSurface = 0
    @ColorRes private var colorUnfocused = 0
    @ColorRes private var colorUnfocusedBorder = 0
    private val viewModel by viewModel<AddItemViewModel>()
    private val isBoughtChecked get() = bought.isChecked
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
        colorError = resolveThemeAttribute(R.attr.colorError)
        colorSurface = resolveThemeAttribute(R.attr.colorSurface)
        colorOnSurface = resolveThemeAttribute(R.attr.colorOnSurface)
        colorUnfocused = resolveThemeAttribute(R.attr.unitUnfocusedColor)
        colorUnfocusedBorder = resolveThemeAttribute(R.attr.unitBorderUnfocusedColor)
        setHasOptionsMenu(true) // for onCreateOptionsMenu() to be called
        setColorOfAllUnits(colorUnfocused) // because sometimes the color is not right
        setupItemNameAutoComplete()
        setupBoughtButton()
        setupListeners()
        scrollView.isVerticalScrollBarEnabled = false
        animateIconInfinitely(expandHandle.drawable)
    }

    private fun resolveThemeAttribute(@AttrRes attr: Int): Int {
        val typedValue = TypedValue()
        context?.theme?.resolveAttribute(attr, typedValue, true)
        return typedValue.resourceId
    }

    private fun setupBoughtButton() = bought.apply {
        // To reverse position of its checkbox icon
        if (Locale.getDefault().language == "fa") layoutDirection = LAYOUT_DIRECTION_LTR
        if (!isPremium) {
            isEnabled = false
            buttonTintList = getColorStateList(context, R.color.unit_btn_unfocused_color)
            setTextColor(getColor(context, R.color.unit_btn_unfocused_color))
            setText(R.string.checkbox_purchased_disabled)
        }
    }

    private fun setupListeners() {
        priceEd.addTextChangedListener(NumberInputWatcher(price_layout, priceEd, getString(R.string.input_suffix_price)))
        quantityEd.addTextChangedListener(NumberInputWatcher(quantity_layout, quantityEd))
        expandHandle.setOnClickListener { expandToFullScreen() }
        dateEd.setOnClickListener { onDateClicked() }
        dateEd.setOnFocusChangeListener { _, hasFocus -> onDateGainedFocus(hasFocus) }
        urgent.setOnCheckedChangeListener { _, isChecked -> onUrgentToggled(isChecked) }
        bought.setOnCheckedChangeListener { _, isChecked -> onBoughtToggled(isChecked) }
        quantityEd.setOnFocusChangeListener { _, hasFocus -> onQuantityFocusChanged(hasFocus) }
        quantityEd.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == IME_ACTION_DONE) onDonePressed()
            return@setOnEditorActionListener true
        }
        scrollView.setOnTouchListener { _, _ ->
            scrollView.isVerticalScrollBarEnabled = !isScrollLocked
            return@setOnTouchListener isScrollLocked
        }
        name.addTextChangedListener(object : TextWatcherAdapter() {
            override fun afterTextChanged(s: Editable?) = onNameChanged()
        })
        quantityEd.addTextChangedListener(object : TextWatcherAdapter() {
            override fun afterTextChanged(s: Editable?) = onQuantityChanged()
        })
        description.addTextChangedListener(object : TextWatcherAdapter() {
            override fun afterTextChanged(s: Editable?) = onDescriptionChanged()
        })
    }

    private fun expandToFullScreen() {
        if (activity is FullScreen) {
            animateAlpha(expandHandle, toAlpha = 0f)
            animateAlpha(description_layout, toAlpha = 1f, startDelay = 200)
            animateAlpha(urgent, toAlpha = 1f, startDelay = 300)
            animateAlpha(bought, toAlpha = 1f, startDelay = 400)
            animateAlpha(divider, toAlpha = 1f, startDelay = 400)
            (activity as FullScreen).expandToFullScreen(parentView)
        }
    }

    private fun setupItemNameAutoComplete() {
        viewModel.itemNameCats.observe(viewLifecycleOwner, Observer { nameCats ->
            this.nameCats = nameCats
            val adapter = ArrayAdapter(context!!,
                    android.R.layout.simple_dropdown_item_1line, nameCats.keys.toList())
            name.setAdapter(adapter)
        })
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
            if (isBoughtChecked) {
                for (store in viewModel.storeList)
                    selectionList.add(SelectDialogRow(store.name, store.category.storeImageRes))
                dialogTitle = R.string.dialog_title_select_store
            } else {
                for (category in Category.values())
                    selectionList.add(SelectDialogRow(getString(category.nameRes), category.imageRes))
                dialogTitle = R.string.dialog_title_select_cat
            }
            val selectDialog = SelectDialogFragment.newInstance(this, dialogTitle, selectionList)
            selectDialog.show(activity!!.supportFragmentManager, "SELECT_DIALOG")
        }
        return true
    }

    private fun price(): Long = try {
        priceEd.text.toString().replace(Regex("[^\\d]"), "").toLong()
    } catch (e: NumberFormatException) {
        0
    }

    private fun quantity(): Item.Quantity {
        val quantity = quantityEd.text.toString().replace(Regex("[^\\d]"), "").toLong()
        val unit = when (btnGrp.checkedButtonId) {
            R.id.btn1 -> UNIT
            R.id.btn2 -> KILOGRAM
            else -> GRAM
        }
        return Item.Quantity(quantity, unit)
    }

    /* FIXME: If date picker dialog is shown (at least once) and a configuration change happens
     *  (e.g. screen rotation), then the date picker is shown. This problem seems to have nothing
     *  to do with the following two methods calling each other. */

    /**
     * DatePickerDialog.show() for persian calendar is passed getActivity().getFragmentManager()
     * because it requires android.app.FragmentManager instead of androidx version.
     *
     * DatePickerDialogFragment.show() is passed getChildFragmentManager() can get the parent (this)
     * fragment and set it as the callback.
     */
    private fun onDateClicked() {
        if (Locale.getDefault().language == "fa") {
            val persianCal = PersianCalendar()
            val datePicker = DatePickerDialog.newInstance(this, persianCal.persianYear,
                    persianCal.persianMonth, persianCal.persianDay)
            val theme = prefs.getString(PREF_THEME, PREF_THEME_DARK)
            datePicker.isThemeDark = (theme == PREF_THEME_DARK) // for changing colors see colors.xml
            datePicker.retainInstance = true

            val selectableDays = arrayOfNulls<PersianCalendar>(10)
            for (i in selectableDays.indices) {
                val selectableDay = PersianCalendar()
                selectableDay.addPersianDate(Calendar.DAY_OF_YEAR, -i)
                selectableDays[i] = selectableDay
            }
            datePicker.selectableDays = selectableDays

            datePicker.show(activity!!.fragmentManager, "DATE_PICKER")
        } else {
            val datePicker = DatePickerDialogFragment()
            // datePicker.setRetainInstance(true);
            datePicker.show(childFragmentManager, "DATE_PICKER")
        }
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
    private fun onDateGainedFocus(focused: Boolean) {
        if (focused) onDateClicked()
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

        val date = String.format(resources.configuration.locale,
                "%s %d %s %d", jalaliCalendar.dayOfWeekString, jalaliCalendar.day,
                jalaliCalendar.monthString, jalaliCalendar.year)

        dateEd.setText(date)
    }

    // On result of Gregorian date picker
    override fun onDateSet(view: DatePicker, year: Int, month: Int, dayOfMonth: Int) {
        val cal = Calendar.getInstance()
        cal.set(year, month, dayOfMonth)
        viewModel.purchaseDate = cal.time

        val format = "MM/dd/yyyy"
        val dateFormat = SimpleDateFormat(format, Locale.US)
        dateEd.setText(dateFormat.format(viewModel.purchaseDate))
    }

    private fun onUrgentToggled(isChecked: Boolean) {
        urgent.setButtonDrawable(if (isChecked) R.drawable.avd_urgent_check else R.drawable.avd_urgent_uncheck)
        animateIcon(CompoundButtonCompat.getButtonDrawable(urgent)!!)
    }

    private fun onBoughtToggled(checked: Boolean) {
        bought.setButtonDrawable(if (checked) R.drawable.avd_expand else R.drawable.avd_collapse)
        animateIcon(CompoundButtonCompat.getButtonDrawable(bought)!!)
        if (selectCategoryTxvi != null) { // to fix bug on config change
            selectCategoryTxvi!!.text = if (checked) getString(R.string.menu_title_select_store) else getString(viewModel.category.nameRes)
            selectCategoryTxvi!!.setTextColor(getColor(context!!, colorOnSurface))
            val icon = if (checked) R.drawable.avd_store_error else R.drawable.ic_item_grocery
            selectCategoryTxvi!!.setCompoundDrawablesRelativeWithIntrinsicBounds(
                    icon, 0, 0, 0
            )
        }
        bought_group.visibility = if (checked) VISIBLE else GONE
        price_layout.error = null
        if (!checked) {
            viewModel.store = null
        }
    }

    private fun onQuantityFocusChanged(hasFocus: Boolean) {
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
            val colors = intArrayOf(getColor(context!!, color), colorSurface)
            btn.backgroundTintList = ColorStateList(states, colors)
        }
    }

    private fun onNameChanged() {
        val itemName = name.text.toString()
        if (itemName.isNotEmpty()) {
            name_layout.error = null // clear error if exists
            setCounterEnabledIfInputLong(name_layout)
            if (!isBoughtChecked) {
                nameCats[itemName]?.let {
                    viewModel.category = it
                    selectCategoryTxvi!!.setCompoundDrawablesRelativeWithIntrinsicBounds(
                            it.imageRes, 0, 0, 0
                    )
                    selectCategoryTxvi!!.setText(it.nameRes)
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
            val item = Item(itemName, quantity(), viewModel.category, urgent.isChecked, bought.isChecked)
            if (!isEmpty(description)) item.description = description.text.toString()
            if (isBoughtChecked && !isEmpty(priceEd)) item.totalPrice = price()
            viewModel.addItem(item, isBoughtChecked)
            resetFields()
        }
    }

    private fun resetFields() {
        name.text.clear()
        name_layout.isCounterEnabled = false
        name.requestFocus()
        quantityEd.setText("1")
        btnGrp.check(R.id.btn1)
        setColorOfAllUnits(colorUnfocused)
        selectCategoryTxvi!!.setCompoundDrawablesRelativeWithIntrinsicBounds(
                R.drawable.ic_item_grocery, 0, 0, 0
        )
        selectCategoryTxvi!!.setTextColor(getColor(context!!, colorOnSurface))
        selectCategoryTxvi!!.text = getString(Category.GROCERY.nameRes)
        viewModel.category = Category.GROCERY
        viewModel.store = null
        description.text?.clear()
        priceEd.text?.clear()
        urgent.isChecked = false
        bought.isChecked = false
    }

    override fun onSelected(index: Int) {
        val name: String
        val imageRes: Int
        if (isBoughtChecked) {
            viewModel.store = viewModel.storeList!![index]
            name = viewModel.store!!.name
            imageRes = viewModel.store!!.category.storeImageRes
        } else {
            val category = Category.values()[index]
            name = getString(category.nameRes)
            imageRes = category.imageRes
            viewModel.category = category
        }
        selectCategoryTxvi!!.setCompoundDrawablesRelativeWithIntrinsicBounds(
                imageRes, 0, 0, 0
        )
        selectCategoryTxvi!!.setTextColor(getColor(context!!, colorOnSurface))
        selectCategoryTxvi!!.text = name
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
        if (isBoughtChecked && viewModel.store == null) {
            selectCategoryTxvi!!.setTextColor(getColor(context!!, colorError))
            selectCategoryTxvi!!.setCompoundDrawablesRelativeWithIntrinsicBounds(
                    R.drawable.avd_store_error, 0, 0, 0
            )
            animateIcon(selectCategoryTxvi!!.compoundDrawablesRelative[0])
            validated = false
        }

        return validated
    }

    private fun isEmpty(et: EditText) = et.text.toString().trim { it <= ' ' }.isEmpty()
}
