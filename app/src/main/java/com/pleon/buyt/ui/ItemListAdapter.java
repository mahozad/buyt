package com.pleon.buyt.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.card.MaterialCardView;
import com.pleon.buyt.R;
import com.pleon.buyt.model.Item;
import com.pleon.buyt.ui.ItemListAdapter.ItemHolder;
import com.pleon.buyt.ui.fragment.ItemListFragment;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.Adapter;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;
import androidx.transition.TransitionManager;

import static android.view.MotionEvent.ACTION_DOWN;
import static android.view.View.GONE;
import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

/**
 * {@link Adapter} that can display a {@link Item} and makes a call to the
 * specified {@link ItemListFragment.Callable}.
 */
public class ItemListAdapter extends Adapter<ItemHolder> {

    private List<Item> mItems;
    private final ItemListFragment.Callable mListener;
    private final int defaultCardBgColor;
    private Context mContext;
    private RecyclerView mRecyclerView;
    private boolean editModeEnabled = false;
    private final Set<Item> selectedItems = new HashSet<>();

    private ItemTouchHelper itemTouchHelper;

    public ItemListAdapter(ItemListFragment.Callable listener, Context context, ItemTouchHelper itemTouchHelper) {
        this.mListener = listener;
        this.mContext = context;
        this.defaultCardBgColor = ContextCompat.getColor(context, R.color.card_background);
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
            Item item = mItems.get(position);
            holder.nameTxvi.setText(item.getName());
            holder.descriptionTxvi.setText(item.getDescription());
            holder.quantityTxvi.setText(item.getQuantity().toString());
            holder.urgentImgvi.setVisibility(item.isUrgent() ? VISIBLE : INVISIBLE);

            holder.expandBtn.setOnTouchListener((v, event) -> {
                if (event.getActionMasked() == ACTION_DOWN) {
                    itemTouchHelper.startDrag(holder);
                }
                return false;
            });

            if (editModeEnabled) {
                holder.expandBtn.setImageResource(R.drawable.ic_drag_handle);
                holder.expandBtn.setVisibility(VISIBLE);
            } else if (item.getDescription() != null) {
                holder.expandBtn.setImageResource(R.drawable.ic_expand);
                holder.expandBtn.setVisibility(VISIBLE);
            } else {
                holder.expandBtn.setVisibility(INVISIBLE);
            }

            // Restore selected state of the Item
            if (selectedItems.contains(item)) {
                holder.cardForeground.setCardBackgroundColor(ContextCompat.getColor(mContext, R.color.colorPrimaryDark));
            } else {
                holder.cardForeground.setCardBackgroundColor(defaultCardBgColor);
            }

            // Restore expanded state of the item
            holder.descriptionTxvi.setVisibility(item.isExpanded() ? VISIBLE : GONE);

            // TODO: which callback method is the best for setting these listeners? (e.g. onCreate or...?)

            holder.cardForeground.setOnClickListener(container -> {
                // TODO: this may be done with color state list
                int color = ContextCompat.getColor(mContext, R.color.colorPrimaryDark);
                holder.cardForeground.setCardBackgroundColor(color);
                selectedItems.add(item);
            });

            holder.expandBtn.setOnClickListener(expBtn -> {
                holder.descriptionTxvi.setVisibility(holder.descriptionTxvi.getVisibility() == VISIBLE ? GONE : VISIBLE);
                item.setExpanded(holder.descriptionTxvi.getVisibility() == VISIBLE);
                TransitionManager.beginDelayedTransition(mRecyclerView);
            });
        } else {
            // Covers the case of data not being ready yet.
            // set a placeholder or something
        }
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

    public Set<Item> getSelectedItems() {
        return selectedItems;
    }

    public void clearSelectedItems() {
        selectedItems.clear();
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

        final View view; // the view (row layout) for the item
        final TextView nameTxvi;
        final TextView descriptionTxvi;
        final TextView quantityTxvi;
        final FrameLayout cardContainer;
        final ImageButton expandBtn;
        final ImageView urgentImgvi;

        // just for the purpose of swipe-to-delete
        public MaterialCardView cardBackground;
        public MaterialCardView cardForeground;

        ItemHolder(View view) {
            super(view);
            this.view = view;
            this.nameTxvi = view.findViewById(R.id.item_name);
            this.descriptionTxvi = view.findViewById(R.id.description);
            this.quantityTxvi = view.findViewById(R.id.item_quantity);
            this.cardContainer = view.findViewById(R.id.cardContainer);
            this.expandBtn = view.findViewById(R.id.expandButton);
            this.urgentImgvi = view.findViewById(R.id.urgentIcon);

            cardBackground = view.findViewById(R.id.cardBackground);
            cardForeground = view.findViewById(R.id.cardForeground);
        }
    }
}
