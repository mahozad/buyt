package com.pleon.buyt.ui.fragment;

import android.content.Context;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.DrawableContainer.DrawableContainerState;
import android.graphics.drawable.StateListDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.google.android.material.textfield.TextInputLayout;
import com.pleon.buyt.R;
import com.pleon.buyt.model.Item;
import com.pleon.buyt.model.Quantity;
import com.pleon.buyt.model.Quantity.Unit;

import androidx.annotation.NonNull;
import androidx.core.widget.CompoundButtonCompat;
import androidx.fragment.app.Fragment;
import butterknife.BindView;
import butterknife.BindViews;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnFocusChange;
import butterknife.OnTextChanged;
import butterknife.Unbinder;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static butterknife.OnTextChanged.Callback.AFTER_TEXT_CHANGED;

public class AddItemFragment extends Fragment {

    public interface Callback {

        void onSubmit(Item item);

        void onBoughtToggled(boolean checked);
    }

    private Callback callback;

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
    @BindView(R.id.price_container) FrameLayout priceContainer;

    private Unbinder unbinder;

    private long selectedStoreId;

    private static int LAST_ITEM_ORDER;

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

        for (RadioButton unitRdbtn : unitRdbtns) {
            // disable by default (because quantity input is not focused yet)
            unitRdbtn.setEnabled(false);
        }

        return view;
    }

    @OnCheckedChanged(R.id.urgent)
    void onUrgentToggled(boolean isChecked) {
        urgentChbx.setButtonDrawable(isChecked ? R.drawable.avd_urgent_check : R.drawable.avd_urgent_uncheck);
        ((Animatable) CompoundButtonCompat.getButtonDrawable(urgentChbx)).start();
    }

    @OnCheckedChanged(R.id.bought)
    void onBoughtToggled(boolean isChecked) {
        callback.onBoughtToggled(isChecked);
        priceContainer.setVisibility(isChecked ? VISIBLE : GONE);
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

    public void onDonePressed() {
        boolean validated = validateFields();

        if (validated) {
            String name = nameEdtx.getText().toString();
            Quantity quantity = getQuantity();
            Item item = new Item(name, quantity, urgentChbx.isChecked(), boughtChbx.isChecked());

            if (!isEmpty(descriptionEdtx)) {
                item.setDescription(descriptionEdtx.getText().toString());
            }
            if (!isEmpty(priceEdtx)) {
                item.setTotalPrice(Long.parseLong(priceEdtx.getText().toString()));
            }

            item.setPosition(LAST_ITEM_ORDER);
            callback.onSubmit(item);
        }
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
