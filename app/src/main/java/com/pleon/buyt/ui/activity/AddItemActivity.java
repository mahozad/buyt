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

import java.util.Date;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProviders;

import static java.util.Collections.singletonList;

public class AddItemActivity extends AppCompatActivity implements AddItemFragment.Callback {

    private AddItemFragment addItemFragment;
    private MainViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_item);

        viewModel = ViewModelProviders.of(this).get(MainViewModel.class);

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

    /**
     * Called for adding regular (not bought) item.
     * <p>
     * Calling finish() in this method is safe because insertion of item is run in an
     * {@link android.os.AsyncTask AsyncTask} which is responsible for finishing its job in
     * any case (even if the activity is destroyed).
     *
     * @param item
     */
    @Override
    public void onSubmit(Item item) {
        viewModel.addItem(item);
        finish();
    }

    /**
     * Called for adding purchased item.
     * <p>
     * Calling finish() in this method is safe because database operations are run in an
     * {@link android.os.AsyncTask AsyncTask} which is responsible for finishing its job in
     * any case (even if the activity is destroyed).
     *
     * @param item
     * @param store
     */
    @Override
    public void onSubmitPurchasedItem(Item item, Store store, Date purchaseDate) {
        viewModel.addItem(item);
        // FIXME: purchaseId is not set for the item
        viewModel.buy(singletonList(item), store, purchaseDate);
        finish();
    }
}
