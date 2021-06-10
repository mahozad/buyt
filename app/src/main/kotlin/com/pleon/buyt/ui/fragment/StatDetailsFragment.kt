package com.pleon.buyt.ui.fragment

import android.os.Bundle
import android.view.View
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.pleon.buyt.R
import com.pleon.buyt.ui.DateHeaderDecoration
import com.pleon.buyt.ui.DateHeaderDecoration.HasStickyHeader
import com.pleon.buyt.ui.adapter.PurchaseDetailAdapter
import com.pleon.buyt.util.animateAlpha
import com.pleon.buyt.viewmodel.StatsViewModel
import kotlinx.android.synthetic.main.fragment_stat_details.*
import kotlinx.coroutines.flow.collect
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class StatDetailsFragment : BaseFragment() {

    private val viewModel by sharedViewModel<StatsViewModel>()
    private val adapter by inject<PurchaseDetailAdapter>()

    override fun layout() = R.layout.fragment_stat_details

    override fun onViewCreated(view: View, savedState: Bundle?) {
        recyclerView.adapter = adapter
        recyclerView.addItemDecoration(DateHeaderDecoration(recyclerView, adapter))
        recyclerView.addOnScrollListener(recyclerViewScrollListener())
        lifecycleScope.launchWhenStarted {
            viewModel.purchaseDetails.collect { purchaseDetails ->
                adapter.items = purchaseDetails
                animateAlpha(emptyHint, if (purchaseDetails.isEmpty()) 1f else 0f, duration = 0)
            }
        }
    }

    // For date headers to be shown correctly and smoothly, this listener is required
    private fun recyclerViewScrollListener() = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            val layoutMgr = recyclerView.layoutManager as LinearLayoutManager
            // Because of the viewHolder reuse, upcoming items alpha might be 0 so we set it 1
            // Note that getChildAt() is based on the visible items not real item positions
            for (i in 0..recyclerView.childCount) layoutMgr.getChildAt(i)?.alpha = 1f
            val firstItem = layoutMgr.findFirstVisibleItemPosition()
            if (firstItem >= 0 && (recyclerView.adapter as HasStickyHeader).isHeader(firstItem)) {
                layoutMgr.getChildAt(0)?.alpha = 0f
            }
        }
    }
}
