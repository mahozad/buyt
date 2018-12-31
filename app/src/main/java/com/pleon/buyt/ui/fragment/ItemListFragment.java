package com.pleon.buyt.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.snackbar.BaseTransientBottomBar.BaseCallback;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.pleon.buyt.R;
import com.pleon.buyt.model.Item;
import com.pleon.buyt.ui.ItemListAdapter;
import com.pleon.buyt.ui.ItemTouchHelperCallback;
import com.pleon.buyt.viewmodel.ItemListViewModel;

import java.util.Collections;
import java.util.Set;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import static com.google.android.material.snackbar.Snackbar.LENGTH_LONG;

public class ItemListFragment extends Fragment {

    private RecyclerView itemRecyclerView;
    private ItemListAdapter itemAdapter;
    private ItemListViewModel itemListViewModel;
    private ItemTouchHelperCallback itemTouchHelperCallback;
    private int itemsLastSize = 0;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ItemListFragment() {
        // Required empty constructor
    }

    public static ItemListFragment newInstance() {
        return new ItemListFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    // Unlike Activities, in a Fragment you inflate the fragment's view in onCreateView() method.
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_item_list, container, false);

        itemListViewModel = ViewModelProviders.of(this).get(ItemListViewModel.class);
        itemListViewModel.getAll().observe(this, items -> {
            // FIXME: What if 2 items were deleted and also 1 item was added? And it seems to be smelly code.
            if (items.size() >= itemsLastSize) { // set only if item is added (not deleted)
                itemAdapter.setItems(items);
            }
            itemsLastSize = items.size();
        });

        // the root and only element in this fragment is a RecyclerView
        itemRecyclerView = (RecyclerView) view;

        // for swipe-to-delete and drag-n-drop of item
        itemTouchHelperCallback = new ItemTouchHelperCallback(
                new ItemTouchHelperCallback.ItemTouchHelperListener() {
                    @Override
                    public void onMoved(int oldPosition, int newPosition) {
                        Collections.swap(itemAdapter.getItems(), newPosition, oldPosition);
                        itemAdapter.notifyItemMoved(oldPosition, newPosition);
                    }

                    @Override
                    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                        // Backup the item for undo purpose
                        int itemIndex = viewHolder.getAdapterPosition();
                        Item item = itemAdapter.getItem(itemIndex);

                        // Delete only from the adapter
                        itemAdapter.removeItem(itemIndex);

                        Snackbar.make(getActivity().findViewById(R.id.snackBarContainer),
                                "Item \"" + item.getName() + "\" deleted", LENGTH_LONG)
                                .addCallback(new BaseCallback<Snackbar>() {
                                    public void onDismissed(Snackbar transientBottomBar, int event) {
                                        if (event != DISMISS_EVENT_ACTION) {
                                            // Dismiss wasn't because of tapping "UNDO" so delete the item completely
                                            itemListViewModel.deleteItem(item);
                                        }
                                    }
                                })
                                .setAction("UNDO", v -> itemAdapter.addItem(item, itemIndex))
                                .show();
                    }
                });

        ItemTouchHelper touchHelper = new ItemTouchHelper(itemTouchHelperCallback);
        touchHelper.attachToRecyclerView(itemRecyclerView);

        itemAdapter = new ItemListAdapter(touchHelper);
        itemRecyclerView.setAdapter(itemAdapter);
        return view;
    }

    public void toggleEditMode() {
        itemAdapter.toggleEditMode();
        itemTouchHelperCallback.toggleEditMode();
    }

    public Set<Item> getSelectedItems() {
        return itemAdapter.getSelectedItems();
    }

    public void clearSelectedItems() {
        itemAdapter.clearSelectedItems();
    }

    public void enableItemsCheckbox() {
        itemAdapter.togglePriceInput();
    }

    public boolean validateSelectedItemsPrice() {
        boolean validated = true;
        for (Item item : itemAdapter.getSelectedItems()) {
            if (item.getPrice() == 0) {
                int itemIndex = itemAdapter.getItems().indexOf(item); // FIXME: maybe heavy operation
                View itemView = itemAdapter.recyclerView.getLayoutManager().findViewByPosition(itemIndex);
                TextInputLayout priceLayout = itemView.findViewById(R.id.price_layout);
                priceLayout.setError("price cannot be empty");
                validated = false;
            }
        }
        return validated;
    }
}
