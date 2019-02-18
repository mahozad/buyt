package com.pleon.buyt.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.pleon.buyt.R;
import com.pleon.buyt.model.Store;
import com.pleon.buyt.ui.fragment.CreateStoreFragment;
import com.pleon.buyt.viewmodel.CreateStoreViewModel;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProviders;

public class CreateStoreActivity extends AppCompatActivity implements CreateStoreFragment.Callback {

    private CreateStoreViewModel viewModel;
    private CreateStoreFragment createStoreFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_store);

        // This MUST be set as CreateStoreFragment needs ActionBar.
        setSupportActionBar(findViewById(R.id.bottom_bar));

        viewModel = ViewModelProviders.of(this).get(CreateStoreViewModel.class);

        FragmentManager fragMgr = getSupportFragmentManager();
        createStoreFragment = (CreateStoreFragment) fragMgr.findFragmentById(R.id.fragment_create_store);

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

    /**
     * Calling finish() in this method is safe because insertion of store is run in an
     * {@link android.os.AsyncTask AsyncTask} which is responsible for finishing its job in
     * any case (even if the activity is destroyed).
     *
     * @param store
     */
    @Override
    public void onSubmit(Store store) {
        viewModel.addStore(store).observe(this, insertedStore -> {
            setResult(RESULT_OK, new Intent().putExtra("STORE", insertedStore));
            finish();
        });
    }
}
