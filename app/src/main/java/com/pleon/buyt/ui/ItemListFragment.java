package com.pleon.buyt.ui;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import com.pleon.buyt.R;
import com.pleon.buyt.model.Item;
import com.pleon.buyt.viewmodel.ItemListViewModel;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
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
    private int itemListCurrentSize;

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
            CheckBox checkbox = view.findViewById(R.id.checkBox);
            checkbox.setEnabled(true);
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
        adapter = new ItemListAdapter(mHostActivity, getActivity().getApplicationContext());
        mItemRecyclerView.setAdapter(adapter);
        return view;
    }

    @Override
    public void onPause() {
        super.onPause();
        // store the current size of the items in the list
        itemListCurrentSize = adapter.getItemCount();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Check if size of the items has changed (a new item_list_row is added). This is to ensure that
        // the user has not entered to the "Add Activity" and then immediately pressed back button
        // which causes the notifyItemInserted() method to throw exception
        if (itemListCurrentSize != adapter.getItemCount()) {
            adapter.notifyItemInserted(adapter.getItemCount() - 1);
        }
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
}
