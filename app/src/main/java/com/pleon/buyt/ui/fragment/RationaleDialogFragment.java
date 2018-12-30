package com.pleon.buyt.ui.fragment;

import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;

import com.pleon.buyt.R;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;

// DialogFragment is just another Fragment
public class RationaleDialogFragment extends AppCompatDialogFragment {

    public static RationaleDialogFragment newInstance(int title, int message, boolean permissionRequired) {
        RationaleDialogFragment instance = new RationaleDialogFragment();

        Bundle args = new Bundle();
        args.putInt("TITLE", title);
        args.putInt("MESSAGE", message);
        args.putBoolean("PERMISSION_REQUIRED", permissionRequired);
        instance.setArguments(args);

        return instance;
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
        boolean permissionRequired = getArguments().getBoolean("PERMISSION_REQUIRED");

        AlertDialog dialog = new AlertDialog.Builder(getActivity())
                .setIcon(R.drawable.ic_location_off)
                .setPositiveButton(getString(R.string.go_to_settings), (d, which) -> {
                    startActivity(makeIntent(permissionRequired));
                })
                .setNegativeButton(getString(R.string.not_now), (d, which) -> {
                    // TODO: user refused to enable location
                }).create();

        // getText is to preserve html formats
        dialog.setTitle(getText(getArguments().getInt("TITLE")));
        dialog.setMessage(getText(getArguments().getInt("MESSAGE")));
        // dialog.setCancelable(false); // Prevent dialog from getting dismissed on back key pressed
        dialog.setCanceledOnTouchOutside(false);
        return dialog;
    }

    private Intent makeIntent(boolean permissionRequired) {
        Intent intent;
        if (permissionRequired) {
            intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            Uri uri = Uri.fromParts("package", getActivity().getPackageName(), null);
            intent.setData(uri);
        } else {
            // just location is off
            intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        }
        return intent;
    }
}
