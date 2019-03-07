package com.pleon.buyt.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.snackbar.BaseTransientBottomBar.BaseCallback;
import com.google.android.material.snackbar.Snackbar;
import com.pleon.buyt.R;
import com.pleon.buyt.model.Store;
import com.pleon.buyt.ui.TouchHelperCallback;
import com.pleon.buyt.ui.TouchHelperCallback.ItemTouchHelperListener;
import com.pleon.buyt.ui.adapter.StoreListAdapter;
import com.pleon.buyt.viewmodel.StoreListViewModel;

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

public class StoreListFragment extends Fragment implements ItemTouchHelperListener {

    @BindView(R.id.list) RecyclerView storeRecyclerView;
    private StoreListAdapter adapter;
    private StoreListViewModel viewModel;
    private Unbinder unbinder;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public StoreListFragment() {
        // Required empty constructor
    }

    // Unlike Activities, in a Fragment you inflate the fragment's view in onCreateView() method.
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_store_list, container, false);
        unbinder = ButterKnife.bind(this, view); // unbinding is required only for Fragments

        viewModel = ViewModelProviders.of(this).get(StoreListViewModel.class);
        // In fragments use getViewLifecycleOwner() as owner argument
        viewModel.getAllStores().observe(getViewLifecycleOwner(), stores -> adapter.setStores(stores));

        // for swipe-to-delete of store
        TouchHelperCallback touchHelperCallback = new TouchHelperCallback(this);
        new ItemTouchHelper(touchHelperCallback).attachToRecyclerView(storeRecyclerView);

        adapter = new StoreListAdapter(getContext());
        storeRecyclerView.setAdapter(adapter);
        return view;
    }

    @Override
    public void onMoved(int oldPosition, int newPosition) {
        // Not needed
    }

    @Override
    public void onSwiped(ViewHolder viewHolder, int direction) {
        // Backup the item for undo purpose
        Store store = adapter.getStore(viewHolder.getAdapterPosition());

        store.setFlaggedForDeletion(true);
        viewModel.updateStore(store);

        showUndoSnackbar(store);
    }

    private void showUndoSnackbar(Store store) { // FIXME: Duplicate method
        Snackbar snackbar = Snackbar.make(getActivity().findViewById(R.id.snackBarContainer),
                getString(R.string.snackbar_message_item_deleted, store.getName()), LENGTH_LONG);
        snackbar.setAction(getString(R.string.snackbar_action_undo), v -> {
            store.setFlaggedForDeletion(false);
            viewModel.updateStore(store);
        });
        snackbar.addCallback(new BaseCallback<Snackbar>() {
            public void onDismissed(Snackbar transientBottomBar, int event) {
                if (event != DISMISS_EVENT_ACTION) { // If dismiss wasn't because of "UNDO"...
                    // ... then delete the store from database
                    viewModel.deleteStore(store);
                }
            }
        });
        snackbar.show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // set the bindings to null (frees up memory)
        unbinder.unbind();
    }
}
