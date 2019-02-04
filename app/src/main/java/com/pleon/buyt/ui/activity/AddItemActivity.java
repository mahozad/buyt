package com.pleon.buyt.ui.activity;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.pleon.buyt.R;
import com.pleon.buyt.model.Item;
import com.pleon.buyt.model.Store;
import com.pleon.buyt.ui.fragment.AddItemFragment;
import com.pleon.buyt.viewmodel.MainViewModel;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProviders;

import static java.util.Collections.singletonList;

public class AddItemActivity extends AppCompatActivity implements AddItemFragment.Callback {

    private AddItemFragment addItemFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_item);

        BottomAppBar mBottomAppBar = findViewById(R.id.bottom_bar);
        setSupportActionBar(mBottomAppBar); // This MUST be set as AddItemFragment needs ActionBar.

        FragmentManager fragMgr = getSupportFragmentManager();
        addItemFragment = (AddItemFragment) fragMgr.findFragmentById(R.id.fragment_add_item);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(v -> addItemFragment.onDonePressed()); // notify fragment fab was clicked
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

    // For regular item (not bought)
    @Override
    public void onSubmit(Item item) {
        MainViewModel mainViewModel = ViewModelProviders.of(this).get(MainViewModel.class);
        mainViewModel.addItem(item);
        // Calling finish() is safe here. We are sure that the item will be added to database,
        // because it is executed in a separate thread.
        finish();
    }

    // For bought item
    @Override
    public void onSubmit(Item item, Store store) {
        MainViewModel mainViewModel = ViewModelProviders.of(this).get(MainViewModel.class);
        mainViewModel.addItem(item);
        // FIXME: the item purchaseId is not set
        mainViewModel.buy(singletonList(item), store);
        // Calling finish() is safe here. We are sure that the item will be added to database,
        // because it is executed in a separate thread in ViewModel.
        finish();
    }
}
