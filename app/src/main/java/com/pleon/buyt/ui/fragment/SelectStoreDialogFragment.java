package com.pleon.buyt.ui.fragment;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import com.pleon.buyt.R;
import com.pleon.buyt.model.Store;
import com.pleon.buyt.ui.adapter.StoreListAdapter;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;

import static android.content.DialogInterface.BUTTON_POSITIVE;

// DialogFragment is just another Fragment
public class SelectStoreDialogFragment extends AppCompatDialogFragment implements StoreListAdapter.Callback {

    public interface Callback {
        void onStoreSelected(Store store);
    }

    private AlertDialog dialog;
    private Callback callback;

    public static SelectStoreDialogFragment newInstance(ArrayList<Store> nearStores) {
        SelectStoreDialogFragment fragment = new SelectStoreDialogFragment();
        Bundle args = new Bundle();
        args.putSerializable("NEAR_STORES", nearStores);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * When you override {@code onCreateDialog}, Android COMPLETELY IGNORES several
     * attributes of the root node of the .xml Layout you inflate. This includes,
     * but probably isn't limited to:
     * <li>background_color</li>
     * <li>layout_gravity</li>
     * <li>layout_width</li>
     * <li>layout_height</li>
     * <p>
     * See <a href="https://stackoverflow.com/a/41495370/8583692">this very good explanation</a>
     *
     * @param savedInstanceState
     * @return
     */
    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View customView = inflater.inflate(R.layout.fragment_store_list, null);

        StoreListAdapter adapter = new StoreListAdapter(getActivity().getApplicationContext(), this);
        adapter.setStores((List<Store>) getArguments().getSerializable("NEAR_STORES"));

        RecyclerView storeRecyclerView = customView.findViewById(R.id.storeList);
        storeRecyclerView.setAdapter(adapter);
        // remove RecyclerView blinking animation
        ((SimpleItemAnimator) storeRecyclerView.getItemAnimator()).setSupportsChangeAnimations(false);

        dialog = new AlertDialog.Builder(getActivity())
                .setView(customView).setTitle("TITLE")
                .setPositiveButton("OK", (d, which) -> {
                    Store selectedStore = adapter.getSelectedStores();
                    callback.onStoreSelected(selectedStore);
                })
                .setNegativeButton("CANCEL", (d, which) -> {
                    // cancel
                }).create();

        dialog.setCanceledOnTouchOutside(false);
        // Disable OK button by default (the button can be get only after the dialog is shown)
        dialog.setOnShowListener(d -> ((AlertDialog) d).getButton(BUTTON_POSITIVE).setEnabled(false));

        return dialog;
    }


    @Override
    public void onStoreClick() {
        dialog.getButton(BUTTON_POSITIVE).setEnabled(true);
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
}
