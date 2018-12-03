package com.pleon.buyt;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import com.pleon.buyt.ItemContent.Item;
import com.pleon.buyt.database.AppDatabase;

import java.util.ArrayList;
import java.util.List;

import androidx.recyclerview.widget.RecyclerView.Adapter;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;

/**
 * {@link Adapter} that can display a {@link Item} and makes a call to the
 * specified {@link ItemListFragment.Callable}.
 */
public class ItemAdapter extends Adapter<ItemAdapter.ItemHolder> {

    public static List<Item> mItemsCart = new ArrayList<>();
    private final ItemListFragment.Callable mListener;

    public ItemAdapter(ItemListFragment.Callable listener, Context context) {
        mListener = listener;
        new Thread(() -> {
            mItemsCart = AppDatabase.getDatabase(context).itemDao().getAllItems();
            notifyDataSetChanged();
        }).start();
    }

    @Override
    public ItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item, parent, false);
        return new ItemHolder(view);
    }

    @Override
    public void onBindViewHolder(final ItemHolder holder, int position) {
        holder.mItem = mItemsCart.get(position);
        holder.mNameTextView.setText(mItemsCart.get(position).name);
        holder.mPriceTextView.setText(mItemsCart.get(position).price);

        holder.mCheckbox.setOnClickListener(v -> {
            if (mListener != null) {
                // Notify the active callbacks interface (the activity, if the
                // fragment is attached to one) that an item has been selected.
                mListener.onListFragmentInteraction(holder.mItem);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mItemsCart.size();
    }

    // Adapter (and RecyclerView) works with ViewHolders instead of direct Views.
    public class ItemHolder extends ViewHolder {

        public final View mView; // the view (row layout) for the item
        public final CheckBox mCheckbox;
        public final TextView mNameTextView;
        public final TextView mPriceTextView;
        public Item mItem; // the item object itself

        public ItemHolder(View view) {
            super(view);
            mView = view;
            mCheckbox = view.findViewById(R.id.checkBox);
            mNameTextView = view.findViewById(R.id.item_name);
            mPriceTextView = view.findViewById(R.id.item_price);
        }
    }
}
