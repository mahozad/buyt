package com.pleon.buyt.ui

import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import com.google.android.material.textfield.TextInputLayout
import com.pleon.buyt.util.FormatterUtil.formatPrice

class NumberInputWatcher(private val textInputLayout: TextInputLayout,
                         private val editText: EditText,
                         private val suffix: String = "")
    : TextWatcher {

    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}

    override fun afterTextChanged(s: Editable) {
        textInputLayout.error = null // clear error if exists

        editText.removeTextChangedListener(this)
        val numberString = editText.text.toString().replace("[^\\d]".toRegex(), "")
        if (numberString.isEmpty()) {
            editText.setText(suffix)
            editText.setSelection(0)
        } else {
            editText.setText("${formatPrice(numberString)}$suffix")
            editText.setSelection(editText.text.length - suffix.length)
        }
        editText.addTextChangedListener(this)
    }

}
