package com.pleon.buyt.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.pleon.buyt.R
import com.pleon.buyt.model.Statistics
import com.pleon.buyt.ui.adapter.PurchaseDetailAdapter
import com.pleon.buyt.viewmodel.StatisticsViewModel
import kotlinx.android.synthetic.main.fragment_state_details.*

class StateDetailsFragment : Fragment() {

    private lateinit var adapter: PurchaseDetailAdapter
    private lateinit var viewModel: StatisticsViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedState: Bundle?)
            : View = inflater.inflate(R.layout.fragment_state_details, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel = ViewModelProviders.of(activity!!).get(StatisticsViewModel::class.java)
        adapter = PurchaseDetailAdapter().also { recyclerView.adapter = it }
    }

    fun showStats(stats: Statistics) {
        adapter.items = stats.purchaseDetails!!.toMutableList()
    }
}
