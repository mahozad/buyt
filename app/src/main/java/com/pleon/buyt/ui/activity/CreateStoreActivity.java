package com.pleon.buyt.ui.activity;

import android.location.Location;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.pleon.buyt.R;
import com.pleon.buyt.model.Store;
import com.pleon.buyt.ui.fragment.CreateStoreFragment;
import com.pleon.buyt.viewmodel.StoreViewModel;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProviders;

public class CreateStoreActivity extends AppCompatActivity implements CreateStoreFragment.Callback {

    private CreateStoreFragment createStoreFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_store);

        Bundle extras = getIntent().getExtras();
        Location location = (extras != null) ?
                getIntent().getParcelableExtra(MainActivity.EXTRA_LOCATION) : null;

        setSupportActionBar(findViewById(R.id.bottom_bar)); // This MUST be set as CreateStoreFragment needs ActionBar.

        FragmentManager fragMgr = getSupportFragmentManager();
        createStoreFragment = (CreateStoreFragment) fragMgr.findFragmentById(R.id.createStoreContainer);

        if (createStoreFragment == null) {
            createStoreFragment = CreateStoreFragment.newInstance(location);
            fragMgr.beginTransaction()
                    .add(R.id.createStoreContainer, createStoreFragment)
                    .commit();
        }

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(v -> createStoreFragment.onDonePressed());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        /* FIXME: This statement was added to fix the menu item showing behind the fab.
         * Remove it if you update the material library and see if it's fixed */
        ((BottomAppBar) findViewById(R.id.bottom_bar)).setFabAlignmentMode(BottomAppBar.FAB_ALIGNMENT_MODE_END);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return true;
    }

    @Override
    public void onSubmit(Store store) {
        ViewModelProviders.of(this).get(StoreViewModel.class).insertForObserver(store);
        // Calling finish() is safe here. We are sure that the item will be added to
        // database because it is executed in a separate thread in ViewModel.
        finish();
    }
}
