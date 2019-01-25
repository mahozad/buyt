package com.pleon.buyt.ui.activity;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.pleon.buyt.R;
import com.pleon.buyt.model.Item;
import com.pleon.buyt.model.Store;
import com.pleon.buyt.ui.dialog.SelectDialogFragment;
import com.pleon.buyt.ui.dialog.SelectionDialogRow;
import com.pleon.buyt.ui.fragment.AddItemFragment;
import com.pleon.buyt.viewmodel.MainViewModel;

import java.util.ArrayList;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.ActionMenuView;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProviders;

public class AddItemActivity extends AppCompatActivity
        implements AddItemFragment.Callback, SelectDialogFragment.Callback {

    private AddItemFragment addItemFragment;
    private TextView selectCategoryTxvi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_item);

        BottomAppBar mBottomAppBar = findViewById(R.id.bottom_bar);
        setSupportActionBar(mBottomAppBar);
        ActionMenuView actionMenuView = findViewById(R.id.menu_view);
        // delegate to activity method
        actionMenuView.setOnMenuItemClickListener(this::onOptionsItemSelected);

        FragmentManager fragMgr = getSupportFragmentManager();
        addItemFragment = (AddItemFragment) fragMgr.findFragmentById(R.id.container_fragment_add_item);
        if (addItemFragment == null) {
            int itemOrder = getIntent().getIntExtra(MainActivity.EXTRA_ITEM_ORDER, 0);
            addItemFragment = AddItemFragment.newInstance(itemOrder);
            fragMgr.beginTransaction()
                    .add(R.id.container_fragment_add_item, addItemFragment)
                    .commit();
        }

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(v -> addItemFragment.onDonePressed()); // notify fragment fab was clicked
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        ActionMenuView actionMenuView = findViewById(R.id.menu_view);
        Menu mainMenu = actionMenuView.getMenu();
        getMenuInflater().inflate(R.menu.menu_add_item, mainMenu);

        selectCategoryTxvi = mainMenu.findItem(R.id.action_item_category).getActionView().findViewById(R.id.select_store);

        // Setting up "Choose store" action because it has custom layout
        MenuItem item = mainMenu.findItem(R.id.action_item_category);
        item.getActionView().setOnClickListener(v -> onOptionsItemSelected(item));

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_cancel:
                finish();
                break;
            case R.id.action_item_category:
                // FIXME: initialize this only once
                ArrayList<SelectionDialogRow> selectionList = new ArrayList<>(); // dialog requires ArrayList
                if (addItemFragment.isBoughtChecked()) {
                    ViewModelProviders.of(this).get(MainViewModel.class).getAllStores().observe(this, stores -> {
                        for (Store store : stores) {
                            SelectionDialogRow selection = new SelectionDialogRow(store.getName(), store.getCategory().getImage());
                            selectionList.add(selection);
                        }
                    });
                } else {
                    for (Item.Category category : Item.Category.values()) {
                        SelectionDialogRow selection = new SelectionDialogRow(category.name(), category.getImage());
                        selectionList.add(selection);
                    }
                }
                SelectDialogFragment selectStoreDialog = SelectDialogFragment.newInstance(selectionList);
                selectStoreDialog.show(getSupportFragmentManager(), "SELECT_ITEM_DIALOG");
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    @Override
    public void onBoughtToggled(boolean checked) {
        selectCategoryTxvi.setText(checked ? "Select store" : addItemFragment.getItemCategory().name());
    }

    @Override
    public void onSubmit(Item item) {
        MainViewModel mainViewModel = ViewModelProviders.of(this).get(MainViewModel.class);
        mainViewModel.addItem(item);
        // Calling finish() is safe here. We are sure that the item will be added to database,
        // because it is executed in a separate thread.
        finish();
    }

    @Override
    public void onSelected(int index) {
        selectCategoryTxvi.setCompoundDrawablesRelativeWithIntrinsicBounds(Item.Category.values()[index].getImage(), 0, 0, 0);
        selectCategoryTxvi.setText(Item.Category.values()[index].name());
        addItemFragment.setItemCategory(Item.Category.values()[index]);
    }
}
