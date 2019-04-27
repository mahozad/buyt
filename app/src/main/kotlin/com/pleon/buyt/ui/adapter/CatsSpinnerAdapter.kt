package com.pleon.buyt.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.pleon.buyt.R
import com.pleon.buyt.model.Category

class CatsSpinnerAdapter(cxt: Context) : BaseAdapter() {

    private var inflater = LayoutInflater.from(cxt)

    override fun getCount() = Category.values().size

    override fun getItem(index: Int) = Category.values()[index]

    override fun getItemId(index: Int) = 0L

    override fun getView(index: Int, view: View?, viewGroup: ViewGroup): View {
        val view = inflater.inflate(R.layout.spinner_category_entry, null)

        val cat = Category.values()[index]
        (view as TextView).setCompoundDrawablesRelativeWithIntrinsicBounds(
                cat.storeImageRes, 0, 0, 0
        )
        view.setText(cat.storeNameRes)

        return view
    }
}
