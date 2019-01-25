package com.pleon.buyt.ui.fragment;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.google.android.material.textfield.TextInputLayout;
import com.pleon.buyt.R;
import com.pleon.buyt.model.Coordinates;
import com.pleon.buyt.model.Store;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class CreateStoreFragment extends Fragment {

    public interface Callback {
        void onSubmit(Store store);
    }

    @BindView(R.id.name_layout) TextInputLayout nameTxInLt;
    @BindView(R.id.name) EditText nameEdtx;
    private Unbinder unbinder;
    private Callback callback;
    private Store.Category storeCategory = Store.Category.GENERIC;

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
        unbinder = ButterKnife.bind(this, view); // unbind() is required only for Fragments

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof Callback) {
            callback = (Callback) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement Callback");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        callback = null;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // set the bindings to null (frees up memory)
        unbinder.unbind();
    }

    public void setStoreCategory(Store.Category category) {
        this.storeCategory = category;
    }

    // FIXME: These methods are duplicate (from AddItemFragment). Refactor them

    public void onDonePressed() {
        if (validateFields()) {
            Coordinates coordinates = new Coordinates(getArguments().getParcelable("LOCATION"));
            String name = nameEdtx.getText().toString();
            Store store = new Store(coordinates, name, storeCategory);
            callback.onSubmit(store);
        }
    }

    private boolean validateFields() {
        if (isEmpty(nameEdtx)) {
            nameTxInLt.setError("Name cannot be empty");
            return false;
        }
        return true;
    }

    private boolean isEmpty(@NonNull EditText editText) {
        return editText.getText().toString().trim().length() == 0;
    }
}
