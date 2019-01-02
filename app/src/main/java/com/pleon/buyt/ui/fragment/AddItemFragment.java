package com.pleon.buyt.ui.fragment;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.DrawableContainer.DrawableContainerState;
import android.graphics.drawable.StateListDrawable;
import android.os.Bundle;
import android.text.Editable;
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
import com.pleon.buyt.TextWatcherAdapter;
import com.pleon.buyt.model.Item;
import com.pleon.buyt.model.Quantity;
import com.pleon.buyt.model.Quantity.Unit;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link AddItemFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link AddItemFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AddItemFragment extends Fragment {

    public interface Callback {

        void onSubmit(Item item);

        void onBoughtToggle(boolean checked);
    }

    private Callback callback;

    private TextInputLayout nameTxInLt;
    private EditText nameEdtx;
    private TextInputLayout quantityTxinlt;
    private EditText quantityEdtx;
    private RadioGroup unitRdgrp;
    private RadioButton[] unitRdbtns;
    private EditText descriptionEdtx;
    private CheckBox urgentChbx;
    private CheckBox boughtChbx;
    private TextInputLayout priceTxinlt;
    private EditText priceEdtx;

    private long selectedStoreId;

    private static int NEXT_ITEM_ORDER;

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
            NEXT_ITEM_ORDER = getArguments().getInt("NEXT_ITEM_ORDER");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_add_item, container, false);

        nameTxInLt = view.findViewById(R.id.name_layout);
        nameEdtx = view.findViewById(R.id.name);
        quantityTxinlt = view.findViewById(R.id.quantity_layout);
        quantityEdtx = view.findViewById(R.id.quantity);
        unitRdgrp = view.findViewById(R.id.radio_group);
        unitRdbtns = new RadioButton[unitRdgrp.getChildCount()];
        descriptionEdtx = view.findViewById(R.id.description);
        urgentChbx = view.findViewById(R.id.urgent);
        boughtChbx = view.findViewById(R.id.bought);
        priceTxinlt = view.findViewById(R.id.price_layout);
        priceEdtx = view.findViewById(R.id.price);

        setupListeners();

        FrameLayout priceContainer = view.findViewById(R.id.price_container);
        boughtChbx.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    callback.onBoughtToggle(isChecked);
                    priceContainer.setVisibility(isChecked ? VISIBLE : GONE);
                }
        );

        for (int i = 0; i < unitRdgrp.getChildCount(); i++) {
            unitRdbtns[i] = (RadioButton) unitRdgrp.getChildAt(i);
            // disable by default (because quantity input is not focused yet)
            unitRdbtns[i].setEnabled(false);
        }

        quantityEdtx.setOnFocusChangeListener((v, hasFocus) -> {
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
        );
        return view;
    }

    private void setColorOfAllUnitsForEnabledState(int color) {
        for (RadioButton unitRdbtn : unitRdbtns) {
            StateListDrawable sld = (StateListDrawable) unitRdbtn.getBackground();
            DrawableContainerState dcs = (DrawableContainerState) sld.getConstantState();
            // <color> element for checked state (<color> index 3 in unit_selector.xml)
            ColorDrawable checkedColor = (ColorDrawable) dcs.getChild(3);

            checkedColor.setColor(getResources().getColor(color));
        }
    }

    private void setupListeners() {
        nameEdtx.addTextChangedListener(new TextWatcherAdapter() {
            @Override
            public void afterTextChanged(Editable s) {
                nameTxInLt.setError(null); // clear error if exists
            }
        });

        quantityEdtx.addTextChangedListener(new TextWatcherAdapter() {
            @Override
            public void afterTextChanged(Editable s) {
                quantityTxinlt.setError(null); // clear error if exists
                setColorOfAllUnitsForEnabledState(R.color.colorPrimary);
            }
        });

        priceEdtx.addTextChangedListener(new TextWatcherAdapter() {
            @Override
            public void afterTextChanged(Editable s) {
                priceTxinlt.setError(null); // clear error if exists
            }
        });
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
                item.setPrice(Long.parseLong(priceEdtx.getText().toString()));
            }

            item.setPosition(NEXT_ITEM_ORDER);
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
