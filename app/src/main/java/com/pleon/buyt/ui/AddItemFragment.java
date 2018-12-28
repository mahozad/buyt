package com.pleon.buyt.ui;

import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.DrawableContainer.DrawableContainerState;
import android.graphics.drawable.StateListDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.pleon.buyt.R;

import androidx.fragment.app.Fragment;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link AddItemFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link AddItemFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AddItemFragment extends Fragment {

    private EditText nameField;
    private EditText priceField;

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

        nameField = view.findViewById(R.id.name);
        priceField = view.findViewById(R.id.price);

        RadioGroup radioGroup = view.findViewById(R.id.radio_group);
        RadioButton[] radioButtons = new RadioButton[radioGroup.getChildCount()];
        for (int i = 0; i < radioGroup.getChildCount(); i++) {
            radioButtons[i] = (RadioButton) radioGroup.getChildAt(i);
        }
        view.findViewById(R.id.quantity).setOnFocusChangeListener((v, hasFocus) -> {
                    // should set the new color for all radio buttons
                    for (RadioButton radioButton : radioButtons) {
                        StateListDrawable sld = (StateListDrawable) radioButton.getBackground();
                        DrawableContainerState dcs = (DrawableContainerState) sld.getConstantState();
                        // <color> element for checked state (<color> index 3 in unit_selector.xml)
                        ColorDrawable checkedColor = (ColorDrawable) dcs.getChild(3);

                        int newColor = getResources().getColor(hasFocus ? R.color.colorPrimary : R.color.unfocused);
                        checkedColor.setColor(newColor);
                    }
                }
        );

        return view;
    }

//    public Item getSubmittedItem() {
//
//    }

    public String getItemName() {
        return nameField.getText().toString();
    }

    public float getItemPrice() {
        return Float.parseFloat(priceField.getText().toString());
    }
}
