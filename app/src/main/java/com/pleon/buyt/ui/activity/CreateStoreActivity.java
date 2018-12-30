package com.pleon.buyt.ui.activity;

import android.location.Location;
import android.os.Bundle;

import com.pleon.buyt.R;
import com.pleon.buyt.ui.fragment.CreateStoreFragment;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

public class CreateStoreActivity extends AppCompatActivity implements CreateStoreFragment.OnFragmentInteractionListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_store);

        // TODO: extract the name of extra

        Bundle extras = getIntent().getExtras();
        Location location = extras != null ? getIntent().getParcelableExtra("LOCATION") : null;

        FragmentManager fragMgr = getSupportFragmentManager();
        Fragment createStoreFragment = fragMgr.findFragmentById(R.id.createStoreContainer);

        if (createStoreFragment == null) {
            fragMgr.beginTransaction()
                    .add(R.id.createStoreContainer, CreateStoreFragment.newInstance(location))
                    .commit();
        }
    }

    @Override
    public void onStoreCreated(long storeId) {

    }
}