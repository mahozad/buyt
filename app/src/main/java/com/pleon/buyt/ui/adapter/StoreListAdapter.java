package com.pleon.buyt.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.pleon.buyt.R;
import com.pleon.buyt.model.Store;
import com.pleon.buyt.ui.BaseViewHolder;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView.Adapter;
import butterknife.BindView;
import butterknife.ButterKnife;

public class StoreListAdapter extends Adapter<StoreListAdapter.StoreHolder> {

    private Context context;
    private List<Store> stores;

    public StoreListAdapter(Context context) {
        this.context = context;
        // setHasStableIds is an optimization hint that you give to the RecyclerView
        // and tell it "when I provide a ViewHolder, its id is unique and will not change."
        setHasStableIds(true);
    }

    @NonNull
    @Override
    public StoreHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.store_list_row, parent, false);
        return new StoreHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull StoreHolder holder, int position) {
        // keep this method as lightweight as possible as it is called for every row
        if (stores != null) {
            Store store = stores.get(position);
            holder.categoryImgVi.setImageResource(store.getCategory().getStoreImageRes());
            holder.nameTxVi.setText(store.getName());
            holder.delRevealView.setAlpha(0f); // for the case of undo of deleted item
        } // else: case of data not being ready yet; set a placeholder or something
    }

    @Override
    public int getItemCount() {
        return stores == null ? 0 : stores.size();
    }

    // setHasStableIds() should also be set (in e.g. constructor). This is an optimization hint that you
    // give to the RecyclerView and tell it "when I provide a ViewHolder, its id is unique and won't change."
    @Override
    public long getItemId(int position) {
        return stores.get(position).getStoreId();
    }

    public void setStores(List<Store> stores) {
        this.stores = stores;
        notifyDataSetChanged();
    }

    public Store getStore(int position) {
        return stores.get(position);
    }

    // Adapter (and RecyclerView) work with ViewHolders instead of direct Views.
    class StoreHolder extends BaseViewHolder {

        @BindView(R.id.storeIcon) ImageView categoryImgVi;
        @BindView(R.id.storeName) TextView nameTxVi;

        StoreHolder(View itemView) {
            super(itemView); // the view (row layout) for the item
            ButterKnife.bind(this, itemView); // unbind() is required only for Fragments
        }
    }
}
