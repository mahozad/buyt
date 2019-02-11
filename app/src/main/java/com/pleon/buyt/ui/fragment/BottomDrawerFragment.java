package com.pleon.buyt.ui.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.navigation.NavigationView;
import com.pleon.buyt.R;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class BottomDrawerFragment extends BottomSheetDialogFragment
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "SHEET";

    public static BottomDrawerFragment newInstance() {
        return new BottomDrawerFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottomsheet_menu_fragment, container, false);
        NavigationView viewById = view.findViewById(R.id.navigation_view);
        viewById.setNavigationItemSelectedListener(this);
        return view;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        Log.i(TAG, "onNavigationItemSelected: salam");
        return true;
    }
}
