package com.pleon.buyt.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.pleon.buyt.model.Statistics
import com.pleon.buyt.ui.DateHeaderDecoration
import com.pleon.buyt.ui.adapter.PurchaseDetailAdapter
import com.pleon.buyt.viewmodel.StatisticsViewModel
import kotlinx.android.synthetic.main.fragment_state_details.*
import java.text.SimpleDateFormat


class StateDetailsFragment : Fragment() {

    private lateinit var adapter: PurchaseDetailAdapter
    private lateinit var viewModel: StatisticsViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedState: Bundle?)
            : View = inflater.inflate(com.pleon.buyt.R.layout.fragment_state_details, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel = ViewModelProviders.of(activity!!).get(StatisticsViewModel::class.java)
        adapter = PurchaseDetailAdapter(context!!)
        recyclerView.adapter = adapter
        recyclerView.addItemDecoration(DateHeaderDecoration(recyclerView, adapter))
    }

    fun showStats(stats: Statistics) {
        val list = mutableListOf<Any>()
        val fmt = SimpleDateFormat("yyyyMMdd")
        var date = stats.purchaseDetails!![0].purchase.date
        list.add(date)
        for (detail in stats.purchaseDetails!!) {
            if (fmt.format(detail.purchase.date) != fmt.format(date)) {
                list.add(detail.purchase.date)
                date = detail.purchase.date
            }
            list.add(detail)
        }
        adapter.items = list
    }
}
