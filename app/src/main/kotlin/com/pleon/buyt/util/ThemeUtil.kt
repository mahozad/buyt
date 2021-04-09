package com.pleon.buyt.util

import android.content.Context
import android.util.TypedValue
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes

@ColorRes fun Context?.resolveThemeColorRes(@AttrRes attr: Int): Int {
    val typedValue = TypedValue()
    this?.theme?.resolveAttribute(attr, typedValue, true)
    return typedValue.resourceId
}

/**
 * Use this function directly instead of *getColor(cxt, resolveThemeColorRes())*
 * if it throws an exception. Maybe the exception is thrown when
 * the color is only defined in an <item> element in the styles
 * and is not defined in colors.xml. For example *R.attr.colorOnSurface*.
 * */
@ColorInt fun Context?.resolveThemeColorVal(@AttrRes attr: Int): Int {
    val typedValue = TypedValue()
    this?.theme?.resolveAttribute(attr, typedValue, true)
    return typedValue.data
}
