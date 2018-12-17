package com.pleon.buyt.ui;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.pleon.buyt.R;
import com.pleon.buyt.model.Store;
import com.pleon.buyt.viewmodel.StoreViewModel;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProviders;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link CreateStoreFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link CreateStoreFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CreateStoreFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private Button mAddButton;

    private OnFragmentInteractionListener mListener;

    public CreateStoreFragment() {
        // Required empty public constructor
    }

    public static CreateStoreFragment newInstance() {
        CreateStoreFragment fragment = new CreateStoreFragment();
//        Bundle args = new Bundle();
//        args.putString(ARG_PARAM1, param1);
//        args.putString(ARG_PARAM2, param2);
//        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
//            mParam1 = getArguments().getString(ARG_PARAM1);
//            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_create_store, container, false);
        mAddButton = view.findViewById(R.id.addStoreButton);
        mAddButton.setOnClickListener(button -> {
            EditText name = view.findViewById(R.id.storeName);
            EditText category = view.findViewById(R.id.storeCategory);
            Store store = new Store(null, name.getText().toString(), category.getText().toString());
            // get the same ViewModel as the containing activity
            ViewModelProviders.of((FragmentActivity) mListener).get(StoreViewModel.class).setCreatedStore(store);
        });
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
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
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        void onStoreCreated(long storeId);
    }
}
