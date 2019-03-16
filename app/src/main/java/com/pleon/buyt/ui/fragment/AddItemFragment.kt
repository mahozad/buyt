package com.pleon.buyt.ui.fragment

import android.content.Context
import android.graphics.drawable.Animatable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.DrawableContainer.DrawableContainerState
import android.graphics.drawable.StateListDrawable
import android.os.Bundle
import android.preference.PreferenceManager
import android.text.Editable
import android.text.TextWatcher
import android.util.TypedValue
import android.view.*
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.*
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.core.widget.CompoundButtonCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.textfield.TextInputLayout
import com.mohamadamin.persianmaterialdatetimepicker.date.DatePickerDialog
import com.mohamadamin.persianmaterialdatetimepicker.utils.PersianCalendar
import com.pleon.buyt.R
import com.pleon.buyt.model.Category
import com.pleon.buyt.model.Item
import com.pleon.buyt.model.Quantity
import com.pleon.buyt.model.Quantity.Unit
import com.pleon.buyt.model.Store
import com.pleon.buyt.ui.NumberInputWatcher
import com.pleon.buyt.ui.activity.BaseActivity
import com.pleon.buyt.ui.activity.MainActivity
import com.pleon.buyt.ui.dialog.DatePickerFragment
import com.pleon.buyt.ui.dialog.SelectDialogFragment
import com.pleon.buyt.ui.dialog.SelectionDialogRow
import com.pleon.buyt.viewmodel.AddItemViewModel
import com.pleon.buyt.viewmodel.MainViewModel
import ir.huri.jcal.JalaliCalendar
import kotlinx.android.synthetic.main.fragment_add_item.*
import kotlinx.android.synthetic.main.fragment_add_item.view.*
import java.text.SimpleDateFormat
import java.util.*

/**
 * This fragment requires a Toolbar as it needs to inflate and use a menu item for selection of
 * store category. So the activities using this fragment must have a Toolbar set.
 */
class AddItemFragment : Fragment(), DatePickerDialog.OnDateSetListener, SelectDialogFragment.Callback, android.app.DatePickerDialog.OnDateSetListener {

    @ColorRes private var colorOnSurface: Int = 0 // this color varies based on the theme
    @ColorRes private var colorError: Int = 0 // this color varies based on the theme

    private lateinit var unitRdbtns: Array<RadioButton>
    private var callback: Callback? = null
    private var viewModel: AddItemViewModel? = null
    private var selectCategoryTxvi: TextView? = null

    private val isBoughtChecked: Boolean
        get() = bought.isChecked

    private val price: Long
        get() {
            return try {
                java.lang.Long.parseLong(priceEd!!.text.toString().replace("[^\\d]".toRegex(), ""))
            } catch (e: NumberFormatException) {
                0
            }
        }

    private val quantity: Quantity
        get() {
            val quantity = java.lang.Long.parseLong(quantityEd!!.text.toString().replace("[^\\d]".toRegex(), ""))

            val idOfSelectedUnit = radio_group.checkedRadioButtonId
            val selectedUnit = view!!.findViewById<RadioButton>(idOfSelectedUnit)
            val indexOfSelectedUnit = radio_group.indexOfChild(selectedUnit)
            val unit = Unit.values()[indexOfSelectedUnit]

            return Quantity(quantity, unit)
        }

    override fun onCreate(savedState: Bundle?) {
        super.onCreate(savedState)

        viewModel = ViewModelProviders.of(this).get(AddItemViewModel::class.java)

        // When an activity is opened from an intent, the bundle of extras is delivered to the activity
        // both when the configuration changes and when the system restores the activity from process kill.
        viewModel!!.itemOrder = activity!!.intent.getIntExtra(MainActivity.EXTRA_ITEM_ORDER, 0)

        val typedValue = TypedValue()
        context!!.theme.resolveAttribute(R.attr.colorError, typedValue, true)
        colorError = typedValue.resourceId

        context!!.theme.resolveAttribute(R.attr.colorOnSurface, typedValue, true)
        colorOnSurface = typedValue.resourceId
    }

