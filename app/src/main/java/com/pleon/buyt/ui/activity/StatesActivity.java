package com.pleon.buyt.ui.activity;

import android.content.SharedPreferences;
import android.graphics.drawable.Animatable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.pleon.buyt.R;
import com.pleon.buyt.model.Category;
import com.pleon.buyt.ui.dialog.SelectDialogFragment;
import com.pleon.buyt.ui.dialog.SelectionDialogRow;
import com.pleon.buyt.ui.fragment.StatesFragment;

import java.util.ArrayList;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.preference.PreferenceManager;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.pleon.buyt.ui.activity.MainActivity.DEFAULT_THEME;
import static com.pleon.buyt.ui.activity.MainActivity.KEY_PREF_THEME;

public class StatesActivity extends AppCompatActivity implements SelectDialogFragment.Callback {

    private static final String TAG = "STATES";

    @BindView(R.id.bottom_bar) BottomAppBar bottomAppBar;
    @BindView(R.id.fab) FloatingActionButton fab;

    private StatesFragment statesFragment;
    private ArrayList<SelectionDialogRow> filterList;
    private MenuItem filterMenuItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_states);
        ButterKnife.bind(this); // unbind() is not required for activities

        setSupportActionBar(bottomAppBar);

        FragmentManager fragMgr = getSupportFragmentManager();
        statesFragment = (StatesFragment) fragMgr.findFragmentById(R.id.statesFragment);

        filterList = new ArrayList<>();
        filterList.add(new SelectionDialogRow(getString(R.string.no_filter), R.drawable.ic_filter));
        for (Category category : Category.values()) {
            filterList.add(new SelectionDialogRow(category.name(), category.getImageRes()));
        }
    }

    private void setTheme() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String theme = preferences.getString(KEY_PREF_THEME, DEFAULT_THEME);
        setTheme(DEFAULT_THEME.equals(theme) ? R.style.AppTheme : R.style.LightTheme);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        fab.setImageResource(statesFragment.getPeriod().getImageRes());
        ((Animatable) fab.getDrawable()).start();

        // menu items should be restored in onCreateOptionsMenu()
    }

    @OnClick(R.id.fab)
    void onFabClick() {
        statesFragment.togglePeriod();
        fab.setImageResource(statesFragment.getPeriod().getImageRes());
        ((Animatable) fab.getDrawable()).start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_bottom_states, menu);

        filterMenuItem = menu.getItem(0);
        filterMenuItem.setIcon(statesFragment.getFilter() == null ?
                R.drawable.ic_filter : statesFragment.getFilter().getImageRes());

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_filter:
                SelectDialogFragment dialog = SelectDialogFragment
                        .newInstance(this, R.string.dialog_title_select_filter, filterList);
                dialog.show(getSupportFragmentManager(), "SELECTION_DIALOG");
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
    public void onSelected(int index) {
        SelectionDialogRow selection = filterList.get(index);
        filterMenuItem.setIcon(selection.getImage());
        statesFragment.setFilter(selection.getName().equals(getString(R.string.no_filter)) ?
                null : Category.valueOf(selection.getName()));
    }
}
