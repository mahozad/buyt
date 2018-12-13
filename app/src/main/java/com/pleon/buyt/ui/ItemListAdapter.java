package com.pleon.buyt.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import com.pleon.buyt.R;
import com.pleon.buyt.model.Item;
import com.pleon.buyt.ui.ItemListAdapter.ItemHolder;

import java.util.List;

import androidx.recyclerview.widget.RecyclerView.Adapter;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;

/**
 * {@link Adapter} that can display a {@link Item} and makes a call to the
 * specified {@link ItemListFragment.Callable}.
 */
public class ItemListAdapter extends Adapter<ItemHolder> {

    private List<Item> mItems; // updated automatically by setItems() method
    private final ItemListFragment.Callable mListener;

    public ItemListAdapter(ItemListFragment.Callable listener, Context context) {
        mListener = listener;
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
            holder.mPriceTextView.setText(mItems.get(position).getPrice());
        } else {
            // Covers the case of data not being ready yet.
            // set a placeholder or something
        }

        holder.mCheckbox.setOnClickListener(v -> {
            if (mListener != null) {
                // Notify the active callbacks interface (the activity, if the
                // fragment is attached to one) that an item has been selected.
                mListener.onItemCheckboxClicked(holder.mItem);
            }
        });
    }

    public void setItems(List<Item> items) {
        mItems = items;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        if (mItems == null) {
            return 0;
        }
        return mItems.size();
    }

    // Adapter (and RecyclerView) works with ViewHolders instead of direct Views.
    public class ItemHolder extends ViewHolder {

        public final View mView; // the view (row layout) for the item_list_row
        public final CheckBox mCheckbox;
        public final TextView mNameTextView;
        public final TextView mPriceTextView;
        public Item mItem; // the item_list_row object itself

        public ItemHolder(View view) {
            super(view);
            mView = view;
            mCheckbox = view.findViewById(R.id.checkBox);
            mNameTextView = view.findViewById(R.id.item_name);
            mPriceTextView = view.findViewById(R.id.item_price);
        }
    }
}
