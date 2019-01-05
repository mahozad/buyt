package com.pleon.buyt.ui;

import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputLayout;
import com.pleon.buyt.R;
import com.pleon.buyt.TextWatcherAdapter;
import com.pleon.buyt.model.Item;
import com.pleon.buyt.ui.ItemListAdapter.ItemHolder;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.Adapter;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;
import androidx.transition.TransitionManager;

import static android.view.MotionEvent.ACTION_DOWN;
import static android.view.View.GONE;
import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

public class ItemListAdapter extends Adapter<ItemHolder> {

    private List<Item> allItems;
    public RecyclerView recyclerView;
    private boolean editModeEnabled = false;
    private boolean selectionModeEnabled = false;
    private final Set<Item> selectedItems = new HashSet<>();

    private ItemTouchHelper itemTouchHelper;

    public ItemListAdapter(ItemTouchHelper itemTouchHelper) {
        this.itemTouchHelper = itemTouchHelper;
        // setHasStableIds is an optimization hint that you can give to the RecyclerView.
        // You're telling it "when I provide a ViewHolder, its id is unique and will not change."
        setHasStableIds(true);
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
        this.recyclerView = recyclerView;
    }

    @Override
    public ItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_list_row, parent, false);
        return new ItemHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final ItemHolder holder, int position) {
        if (allItems != null) {
            Item item = allItems.get(position);
            holder.nameTxVi.setText(item.getName());
            holder.descriptionTxVi.setText(item.getDescription());
            holder.quantityTxVi.setText(item.getQuantity().toString());
            holder.urgentImgVi.setVisibility(item.isUrgent() ? VISIBLE : INVISIBLE);

            holder.expandDragBtn.setOnTouchListener((v, event) -> {
                if (event.getActionMasked() == ACTION_DOWN) {
                    itemTouchHelper.startDrag(holder);
                }
                return false;
            });

            holder.selectChBx.setOnClickListener(v -> {
                if (holder.priceContainer.getVisibility() == GONE) {
                    holder.priceContainer.setVisibility(VISIBLE);
                    selectedItems.add(item);
                } else {
                    holder.priceContainer.setVisibility(GONE);
                    selectedItems.remove(item);
                }
            });

            if (selectionModeEnabled) {
                holder.selectChBx.setVisibility(VISIBLE);
            } else if (editModeEnabled) {
                holder.expandDragBtn.setImageResource(R.drawable.ic_drag_handle);
                holder.expandDragBtn.setVisibility(VISIBLE);
            } else if (item.getDescription() != null) {
                holder.expandDragBtn.setImageResource(R.drawable.ic_expand);
                holder.expandDragBtn.setVisibility(VISIBLE);
            } else {
                holder.expandDragBtn.setVisibility(INVISIBLE);
            }


            holder.priceEdTx.addTextChangedListener(new TextWatcherAdapter() {
                @Override
                public void afterTextChanged(Editable s) {
                    String priceString = holder.priceEdTx.getText().toString();
                    if (!priceString.isEmpty()) {
                        long price = Long.parseLong(priceString);
                        item.setPrice(price);
                    }
                }
            });


            // Restore selected state of the Item
            holder.selectChBx.setChecked(selectedItems.contains(item));

            // Restore expanded state of the item
            holder.descriptionTxVi.setVisibility(item.isExpanded() ? VISIBLE : GONE);

            // TODO: which callback method is the best for setting these listeners? (e.g. onCreate or...?)

            holder.cardForeground.setOnClickListener(container -> {
                if (selectionModeEnabled) {
                    holder.selectChBx.performClick();
                }
            });

            holder.expandDragBtn.setOnClickListener(expBtn -> {
                holder.descriptionTxVi.setVisibility(holder.descriptionTxVi.getVisibility() == VISIBLE ? GONE : VISIBLE);
                item.setExpanded(holder.descriptionTxVi.getVisibility() == VISIBLE);
                TransitionManager.beginDelayedTransition(recyclerView);
            });
        } else {
            // Covers the case of data not being ready yet.
            // set a placeholder or something
        }
    }

    @Override
    public int getItemCount() {
        if (allItems == null) {
            return 0;
        }
        return allItems.size();
    }

    // setHasStableIds() should be set for the adapter. This is an optimization hint that you can
    // give to the RecyclerView. You're telling it "when I provide a ViewHolder, its id is unique and won't change."
    @Override
    public long getItemId(int position) {
        return allItems.get(position).getId();
    }

    public List<Item> getItems() {
        return allItems;
    }

    public void setItems(List<Item> items) {
        allItems = items;
        notifyDataSetChanged();
    }

    public Item getItem(int position) {
        return allItems.get(position);
    }

    public Set<Item> getSelectedItems() {
        return selectedItems;
    }

    public void clearSelectedItems() {
        selectedItems.clear();
    }

    public void addItem(Item item, int position) {
        allItems.add(position, item);
        notifyItemInserted(position);
    }

    public void removeItem(int position) {
        allItems.remove(position);
        notifyItemRemoved(position);
    }

    public void toggleEditMode() {
        editModeEnabled = !editModeEnabled;
        notifyDataSetChanged();
    }

    public void togglePriceInput(boolean enabled) {
        selectionModeEnabled = enabled;
        notifyDataSetChanged();
    }

    // Adapter (and RecyclerView) works with ViewHolders instead of direct Views.
    public class ItemHolder extends ViewHolder {

        final View view; // the view (row layout) for the item
        final TextView nameTxVi;
        final TextView descriptionTxVi;
        final TextView quantityTxVi;
        final FrameLayout cardContainer;
        final ImageButton expandDragBtn;
        final ImageView urgentImgVi;
        final CheckBox selectChBx;
        final FrameLayout priceContainer;
        final TextInputLayout priceTxInLt;
        final EditText priceEdTx;

        public boolean animationMode = false;

        // just for the purpose of swipe-to-delete
        public MaterialCardView cardBackground;
        public MaterialCardView cardForeground;
        final FrameLayout circularReveal;

        ItemHolder(View view) {
            super(view);
            this.view = view;
            this.nameTxVi = view.findViewById(R.id.item_name);
            this.descriptionTxVi = view.findViewById(R.id.description);
            this.quantityTxVi = view.findViewById(R.id.item_quantity);
            this.cardContainer = view.findViewById(R.id.cardContainer);
            this.expandDragBtn = view.findViewById(R.id.expandDragButton);
            this.urgentImgVi = view.findViewById(R.id.urgentIcon);
            this.selectChBx = view.findViewById(R.id.selectCheckBox);
            this.priceContainer = view.findViewById(R.id.price_container);
            this.priceTxInLt = view.findViewById(R.id.price_layout);
            this.priceEdTx = view.findViewById(R.id.price);

            this.cardBackground = view.findViewById(R.id.cardBackground);
            this.cardForeground = view.findViewById(R.id.cardForeground);
            this.circularReveal = view.findViewById(R.id.circular_reveal);
        }
    }
}
