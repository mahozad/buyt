package com.pleon.buyt.ui.activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.pleon.buyt.R;
import com.pleon.buyt.model.Item;
import com.pleon.buyt.model.Store;
import com.pleon.buyt.ui.fragment.AddItemFragment;
import com.pleon.buyt.viewmodel.AddItemViewModel;

import java.util.Date;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProviders;
import androidx.preference.PreferenceManager;

import static com.pleon.buyt.ui.activity.MainActivity.DEFAULT_THEME;
import static com.pleon.buyt.ui.activity.MainActivity.KEY_PREF_THEME;

public class AddItemActivity extends AppCompatActivity implements AddItemFragment.Callback {

    private AddItemFragment addItemFragment;
    private AddItemViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_item);

        viewModel = ViewModelProviders.of(this).get(AddItemViewModel.class);

        BottomAppBar mBottomAppBar = findViewById(R.id.bottom_bar);
        setSupportActionBar(mBottomAppBar); // This MUST be set as AddItemFragment needs ActionBar.

        FragmentManager fragMgr = getSupportFragmentManager();
        addItemFragment = (AddItemFragment) fragMgr.findFragmentById(R.id.fragment_add_item);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(v -> addItemFragment.onDonePressed()); // notify fragment fab was clicked
    }

    private void setTheme() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String theme = preferences.getString(KEY_PREF_THEME, DEFAULT_THEME);
        setTheme(DEFAULT_THEME.equals(theme) ? R.style.AppTheme : R.style.LightTheme);
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
        viewModel.addPurchasedItem(item, store, purchaseDate);
        finish();
    }
}
