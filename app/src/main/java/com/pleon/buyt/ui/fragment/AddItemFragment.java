package com.pleon.buyt.ui.fragment;

import android.app.DatePickerDialog;
import android.content.Context;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.DrawableContainer.DrawableContainerState;
import android.graphics.drawable.StateListDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.google.android.material.textfield.TextInputLayout;
import com.mohamadamin.persianmaterialdatetimepicker.utils.PersianCalendar;
import com.pleon.buyt.R;
import com.pleon.buyt.model.Item;
import com.pleon.buyt.model.Quantity;
import com.pleon.buyt.model.Quantity.Unit;
import com.pleon.buyt.model.Store;
import com.pleon.buyt.ui.dialog.DatePickerFragment;
import com.pleon.buyt.ui.dialog.SelectDialogFragment;
import com.pleon.buyt.ui.dialog.SelectionDialogRow;
import com.pleon.buyt.viewmodel.MainViewModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.Group;
import androidx.core.widget.CompoundButtonCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import butterknife.BindView;
import butterknife.BindViews;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;
import butterknife.OnFocusChange;
import butterknife.OnTextChanged;
import butterknife.Unbinder;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static butterknife.OnTextChanged.Callback.AFTER_TEXT_CHANGED;

/**
 * This fragment requires a Toolbar as it needs to inflate and use a menu item for selection of
 * store category. So the activities using this fragment must have a Toolbar set.
 */
