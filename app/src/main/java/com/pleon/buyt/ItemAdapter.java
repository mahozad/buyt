package com.pleon.buyt;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.pleon.buyt.ItemContent.Item;
import com.pleon.buyt.ItemListFragment.OnListFragmentInteractionListener;

import java.util.List;

import androidx.recyclerview.widget.RecyclerView.Adapter;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;

/**
 * {@link Adapter} that can display a {@link Item} and makes a call to the
 * specified {@link OnListFragmentInteractionListener}.
 */
public class ItemAdapter extends Adapter<ItemAdapter.ItemHolder> {

    private final List<Item> mItems;
    private final OnListFragmentInteractionListener mListener;

    public ItemAdapter(List<Item> items, OnListFragmentInteractionListener listener) {
        mItems = items;
        mListener = listener;
    }

    @Override
    public ItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_item, parent, false);
        return new ItemHolder(view);
    }

    @Override
    public void onBindViewHolder(final ItemHolder holder, int position) {
        holder.mItem = mItems.get(position);
        holder.mIdTextView.setText(mItems.get(position).id);
        holder.mNameTextView.setText(mItems.get(position).name);
        holder.mPriceTextView.setText(mItems.get(position).price);

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onListFragmentInteraction(holder.mItem);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    // Adapter (and RecyclerView) works with ViewHolders instead of direct Views.
    public class ItemHolder extends ViewHolder {

        public final View mView; // the view (row layout) for the item
        public final TextView mIdTextView;
        public final TextView mNameTextView;
        public final TextView mPriceTextView;
        public Item mItem; // the item object itself

        public ItemHolder(View view) {
            super(view);
            mView = view;
            mIdTextView = view.findViewById(R.id.item_number);
            mNameTextView = view.findViewById(R.id.item_name);
            mPriceTextView = view.findViewById(R.id.item_price);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mNameTextView.getText() + "'";
        }
    }
}
