package com.pleon.buyt.ui.activity;

import android.graphics.drawable.Animatable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.db.chart.animation.Animation;
import com.db.chart.model.LineSet;
import com.db.chart.view.LineChartView;
import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.pleon.buyt.R;
import com.pleon.buyt.model.Category;
import com.pleon.buyt.model.DailyCost;
import com.pleon.buyt.ui.dialog.SelectDialogFragment;
import com.pleon.buyt.ui.dialog.SelectionDialogRow;
import com.pleon.buyt.viewmodel.StatisticsViewModel;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProviders;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.db.chart.renderer.AxisRenderer.LabelPosition.NONE;

public class StatesActivity extends AppCompatActivity implements SelectDialogFragment.Callback {

    private static final String TAG = "STATES";

    @BindView(R.id.bottom_bar) BottomAppBar bottomAppBar;
    @BindView(R.id.fab) FloatingActionButton fab;
    @BindView(R.id.chart) LineChartView lineChart;
    @BindView(R.id.chartCaption) TextView chartCaption;

    private StatisticsViewModel viewModel;
    private ArrayList<SelectionDialogRow> filterList;
    private MenuItem filterMenuItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_states);
        ButterKnife.bind(this); // unbind() is not required for activities

        setSupportActionBar(bottomAppBar);
        viewModel = ViewModelProviders.of(this).get(StatisticsViewModel.class);

        String caption = getString(R.string.chart_caption, viewModel.getPeriod().length);
        chartCaption.setText(caption);

        filterList = new ArrayList<>();
        filterList.add(new SelectionDialogRow(getString(R.string.no_filter), R.drawable.ic_filter));
        for (Category category : Category.values()) {
            filterList.add(new SelectionDialogRow(category.name(), category.getImageRes()));
        }

        showAnalytics();
    }

    @OnClick(R.id.fab)
    void onFabClick() {
        viewModel.togglePeriod();
        String caption = getString(R.string.chart_caption, viewModel.getPeriod().length);
        fab.setImageResource(viewModel.getPeriod().getImageRes());
        ((Animatable) fab.getDrawable()).start();
        chartCaption.setText(caption);
        showAnalytics();
    }

    private void showAnalytics() {
        viewModel.getStatistics().observe(this, statistics -> {
            showGraph(statistics.getDailyCosts());

            TextView totalSpending = findViewById(R.id.textView3);
            totalSpending.setText(statistics.getTotalPurchaseCost() + "");

            TextView averagePurchaseCost = findViewById(R.id.textView);
            averagePurchaseCost.setText(statistics.getAveragePurchaseCost() + "");

            TextView mostPurchasedCategory = findViewById(R.id.textView13);
            mostPurchasedCategory.setText(statistics.getMostPurchasedCategory() + "");
        });
    }

    private void showGraph(List<DailyCost> dailyCosts) {
        lineChart.reset();

        int now = 0;
        for (DailyCost cost : dailyCosts) {
            if (cost.getCost() == -1) {
                now = cost.getDay();
                dailyCosts.remove(cost);
            }
        }

        Map<Integer, Long> dayToCostMap = new HashMap<>();
        for (DailyCost cost : dailyCosts) {
            dayToCostMap.put(cost.getDay(), cost.getCost());
        }

        LineSet dataSet = new LineSet();
        for (int i = now - viewModel.getPeriod().length + 1; i <= now; i++) {
            dataSet.addPoint("" + i, dayToCostMap.containsKey(i) ? dayToCostMap.get(i) : 0);
        }

        dataSet.setDotsColor(ContextCompat.getColor(this, R.color.colorPrimary));
        dataSet.setDotsRadius(3);
        dataSet.setSmooth(false); // TODO: Add an options in settings for the user to toggle this
        dataSet.setColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));
        dataSet.setThickness(2.5f);

        DecimalFormat moneyFormat = new DecimalFormat("\u00A4##,###");
        if (getResources().getConfiguration().locale.getDisplayName().equals("فارسی (ایران)")) {
            // for Farsi, \u00A4 is ریال but we want something else (e.g. ت)
            moneyFormat = new DecimalFormat("##,### ت");
        }
        lineChart.setLabelsFormat(moneyFormat);

        int[] colors = getResources().getIntArray(R.array.lineChartGradient);
        float[] steps = {0.0f, 0.5f, 1.0f};
        dataSet.setGradientFill(colors, steps);
        lineChart.addData(dataSet);
        lineChart.setXLabels(NONE);
        lineChart.show(new Animation(500));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_bottom_states, menu);
        filterMenuItem = menu.getItem(0);
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
        if (filterList.get(index).getName().equals(getString(R.string.no_filter))) {
            viewModel.setFilter(null);
        } else {
            viewModel.setFilter(Category.valueOf(filterList.get(index).getName()));
        }
        filterMenuItem.setIcon(filterList.get(index).getImage());
        showAnalytics();
    }
}
