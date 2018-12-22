package com.pleon.buyt.ui;

import android.content.res.Resources;
import android.graphics.Canvas;
import android.util.DisplayMetrics;
import android.view.View;

import androidx.annotation.NonNull;
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
    private boolean editModeEnabled = false; // for enable drag n drop of Items
    // in pixel (so it should be calculated to be same distance on all devices)
    private float maxSwipeDistance;

    public RecyclerItemTouchHelper(int dragDirs, int swipeDirs, RecyclerItemTouchHelperListener listener) {
        super(dragDirs, swipeDirs);
        this.listener = listener;

        float dp = 88f; // max dist in dp unit
        DisplayMetrics displayMetrics = Resources.getSystem().getDisplayMetrics();
        this.maxSwipeDistance = applyDimension(COMPLEX_UNIT_DIP, dp, displayMetrics); // max dist in px unit
    }

    @Override
    public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
        if (editModeEnabled) {
            return super.getMovementFlags(recyclerView, viewHolder);
        } else { // disable drag n drop of Items
            return makeMovementFlags(0, getSwipeDirs(recyclerView, viewHolder));
        }
    }

    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
        if (viewHolder.getAdapterPosition() != target.getAdapterPosition()) {
            listener.onMoved(viewHolder.getAdapterPosition(), target.getAdapterPosition());
        }
        return true;
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
        listener.onSwiped(viewHolder, direction, viewHolder.getAdapterPosition());
    }

    @Override
    public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
        if (viewHolder != null) {
            View view = ((ItemListAdapter.ItemHolder) viewHolder).cardBackground;
            getDefaultUIUtil().onSelected(view);
        }
    }

    @Override
    public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        View view = ((ItemListAdapter.ItemHolder) viewHolder).cardForeground;
        getDefaultUIUtil().clearView(view);
    }

    @Override
    public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder,
                            float dX, float dY, int actionState, boolean isCurrentlyActive) {

        // TODO: here I can also detect how much swipe is done (with dX or dY) and for example show
        // different icon or change the color of icon when reached a threshold

        View view = null;
        if (actionState == ItemTouchHelper.ACTION_STATE_DRAG) {
            // if it's drag-n-drop then move the whole card
            view = ((ItemListAdapter.ItemHolder) viewHolder).mCardContainer;
        } else if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
            // if it's swipe then just move the foreground
            view = ((ItemListAdapter.ItemHolder) viewHolder).cardForeground;
        }

        // Here I have limited swipe distance. For full swipe, just remove this line.
        // Also if the speed of swipe should be reduced, dX can be divided by for example 2
        dX = dX > -maxSwipeDistance ? dX : -maxSwipeDistance;

        getDefaultUIUtil().onDraw(c, recyclerView, view, dX, dY, actionState, isCurrentlyActive);
    }


    public void toggleEditMode() {
        editModeEnabled = !editModeEnabled;
    }

    public interface RecyclerItemTouchHelperListener {

        void onMoved(int oldPosition, int newPosition);

        void onSwiped(RecyclerView.ViewHolder viewHolder, int direction, int position);
    }
}
