package com.pleon.buyt.ui.activity;

import android.location.Location;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.pleon.buyt.R;
import com.pleon.buyt.ui.fragment.CreateStoreFragment;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.ActionMenuView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

public class CreateStoreActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_store);

        Bundle extras = getIntent().getExtras();
        Location location = (extras != null) ?
                getIntent().getParcelableExtra(MainActivity.EXTRA_LOCATION) : null;

        setSupportActionBar(findViewById(R.id.bottom_bar));

        ActionMenuView actionMenuView = findViewById(R.id.action_menu_view);
        // delegate to activity method
        actionMenuView.setOnMenuItemClickListener(this::onOptionsItemSelected);

        FragmentManager fragMgr = getSupportFragmentManager();
        Fragment createStoreFragment = fragMgr.findFragmentById(R.id.createStoreContainer);

        if (createStoreFragment == null) {
            fragMgr.beginTransaction()
                    .add(R.id.createStoreContainer, CreateStoreFragment.newInstance(location))
                    .commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        ActionMenuView actionMenuView = findViewById(R.id.action_menu_view);
        Menu mainMenu = actionMenuView.getMenu();
        getMenuInflater().inflate(R.menu.menu_add_store, mainMenu);

        // Setting up "Select category" action because it has custom layout
        MenuItem item = mainMenu.findItem(R.id.action_select_category);
        item.getActionView().setOnClickListener(v -> onOptionsItemSelected(item));

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_cancel:
                finish();
                break;
            case R.id.action_select_category:
                // TODO: Implement select category dialog
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }
}
