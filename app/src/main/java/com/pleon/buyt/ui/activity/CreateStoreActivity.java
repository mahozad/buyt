package com.pleon.buyt.ui.activity;

import android.location.Location;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.pleon.buyt.R;
import com.pleon.buyt.model.Store;
import com.pleon.buyt.ui.dialog.SelectDialogFragment;
import com.pleon.buyt.ui.dialog.SelectionDialogRow;
import com.pleon.buyt.ui.fragment.CreateStoreFragment;
import com.pleon.buyt.viewmodel.StoreViewModel;

import java.util.ArrayList;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.ActionMenuView;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProviders;

public class CreateStoreActivity extends AppCompatActivity
        implements SelectDialogFragment.Callback, CreateStoreFragment.Callback {

    private CreateStoreFragment createStoreFragment;
    private TextView selectCategoryTxvi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_store);

        Bundle extras = getIntent().getExtras();
        Location location = (extras != null) ?
                getIntent().getParcelableExtra(MainActivity.EXTRA_LOCATION) : null;

        setSupportActionBar(findViewById(R.id.bottom_bar));

        ActionMenuView actionMenuView = findViewById(R.id.menu_view);
        // delegate to activity method
        actionMenuView.setOnMenuItemClickListener(this::onOptionsItemSelected);

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
        ActionMenuView actionMenuView = findViewById(R.id.menu_view);
        Menu mainMenu = actionMenuView.getMenu();
        getMenuInflater().inflate(R.menu.menu_add_store, mainMenu);

        selectCategoryTxvi = mainMenu.findItem(R.id.action_store_category).getActionView().findViewById(R.id.select_category);

        // Setting up "Select category" action because it has custom layout
        MenuItem item = mainMenu.findItem(R.id.action_store_category);
        item.getActionView().setOnClickListener(v -> onOptionsItemSelected(item));

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_cancel:
                finish();
                break;
            case R.id.action_store_category:
                // FIXME: initialize this only once
                ArrayList<SelectionDialogRow> selectionList = new ArrayList<>(); // dialog requires ArrayList
                for (Store.Category category : Store.Category.values()) {
                    SelectionDialogRow selection = new SelectionDialogRow(getString(category.getNameRes()), category.getImageRes());
                    selectionList.add(selection);
                }
                SelectDialogFragment selectStoreDialog = SelectDialogFragment.newInstance(selectionList);
                selectStoreDialog.show(getSupportFragmentManager(), "SELECT_STORE_DIALOG");
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    @Override
    public void onSelected(int index) {
        selectCategoryTxvi.setCompoundDrawablesRelativeWithIntrinsicBounds(Store.Category.values()[index].getImageRes(), 0, 0, 0);
        selectCategoryTxvi.setText(Store.Category.values()[index].getNameRes());
        createStoreFragment.setStoreCategory(Store.Category.values()[index]);
    }

    @Override
    public void onSubmit(Store store) {
        ViewModelProviders.of(this).get(StoreViewModel.class).insertForObserver(store);
        // Calling finish() is safe here. We are sure that the item will be added to
        // database because it is executed in a separate thread.
        finish();
    }
}
