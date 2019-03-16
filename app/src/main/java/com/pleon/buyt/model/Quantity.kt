package com.pleon.buyt.model

import androidx.annotation.StringRes
import com.pleon.buyt.R

class Quantity(var quantity: Long, var unit: Unit?) {

    enum class Unit constructor(@field:StringRes val nameRes: Int) {
        UNIT(R.string.quantity_unit), KILOGRAM(R.string.quantity_kilogram), GRAM(R.string.quantity_gram)
    }

    override fun toString() = "$quantity ${unit.toString().toLowerCase()}"
}
