package com.pleon.buyt.adapter;

import android.graphics.drawable.Animatable;
import android.view.LayoutInflater;
import android.view.MotionEvent;
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
import com.pleon.buyt.adapter.ItemListAdapter.ItemHolder;
import com.pleon.buyt.model.Item;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.Adapter;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;
import butterknife.OnTextChanged;
import butterknife.OnTouch;

import static android.view.MotionEvent.ACTION_DOWN;
import static android.view.View.GONE;
import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;
import static androidx.transition.TransitionManager.beginDelayedTransition;
import static java.lang.Long.parseLong;

public class ItemListAdapter extends Adapter<ItemHolder> {

    private List<Item> items;
    public RecyclerView recyclerView;
    private boolean dragModeEnabled = false;
    private boolean selectionModeEnabled = false;
    private final Set<Item> selectedItems = new HashSet<>();

    private ItemTouchHelper itemTouchHelper;

    public ItemListAdapter(ItemTouchHelper itemTouchHelper) {
        this.itemTouchHelper = itemTouchHelper;
        // setHasStableIds is an optimization hint that you can give to the RecyclerView
        // that tells it "when I provide a ViewHolder, its id is unique and will not change."
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
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        this.recyclerView = recyclerView;
    }

    @NonNull
    @Override
    public ItemHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_list_row, parent, false);
        return new ItemHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemHolder holder, int position) {
        if (items != null) {
            Item item = items.get(position);
            holder.nameTxVi.setText(item.getName());
            holder.descTxVi.setText(item.getDescription());
            holder.quantityTxVi.setText(item.getQuantity().toString());
            holder.urgentImgVi.setVisibility(item.isUrgent() ? VISIBLE : INVISIBLE);
            holder.selectChBx.setChecked(selectedItems.contains(item));
            holder.descTxVi.setVisibility(item.isExpanded() ? VISIBLE : GONE);

            if (selectionModeEnabled) {
                holder.selectChBx.setVisibility(VISIBLE);
                holder.expandDragBtn.setVisibility(INVISIBLE);
            } else {
                holder.selectChBx.setVisibility(INVISIBLE);
                if (dragModeEnabled) {
                    holder.expandDragBtn.setImageResource(R.drawable.ic_drag_handle);
                    holder.expandDragBtn.setVisibility(VISIBLE);
                } else if (item.getDescription() != null) {
                    holder.expandDragBtn.setImageResource(R.drawable.avd_expand);
                    holder.expandDragBtn.setVisibility(VISIBLE);
                } else {
                    holder.expandDragBtn.setVisibility(INVISIBLE);
                }
            }
        } else {
            // Covers the case of data not being ready yet.
            // set a placeholder or something
        }
    }

    @Override
    public int getItemCount() {
        if (items == null) {
            return 0;
        }
        return items.size();
    }

    // setHasStableIds() should also be set (in e.g. constructor). This is an optimization hint that you can
    // give to the RecyclerView that tells it "when I provide a ViewHolder, its id is unique and won't change."
    @Override
    public long getItemId(int position) {
        return items.get(position).getId();
    }

    public List<Item> getItems() {
        return items;
    }

    public void setItems(List<Item> items) {
        this.items = items;
        notifyDataSetChanged();
    }

    public Item getItem(int position) {
        return items.get(position);
    }

    public Set<Item> getSelectedItems() {
        return selectedItems;
    }

    public void clearSelectedItems() {
        selectedItems.clear();
    }

    public void addItem(Item item, int position) {
        items.add(position, item);
        notifyItemInserted(position);
    }

    public void removeItem(int position) {
        items.remove(position);
        notifyItemRemoved(position);
    }

    public void toggleEditMode() {
        dragModeEnabled = !dragModeEnabled;
        notifyDataSetChanged();
    }

    public void togglePriceInput(boolean enabled) {
        selectionModeEnabled = enabled;
        notifyDataSetChanged();
    }

    // Adapter (and RecyclerView) work with ViewHolders instead of direct Views.
    public class ItemHolder extends ViewHolder {

        @BindView(R.id.item_name) TextView nameTxVi;
        @BindView(R.id.description) TextView descTxVi;
        @BindView(R.id.item_quantity) TextView quantityTxVi;
        @BindView(R.id.expandDragButton) ImageButton expandDragBtn;
        @BindView(R.id.urgentIcon) ImageView urgentImgVi;
        @BindView(R.id.selectCheckBox) CheckBox selectChBx;
        @BindView(R.id.price_container) FrameLayout priceContainer;
        @BindView(R.id.price_layout) TextInputLayout priceTxInLt;
        @BindView(R.id.price) EditText priceEdTx;
        @BindView(R.id.cardContainer) public FrameLayout cardCtn;
        @BindView(R.id.cardBackground) public MaterialCardView cardBg;
        @BindView(R.id.cardForeground) public MaterialCardView cardFg;
        @BindView(R.id.circular_reveal) public ImageView delCircularReveal;

        public boolean delAnimating = false;

        ItemHolder(View itemView) {
            super(itemView); // the view (row layout) for the item
            ButterKnife.bind(this, itemView); // unbind() is required only for Fragments
        }

        @OnTouch(R.id.expandDragButton)
        boolean onDragHandleTouch(MotionEvent event) {
            if (event.getActionMasked() == ACTION_DOWN) {
                itemTouchHelper.startDrag(this);
            }
            return false;
        }

        @OnClick(R.id.expandDragButton)
        void onExpandToggle() {
            Item item = items.get(getAdapterPosition());

            expandDragBtn.setImageResource(item.isExpanded() ? R.drawable.avd_collapse : R.drawable.avd_expand);
            ((Animatable) expandDragBtn.getDrawable()).start();

            descTxVi.setVisibility(descTxVi.getVisibility() == GONE ? VISIBLE : GONE);
            item.setExpanded(descTxVi.getVisibility() == VISIBLE);

            beginDelayedTransition(recyclerView);
        }

        @OnClick(R.id.cardForeground)
        void onCardClick() {
            if (selectionModeEnabled) {
                selectChBx.performClick();
            } else if (!descTxVi.getText().toString().isEmpty()) {
                expandDragBtn.post(() -> expandDragBtn.performClick());
            }
        }

        @OnTextChanged(R.id.price)
        void onPriceChanged() {
            String priceString = priceEdTx.getText().toString();
            if (!priceString.isEmpty()) {
                long price = parseLong(priceString);
                Item item = items.get(getAdapterPosition());
                item.setPrice(price);
            }
        }

        @OnCheckedChanged(R.id.selectCheckBox)
        void onItemSelected(boolean isChecked) {
            if (isChecked) {
                priceContainer.setVisibility(VISIBLE);
                selectedItems.add(items.get(getAdapterPosition()));
            } else {
                priceContainer.setVisibility(GONE);
                selectedItems.remove(items.get(getAdapterPosition()));
            }
        }
    }
}
