package com.pleon.buyt.ui.fragment

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders.of
import com.pleon.buyt.R
import com.pleon.buyt.database.dto.PurchaseDetail
import com.pleon.buyt.ui.DateHeaderDecoration
import com.pleon.buyt.ui.adapter.PurchaseDetailAdapter
import com.pleon.buyt.viewmodel.StatsViewModel
import com.pleon.buyt.viewmodel.ViewModelFactory
import kotlinx.android.synthetic.main.fragment_stat_details.*
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class StatDetailsFragment : BaseFragment() {

    @Inject internal lateinit var viewModelFactory: ViewModelFactory<StatsViewModel>
    private lateinit var adapter: PurchaseDetailAdapter

    override fun layout() = R.layout.fragment_stat_details

    override fun onViewCreated(view: View, savedState: Bundle?) {
        adapter = PurchaseDetailAdapter(context!!)
        recyclerView.adapter = adapter
        recyclerView.addItemDecoration(DateHeaderDecoration(recyclerView, adapter))

        val viewModel = of(activity!!, viewModelFactory).get(StatsViewModel::class.java)
        viewModel.stats.observe(viewLifecycleOwner, Observer { stats -> showStats(stats.purchaseDetails) })
    }

    private fun showStats(purchaseDetails: List<PurchaseDetail>) {
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
