package com.pleon.buyt.ui

import android.content.Context
import android.graphics.Canvas
import android.view.View
import android.view.View.VISIBLE
import android.view.ViewAnimationUtils.createCircularReveal
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ItemTouchHelper.*
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.pleon.buyt.R
import com.pleon.buyt.util.animateAlpha
import com.pleon.buyt.util.animateIcon
import kotlinx.android.synthetic.main.item_list_row.view.*
import org.jetbrains.anko.dimen
import org.jetbrains.anko.dip
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sign
import kotlin.math.sqrt

private const val MAX_SWIPE_DIST = 88f // in dp unit
private const val SWIPE_THRESHOLD = 0.3f // to be considered done

/**
 * ItemTouchHelper is a powerful utility that takes care of everything we need for adding
 * drag & drop and swipe-to-dismiss to our RecyclerView.
 *
 * In order to use ItemTouchHelper, we’ll create an ItemTouchHelper.Callback to
 * listen for “move” and “swipe” events.
 */
class TouchHelperCallback(cxt: Context, private var listener: ItemTouchHelperListener)
    : ItemTouchHelper.Callback() {

    interface ItemTouchHelperListener {
        fun onMoved(oldPosition: Int, newPosition: Int)
        fun onSwiped(viewHolder: ViewHolder, direction: Int)
    }

    private var dragModeEnabled = false
    private val maxSwipeDistInPx = cxt.dip(MAX_SWIPE_DIST).toFloat()
    private val normalElevation = cxt.dimen(R.dimen.item_normal_elevation)
    private val swipeElevation = cxt.dimen(R.dimen.item_swipe_elevation)
    private val dragElevation = cxt.dimen(R.dimen.item_drag_elevation)

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
        if (viewHolder == null) return
        val view = (viewHolder as BaseViewHolder).itemView.cardBackground
        ItemTouchHelper.Callback.getDefaultUIUtil().onSelected(view)
    }

    override fun clearView(recyclerView: RecyclerView, viewHolder: ViewHolder) {
        val view = (viewHolder as BaseViewHolder).itemView.cardForeground
        view.isDragged = false // enabled in onDragHandleTouch() method of the view holder
        ItemTouchHelper.Callback.getDefaultUIUtil().clearView(view)
    }

    // If you want to reduce or increase the speed of swipe, multiply dX by the desired factor
    override fun onChildDraw(c: Canvas, recyclerView: RecyclerView, holder: ViewHolder,
                             dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {
        val viewHolder = holder as BaseViewHolder

        // If it's drag-n-drop move the whole card; if it's swipe just move the foreground
        val view = if (actionState == ACTION_STATE_DRAG) viewHolder.itemView.cardContainer else viewHolder.itemView.cardForeground

        // Limit the swipe distance. abs() and sign() are to support both LTR and RTL configs.
        val newDX = if (abs(dX) < maxSwipeDistInPx) dX else dX.sign * maxSwipeDistInPx

        if (isCurrentlyActive && actionState == ACTION_STATE_SWIPE)
            ViewCompat.setElevation(view, swipeElevation.toFloat())
        else
            ViewCompat.setElevation(view, normalElevation.toFloat())

        // Animate delete circular reveal
        if (abs(newDX) == maxSwipeDistInPx && !viewHolder.delAnimating) {
            viewHolder.itemView.delete_icon.setImageResource(R.drawable.avd_delete_open)
            animateIcon(viewHolder.itemView.delete_icon.drawable)
            showCircularReveal(viewHolder, viewHolder.itemView.circular_reveal)
        } else if (abs(newDX) < maxSwipeDistInPx && viewHolder.delAnimating) {
            viewHolder.itemView.delete_icon.setImageResource(R.drawable.avd_delete_close)
            animateIcon(viewHolder.itemView.delete_icon.drawable)
            hideCircularReveal(viewHolder, viewHolder.itemView.circular_reveal)
        }

        ItemTouchHelper.Callback.getDefaultUIUtil().onDraw(c, recyclerView, view, newDX, dY, actionState, isCurrentlyActive)
    }

    private fun showCircularReveal(viewHolder: BaseViewHolder, view: View) {
        viewHolder.delAnimating = true
        view.visibility = VISIBLE
        view.alpha = 1.0f
        val finalRadius = calculateFinalRadius(view)
        createCircularReveal(view, view.width / 2, view.height / 2, 0f, finalRadius)
                .setDuration(200)
                .start()

    }

    private fun calculateFinalRadius(view: View): Float = with(view) {
        val roundCorner = dip(2)
        return sqrt((width / 2f).pow(2) + (height / 2f).pow(2)) - roundCorner
    }

    private fun hideCircularReveal(viewHolder: BaseViewHolder, revealView: View) {
        viewHolder.delAnimating = false
        animateAlpha(revealView, toAlpha = 0f, duration = 100)
    }

    // Set how much swipe is considered done. Default is 0.5f.
    override fun getSwipeThreshold(viewHolder: ViewHolder) = SWIPE_THRESHOLD

    // If you want to completely disable drag-n-drop, override getMovementFlags()
    override fun isLongPressDragEnabled() = false

    fun toggleDragMode() {
        dragModeEnabled = !dragModeEnabled
    }
}