    /**
     * Your fragments can contribute menu items to the activity's Options Menu (and, consequently,
     * the app bar) by implementing onCreateOptionsMenu(). In order for this method to receive calls,
     * however, setHasOptionsMenu() must be called during onCreate(), to indicate that the fragment
     * would like to add items to the Options Menu. Otherwise, the fragment doesn't receive a call
     * to onCreateOptionsMenu(). Any items that you then add to the Options Menu from the fragment
     * are appended to the existing menu items.
     *
     *
     * Note: Although your fragment receives an on-item-selected callback for each menu item it adds,
     * the activity is first to receive the respective callback when the user selects a menu item.
     * If the activity's implementation of the on-item-selected callback does not handle the
     * selected item, then the event is passed to the fragment's callback.
     *
     * @param inflater
     * @param container
     * @param savedState
     * @return
     */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_add_item, container, false)
    }

    override fun onViewCreated(view: View, savedState: Bundle?) {
        unitRdbtns = arrayOf(view.unit, view.kilogram, view.gram)

        setHasOptionsMenu(true) // for the onCreateOptionsMenu() method to be called

        // disable by default (because quantity input is not focused yet)
        for (unitRdbtn in unitRdbtns) {
            unitRdbtn.isEnabled = false
        }

        // Set up auto complete for item name
        viewModel!!.itemNames.observe(viewLifecycleOwner, Observer { names ->
            val adapter = ArrayAdapter<String>(context!!,
                    android.R.layout.simple_dropdown_item_1line, names)
            name!!.setAdapter<ArrayAdapter<String>>(adapter)
        })

        val priceSuffix = getString(R.string.input_suffix_price)
        priceEd.addTextChangedListener(NumberInputWatcher(price_layout, priceEd, priceSuffix))
        quantityEd.addTextChangedListener(NumberInputWatcher(quantity_layout, quantityEd, null))

        dateEd.setOnClickListener { onDateClicked() }
        dateEd.setOnFocusChangeListener { _, hasFocus -> onDateGainedFocus(hasFocus) }
        urgent.setOnCheckedChangeListener { _, isChecked -> onUrgentToggled(isChecked) }
        bought.setOnCheckedChangeListener { _, isChecked -> onBoughtToggled(isChecked) }
        quantityEd.setOnFocusChangeListener { _, hasFocus -> onQuantityFocusChanged(hasFocus) }
        name.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                onNameChanged()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        quantityEd.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                onQuantityChanged()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        description.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                onDescriptionChanged()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    /**
     * For this method to be called, it is required that setHasOptionsMenu() has been set.
     *
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

        if (viewModel!!.store != null) {
            selectCategoryTxvi!!.text = viewModel!!.store!!.name
            selectCategoryTxvi!!.setCompoundDrawablesRelativeWithIntrinsicBounds(viewModel!!.store!!.category.storeImageRes, 0, 0, 0)
        } else if (bought.isChecked) {
            selectCategoryTxvi!!.setText(R.string.menu_title_select_store)
            selectCategoryTxvi!!.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_store, 0, 0, 0)
        }

        // Setting up "Choose category" action because it has custom layout
        val menuItem = menu.findItem(R.id.action_item_category)
        menuItem.actionView.setOnClickListener { v -> onOptionsItemSelected(menuItem) }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_item_category) {
            // FIXME: initialize this only once
            val selectionList = ArrayList<SelectionDialogRow>() // dialog requires ArrayList
            if (isBoughtChecked) {
                ViewModelProviders.of(this).get(MainViewModel::class.java).allStores.observe(viewLifecycleOwner, Observer { stores ->
                    viewModel!!.storeList = stores
                    selectionList.clear()
                    for (store in stores) {
                        val selection = SelectionDialogRow(store.name, store.category.storeImageRes)
                        selectionList.add(selection)
                    }
                    val selectStoreDialog = SelectDialogFragment
                            .newInstance(this, R.string.dialog_title_select_store, selectionList)
                    selectStoreDialog.show(activity!!.supportFragmentManager, "SELECT_ITEM_DIALOG")
                })
            } else {
                for (category in Category.values()) {
                    val selection = SelectionDialogRow(getString(category.nameRes), category.imageRes)
                    selectionList.add(selection)
                }
                val selectStoreDialog = SelectDialogFragment
                        .newInstance(this, R.string.dialog_title_select_cat, selectionList)
                selectStoreDialog.show(activity!!.supportFragmentManager, "SELECT_ITEM_DIALOG")
            }
        }
        return true
    }

    // FIXME: If date picker dialog is shown (at least once) and a configuration change happens
    // (e.g. screen rotation), then the date picker is shown. This problem seems to have nothing
    // to do with the following two methods calling each other.

    /**
     * DatePickerDialog.show() for persian calendar is passed getActivity().getFragmentManager()
     * because it requires android.app.FragmentManager instead of androidx version.
     *
     *
     * DatePickerFragment.show() is passed getChildFragmentManager() can get the parent (this)
     * fragment and set it as the callback.
     */
    private fun onDateClicked() {
        if (activity!!.resources.configuration.locale.displayName == "فارسی (ایران)") {
            val persianCal = PersianCalendar()
            val datePicker = DatePickerDialog
                    .newInstance(this, persianCal.persianYear, persianCal.persianMonth, persianCal.persianDay)
            val theme = PreferenceManager.getDefaultSharedPreferences(context).getString("theme", BaseActivity.DEFAULT_THEME)
            if (theme == BaseActivity.DEFAULT_THEME) {
                datePicker.isThemeDark = true // if you want to change colors see colors.xml
            }
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
            val datePicker = DatePickerFragment()
            //            datePicker.setRetainInstance(true);
            datePicker.show(childFragmentManager, "DATE_PICKER")
        }
    }

    /**
     * Calls the onClick method of the date field.
     *
     *
     * This method is required because if focusable is set to true (and that is required
     * because of the textField border to change color), then the first click on the text view
     * will not open the date picker dialog because if the field does not have focus the first
     * click on it only requests focus.
     *
     * @param focused if the view is focused
     */
    private fun onDateGainedFocus(focused: Boolean) {
        if (focused) {
            onDateClicked()
        }
    }

    // On result of Persian date picker

    /**
     * Because we want all the dates in the database to be in same format, we convert the given
     * persian date to Gregorian.
     *
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
        var month = month
        // PersianCalendar cal = new PersianCalendar();
        // cal.set(year, month, day);

        val jalaliCalendar = JalaliCalendar(year, ++month, day)
        viewModel!!.purchaseDate = jalaliCalendar.toGregorian().time

        val date = String.format(resources.configuration.locale,
                "%s %d %s %d", jalaliCalendar.dayOfWeekString, jalaliCalendar.day,
                jalaliCalendar.monthString, jalaliCalendar.year)

        dateEd.setText(date)
    }

    // On result of Gregorian date picker
    override fun onDateSet(view: DatePicker, year: Int, month: Int, dayOfMonth: Int) {
        val cal = Calendar.getInstance()
        cal.set(year, month, dayOfMonth)
        viewModel!!.purchaseDate = cal.time

        val format = "MM/dd/yyyy"
        val dateFormat = SimpleDateFormat(format, Locale.US)
        dateEd.setText(dateFormat.format(viewModel!!.purchaseDate))
    }

    private fun onUrgentToggled(isChecked: Boolean) {
        urgent.setButtonDrawable(if (isChecked) R.drawable.avd_urgent_check else R.drawable.avd_urgent_uncheck)
        (CompoundButtonCompat.getButtonDrawable(urgent) as Animatable).start()
    }

    private fun onBoughtToggled(checked: Boolean) {
        if (selectCategoryTxvi != null) { // to fix bug on config change
            selectCategoryTxvi!!.text = if (checked) getString(R.string.menu_title_select_store) else getString(viewModel!!.category.nameRes)
            selectCategoryTxvi!!.setTextColor(ContextCompat.getColor(context!!, colorOnSurface))
            @DrawableRes val icon = if (checked) R.drawable.avd_store_error else R.drawable.ic_item_grocery
            selectCategoryTxvi!!.setCompoundDrawablesRelativeWithIntrinsicBounds(icon, 0, 0, 0)
        }
        bought_group.visibility = if (checked) VISIBLE else GONE
        price_layout.error = null
        if (!checked) {
            viewModel!!.store = null
        }
    }

    private fun onQuantityFocusChanged(hasFocus: Boolean) {
        // should enable and set the new color for EACH radio button
        for (unitRdbtn in unitRdbtns) {
            unitRdbtn.isEnabled = hasFocus
        }

        var color = colorError
        if (hasFocus && quantity_layout!!.error == null) {
            color = R.color.colorPrimary
        } else if (!hasFocus && quantity_layout!!.error == null) {
            color = R.color.unfocused
        }
        setColorOfAllUnitsForEnabledState(color)
    }

    private fun setColorOfAllUnitsForEnabledState(color: Int) {
        val unitRdbtns = arrayOf(unit, kilogram, gram)
        for (unitRdbtn in unitRdbtns) {
            val sld = unitRdbtn.background as StateListDrawable
            val dcs = sld.constantState as DrawableContainerState
            // <color> element for checked state (<color> index 3 in unit_background_selector.xml)
            val checkedColor = dcs.getChild(3) as ColorDrawable

            checkedColor.color = resources.getColor(color)
        }
    }

    private fun onNameChanged() {
        if (!name!!.text.toString().isEmpty()) { // to prevent error with config change
            name_layout!!.error = null // clear error if exists
            setCounterEnabledIfInputLong(name_layout!!)
        }
    }

    private fun onQuantityChanged() {
        if (!quantityEd!!.text.toString().isEmpty()) { // to prevent error with config change
            quantity_layout!!.error = null // clear error if exists
        }
        setColorOfAllUnitsForEnabledState(R.color.colorPrimary)
    }

    private fun onDescriptionChanged() {
        setCounterEnabledIfInputLong(description_layout)
    }

    private fun setCounterEnabledIfInputLong(layout: TextInputLayout) {
        val inputLength = layout.editText!!.text.toString().length
        layout.isCounterEnabled = inputLength > layout.counterMaxLength * 0.66
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is Callback) {
            callback = context
        } else {
            throw RuntimeException("$context must implement Callback")
        }
    }

    override fun onDetach() {
        super.onDetach()
        callback = null
    }

    fun onDonePressed() {
        val validated = validateFields()

        if (validated) {
            val name = name.text.toString()
            val quantity = quantity
            val item = Item(name, quantity, urgent.isChecked, bought.isChecked, viewModel!!.category)
            item.position = viewModel!!.itemOrder

            if (!isEmpty(description)) {
                item.description = description.text.toString()
            }
            if (isBoughtChecked && !isEmpty(priceEd)) {
                item.totalPrice = price
            }

            if (isBoughtChecked) {
                item.category = viewModel!!.store!!.category
                callback!!.onSubmitPurchasedItem(item, viewModel!!.store!!, viewModel!!.purchaseDate)
            } else {
                callback!!.onSubmit(item)
            }
        }
    }

    override fun onSelected(index: Int) {
        val name: String
        val imageRes: Int
        if (isBoughtChecked) {
            viewModel!!.store = viewModel!!.storeList!![index]
            name = viewModel!!.store!!.name
            imageRes = viewModel!!.store!!.category.storeImageRes
        } else {
            val category = Category.values()[index]
            name = resources.getString(category.nameRes)
            imageRes = category.imageRes
            viewModel!!.category = category
        }
        selectCategoryTxvi!!.setCompoundDrawablesRelativeWithIntrinsicBounds(imageRes, 0, 0, 0)
        selectCategoryTxvi!!.setTextColor(ContextCompat.getColor(context!!, colorOnSurface))
        selectCategoryTxvi!!.text = name
    }

    private fun validateFields(): Boolean {
        var validated = true

        if (isEmpty(name!!)) {
            name_layout!!.error = getString(R.string.input_error_name)
            validated = false
        }
        if (isEmpty(quantityEd!!)) {
            quantity_layout!!.error = getString(R.string.input_error_quantity)
            setColorOfAllUnitsForEnabledState(colorError)
            validated = false
        }
        if (bought.isChecked && (isEmpty(priceEd) || price == 0L)) {
            price_layout.error = getString(R.string.input_error_price)
            validated = false
        }
        if (isBoughtChecked && viewModel!!.store == null) {
            selectCategoryTxvi!!.setTextColor(ContextCompat.getColor(context!!, colorError))
            selectCategoryTxvi!!.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.avd_store_error, 0, 0, 0)
            (selectCategoryTxvi!!.compoundDrawablesRelative[0] as Animatable).start()
            validated = false
        }

        return validated
    }

    private fun isEmpty(editText: EditText) =
            editText.text.toString().trim { it <= ' ' }.isEmpty()

    interface Callback {
        fun onSubmit(item: Item)
        fun onSubmitPurchasedItem(item: Item, store: Store, purchaseDate: Date)
    }
}
