package com.pleon.buyt.ui

import android.text.Editable
import android.widget.EditText
import com.google.android.material.textfield.TextInputLayout
import com.pleon.buyt.util.FormatterUtil.formatPrice
import com.pleon.buyt.util.TextUtil.removeNonDigitChars

class NumberInputWatcher(private val textInputLayout: TextInputLayout,
                         private val editText: EditText,
                         private val suffix: String = "")
    : TextWatcherAdapter() {

    override fun afterTextChanged(s: Editable) {
        textInputLayout.error = null // clear error if exists

        editText.removeTextChangedListener(this)
        val numberString = editText.text!!.removeNonDigitChars()
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
