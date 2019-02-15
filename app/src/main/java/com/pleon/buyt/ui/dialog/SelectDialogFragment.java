package com.pleon.buyt.ui.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import com.pleon.buyt.R;
import com.pleon.buyt.ui.adapter.SelectionListAdapter;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;

import static android.content.DialogInterface.BUTTON_POSITIVE;

// DialogFragment is just another Fragment
public class SelectDialogFragment extends AppCompatDialogFragment implements SelectionListAdapter.Callback {

    public interface Callback {
        void onSelected(int index);
    }

    private AlertDialog dialog;
    private static Callback callback;

    public static SelectDialogFragment newInstance(Callback callback, @StringRes int title,
                                                   ArrayList<SelectionDialogRow> list) {
        // FIXME: callback should be set in the onAttach() method, but because the context passed to it
        // is the containing activity and not the containing fragment, we passed it here
        SelectDialogFragment.callback = callback;

        SelectDialogFragment fragment = new SelectDialogFragment();
        Bundle args = new Bundle();
        args.putSerializable("LIST", list);
        fragment.setArguments(args);
        args.putInt("TITLE", title);

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
        View customView = inflater.inflate(R.layout.fragment_selection_list, null);

        SelectionListAdapter adapter = new SelectionListAdapter(getActivity().getApplicationContext(), this);
        adapter.setList((List<SelectionDialogRow>) getArguments().getSerializable("LIST"));

        RecyclerView storeRecyclerView = customView.findViewById(R.id.storeList);
        storeRecyclerView.setAdapter(adapter);
        // remove RecyclerView blinking animation
        ((SimpleItemAnimator) storeRecyclerView.getItemAnimator()).setSupportsChangeAnimations(false);

        dialog = new AlertDialog.Builder(getActivity())
                .setView(customView).setTitle(getString(getArguments().getInt("TITLE")))
                .setPositiveButton(android.R.string.ok, (d, which) -> {
                    int selectedIndex = adapter.getSelectedIndex();
                    callback.onSelected(selectedIndex);
                })
                .setNegativeButton(android.R.string.cancel, (d, which) -> {
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

//    @Override
//    public void onAttach(Context context) {
//        super.onAttach(context);
//        if (context instanceof Callback) {
//            callback = (Callback) context;
//        } else {
//            throw new RuntimeException(context.toString() + " must implement Callback");
//        }
//    }

    @Override
    public void onDetach() {
        super.onDetach();
        callback = null;
    }
}
