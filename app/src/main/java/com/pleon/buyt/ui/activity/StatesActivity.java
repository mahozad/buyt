package com.pleon.buyt.ui.activity;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.db.chart.animation.Animation;
import com.db.chart.model.LineSet;
import com.db.chart.view.LineChartView;
import com.google.android.material.bottomappbar.BottomAppBar;
import com.pleon.buyt.R;
import com.pleon.buyt.model.WeekdayCost;
import com.pleon.buyt.viewmodel.MainViewModel;

import java.text.DecimalFormat;
import java.util.HashMap;
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

    private MainViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_states);
        ButterKnife.bind(this); // unbind() is not required for activities

        setSupportActionBar(bottomAppBar);
        viewModel = ViewModelProviders.of(this).get(MainViewModel.class);
        show30DayCosts();
    }

    private void show30DayCosts() {
        viewModel.getLast30DaysCosts().observe(this, costs -> {
            lineChart.reset();

            int now = 0;
            for (WeekdayCost cost : costs) {
                if (cost.getCost() == -1) {
                    now = cost.getDay();
                    costs.remove(cost);
                }
            }

            Map<Integer, Long> dayToCostMap = new HashMap<>();
            for (WeekdayCost cost : costs) {
                dayToCostMap.put(cost.getDay(), cost.getCost());
            }

            LineSet dataSet = new LineSet();
            for (int i = now - 15; i <= now; i++) {
                dataSet.addPoint("" + i, dayToCostMap.containsKey(i) ? dayToCostMap.get(i) : 0);
            }

//            dataSet.setDotsColor(ContextCompat.getColor(this, R.color.colorAccent));
//            dataSet.setDotsRadius(2);
            dataSet.setSmooth(false); // TODO: Add an options in settings for the user to toggle this
            dataSet.setColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));
            dataSet.setThickness(2.5f);

            DecimalFormat moneyFormat = new DecimalFormat("\u00A4##,###");
            if (getResources().getConfiguration().locale.getDisplayName().equals("فارسی (ایران)")) {
                // for Farsi, \u00A4 is ریال but we want something else (e.g. ت)
                moneyFormat = new DecimalFormat("##,### ت");
            }
            lineChart.setLabelsFormat(moneyFormat);

            int[] colors2 = getResources().getIntArray(R.array.lineChartGradient);
            float[] steps2 = {0.0f, 0.5f, 1.0f};
            dataSet.setGradientFill(colors2, steps2);
            lineChart.addData(dataSet);
            lineChart.setXLabels(NONE);
            lineChart.show(new Animation(500));
        });
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
                // TODO: 2019-02-11
            break;

            /* If setSupportActionBar() is used to set up the BottomAppBar, navigation menu item
             * can be identified by checking if the id of menu item equals android.R.id.home. */
            case android.R.id.home:
                finish();
                break;

            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }
}
