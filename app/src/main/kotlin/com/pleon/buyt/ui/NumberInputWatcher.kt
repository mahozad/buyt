package com.pleon.buyt.ui

import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import com.google.android.material.textfield.TextInputLayout
import com.pleon.buyt.util.FormatterUtil.formatPrice

class NumberInputWatcher(private val textInputLayout: TextInputLayout,
                         private val editText: EditText, inputSuffix: String?
) : TextWatcher {

    private val inputSuffix: String = inputSuffix ?: ""

    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}

    override fun afterTextChanged(s: Editable) {
        textInputLayout.error = null // clear error if exists

        editText.removeTextChangedListener(this)
        val numberString = editText.text.toString().replace("[^\\d]".toRegex(), "")
        if (numberString.isEmpty()) {
            editText.setText(inputSuffix)
            editText.setSelection(0)
        } else {
            editText.setText("${formatPrice(numberString)}$inputSuffix")
            editText.setSelection(editText.text.length - inputSuffix.length)
        }
        editText.addTextChangedListener(this)
    }
}
