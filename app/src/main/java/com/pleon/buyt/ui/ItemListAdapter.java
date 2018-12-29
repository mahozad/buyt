package com.pleon.buyt.ui;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.material.card.MaterialCardView;
import com.pleon.buyt.R;
import com.pleon.buyt.model.Item;
import com.pleon.buyt.ui.ItemListAdapter.ItemHolder;
import com.pleon.buyt.ui.fragment.ItemListFragment;

import java.util.List;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.Adapter;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;
import androidx.transition.TransitionManager;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

/**
 * {@link Adapter} that can display a {@link Item} and makes a call to the
 * specified {@link ItemListFragment.Callable}.
 */
public class ItemListAdapter extends Adapter<ItemHolder> {

    private List<Item> mItems;
    private final ItemListFragment.Callable mListener;
    private final int defaultCardBgColor = Color.parseColor("#424242");
    private Context mContext;
    private RecyclerView mRecyclerView;
    private boolean editModeEnabled = false;

    private ItemTouchHelper itemTouchHelper;

    public ItemListAdapter(ItemListFragment.Callable listener, Context context, ItemTouchHelper itemTouchHelper) {
        this.mListener = listener;
        this.mContext = context;
        this.itemTouchHelper = itemTouchHelper;
    }

    /**
     * Gets a reference of the enclosing RecyclerView.
     * <p>
     * Note that if the adapter is assigned to multiple RecyclerViews, then only one
     * of them is assigned to the filed because every time the adapter is attached to a new
     * RecyclerView, this method is called and therefore the field is overwritten.
     *
     * @param recyclerView the enclosing RecyclerView
     */
    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        mRecyclerView = recyclerView;
    }

    @Override
    public ItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_list_row, parent, false);
        return new ItemHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final ItemHolder holder, int position) {
        if (mItems != null) {
            holder.mItem = mItems.get(position);
            holder.mNameTextView.setText(mItems.get(position).getName());
            holder.mDescription.setText(mItems.get(position).getDescription());

            holder.mExpandButton.setOnTouchListener((v, event) -> {
                if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
                    itemTouchHelper.startDrag(holder);
                }
                return false;
            });

            // change expand icon to drag handler icon if in edit mode
            if (editModeEnabled) {
                holder.mExpandButton.setImageResource(R.drawable.ic_drag_handle);
            } else {
                holder.mExpandButton.setImageResource(R.drawable.ic_expand);
            }

            // Restore selected state of the Item
            if (holder.mItem.isSelected()) {
                holder.cardForeground.setCardBackgroundColor(ContextCompat.getColor(mContext, R.color.colorPrimaryDark));
            } else {
                holder.cardForeground.setCardBackgroundColor(defaultCardBgColor);
            }

            // Restore expanded state of the item
            holder.mDescription.setVisibility(holder.mItem.isExpanded() ? VISIBLE : GONE);
        } else {
            // Covers the case of data not being ready yet.
            // set a placeholder or something
        }

        // which callback method is the best for setting these listeners? (e.g. onCreate or...?)

        holder.mCardContainer.setOnClickListener(container -> {
            // TODO: this can be done with color state list
            int color = ContextCompat.getColor(mContext, R.color.colorPrimaryDark);
            holder.cardForeground.setCardBackgroundColor(color);
        });

        holder.mExpandButton.setOnClickListener(expBtn -> {
            if (holder.mDescription.getVisibility() == VISIBLE) {
                holder.mDescription.setVisibility(GONE);
            } else {
                holder.mDescription.setVisibility(VISIBLE);
            }
            holder.mItem.setExpanded(holder.mDescription.getVisibility() == VISIBLE);
            TransitionManager.beginDelayedTransition(mRecyclerView);
        });
    }

    @Override
    public int getItemCount() {
        if (mItems == null) {
            return 0;
        }
        return mItems.size();
    }

    public List<Item> getItems() {
        return mItems;
    }

    public void setItems(List<Item> items) {
        mItems = items;
        notifyDataSetChanged();
    }

    public Item getItem(int position) {
        return mItems.get(position);
    }

    public void addItem(Item item, int position) {
        mItems.add(position, item);
    }

    public void removeItem(int position) {
        mItems.remove(position);
        notifyItemRemoved(position);
    }

    public void toggleEditMode() {
        editModeEnabled = !editModeEnabled;
        notifyDataSetChanged();
    }

    // Adapter (and RecyclerView) works with ViewHolders instead of direct Views.
    public class ItemHolder extends ViewHolder {

        public final View mView; // the view (row layout) for the item_list_row
        public final TextView mNameTextView;
        public FrameLayout mCardContainer;
        public ImageButton mExpandButton;
        public TextView mDescription;
        public Item mItem; // the item_list_row object itself

        // just for the purpose of delete swipe
        public MaterialCardView cardBackground;
        public MaterialCardView cardForeground;

        public ItemHolder(View view) {
            super(view);
            mView = view;
            mNameTextView = view.findViewById(R.id.item_name);
            mCardContainer = view.findViewById(R.id.cardContainer);
            mExpandButton = view.findViewById(R.id.expandButton);
            mDescription = view.findViewById(R.id.description);

            cardBackground = view.findViewById(R.id.cardBackground);
            cardForeground = view.findViewById(R.id.cardForeground);
        }
    }
}
