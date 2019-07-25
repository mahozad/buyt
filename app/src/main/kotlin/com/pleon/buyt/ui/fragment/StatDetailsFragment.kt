package com.pleon.buyt.ui.fragment

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.pleon.buyt.R
import com.pleon.buyt.database.dto.PurchaseDetail
import com.pleon.buyt.ui.DateHeaderDecoration
import com.pleon.buyt.ui.DateHeaderDecoration.HasStickyHeader
import com.pleon.buyt.ui.adapter.PurchaseDetailAdapter
import com.pleon.buyt.util.AnimationUtil
import com.pleon.buyt.util.FormatterUtil.formatDate
import com.pleon.buyt.viewmodel.StatsViewModel
import kotlinx.android.synthetic.main.fragment_stat_details.*
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class StatDetailsFragment : BaseFragment() {

    private val viewModel by sharedViewModel<StatsViewModel>()
    private val adapter by inject<PurchaseDetailAdapter>()

    override fun layout() = R.layout.fragment_stat_details

    override fun onViewCreated(view: View, savedState: Bundle?) {
        recyclerView.adapter = adapter
        recyclerView.addItemDecoration(DateHeaderDecoration(recyclerView, adapter))
        setScrollListener()
        viewModel.purchaseDetails.observe(viewLifecycleOwner, Observer { purchaseDetails ->
            showStats(purchaseDetails)
            AnimationUtil.animateAlpha(emptyHint, if (purchaseDetails.isEmpty()) 1f else 0f)
        })
    }

    private fun setScrollListener() {
        // For date headers to be shown correctly and smoothly, this listener is required
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                // because of the viewHolder reuse, upcoming items alpha might be 0 so we set it 1
                // Note that getChildAt() is based on the visible items not real item positions
                var i = 0
                while (layoutManager.getChildAt(i) != null) {
                    layoutManager.getChildAt(i)!!.alpha = 1f
                    i++
                }
                val firstItem = layoutManager.findFirstVisibleItemPosition()
                if (firstItem >= 0 && (recyclerView.adapter as HasStickyHeader).isHeader(firstItem)) {
                    layoutManager.getChildAt(0)!!.alpha = 0f
                }
            }
        })
    }

    private fun showStats(purchaseDetails: List<PurchaseDetail>) {
        if (purchaseDetails.isEmpty()) {
            adapter.items = emptyList() // required to remove previous items on period toggle
            return
        }

        // Add dates and details to a list together
        var date = purchaseDetails[0].purchase.date
        val list = mutableListOf<Any>(date)
        for (detail in purchaseDetails) {
            if (formatDate(detail.purchase.date) != formatDate(date)) {
                list.add(detail.purchase.date)
                date = detail.purchase.date
            }
            list.add(detail)
        }

        adapter.items = list
    }
}
