package com.pleon.buyt.ui;

import android.content.res.Resources;
import android.graphics.Canvas;
import android.util.DisplayMetrics;
import android.view.View;

import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import static android.util.TypedValue.COMPLEX_UNIT_DIP;
import static android.util.TypedValue.applyDimension;

/**
 * ItemTouchHelper is a powerful utility that takes care of everything we need to add both
 * drag & drop and swipe-to-dismiss to our RecyclerView.
 * <p>
 * In order to use ItemTouchHelper, we’ll create an ItemTouchHelper.Callback.
 * This is the interface that allows us to listen for “move” and “swipe” events.
 */
public class RecyclerItemTouchHelper extends ItemTouchHelper.SimpleCallback {

    private RecyclerItemTouchHelperListener listener;
    // in pixel (so it should be calculated to be same distance on all devices)
    private float maxSwipeDistance;

    public RecyclerItemTouchHelper(int dragDirs, int swipeDirs, RecyclerItemTouchHelperListener listener) {
        super(dragDirs, swipeDirs);
        this.listener = listener;

        float dp = 88f; // max dist in dp unit
        DisplayMetrics displayMetrics = Resources.getSystem().getDisplayMetrics();
        maxSwipeDistance = applyDimension(COMPLEX_UNIT_DIP, dp, displayMetrics); // max dist in px unit
    }

    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
        // Here is if you want to move items up/down
        if (viewHolder.getAdapterPosition() != target.getAdapterPosition()) {
            listener.onMoved(viewHolder.getAdapterPosition(), target.getAdapterPosition());
        }
        return true;
    }

    @Override
    public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
        if (viewHolder != null) {
            View foregroundView = ((ItemListAdapter.ItemHolder) viewHolder).cardBackground;

            getDefaultUIUtil().onSelected(foregroundView);
        }
    }

    @Override
    public void onChildDrawOver(Canvas c, RecyclerView recyclerView,
                                RecyclerView.ViewHolder viewHolder, float dX, float dY,
                                int actionState, boolean isCurrentlyActive) {
        View foregroundView = ((ItemListAdapter.ItemHolder) viewHolder).cardForeground;
        getDefaultUIUtil().onDrawOver(c, recyclerView, foregroundView, dX, dY,
                actionState, isCurrentlyActive);
    }

    @Override
    public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        View foregroundView = ((ItemListAdapter.ItemHolder) viewHolder).cardForeground;
        getDefaultUIUtil().clearView(foregroundView);
    }

    @Override
    public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder,
                            float dX, float dY, int actionState, boolean isCurrentlyActive) {

        // TODO: here I can also detect how much swipe is done (with dX or dY) and for example show
        // different icon or change the color of icon when reached a threshold

        View foregroundView = null;
        if (actionState == ItemTouchHelper.ACTION_STATE_DRAG) {
            // if it's drag n drop then move the whole card
            foregroundView = ((ItemListAdapter.ItemHolder) viewHolder).mCardContainer;
        } else if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
            // if it's swipe then just move the foreground
            foregroundView = ((ItemListAdapter.ItemHolder) viewHolder).cardForeground;
        }

        // Here I have limited swipe distance. For full swipe, just remove this line.
        // Also if the speed of swipe should be reduced, dX can be divided by for example 2
        dX = dX > -maxSwipeDistance ? dX : -maxSwipeDistance;

        getDefaultUIUtil().onDraw(c, recyclerView, foregroundView, dX, dY,
                actionState, isCurrentlyActive);
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
        listener.onSwiped(viewHolder, direction, viewHolder.getAdapterPosition());
    }

    @Override
    public int convertToAbsoluteDirection(int flags, int layoutDirection) {
        return super.convertToAbsoluteDirection(flags, layoutDirection);
    }

    public interface RecyclerItemTouchHelperListener {

        void onMoved(int oldPosition, int newPosition);

        void onSwiped(RecyclerView.ViewHolder viewHolder, int direction, int position);
    }
}
