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
import android.widget.FrameLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.pleon.buyt.R;
import com.pleon.buyt.TextWatcherAdapter;
import com.pleon.buyt.model.Item;
import com.pleon.buyt.model.Quantity;
import com.pleon.buyt.model.Quantity.Unit;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatCheckBox;
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
    }

    private Callback callback;

    private TextInputEditText etName;
    private TextInputLayout tilName;
    private TextInputEditText etQuantity;
    private TextInputLayout tilQuantity;
    private TextInputEditText etDescription;
    private AppCompatCheckBox cbUrgent;
    private AppCompatCheckBox cbBought;
    private TextInputEditText etPrice;
    private TextInputLayout tilPrice;
    private RadioGroup rgUnit;
    private long selectedStoreId;

    private RadioButton[] unitRadioButtons;

    public AddItemFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment AddItemFragment.
     */
    public static AddItemFragment newInstance() {
        AddItemFragment fragment = new AddItemFragment();
//        Bundle args = new Bundle();
//        args.putString(ARG_PARAM1, param1);
//        args.putString(ARG_PARAM2, param2);
//        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        if (getArguments() != null) {
//            mParam1 = getArguments().getString(ARG_PARAM1);
//            mParam2 = getArguments().getString(ARG_PARAM2);
//        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_add_item, container, false);

        etName = view.findViewById(R.id.name);
        tilName = view.findViewById(R.id.name_layout);
        etQuantity = view.findViewById(R.id.quantity);
        tilQuantity = view.findViewById(R.id.quantity_layout);
        etDescription = view.findViewById(R.id.description);
        cbUrgent = view.findViewById(R.id.urgent);
        cbBought = view.findViewById(R.id.bought);
        etPrice = view.findViewById(R.id.price);
        tilPrice = view.findViewById(R.id.price_layout);
        rgUnit = view.findViewById(R.id.radio_group);

        setupListeners();

        FrameLayout priceContainer = view.findViewById(R.id.price_container);
        cbBought.setOnCheckedChangeListener((buttonView, isChecked) ->
                priceContainer.setVisibility(isChecked ? VISIBLE : GONE)
        );

        unitRadioButtons = new RadioButton[rgUnit.getChildCount()];
        for (int i = 0; i < rgUnit.getChildCount(); i++) {
            unitRadioButtons[i] = (RadioButton) rgUnit.getChildAt(i);
            unitRadioButtons[i].setEnabled(false); // disabled by default (because quantity input is not focused)
        }

        etQuantity.setOnFocusChangeListener((v, hasFocus) -> {
                    // should enable and set the new color for EACH radio button
                    for (RadioButton unitButton : unitRadioButtons) {
                        unitButton.setEnabled(hasFocus);
                    }
                    int color = R.color.error;
                    if (hasFocus && tilQuantity.getError() == null) {
                        color = R.color.colorPrimary;
                    } else if (!hasFocus && tilQuantity.getError() == null) {
                        color = R.color.unfocused;
                    }
                    setUnitsColorForEnabledState(color);
                }
        );

        return view;
    }

    private void setUnitsColorForEnabledState(int color) {
        for (RadioButton unitButton : unitRadioButtons) {
            StateListDrawable sld = (StateListDrawable) unitButton.getBackground();
            DrawableContainerState dcs = (DrawableContainerState) sld.getConstantState();
            // <color> element for checked state (<color> index 3 in unit_selector.xml)
            ColorDrawable checkedColor = (ColorDrawable) dcs.getChild(3);

            checkedColor.setColor(getResources().getColor(color));
        }
    }

    private void setupListeners() {
        etName.addTextChangedListener(new TextWatcherAdapter() {
            @Override
            public void afterTextChanged(Editable s) {
                tilName.setError(null); // clear error if exists
            }
        });

        etQuantity.addTextChangedListener(new TextWatcherAdapter() {
            @Override
            public void afterTextChanged(Editable s) {
                tilQuantity.setError(null); // clear error if exists
                setUnitsColorForEnabledState(R.color.colorPrimary);
            }
        });

        etPrice.addTextChangedListener(new TextWatcherAdapter() {
            @Override
            public void afterTextChanged(Editable s) {
                tilPrice.setError(null); // clear error if exists
            }
        });
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof Callback) {
            callback = (Callback) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement Callable");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        callback = null;
    }

    public void onDonePressed() {
        if (!validateFields()) {
            return;
        }

        String name = etName.getText().toString();
        Quantity quantity = getQuantity();

        Item item = new Item(name, quantity, cbUrgent.isChecked(), cbBought.isChecked());

        if (!isEmpty(etDescription)) {
            item.setDescription(etDescription.getText().toString());
        }
        if (!isEmpty(etPrice)) {
            item.setPrice(Long.parseLong(etPrice.getText().toString()));
        }

        callback.onSubmit(item);
    }

    private Quantity getQuantity() {
        long quantity = Long.parseLong(etQuantity.getText().toString());

        int checkedUnitId = rgUnit.getCheckedRadioButtonId();
        RadioButton checkedUnit = getView().findViewById(checkedUnitId);
        int checkedIndex = rgUnit.indexOfChild(checkedUnit);
        Unit quantityUnit = Unit.values()[checkedIndex];

        return new Quantity(quantity, quantityUnit);
    }

    private boolean validateFields() {
        boolean validated = true;

        if (isEmpty(etName)) {
            tilName.setError("Name cannot be empty");
            validated = false;
        }
        if (isEmpty(etQuantity)) {
            tilQuantity.setError("Quantity should be specified");
            setUnitsColorForEnabledState(R.color.error);
            validated = false;
        }
        if (cbBought.isChecked() && isEmpty(etPrice)) {
            tilPrice.setError("Price should be specified");
            validated = false;
        }

        return validated;
    }

    private boolean isEmpty(@NonNull TextInputEditText editText) {
        return editText.getText().toString().trim().length() == 0;
    }
}
