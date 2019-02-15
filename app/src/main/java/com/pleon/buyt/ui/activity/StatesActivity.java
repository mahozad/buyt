package com.pleon.buyt.ui.activity;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.db.chart.animation.Animation;
import com.db.chart.model.LineSet;
import com.db.chart.view.LineChartView;
import com.google.android.material.bottomappbar.BottomAppBar;
import com.pleon.buyt.R;
import com.pleon.buyt.model.DailyCost;
import com.pleon.buyt.viewmodel.StatisticsViewModel;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProviders;
import butterknife.BindView;
import butterknife.ButterKnife;

import static com.db.chart.renderer.AxisRenderer.LabelPosition.NONE;

public class StatesActivity extends AppCompatActivity {

    @BindView(R.id.bottom_bar) BottomAppBar bottomAppBar;
    @BindView(R.id.chart) LineChartView lineChart;

    private StatisticsViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_states);
        ButterKnife.bind(this); // unbind() is not required for activities

        setSupportActionBar(bottomAppBar);
        viewModel = ViewModelProviders.of(this).get(StatisticsViewModel.class);

        showAnalytics();
    }

    private void showAnalytics() {
        viewModel.getStatistics().observe(this, statistics -> {
            showGraph(statistics.getDailyCosts());
            TextView view = findViewById(R.id.textView);
            view.setText(statistics.getAveragePurchaseCost() + "");
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
        for (int i = now - viewModel.getPeriod().length; i <= now; i++) {
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
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_toggle_period:
                viewModel.togglePeriod();
                showAnalytics();
                break;
            case android.R.id.home:
                finish();
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }
}