public class AddItemFragment extends Fragment
        implements SelectDialogFragment.Callback, DatePickerDialog.OnDateSetListener,
        com.mohamadamin.persianmaterialdatetimepicker.date.DatePickerDialog.OnDateSetListener {

    public interface Callback {

        void onSubmit(Item item);

        void onSubmit(Item item, Store store);
    }

    @BindView(R.id.name_layout) TextInputLayout nameTxInLt;
    @BindView(R.id.name) EditText nameEdtx;
    @BindView(R.id.quantity_layout) TextInputLayout quantityTxinlt;
    @BindView(R.id.quantity) EditText quantityEdtx;
    @BindView(R.id.radio_group) RadioGroup unitRdgrp;
    @BindViews({R.id.unit, R.id.kilogram, R.id.gram}) RadioButton[] unitRdbtns;
    @BindView(R.id.description) EditText descriptionEdtx;
    @BindView(R.id.urgent) CheckBox urgentChbx;
    @BindView(R.id.bought) CheckBox boughtChbx;
    @BindView(R.id.price_layout) TextInputLayout priceTxinlt;
    @BindView(R.id.price) EditText priceEdtx;
    @BindView(R.id.bought_group) Group boughtContainer;
    @BindView(R.id.date_layout) TextInputLayout dateTxinlt;
    @BindView(R.id.date) EditText dateEdtx;

    private static int LAST_ITEM_ORDER;

    private Item.Category itemCategory = Item.Category.GROCERY;
    private Callback callback;
    private TextView selectCategoryTxvi;
    private List<Store> storeList;
    private Unbinder unbinder;
    private Store store;

    public AddItemFragment() {
        // Required empty constructor
    }

    public static AddItemFragment newInstance(int nextItemOrder) {
        AddItemFragment fragment = new AddItemFragment();

        Bundle args = new Bundle();
        args.putInt("NEXT_ITEM_ORDER", nextItemOrder);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            LAST_ITEM_ORDER = getArguments().getInt("NEXT_ITEM_ORDER");
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_item, container, false);
        unbinder = ButterKnife.bind(this, view); // unbind() is required only for Fragments
        setHasOptionsMenu(true); // for the onCreateOptionsMenu() method to be called

        for (RadioButton unitRdbtn : unitRdbtns) {
            // disable by default (because quantity input is not focused yet)
            unitRdbtn.setEnabled(false);
        }

        return view;
    }

    /**
     * For this method to be called, it is required that setHasOptionsMenu() has been set.
     * <p>
     * Note that the containing activity must have a Toolbar set so this fragment can inflate and
     * use its own menu item.
     *
     * @param menu
     * @param inflater
     */
    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_add_item, menu);

        selectCategoryTxvi = menu.findItem(R.id.action_item_category).getActionView().findViewById(R.id.select_store);
        // Setting up "Choose category" action because it has custom layout
        MenuItem menuItem = menu.findItem(R.id.action_item_category);
        menuItem.getActionView().setOnClickListener(v -> onOptionsItemSelected(menuItem));
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_item_category) {
            // FIXME: initialize this only once
            ArrayList<SelectionDialogRow> selectionList = new ArrayList<>(); // dialog requires ArrayList
            if (isBoughtChecked()) {
                ViewModelProviders.of(this).get(MainViewModel.class).getAllStores().observe(this, stores -> {
                    storeList = stores;
                    selectionList.clear();
                    for (Store store : stores) {
                        SelectionDialogRow selection = new SelectionDialogRow(store.getName(), store.getCategory().getImageRes());
                        selectionList.add(selection);
                    }
                    SelectDialogFragment selectStoreDialog = SelectDialogFragment.newInstance(this, selectionList);
                    selectStoreDialog.show(getActivity().getSupportFragmentManager(), "SELECT_ITEM_DIALOG");
                });
            } else {
                for (Item.Category category : Item.Category.values()) {
                    SelectionDialogRow selection = new SelectionDialogRow(getString(category.getNameRes()), category.getImageRes());
                    selectionList.add(selection);
                }
                SelectDialogFragment selectStoreDialog = SelectDialogFragment.newInstance(this, selectionList);
                selectStoreDialog.show(getActivity().getSupportFragmentManager(), "SELECT_ITEM_DIALOG");
            }
        }
        return true;
    }

    // FIXME: If date picker dialog is shown (at least once) and a configuration change happens
    // (e.g. screen rotation), then the date picker is shown. This problem seems to have nothing
    // to do with the following two methods calling each other.

    /**
     * DatePickerDialog.show() for persian calendar is passed getActivity().getFragmentManager()
     * because it requires android.app.FragmentManager instead of androidx version.
     * <p>
     * DatePickerFragment.show() is passed getChildFragmentManager() can get the parent (this)
     * fragment and set it as the callback.
     */
    @OnClick(R.id.date)
    void onDateClicked() {
        if (getActivity().getResources().getConfiguration().locale.getDisplayName().equals("فارسی (ایران)")) {
            PersianCalendar persianCal = new PersianCalendar();
            com.mohamadamin.persianmaterialdatetimepicker.date.DatePickerDialog datePicker = com.mohamadamin.persianmaterialdatetimepicker.date.DatePickerDialog
                    .newInstance(this, persianCal.getPersianYear(), persianCal.getPersianMonth(), persianCal.getPersianDay());
            datePicker.setThemeDark(true); // if you want to change colors see colors.xml
            datePicker.show(getActivity().getFragmentManager(), "DATE_PICKER");
        } else {
            new DatePickerFragment().show(getChildFragmentManager(), "DATE_PICKER");
        }
    }

    /**
     * Calls the onClick method of the date field.
     * <p>
     * This method is required because if focusable is set to true (and that is required
     * because of the textField border to change color), then the first click on the text view
     * will not open the date picker dialog because if the field does not have focus the first
     * click on it only requests focus.
     *
     * @param focused if the view is focused
     */
    @OnFocusChange(R.id.date)
    void onDateGainedFocus(boolean focused) {
        if (focused) {
            onDateClicked();
        }
    }

    // On result of Persian date picker
    @Override
    public void onDateSet(com.mohamadamin.persianmaterialdatetimepicker.date.DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
        PersianCalendar cal = new PersianCalendar();
        cal.set(year, monthOfYear, dayOfMonth);

        String myFormat = "yyyy/MM/dd";
        SimpleDateFormat dateFormat = new SimpleDateFormat(myFormat, Locale.US);
        dateEdtx.setText(dateFormat.format(cal.getTime()));
    }

    // On result of Gregorian date picker
    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        Calendar cal = Calendar.getInstance();
        cal.set(year, month, dayOfMonth);

        String myFormat = "MM/dd/yyyy";
        SimpleDateFormat dateFormat = new SimpleDateFormat(myFormat, Locale.US);
        dateEdtx.setText(dateFormat.format(cal.getTime()));
    }

    @OnCheckedChanged(R.id.urgent)
    void onUrgentToggled(boolean isChecked) {
        urgentChbx.setButtonDrawable(isChecked ? R.drawable.avd_urgent_check : R.drawable.avd_urgent_uncheck);
        ((Animatable) CompoundButtonCompat.getButtonDrawable(urgentChbx)).start();
    }

    @OnCheckedChanged(R.id.bought)
    void onBoughtToggled(boolean checked) {
        selectCategoryTxvi.setText(checked ? getString(R.string.action_select_store) : getString(itemCategory.getNameRes()));
        boughtContainer.setVisibility(checked ? VISIBLE : GONE);
    }

    @OnFocusChange(R.id.quantity)
    void onQuantityFocusChanged(boolean hasFocus) {
        // should enable and set the new color for EACH radio button
        for (RadioButton unitButton : unitRdbtns) {
            unitButton.setEnabled(hasFocus);
        }
        int color = R.color.error;
        if (hasFocus && quantityTxinlt.getError() == null) {
            color = R.color.colorPrimary;
        } else if (!hasFocus && quantityTxinlt.getError() == null) {
            color = R.color.unfocused;
        }
        setColorOfAllUnitsForEnabledState(color);
    }

    private void setColorOfAllUnitsForEnabledState(int color) {
        for (RadioButton unitRdbtn : unitRdbtns) {
            StateListDrawable sld = (StateListDrawable) unitRdbtn.getBackground();
            DrawableContainerState dcs = (DrawableContainerState) sld.getConstantState();
            // <color> element for checked state (<color> index 3 in unit_background_selector.xml)
            ColorDrawable checkedColor = (ColorDrawable) dcs.getChild(3);

            checkedColor.setColor(getResources().getColor(color));
        }
    }

    @OnTextChanged(value = R.id.name, callback = AFTER_TEXT_CHANGED)
    void onNameChanged() {
        nameTxInLt.setError(null); // clear error if exists
    }

    @OnTextChanged(value = R.id.quantity, callback = AFTER_TEXT_CHANGED)
    void onQuantityChanged() {
        quantityTxinlt.setError(null); // clear error if exists
        setColorOfAllUnitsForEnabledState(R.color.colorPrimary);
    }

    @OnTextChanged(value = R.id.price, callback = AFTER_TEXT_CHANGED)
    void onPriceChanged() {
        priceTxinlt.setError(null); // clear error if exists
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof Callback) {
            callback = (Callback) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement Callback");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        callback = null;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // set the bindings to null (frees up memory)
        unbinder.unbind();
    }

    private boolean isBoughtChecked() {
        return boughtChbx.isChecked();
    }

    public void onDonePressed() {
        boolean validated = validateFields();

        if (validated) {
            String name = nameEdtx.getText().toString();
            Quantity quantity = getQuantity();
            Item item = new Item(name, quantity, urgentChbx.isChecked(), boughtChbx.isChecked(), itemCategory);
            item.setPosition(LAST_ITEM_ORDER);

            if (!isEmpty(descriptionEdtx)) {
                item.setDescription(descriptionEdtx.getText().toString());
            }
            if (isBoughtChecked() && !isEmpty(priceEdtx)) {
                item.setTotalPrice(Long.parseLong(priceEdtx.getText().toString()));
            }

            if (isBoughtChecked()) {
//                item.setCategory(); // TODO: set it to the category of the selected store
                callback.onSubmit(item, store);
            } else {
                callback.onSubmit(item);
            }
        }
    }

    @Override
    public void onSelected(int index) {
        String name;
        int imageRes;
        if (isBoughtChecked()) {
            Store.Category category = Store.Category.values()[index];
            imageRes = category.getImageRes();
            name = storeList.get(index).getName();
            store = storeList.get(index);
        } else {
            Item.Category category = Item.Category.values()[index];
            imageRes = category.getImageRes();
            name = getResources().getString(category.getNameRes());
            itemCategory = category;
        }
        selectCategoryTxvi.setCompoundDrawablesRelativeWithIntrinsicBounds(imageRes, 0, 0, 0);
        selectCategoryTxvi.setText(name);
    }

    private boolean validateFields() {
        boolean validated = true;

        if (isEmpty(nameEdtx)) {
            nameTxInLt.setError("Name cannot be empty");
            validated = false;
        }
        if (isEmpty(quantityEdtx)) {
            quantityTxinlt.setError("Quantity should be specified");
            setColorOfAllUnitsForEnabledState(R.color.error);
            validated = false;
        }
        if (boughtChbx.isChecked() && isEmpty(priceEdtx)) {
            priceTxinlt.setError("Price should be specified");
            validated = false;
        }

        return validated;
    }

    private Quantity getQuantity() {
        long quantity = Long.parseLong(quantityEdtx.getText().toString());

        int idOfSelectedUnit = unitRdgrp.getCheckedRadioButtonId();
        RadioButton SelectedUnit = getView().findViewById(idOfSelectedUnit);
        int indexOfSelectedUnit = unitRdgrp.indexOfChild(SelectedUnit);
        Unit unit = Unit.values()[indexOfSelectedUnit];

        return new Quantity(quantity, unit);
    }

    private boolean isEmpty(@NonNull EditText editText) {
        return editText.getText().toString().trim().length() == 0;
    }
}
