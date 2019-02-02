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
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProviders;

import static java.util.Collections.singletonList;

public class AddItemActivity extends AppCompatActivity
        implements AddItemFragment.Callback, SelectDialogFragment.Callback {

    private AddItemFragment addItemFragment;
    private TextView selectCategoryTxvi;
    private List<Store> storeList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_item);

        BottomAppBar mBottomAppBar = findViewById(R.id.bottom_bar);
        setSupportActionBar(mBottomAppBar);

        FragmentManager fragMgr = getSupportFragmentManager();
        addItemFragment = (AddItemFragment) fragMgr.findFragmentById(R.id.fragment_add_item);
//        if (addItemFragment == null) {
//            int itemOrder = getIntent().getIntExtra(MainActivity.EXTRA_ITEM_ORDER, 0);
//            addItemFragment = AddItemFragment.newInstance(itemOrder);
//            fragMgr.beginTransaction()
//                    .add(R.id.container_fragment_add_item, addItemFragment)
//                    .commit();
//        }

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(v -> addItemFragment.onDonePressed()); // notify fragment fab was clicked
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_add_item, menu);

        selectCategoryTxvi = menu.findItem(R.id.action_item_category).getActionView().findViewById(R.id.select_store);
        // Setting up "Choose category" action because it has custom layout
        MenuItem menuItem = menu.findItem(R.id.action_item_category);
        menuItem.getActionView().setOnClickListener(v -> onOptionsItemSelected(menuItem));

        // FIXME: This statement was added to fix the menu item showing behind the fab.
        // Remove it if you update the material library and see if it's fixed
        ((BottomAppBar) findViewById(R.id.bottom_bar)).setFabAlignmentMode(BottomAppBar.FAB_ALIGNMENT_MODE_END);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_item_category:
                // FIXME: initialize this only once
                ArrayList<SelectionDialogRow> selectionList = new ArrayList<>(); // dialog requires ArrayList
                if (addItemFragment.isBoughtChecked()) {
                    ViewModelProviders.of(this).get(MainViewModel.class).getAllStores().observe(this, stores -> {
                        storeList = stores;
                        selectionList.clear();
                        for (Store store : stores) {
                            SelectionDialogRow selection = new SelectionDialogRow(store.getName(), store.getCategory().getImageRes());
                            selectionList.add(selection);
                        }
                        SelectDialogFragment selectStoreDialog = SelectDialogFragment.newInstance(selectionList);
                        selectStoreDialog.show(getSupportFragmentManager(), "SELECT_ITEM_DIALOG");
                    });
                } else {
                    for (Item.Category category : Item.Category.values()) {
                        SelectionDialogRow selection = new SelectionDialogRow(getString(category.getNameRes()), category.getImageRes());
                        selectionList.add(selection);
                    }
                    SelectDialogFragment selectStoreDialog = SelectDialogFragment.newInstance(selectionList);
                    selectStoreDialog.show(getSupportFragmentManager(), "SELECT_ITEM_DIALOG");
                }
                break;
            case android.R.id.home:
                finish();
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    @Override
    public void onBoughtToggled(boolean checked) {
        selectCategoryTxvi.setText(checked ? getString(R.string.action_select_store) : getString(addItemFragment.getItemCategory().getNameRes()));
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
        // because it is executed in a separate thread.
        finish();
    }

    @Override
    public void onSelected(int index) {
        String name;
        int imageRes;
        if (addItemFragment.isBoughtChecked()) {
            Store.Category category = Store.Category.values()[index];
            imageRes = category.getImageRes();
            name = storeList.get(index).getName();
            addItemFragment.setStore(storeList.get(index));
        } else {
            Item.Category category = Item.Category.values()[index];
            imageRes = category.getImageRes();
            name = getResources().getString(category.getNameRes());
            addItemFragment.setItemCategory(category);
        }
        selectCategoryTxvi.setCompoundDrawablesRelativeWithIntrinsicBounds(imageRes, 0, 0, 0);
        selectCategoryTxvi.setText(name);
    }
}
