package com.pleon.buyt.util

import java.util.*

object TextUtil {

    fun localizeDigits(input: String) = if (Locale.getDefault().language == "fa") input
            .replace('0', '۰').replace('1', '۱')
            .replace('2', '۲').replace('3', '۳')
            .replace('4', '۴').replace('5', '۵')
            .replace('6', '۶').replace('7', '۷')
            .replace('8', '۸').replace('9', '۹')
    else input
}
