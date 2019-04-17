package com.pleon.buyt.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.pleon.buyt.R
import com.pleon.buyt.model.PurchaseDetail
import com.pleon.buyt.ui.DateHeaderDecoration
import com.pleon.buyt.ui.adapter.PurchaseDetailAdapter
import kotlinx.android.synthetic.main.fragment_stat_details.*
import java.text.SimpleDateFormat
import java.util.*

class StatDetailsFragment : Fragment() {

    private lateinit var adapter: PurchaseDetailAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedState: Bundle?)
            : View = inflater.inflate(R.layout.fragment_stat_details, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        adapter = PurchaseDetailAdapter(context!!)
        recyclerView.adapter = adapter
        recyclerView.addItemDecoration(DateHeaderDecoration(recyclerView, adapter))
    }

    fun showStats(purchaseDetails: List<PurchaseDetail>) {
        if (purchaseDetails.isEmpty()) {
            adapter.items = emptyList()
            return
        }

        val fmt = SimpleDateFormat("yyyyMMdd", Locale.US)
        var date = purchaseDetails[0].purchase.date
        val list = mutableListOf<Any>(date)

        for (detail in purchaseDetails) {
            if (fmt.format(detail.purchase.date) != fmt.format(date)) {
                list.add(detail.purchase.date)
                date = detail.purchase.date
            }
            list.add(detail)
        }

        adapter.items = list
    }
}
