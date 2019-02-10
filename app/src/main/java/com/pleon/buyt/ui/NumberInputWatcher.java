package com.pleon.buyt.ui;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import com.google.android.material.textfield.TextInputLayout;

import java.text.DecimalFormat;

import androidx.annotation.Nullable;

public class NumberInputWatcher implements TextWatcher {

    private final DecimalFormat priceFormat = new DecimalFormat("#,###");
    private TextInputLayout textInputLayout;
    private EditText editText;
    private String inputSuffix;

    public NumberInputWatcher(TextInputLayout textInputLayout, EditText editText, @Nullable String inputSuffix) {
        this.textInputLayout = textInputLayout;
        this.editText = editText;
        this.inputSuffix = inputSuffix == null ? "" : inputSuffix;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        textInputLayout.setError(null); // clear error if exists

        editText.removeTextChangedListener(this);
        String numberString = editText.getText().toString().replaceAll("[^\\d]", "");
        if (numberString.isEmpty()) {
            editText.setText(inputSuffix);
            editText.setSelection(0);
        } else {
            editText.setText(priceFormat.format(Long.parseLong(numberString)) + inputSuffix);
            editText.setSelection(editText.getText().length() - inputSuffix.length());
        }
        editText.addTextChangedListener(this);
    }
}
