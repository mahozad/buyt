package com.pleon.buyt.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.navigation.NavigationView;
import com.pleon.buyt.R;
import com.pleon.buyt.ui.activity.StatesActivity;

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
        switch (menuItem.getItemId()) {
            case R.id.states:
                Intent intent = new Intent(getContext(), StatesActivity.class);
                startActivity(intent);
                break;

            case R.id.stores:
                break;

            case R.id.settings:
                break;
        }
        return true;
    }
}
