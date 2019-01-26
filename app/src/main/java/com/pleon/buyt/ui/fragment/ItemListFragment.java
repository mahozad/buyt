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
import com.pleon.buyt.ui.TouchHelperCallback;
import com.pleon.buyt.ui.TouchHelperCallback.ItemTouchHelperListener;
import com.pleon.buyt.ui.adapter.ItemListAdapter;
import com.pleon.buyt.viewmodel.MainViewModel;

import java.util.Collections;
import java.util.Set;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

import static com.google.android.material.snackbar.Snackbar.LENGTH_LONG;
import static java.util.Collections.singletonList;

public class ItemListFragment extends Fragment implements ItemTouchHelperListener {

    @BindView(R.id.list) RecyclerView itemRecyclerView;
    private ItemListAdapter adapter;
    private MainViewModel mainViewModel;
    private TouchHelperCallback touchHelperCallback;
    private Unbinder unbinder;
    private boolean itemsReordered = false;

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
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_item_list, container, false);
        unbinder = ButterKnife.bind(this, view); // unbinding is required only for Fragments

        mainViewModel = ViewModelProviders.of(this).get(MainViewModel.class);
        // In fragments use getViewLifecycleOwner() as owner argument
        mainViewModel.getAllItems().observe(getViewLifecycleOwner(), items -> adapter.setItems(items));

        // for swipe-to-delete and drag-n-drop of item
        touchHelperCallback = new TouchHelperCallback(this);
        ItemTouchHelper touchHelper = new ItemTouchHelper(touchHelperCallback);
        touchHelper.attachToRecyclerView(itemRecyclerView);

        adapter = new ItemListAdapter(getContext(), touchHelper);
        itemRecyclerView.setAdapter(adapter);
        return view;
    }

    @Override
    public void onMoved(int oldPosition, int newPosition) {
        adapter.getItem(oldPosition).setPosition(newPosition);
        adapter.getItem(newPosition).setPosition(oldPosition);
        Collections.swap(adapter.getItems(), newPosition, oldPosition);
        adapter.notifyItemMoved(oldPosition, newPosition);
        itemsReordered = true;
    }

    @Override
    public void onSwiped(ViewHolder viewHolder, int direction) {
        // Backup the item for undo purpose
        int itemIndex = viewHolder.getAdapterPosition();
        Item item = adapter.getItem(itemIndex);

        item.setFlaggedForDeletion(true);
        mainViewModel.updateItems(singletonList(item));

        showUndoSnackbar(item);
    }

    private void showUndoSnackbar(Item item) {
        Snackbar snackbar = Snackbar.make(getActivity().findViewById(R.id.snackBarContainer),
                getString(R.string.item_deleted_message, item.getName()), LENGTH_LONG);
        snackbar.setAction(getString(R.string.undo), v -> {
            item.setFlaggedForDeletion(false);
            mainViewModel.updateItems(singletonList(item));
        });
        snackbar.addCallback(new BaseCallback<Snackbar>() {
            public void onDismissed(Snackbar transientBottomBar, int event) {
                if (event != DISMISS_EVENT_ACTION) { // If dismiss wasn't because of "UNDO"...
                    // ... Then delete the item completely from database
                    mainViewModel.deleteItem(item);
                }
            }
        });
        snackbar.show();
    }

    /**
     * From multiple SO questions it seems that the onPause() is preferred to onStop()
     * for persisting data.
     */
    @Override
    public void onPause() {
        super.onPause();
        if (itemsReordered) {
            mainViewModel.updateItems(adapter.getItems());
            itemsReordered = false;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // set the bindings to null (frees up memory)
        unbinder.unbind();
    }

    public void toggleEditMode() {
        adapter.toggleEditMode();
        touchHelperCallback.toggleDragMode();
    }

    public Set<Item> getSelectedItems() {
        return adapter.getSelectedItems();
    }

    public boolean isSelectedEmpty() {
        return adapter.getSelectedItems().size() == 0;
    }

    public void clearSelectedItems() {
        adapter.clearSelectedItems();
    }

    public void toggleItemsCheckbox(boolean enabled) {
        adapter.togglePriceInput(enabled);
    }

    public boolean validateSelectedItemsPrice() {
        boolean validated = true;
        for (Item item : adapter.getSelectedItems()) {
            if (item.getTotalPrice() == 0) {
                int itemIndex = adapter.getItems().indexOf(item); // FIXME: maybe heavy operation
                View itemView = adapter.recyclerView.getLayoutManager().findViewByPosition(itemIndex);
                TextInputLayout priceLayout = itemView.findViewById(R.id.price_layout);
                priceLayout.setError("price cannot be empty");
                validated = false;
            }
        }
        return validated;
    }

    public boolean isCartEmpty() {
        return adapter.getItems().size() == 0;
    }

    public int getNextItemPosition() {
        return adapter.getItemCount();
    }
}
