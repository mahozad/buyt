package com.pleon.buyt.model

import com.pleon.buyt.R

class Quantity(val quantity: Long, val unit: Unit) {

    enum class Unit (val nameRes: Int) {
        UNIT(R.string.quantity_unit), KILOGRAM(R.string.quantity_kilogram), GRAM(R.string.quantity_gram)
    }

    override fun toString() = "$quantity ${unit.toString().toLowerCase()}"
}
