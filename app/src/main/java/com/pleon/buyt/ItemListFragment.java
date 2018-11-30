package com.pleon.buyt;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.pleon.buyt.ItemAdapter.ItemHolder;
import com.pleon.buyt.ItemContent.Item;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.Adapter;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class ItemListFragment extends Fragment {

    private OnListFragmentInteractionListener mListener;
    private RecyclerView mItemRecyclerView;
    private Adapter<ItemHolder> adapter;
    private int itemListCurrentSize;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ItemListFragment() {
    }

    public static ItemListFragment newInstance(int columnCount) {
        ItemListFragment fragment = new ItemListFragment();
        Bundle args = new Bundle();
        // args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
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

        // the root and only element in this fragment is a RecyclerView
        mItemRecyclerView = (RecyclerView) view;
        Context context = view.getContext();
        mItemRecyclerView.setLayoutManager(new LinearLayoutManager(context));
        adapter = new ItemAdapter(mListener, getActivity().getApplicationContext());
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
        // Check if size of the items has changed (a new item is added). This is to ensure that
        // the user has not entered to the "Add Activity" and then immediately pressed back button
        // which causes the notifyItemInserted() method to throw exception
        if (itemListCurrentSize != adapter.getItemCount()) {
            adapter.notifyItemInserted(adapter.getItemCount() - 1);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnListFragmentInteractionListener) {
            mListener = (OnListFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     */
    public interface OnListFragmentInteractionListener {
        // TODO: Update argument type and name
        void onListFragmentInteraction(Item item);
    }
}
