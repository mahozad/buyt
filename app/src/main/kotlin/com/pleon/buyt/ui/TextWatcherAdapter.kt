package com.pleon.buyt.ui

import android.text.Editable
import android.text.TextWatcher

fun newAfterTextWatcher(function: () -> Unit) = object : TextWatcherAdapter {
    override fun afterTextChanged(s: Editable) = function()
}

fun newBeforeTextWatcher(function: () -> Unit) = object : TextWatcherAdapter {
    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) = function()
}

fun newOnTextWatcher(function: () -> Unit) = object : TextWatcherAdapter {
    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) = function()
}

interface TextWatcherAdapter : TextWatcher {

    override fun afterTextChanged(s: Editable) {}

    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}

}
