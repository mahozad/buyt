package com.pleon.buyt.ui.adapter;

import android.content.Context;
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
import com.pleon.buyt.model.Item;
import com.pleon.buyt.ui.NumberInputWatcher;
import com.pleon.buyt.ui.adapter.ItemListAdapter.ItemHolder;

import java.text.NumberFormat;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.Adapter;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;
import androidx.transition.ChangeBounds;
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

    private Context context;
    private List<Item> items;
    private RecyclerView recyclerView;
    private boolean dragModeEnabled = false;
    private boolean selectionModeEnabled = false;
    private final Set<Item> selectedItems = new HashSet<>();
    private NumberFormat numberFormat;

    private ItemTouchHelper itemTouchHelper;

    public ItemListAdapter(Context context, ItemTouchHelper itemTouchHelper) {
        this.context = context;
        this.itemTouchHelper = itemTouchHelper;
        this.numberFormat = NumberFormat.getInstance();
        // setHasStableIds is an optimization hint that you give to the RecyclerView
        // and tell it "when I provide a ViewHolder, its id is unique and will not change."
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
        // keep this method as lightweight as possible as it is called for every row
        if (items != null) {
            Item item = items.get(position);
            holder.categoryImgVi.setImageResource(item.getCategory().getImageRes());
            holder.nameTxVi.setText(item.getName());
            holder.descTxVi.setText(item.getDescription());
            holder.quantityTxVi.setText(numberFormat.format(item.getQuantity().getQuantity()) + " " + context.getString(item.getQuantity().getUnit().getNameRes()));
            holder.urgentImgVi.setVisibility(item.isUrgent() ? VISIBLE : INVISIBLE);
            holder.selectChBx.setChecked(selectedItems.contains(item));
            holder.descTxVi.setVisibility(selectionModeEnabled || !item.isExpanded() ? GONE : VISIBLE);
            holder.delRevealView.setAlpha(0f); // for the case of undo of deleted item
            holder.priceEdTx.setText(item.getTotalPrice() != 0 ? String.valueOf(item.getTotalPrice()) : "");

            if (selectionModeEnabled) {
                holder.selectChBx.setVisibility(VISIBLE);
                holder.expandDragBtn.setVisibility(INVISIBLE);
            } else {
                holder.selectChBx.setVisibility(INVISIBLE);
                holder.priceContainer.setVisibility(GONE);
                if (dragModeEnabled) {
                    holder.expandDragBtn.setImageResource(R.drawable.ic_drag_handle);
                    holder.expandDragBtn.setVisibility(VISIBLE);
                } else if (item.getDescription() != null) {
                    holder.expandDragBtn.setImageResource(item.isExpanded() ? R.drawable.avd_collapse : R.drawable.avd_expand);
                    holder.expandDragBtn.setVisibility(VISIBLE);
                } else {
                    holder.expandDragBtn.setVisibility(INVISIBLE);
                }
            }
        } // else: case of data not being ready yet; set a placeholder or something
    }

    @Override
    public int getItemCount() {
        return items == null ? 0 : items.size();
    }

    // setHasStableIds() should also be set (in e.g. constructor). This is an optimization hint that you
    // give to the RecyclerView and tell it "when I provide a ViewHolder, its id is unique and won't change."
    @Override
    public long getItemId(int position) {
        return items.get(position).getItemId();
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

    public void toggleEditMode() {
        if (items.size() > 0) { // toggle only if there is any item
            dragModeEnabled = !dragModeEnabled;
            notifyDataSetChanged();
        }
    }

    public void togglePriceInput(boolean enabled) {
        selectionModeEnabled = enabled;
        notifyDataSetChanged();
    }

    // Adapter (and RecyclerView) work with ViewHolders instead of direct Views.
    public class ItemHolder extends ViewHolder {

        @BindView(R.id.categoryIcon) ImageView categoryImgVi;
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
        @BindView(R.id.delete_icon) public ImageView delIcon;
        @BindView(R.id.circular_reveal) public View delRevealView;

        public boolean delAnimating = false;

        ItemHolder(View itemView) {
            super(itemView); // the view (row layout) for the item
            ButterKnife.bind(this, itemView); // unbind() is required only for Fragments

            String suffix = context.getString(R.string.input_suffix_price);
            priceEdTx.addTextChangedListener(new NumberInputWatcher(priceTxInLt, priceEdTx, suffix));
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

            beginDelayedTransition(recyclerView, new ChangeBounds().setDuration(200));

            descTxVi.setVisibility(descTxVi.getVisibility() == GONE ? VISIBLE : GONE);
            item.setExpanded(descTxVi.getVisibility() == VISIBLE);
        }

        @OnClick(R.id.cardForeground)
        void onCardClick() {
            if (selectionModeEnabled) {
                selectChBx.performClick(); // delegate to chBx listener
            } else if (!descTxVi.getText().toString().isEmpty()) {
                expandDragBtn.post(() -> expandDragBtn.performClick());
            }
        }

        @OnTextChanged(R.id.price)
        void onPriceChanged() {
            String priceString = priceEdTx.getText().toString().replaceAll("[^\\d]", "");
            if (!priceString.isEmpty()) {
                long price = parseLong(priceString);
                Item item = items.get(getAdapterPosition());
                item.setTotalPrice(price);
            }
        }

        @OnCheckedChanged(R.id.selectCheckBox)
        void onItemSelected(boolean checked) {
            beginDelayedTransition(recyclerView, new ChangeBounds().setDuration(200));
            if (checked) {
                priceContainer.setVisibility(VISIBLE);
                selectedItems.add(items.get(getAdapterPosition()));
            } else {
                priceContainer.setVisibility(GONE);
                selectedItems.remove(items.get(getAdapterPosition()));
                Item item = items.get(getAdapterPosition());
                item.setTotalPrice(0);
                priceEdTx.getText().clear();
            }
        }
    }
}
