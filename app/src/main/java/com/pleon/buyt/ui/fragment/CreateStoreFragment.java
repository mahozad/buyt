package com.pleon.buyt.ui.fragment;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.material.textfield.TextInputLayout;
import com.pleon.buyt.R;
import com.pleon.buyt.model.Category;
import com.pleon.buyt.model.Coordinates;
import com.pleon.buyt.model.Store;
import com.pleon.buyt.ui.dialog.SelectDialogFragment;
import com.pleon.buyt.ui.dialog.SelectionDialogRow;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * This fragment requires a Toolbar as it needs to inflate and use a menu item for selection of
 * store category. So the activities using this fragment must have a Toolbar set.
 */
public class CreateStoreFragment extends Fragment implements SelectDialogFragment.Callback {

    public interface Callback {
        void onSubmit(Store store);
    }

    @BindView(R.id.name_layout) TextInputLayout nameTxInLt;
    @BindView(R.id.name) EditText nameEdtx;

    private Unbinder unbinder;
    private Callback callback;
    private Category storeCategory = Category.GROCERY;
    private TextView selectCategoryTxvi;

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
        setHasOptionsMenu(true); // for the onCreateOptionsMenu() method to be called

        return view;
    }

    /**
     * For this method to be called, it is required that setHasOptionsMenu() has been set.
     * <p>
     * Note that the containing activity must have a Toolbar set so this fragment can inflate and
     * use its own menu item.
     *
     * @param menu
     * @param inflater
     */
    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_add_store, menu);

        selectCategoryTxvi = menu.findItem(R.id.action_store_category).getActionView().findViewById(R.id.select_category);
        // Setting up "Choose category" action because it has custom layout
        MenuItem menuItem = menu.findItem(R.id.action_store_category);
        menuItem.getActionView().setOnClickListener(v -> onOptionsItemSelected(menuItem));
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_store_category) {
            // FIXME: initialize this only once
            ArrayList<SelectionDialogRow> selectionList = new ArrayList<>(); // dialog requires ArrayList
            for (Category category : Category.values()) {
                SelectionDialogRow selection = new SelectionDialogRow(getString(category.getStoreNameRes()), category.getStoreImageRes());
                selectionList.add(selection);
            }
            SelectDialogFragment selectStoreDialog = SelectDialogFragment.newInstance(this, selectionList);
            selectStoreDialog.show(getActivity().getSupportFragmentManager(), "SELECT_STORE_DIALOG");
        }
        return true;
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


    @Override
    public void onSelected(int index) {
        selectCategoryTxvi.setCompoundDrawablesRelativeWithIntrinsicBounds(Category.values()[index].getStoreImageRes(), 0, 0, 0);
        selectCategoryTxvi.setText(Category.values()[index].getStoreNameRes());
        storeCategory = Category.values()[index];
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
