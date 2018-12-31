package com.pleon.buyt.ui.fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link Callable}
 * interface.
 */
public class ItemListFragment extends Fragment {

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     */
    public interface Callable {
        // TODO: Update argument type and name
        void onItemCheckboxClicked(Item item);
    }

    private Callable mHostActivity;
    private RecyclerView mItemRecyclerView;
    private ItemListAdapter adapter;
    private ItemListViewModel mItemListViewModel;
    private ItemTouchHelperCallback itemTouchHelperCallback;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ItemListFragment() {
    }

    public static ItemListFragment newInstance() {
        ItemListFragment fragment = new ItemListFragment();
//        Bundle args = new Bundle();
//        args.putInt(ARG_COLUMN_COUNT, columnCount);
//        fragment.setArguments(args);
        return fragment;
    }

    public void enableCheckboxes() {
        for (int i = 0; i < mItemRecyclerView.getChildCount(); i++) {
            View view = mItemRecyclerView.getLayoutManager().findViewByPosition(i);
//            CheckBox checkbox = view.findViewById(R.id.checkBox);
//            checkbox.setEnabled(true);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    // Unlike Activities, in a Fragment you inflate the fragment's view in onCreateView() method.
    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_item_list, container, false);

        mItemListViewModel = ViewModelProviders.of(this).get(ItemListViewModel.class);
        mItemListViewModel.getAll().observe(this, items -> adapter.setItems(items));

        // the root and only element in this fragment is a RecyclerView
        mItemRecyclerView = (RecyclerView) view;


        // for swipe-to-delete and drag-n-drop of item
        itemTouchHelperCallback = new ItemTouchHelperCallback(
                new ItemTouchHelperCallback.ItemTouchHelperListener() {
                    @Override
                    public void onMoved(int oldPosition, int newPosition) {
                        Collections.swap(adapter.getItems(), newPosition, oldPosition);
                        adapter.notifyItemMoved(oldPosition, newPosition);
                    }

                    @Override
                    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                        // get the removed item name to display it in snack bar
//                        String name = cartList.get(viewHolder.getAdapterPosition()).getName();

                        // backup of removed item for undo purpose
                        Item deletedItem = adapter.getItem(viewHolder.getAdapterPosition());
                        int deletedIndex = viewHolder.getAdapterPosition();

                        // remove the item from recycler view
                        adapter.removeItem(viewHolder.getAdapterPosition());
//                        mItemListViewModel.deleteItem(adapter.getItem(position));

                        // showing snack bar with Undo option
//                        Snackbar snackbar = Snackbar
//                                .make(, name + " removed from cart!", Snackbar.LENGTH_LONG);
//                        snackbar.setAction("UNDO", new View.OnClickListener() {
//                            @Override
//                            public void onClick(View view) {
//
//                                // undo is selected, restore the deleted item
//                                mAdapter.restoreItem(deletedItem, deletedIndex);
//                            }
//                        });
//                        snackbar.setActionTextColor(Color.YELLOW);
//                        snackbar.show();
                    }
                });

        ItemTouchHelper touchHelper = new ItemTouchHelper(itemTouchHelperCallback);
        touchHelper.attachToRecyclerView(mItemRecyclerView);

        adapter = new ItemListAdapter(mHostActivity, getActivity().getApplicationContext(), touchHelper);
        mItemRecyclerView.setAdapter(adapter);
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof Callable) {
            mHostActivity = (Callable) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement Callable");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mHostActivity = null;
    }

    public void toggleEditMode() {
        adapter.toggleEditMode();
        itemTouchHelperCallback.toggleEditMode();
    }

    public Set<Item> getSelectedItems() {
        return adapter.getSelectedItems();
    }

    public void clearSelectedItems() {
        adapter.clearSelectedItems();
    }

    public void enableItemsCheckbox() {
        adapter.togglePriceInput();
    }

    public boolean validateSelectedItemsPrice() {
        boolean validated = true;
        for (Item item : adapter.getSelectedItems()) {
            if (item.getPrice() == 0) {
                int itemIndex = adapter.getItems().indexOf(item); // FIXME: maybe heavy operation
                View itemView = adapter.mRecyclerView.getLayoutManager().findViewByPosition(itemIndex);
                TextInputLayout priceLayout = itemView.findViewById(R.id.price_layout);
                priceLayout.setError("price cannot be empty");
                validated = false;
            }
        }
        return validated;
    }
}
