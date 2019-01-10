package com.pleon.buyt.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.pleon.buyt.R;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;

import static android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS;

// DialogFragment is just another Fragment
public class LocationOffDialogFragment extends AppCompatDialogFragment {

    private Callback callback;

    public static LocationOffDialogFragment newInstance() {
        return new LocationOffDialogFragment();
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
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        AlertDialog dialog = new AlertDialog.Builder(getActivity())
                .setIcon(R.drawable.ic_location_off)
                .setPositiveButton(getString(R.string.go_to_settings), (d, which) -> {
                    startActivity(new Intent(ACTION_LOCATION_SOURCE_SETTINGS));
                })
                .setNegativeButton(getString(R.string.not_now), (d, which) -> callback.onEnableLocationDenied())
                .create();

        // getText is to preserve html formats
        dialog.setTitle(R.string.location_turn_on_title);
        dialog.setMessage(getText(R.string.location_turn_on_rationale));
        // dialog.setCancelable(false); // Prevent dialog from getting dismissed on back key pressed
        // dialog.setCanceledOnTouchOutside(false);
        return dialog;
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
