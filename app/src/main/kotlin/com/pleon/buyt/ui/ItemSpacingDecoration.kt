package com.pleon.buyt.ui

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import androidx.recyclerview.widget.RecyclerView.State

private const val SPACING = 3

class ItemSpacingDecoration(private val columns: Int,
                            private val isRtl: Boolean) : ItemDecoration() {

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: State) {
        super.getItemOffsets(outRect, view, parent, state)
        if (columns == 2) {
            val position = parent.getChildAdapterPosition(view)
            if (position % columns == 0) {
                outRect.set(if (isRtl) SPACING else 0, 0, if (isRtl) 0 else SPACING, 0)
            } else {
                outRect.set(if (isRtl) 0 else SPACING, 0, if (isRtl) SPACING else 0, 0)
            }
        }
    }

}
