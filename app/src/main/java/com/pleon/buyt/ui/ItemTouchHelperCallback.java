package com.pleon.buyt.ui;

import android.animation.Animator;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;

import static android.util.TypedValue.COMPLEX_UNIT_DIP;
import static android.util.TypedValue.applyDimension;
import static android.view.ViewAnimationUtils.createCircularReveal;
import static androidx.recyclerview.widget.ItemTouchHelper.DOWN;
import static androidx.recyclerview.widget.ItemTouchHelper.START;
import static androidx.recyclerview.widget.ItemTouchHelper.UP;

/**
 * ItemTouchHelper is a powerful utility that takes care of everything we need to add both
 * drag & drop and swipe-to-dismiss to our RecyclerView.
 * <p>
 * In order to use ItemTouchHelper, we’ll create an ItemTouchHelper.Callback.
 * This is the interface that allows us to listen for “move” and “swipe” events.
 */
public class ItemTouchHelperCallback extends ItemTouchHelper.Callback {

    private ItemTouchHelperListener listener;
    private boolean dragModeEnabled = false; // for enable drag n drop of Items
    // in pixel (so it should be calculated to be same distance on all devices)
    private float maxSwipeDistance;
    private int revealWidth;

    public ItemTouchHelperCallback(ItemTouchHelperListener listener) {
        this.listener = listener;

        float dp = 88f; // max dist in dp unit
        DisplayMetrics displayMetrics = Resources.getSystem().getDisplayMetrics();
        this.maxSwipeDistance = applyDimension(COMPLEX_UNIT_DIP, dp, displayMetrics); // max dist in px unit
    }

    // if you want to disable long-press-to-drag of items override this method and return false
    // Note that if you want to completely disable drag-n-drop then override getMovementFlags()
    /*@Override
    public boolean isLongPressDragEnabled() {
        return false;
    }*/

    // If you want to set how much swipe is considered done, override this method. Default is 0.5f.
    /*@Override
    public float getSwipeThreshold(@NonNull RecyclerView.ViewHolder viewHolder) {
        return 0.5f;
    }*/

    @Override
    public int getMovementFlags(RecyclerView recyclerView, ViewHolder viewHolder) {
        int swipeFlags = START;
        int dragFlags = dragModeEnabled ? (UP | DOWN) : (0);
        return makeMovementFlags(dragFlags, swipeFlags);
    }

    @Override
    public boolean onMove(RecyclerView recyclerView, ViewHolder dragged, ViewHolder target) {
        if (dragged.getAdapterPosition() != target.getAdapterPosition()) {
            listener.onMoved(dragged.getAdapterPosition(), target.getAdapterPosition());
        }
        return true;
    }

    @Override
    public void onSwiped(ViewHolder viewHolder, int direction) {
        listener.onSwiped(viewHolder, direction);
    }

    @Override
    public void onSelectedChanged(ViewHolder viewHolder, int actionState) {
        if (viewHolder != null) {
            View view = ((ItemListAdapter.ItemHolder) viewHolder).cardBackground;
            getDefaultUIUtil().onSelected(view);
        }
    }

    @Override
    public void clearView(RecyclerView recyclerView, ViewHolder viewHolder) {
        View view = ((ItemListAdapter.ItemHolder) viewHolder).cardForeground;
        getDefaultUIUtil().clearView(view);
    }

    @Override
    public void onChildDraw(Canvas c, RecyclerView recyclerView, ViewHolder viewHolder,
                            float dX, float dY, int actionState, boolean isCurrentlyActive) {

        // TODO: here I can also detect how much swipe is done (with dX or dY) and for example show
        // different icon or change the color of icon when reached a threshold

        ItemListAdapter.ItemHolder itemHolder = (ItemListAdapter.ItemHolder) viewHolder;

        View view = null;
        if (actionState == ItemTouchHelper.ACTION_STATE_DRAG) {
            // if it's drag-n-drop then move the whole card
            view = itemHolder.cardContainer;
        } else if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
            // if it's swipe then just move the foreground
            view = itemHolder.cardForeground;
        }

        // Here I have limited swipe distance. For full swipe, just remove this line.
        // Also if the speed of swipe should be reduced, dX can be divided by for example 2
        dX = dX > -maxSwipeDistance ? dX : -maxSwipeDistance;

        // animate circular reveal
        FrameLayout revealLayout = itemHolder.circularReveal;
        if (dX == -maxSwipeDistance && !itemHolder.animationMode) {
            Animator anim = createCircularReveal(revealLayout,
                    revealLayout.getWidth() / 2,
                    revealLayout.getHeight() / 2, 0, 80);
            revealLayout.setVisibility(View.VISIBLE);
            anim.setDuration(160);
            anim.start();
            itemHolder.animationMode = true;
        } else if (dX > -maxSwipeDistance && itemHolder.animationMode) {
            itemHolder.animationMode = false;
            revealLayout.setVisibility(View.INVISIBLE);
        }

        getDefaultUIUtil().onDraw(c, recyclerView, view, dX, dY, actionState, isCurrentlyActive);
    }

    @Override
    public float getSwipeThreshold(@NonNull ViewHolder viewHolder) {
        return 0.3f;
    }

    @Override
    public boolean isLongPressDragEnabled() {
        return false;
    }

    public void toggleDragMode() {
        dragModeEnabled = !dragModeEnabled;
    }

    public interface ItemTouchHelperListener {

        void onMoved(int oldPosition, int newPosition);

        void onSwiped(ViewHolder viewHolder, int direction);
    }
}
