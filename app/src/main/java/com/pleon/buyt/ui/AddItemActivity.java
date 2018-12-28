package com.pleon.buyt.ui;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.pleon.buyt.R;
import com.pleon.buyt.model.Item;
import com.pleon.buyt.viewmodel.ItemListViewModel;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.ActionMenuView;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProviders;

public class AddItemActivity extends AppCompatActivity {

    private AddItemFragment addItemFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_item);

        BottomAppBar mBottomAppBar = findViewById(R.id.bottom_bar);
        setSupportActionBar(mBottomAppBar);
        ActionMenuView actionMenuView = findViewById(R.id.action_menu_view);
        // delegate to activity method
        actionMenuView.setOnMenuItemClickListener(this::onOptionsItemSelected);

        FragmentManager fragMgr = getSupportFragmentManager();
        addItemFragment = (AddItemFragment) fragMgr.findFragmentById(R.id.container_fragment_add_item);
        if (addItemFragment == null) {
            addItemFragment = AddItemFragment.newInstance();
            fragMgr.beginTransaction()
                    .add(R.id.container_fragment_add_item, addItemFragment)
                    .commit();
        }

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(v -> {
            addItem();
            finish(); // fixme: how to be sure that item has been added to database?
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        ActionMenuView actionMenuView = findViewById(R.id.action_menu_view);
        getMenuInflater().inflate(R.menu.menu_add_item, actionMenuView.getMenu());
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_discard:
                finish();
                break;
            case R.id.action_select_icon:
                // TODO: Implement select icon dialog
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    private void addItem() {
        ItemListViewModel itemListViewModel = ViewModelProviders.of(this).get(ItemListViewModel.class);
        Item item = new Item(addItemFragment.getItemName(), addItemFragment.getItemPrice(), 1);
        itemListViewModel.insertItem(item);
    }
}
