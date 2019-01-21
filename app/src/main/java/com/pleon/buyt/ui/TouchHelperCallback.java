package com.pleon.buyt.ui;

import android.animation.Animator;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.drawable.Animatable;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.animation.AlphaAnimation;

import com.pleon.buyt.R;
import com.pleon.buyt.ui.adapter.ItemListAdapter;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;

import static android.util.TypedValue.COMPLEX_UNIT_DIP;
import static android.util.TypedValue.applyDimension;
import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;
import static android.view.ViewAnimationUtils.createCircularReveal;
import static androidx.recyclerview.widget.ItemTouchHelper.ACTION_STATE_DRAG;
import static androidx.recyclerview.widget.ItemTouchHelper.DOWN;
import static androidx.recyclerview.widget.ItemTouchHelper.START;
import static androidx.recyclerview.widget.ItemTouchHelper.UP;
import static java.lang.Math.abs;
import static java.lang.Math.max;
import static java.lang.Math.signum;

/**
 * ItemTouchHelper is a powerful utility that takes care of everything we need for adding
 * drag & drop and swipe-to-dismiss to our RecyclerView.
 * <p>
 * In order to use ItemTouchHelper, we’ll create an ItemTouchHelper.Callback to
 * listen for “move” and “swipe” events.
 */
public class TouchHelperCallback extends ItemTouchHelper.Callback {

    private static final float MAX_SWIPE_DIST = 88f; // in dp unit
    private static final float SWIPE_THRESHOLD = 0.3f; // to be considered done

    private float maxSwipeDistInPx;
    private ItemTouchHelperListener listener;
    private boolean dragModeEnabled = false;

    public TouchHelperCallback(ItemTouchHelperListener listener) {
        this.listener = listener;
        DisplayMetrics displayMetrics = Resources.getSystem().getDisplayMetrics();
        this.maxSwipeDistInPx = applyDimension(COMPLEX_UNIT_DIP, MAX_SWIPE_DIST, displayMetrics);
    }

    // If you want to just disable long press drag-n-drop, override isLongPressDragEnabled()
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
            View view = ((ItemListAdapter.ItemHolder) viewHolder).cardBg;
            getDefaultUIUtil().onSelected(view);
        }
    }

    @Override
    public void clearView(RecyclerView recyclerView, ViewHolder viewHolder) {
        View view = ((ItemListAdapter.ItemHolder) viewHolder).cardFg;
        getDefaultUIUtil().clearView(view);
    }

    // If you want to reduce or increase the speed of swipe, multiply dX by the desired factor
    @Override
    public void onChildDraw(Canvas c, RecyclerView recyclerView, ViewHolder viewHolder,
                            float dX, float dY, int actionState, boolean isCurrentlyActive) {
        ItemListAdapter.ItemHolder itemHolder = (ItemListAdapter.ItemHolder) viewHolder;

        // If it's drag-n-drop move the whole card; if it's swipe just move the foreground
        View view = (actionState == ACTION_STATE_DRAG) ? itemHolder.cardCtn : itemHolder.cardFg;

        // Limit the swipe distance. abs() and signum() are to support both LTR and RTL configs.
        dX = (abs(dX) < maxSwipeDistInPx) ? dX : signum(dX) * maxSwipeDistInPx;

        // Animate delete circular reveal
        if (abs(dX) == maxSwipeDistInPx && !itemHolder.delAnimating) {
            itemHolder.delIcon.setImageResource(R.drawable.avd_delete_open);
            ((Animatable) itemHolder.delIcon.getDrawable()).start();
            showCircularReveal(itemHolder, itemHolder.delRevealView);
        } else if (abs(dX) < maxSwipeDistInPx && itemHolder.delAnimating) {
            itemHolder.delIcon.setImageResource(R.drawable.avd_delete_close);
            ((Animatable) itemHolder.delIcon.getDrawable()).start();
            hideCircularReveal(itemHolder, itemHolder.delRevealView);
        }

        getDefaultUIUtil().onDraw(c, recyclerView, view, dX, dY, actionState, isCurrentlyActive);
    }

    private void showCircularReveal(ItemListAdapter.ItemHolder itemHolder, View revealView) {
        itemHolder.delAnimating = true;

        float finalRadius = max(revealView.getWidth(), revealView.getHeight()) / 1.6f;
        int centerX = revealView.getWidth() / 2;
        int centerY = revealView.getHeight() / 2;

        Animator anim = createCircularReveal(revealView, centerX, centerY, 0, finalRadius);
        revealView.setAlpha(1.0f);
        revealView.setVisibility(VISIBLE);
        anim.setDuration(160);
        anim.start();
    }

    private void hideCircularReveal(ItemListAdapter.ItemHolder itemHolder, View revealView) {
        itemHolder.delAnimating = false;

        AlphaAnimation anim = new AlphaAnimation(1.0f, 0.0f);
        anim.setDuration(60);
        revealView.startAnimation(anim);
        revealView.setVisibility(INVISIBLE);
    }

    // Set how much swipe is considered done. Default is 0.5f.
    @Override
    public float getSwipeThreshold(@NonNull ViewHolder viewHolder) {
        return SWIPE_THRESHOLD;
    }

    // If you want to completely disable drag-n-drop, override getMovementFlags()
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
