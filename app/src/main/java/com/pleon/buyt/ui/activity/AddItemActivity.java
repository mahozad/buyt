package com.pleon.buyt.ui.activity;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.pleon.buyt.R;
import com.pleon.buyt.model.Item;
import com.pleon.buyt.ui.fragment.AddItemFragment;
import com.pleon.buyt.viewmodel.ItemListViewModel;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.ActionMenuView;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProviders;

public class AddItemActivity extends AppCompatActivity implements AddItemFragment.Callback {

    private AddItemFragment addItemFragment;
    private TextView selectStoreTxvi;

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
            addItemFragment = AddItemFragment.newInstance(getIntent().getIntExtra("NEXT_ITEM_ORDER", 0));
            fragMgr.beginTransaction()
                    .add(R.id.container_fragment_add_item, addItemFragment)
                    .commit();
        }

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(v -> addItemFragment.onDonePressed()); // notify fragment fab was clicked
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        ActionMenuView actionMenuView = findViewById(R.id.action_menu_view);
        Menu mainMenu = actionMenuView.getMenu();
        getMenuInflater().inflate(R.menu.menu_add_item, mainMenu);

        selectStoreTxvi = mainMenu.findItem(R.id.action_select_store).getActionView().findViewById(R.id.select_store);

        // Setting up "Choose store" action because it has custom layout
        MenuItem item = mainMenu.findItem(R.id.action_select_store);
        item.getActionView().setOnClickListener(v -> onOptionsItemSelected(item));

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_cancel:
                finish();
                break;
            case R.id.action_select_store:
                // TODO: Implement select icon dialog
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    @Override
    public void onBoughtToggled(boolean checked) {
        selectStoreTxvi.setText(checked ? "Select store" : "Select icon");
    }

    @Override
    public void onSubmit(Item item) {
        ItemListViewModel itemListViewModel = ViewModelProviders.of(this).get(ItemListViewModel.class);
        itemListViewModel.insertItem(item);
        // Calling finish() is safe here. We are sure that the item will be added to database,
        // because it is executed in a separate thread.
        finish();
    }
}
