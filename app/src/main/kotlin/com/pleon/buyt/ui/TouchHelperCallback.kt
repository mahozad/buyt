package com.pleon.buyt.ui

import android.content.res.Resources
import android.graphics.Canvas
import android.graphics.drawable.Animatable
import android.util.TypedValue.COMPLEX_UNIT_DIP
import android.util.TypedValue.applyDimension
import android.view.View
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.ViewAnimationUtils.createCircularReveal
import android.view.animation.AlphaAnimation
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ItemTouchHelper.*
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.pleon.buyt.R
import com.pleon.buyt.ui.adapter.ItemListAdapter
import kotlinx.android.synthetic.main.item_list_row.view.*
import java.lang.Math.*

/**
 * ItemTouchHelper is a powerful utility that takes care of everything we need for adding
 * drag & drop and swipe-to-dismiss to our RecyclerView.
 *
 *
 * In order to use ItemTouchHelper, we’ll create an ItemTouchHelper.Callback to
 * listen for “move” and “swipe” events.
 */
class TouchHelperCallback(private val listener: ItemTouchHelperListener) : ItemTouchHelper.Callback() {

    interface ItemTouchHelperListener {
        fun onMoved(oldPosition: Int, newPosition: Int)
        fun onSwiped(viewHolder: ViewHolder, direction: Int)
    }

    private val displayMetrics = Resources.getSystem().displayMetrics
    private val maxSwipeDistInPx = applyDimension(COMPLEX_UNIT_DIP, MAX_SWIPE_DIST, displayMetrics)
    private var dragModeEnabled = false

    // If you want to just disable long press drag-n-drop, override isLongPressDragEnabled()
    override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: ViewHolder): Int {
        val swipeFlags = START
        val dragFlags = if (dragModeEnabled) UP or DOWN else 0
        return makeMovementFlags(dragFlags, swipeFlags)
    }

    override fun onMove(recyclerView: RecyclerView, dragged: ViewHolder, target: ViewHolder): Boolean {
        if (dragged.adapterPosition != target.adapterPosition) {
            listener.onMoved(dragged.adapterPosition, target.adapterPosition)
        }
        return true
    }

    override fun onSwiped(vh: ViewHolder, direction: Int) = listener.onSwiped(vh, direction)

    override fun onSelectedChanged(viewHolder: ViewHolder?, actionState: Int) {
        if (viewHolder != null) {
            val view = (viewHolder as BaseViewHolder).itemView.cardBackground
            ItemTouchHelper.Callback.getDefaultUIUtil().onSelected(view)
        }
    }

    override fun clearView(recyclerView: RecyclerView, viewHolder: ViewHolder) {
        val view = (viewHolder as BaseViewHolder).itemView.cardForeground
        view.isDragged = false // enabled in onDragHandleTouch() method of the view holder
        ItemTouchHelper.Callback.getDefaultUIUtil().clearView(view)
    }

    // If you want to reduce or increase the speed of swipe, multiply dX by the desired factor
    override fun onChildDraw(c: Canvas, recyclerView: RecyclerView, holder: ViewHolder,
                             dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {
        var dX = dX
        val viewHolder = holder as BaseViewHolder

        // If it's drag-n-drop move the whole card; if it's swipe just move the foreground
        val view = if (actionState == ACTION_STATE_DRAG) viewHolder.itemView.cardContainer else viewHolder.itemView.cardForeground

        // Limit the swipe distance. abs() and signum() are to support both LTR and RTL configs.
        dX = if (abs(dX) < maxSwipeDistInPx) dX else signum(dX) * maxSwipeDistInPx

        // Animate delete circular reveal
        if (abs(dX) == maxSwipeDistInPx && !(viewHolder as ItemListAdapter.ItemHolder).delAnimating) {
            viewHolder.itemView.delete_icon.setImageResource(R.drawable.avd_delete_open)
            (viewHolder.itemView.delete_icon.drawable as Animatable).start()
            showCircularReveal(viewHolder, viewHolder.itemView.circular_reveal)
        } else if (abs(dX) < maxSwipeDistInPx && (viewHolder as ItemListAdapter.ItemHolder).delAnimating) {
            viewHolder.itemView.delete_icon.setImageResource(R.drawable.avd_delete_close)
            (viewHolder.itemView.delete_icon.drawable as Animatable).start()
            hideCircularReveal(viewHolder, viewHolder.itemView.circular_reveal)
        }

        ItemTouchHelper.Callback.getDefaultUIUtil().onDraw(c, recyclerView, view, dX, dY, actionState, isCurrentlyActive)
    }

    private fun showCircularReveal(viewHolder: BaseViewHolder, revealView: View) {
        (viewHolder as ItemListAdapter.ItemHolder).delAnimating = true

        val finalRadius = max(revealView.width, revealView.height) / 1.65f
        val centerX = revealView.width / 2
        val centerY = revealView.height / 2

        val anim = createCircularReveal(revealView, centerX, centerY, 0f, finalRadius)
        revealView.alpha = 1.0f
        revealView.visibility = VISIBLE
        anim.duration = 180
        anim.start()
    }

    private fun hideCircularReveal(viewHolder: BaseViewHolder, revealView: View) {
        (viewHolder as ItemListAdapter.ItemHolder).delAnimating = false

        val anim = AlphaAnimation(1.0f, 0.0f)
        anim.duration = 70
        revealView.startAnimation(anim)
        revealView.visibility = INVISIBLE
    }

    // Set how much swipe is considered done. Default is 0.5f.
    override fun getSwipeThreshold(viewHolder: ViewHolder) = SWIPE_THRESHOLD

    // If you want to completely disable drag-n-drop, override getMovementFlags()
    override fun isLongPressDragEnabled() = false

    fun toggleDragMode() {
        dragModeEnabled = !dragModeEnabled
    }

    companion object {
        private const val MAX_SWIPE_DIST = 88f // in dp unit
        private const val SWIPE_THRESHOLD = 0.3f // to be considered done
    }
}