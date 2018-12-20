package com.pleon.buyt.ui;

import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;

import com.pleon.buyt.R;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import static android.content.DialogInterface.BUTTON_NEGATIVE;
import static android.content.DialogInterface.BUTTON_POSITIVE;

public class RationaleDialogFragment extends DialogFragment {

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog alertDialog = new AlertDialog
                .Builder(getActivity()/*,android.R.style.Theme_DeviceDefault_Dialog*/).create();
        alertDialog.setTitle(getString(R.string.use_location_title)); // TODO: extract strings
        alertDialog.setMessage(getText(R.string.use_location_rationale)); // getText to preserve html formats
        alertDialog.setButton(BUTTON_POSITIVE, getString(R.string.go_to_settings), (dialog, which) -> {
            // show app settings where user can click on Permissions and enable location permission
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            Uri uri = Uri.fromParts("package", getActivity().getPackageName(), null);
            intent.setData(uri);
            startActivity(intent);
        });

        alertDialog.setButton(BUTTON_NEGATIVE, getString(R.string.not_now), (dialog, which) -> {
            // user still refused to enable location
        });
        // alertDialog.setCancelable(false); // Prevent dialog from getting dismissed on back key pressed
        alertDialog.setCanceledOnTouchOutside(false); // Prevent dialog from getting dismissed on outside touch
        return alertDialog;
    }
}
