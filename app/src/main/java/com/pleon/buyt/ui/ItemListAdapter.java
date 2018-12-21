package com.pleon.buyt.ui;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.material.card.MaterialCardView;
import com.pleon.buyt.R;
import com.pleon.buyt.model.Item;
import com.pleon.buyt.ui.ItemListAdapter.ItemHolder;

import java.util.List;

import androidx.core.content.ContextCompat;
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

    public ItemListAdapter(ItemListFragment.Callable listener, Context context) {
        mListener = listener;
        mContext = context;
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

            // Restore selected state of the Item
            if (holder.mItem.isSelected()) {
                holder.mCard.setCardBackgroundColor(ContextCompat.getColor(mContext, R.color.colorPrimaryDark));
            } else {
                holder.mCard.setCardBackgroundColor(defaultCardBgColor);
            }

            // Restore expanded state of the item
            holder.mDescription.setVisibility(holder.mItem.isExpanded() ? VISIBLE : GONE);
        } else {
            // Covers the case of data not being ready yet.
            // set a placeholder or something
        }

        // which callback method is the best for setting these listeners? (e.g. onCreate or...?)

        holder.mCard.setOnClickListener(card -> {
            // TODO: this can be done with color state list
            int color = ContextCompat.getColor(mContext, R.color.colorPrimaryDark);
            ((MaterialCardView) card).setCardBackgroundColor(color);
            holder.mItem.setSelected(true);
        });

        holder.mExpand.setOnClickListener(expBtn -> {
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

    public void setItems(List<Item> items) {
        mItems = items;
        notifyDataSetChanged();
    }

    public Item getItem(int position) {
        return mItems.get(position);
    }

    public void removeItem(int position) {
        mItems.remove(position);
        notifyItemRemoved(position);
    }

    // Adapter (and RecyclerView) works with ViewHolders instead of direct Views.
    public class ItemHolder extends ViewHolder {

        public final View mView; // the view (row layout) for the item_list_row
        public final TextView mNameTextView;
        public MaterialCardView mCard;
        public ImageButton mExpand;
        public TextView mDescription;
        public Item mItem; // the item_list_row object itself


        // just for the purpose of delete swipe
        public MaterialCardView swipeBackground;
        public MaterialCardView foreground;

        public ItemHolder(View view) {
            super(view);
            mView = view;
            mNameTextView = view.findViewById(R.id.item_name);
            mCard = view.findViewById(R.id.itemCard);
            mExpand = view.findViewById(R.id.expandButton);
            mDescription = view.findViewById(R.id.description);


            swipeBackground = view.findViewById(R.id.background);
            foreground = view.findViewById(R.id.itemCard);
        }
    }
}
