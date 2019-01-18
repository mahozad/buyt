package com.pleon.buyt.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.TextView;

import com.pleon.buyt.R;
import com.pleon.buyt.model.Store;
import com.pleon.buyt.ui.adapter.StoreListAdapter.StoreHolder;

import java.util.List;

import androidx.recyclerview.widget.RecyclerView.Adapter;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;

public class StoreListAdapter extends Adapter<StoreHolder> {

    private List<Store> stores;
    private Context mContext;
    private Callback callback;
    private int selectedStore = -1;
    private boolean callbackNotified = false;

    public StoreListAdapter(Context context, Callback callback) {
        this.mContext = context;
        this.callback = callback;
    }

    @Override
    public StoreHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.store_list_row, parent, false);
        return new StoreHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final StoreHolder holder, int position) {
        if (stores != null) {
            Store store = stores.get(position);
            holder.nameTxvi.setText(store.getName());
            holder.radioButton.setChecked(position == selectedStore);

            // TODO: which callback method is the best for setting these listeners? (e.g. onCreate or...?)
            View.OnClickListener clickListener = v -> {
                if (!(v instanceof RadioButton)) {
                    holder.radioButton.performClick(); // to show radio button animation
                }

                if (!callbackNotified) {
                    callback.onStoreClick();
                }
                callbackNotified = true;

                notifyItemChanged(selectedStore);
                selectedStore = position;
                notifyItemChanged(selectedStore);
            };

            holder.radioButton.setOnClickListener(clickListener);
            holder.view.setOnClickListener(clickListener);

        } else {
            // Covers the case of data not being ready yet.
            // set a placeholder or something
        }
    }

    @Override
    public int getItemCount() {
        if (stores == null) {
            return 0;
        }
        return stores.size();
    }

    public Store getSelectedStores() {
        return stores.get(selectedStore);
    }

    public void setStores(List<Store> stores) {
        this.stores = stores;
        notifyDataSetChanged();
    }

    // Adapter (and RecyclerView) works with ViewHolders instead of direct Views.
    public class StoreHolder extends ViewHolder {

        final View view; // the view (row layout) for the item
        final TextView nameTxvi;
        final RadioButton radioButton;

        StoreHolder(View view) {
            super(view);
            this.view = view;
            this.nameTxvi = view.findViewById(R.id.storeName);
            this.radioButton = view.findViewById(R.id.storeRadioButton);
        }
    }

    public interface Callback {
        void onStoreClick();
    }

}
