package com.pleon.buyt.ui

import android.content.Context
import android.util.AttributeSet
import com.google.android.material.textfield.TextInputEditText
import com.pleon.buyt.R
import kotlin.math.abs

class TextInputEditTextWithSuffix : TextInputEditText {

    private var suffix: String? = ""

    constructor(cxt: Context) : super(cxt)

    constructor(cxt: Context, attrs: AttributeSet) : super(cxt, attrs) {
        getAttributes(cxt, attrs, 0)
    }

    constructor(cxt: Context, attrs: AttributeSet, defStyleAttr: Int) : super(cxt, attrs, defStyleAttr) {
        getAttributes(cxt, attrs, defStyleAttr)
    }

    private fun getAttributes(cxt: Context, attrs: AttributeSet, defStyleAttr: Int) {
        val a = cxt.obtainStyledAttributes(attrs, R.styleable.TextInputEditTextWithSuffix, defStyleAttr, 0)
        if (a != null) suffix = a.getString(R.styleable.TextInputEditTextWithSuffix_suffix)
        a.recycle()
    }

    /**
     * Reposition the cursor if it is on the suffix.
     */
    override fun onSelectionChanged(selStart: Int, selEnd: Int) {
        if (suffix == null) return
        val userInputLength = abs(text!!.length - suffix!!.length)
        if (selStart > userInputLength || selEnd > userInputLength) {
            setSelection(text!!.length - suffix!!.length)
        }
    }
}
