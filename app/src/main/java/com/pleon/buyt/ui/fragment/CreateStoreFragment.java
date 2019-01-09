package com.pleon.buyt.ui.fragment;

import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.pleon.buyt.R;
import com.pleon.buyt.model.Coordinates;
import com.pleon.buyt.model.Store;
import com.pleon.buyt.viewmodel.StoreViewModel;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

public class CreateStoreFragment extends Fragment {

    private Button mAddButton;

    public CreateStoreFragment() {
        // Required empty constructor
    }

    public static CreateStoreFragment newInstance(Location storeLocation) {
        Bundle args = new Bundle();
        args.putParcelable("LOCATION", storeLocation);

        CreateStoreFragment fragment = new CreateStoreFragment();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_create_store, container, false);

        mAddButton = view.findViewById(R.id.addStoreButton);
        mAddButton.setOnClickListener(button -> {
            String name = ((EditText) view.findViewById(R.id.storeName)).getText().toString();
            String category = ((EditText) view.findViewById(R.id.storeCategory)).getText().toString();
            Coordinates coordinates = new Coordinates(getArguments().getParcelable("LOCATION"));

            Store store = new Store(coordinates, name, category);

            ViewModelProviders.of(this).get(StoreViewModel.class).insertForObserver(store);
            getActivity().finish();
        });
        return view;
    }
}
