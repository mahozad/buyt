package com.pleon.buyt.ui.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.db.chart.animation.Animation;
import com.db.chart.model.LineSet;
import com.db.chart.view.LineChartView;
import com.pleon.buyt.R;
import com.pleon.buyt.model.Category;
import com.pleon.buyt.model.DailyCost;
import com.pleon.buyt.viewmodel.StatisticsViewModel;

import java.text.DecimalFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

import static android.content.Intent.ACTION_TIME_TICK;
import static com.db.chart.renderer.AxisRenderer.LabelPosition.NONE;


public class StatesFragment extends Fragment {

    @BindView(R.id.chart) LineChartView lineChart;
    @BindView(R.id.chartCaption) TextView chartCaption;

    @BindView(R.id.textView3) TextView totalSpentTxvi;
    @BindView(R.id.textView) TextView averagePurchaseCostTxvi;
    @BindView(R.id.textView13) TextView mostPurchasedCatTxvi;
    @BindView(R.id.textView18) TextView numberOfPurchasesTxvi;
    @BindView(R.id.textView6) TextView maxPurchaseCostTxvi;
    @BindView(R.id.textView7) TextView minPurchaseCostTxvi;
    @BindView(R.id.textView9) TextView weekdayWithMaxPurchaseTxvi;
    @BindView(R.id.textView17) TextView storeWithMaxPurchaseTxvi;

    // Update the statistics when date changes (for example time changes from 23:59 to 00:00)
    private Date today = new Date();
    private IntentFilter timeTickIntent = new IntentFilter(ACTION_TIME_TICK);
    private BroadcastReceiver timeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (new Date().getDate() != today.getDate()) {
                showStatistics();
                today = new Date();
            }
        }
    };

    private StatisticsViewModel viewModel;
    private Unbinder unbinder;

    public StatesFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = ViewModelProviders.of(this).get(StatisticsViewModel.class);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_states, container, false);
        unbinder = ButterKnife.bind(this, view); // unbind() is required only for Fragments

        getActivity().registerReceiver(timeReceiver, timeTickIntent);

        String caption = getString(R.string.chart_caption, viewModel.getPeriod().length);
        chartCaption.setText(caption);

        showStatistics();

        return view;
    }

    private void showStatistics() {
        viewModel.getStatistics().observe(this, statistics -> {
            showGraph(statistics.getDailyCosts());

            totalSpentTxvi.setText(statistics.getTotalPurchaseCost());
            averagePurchaseCostTxvi.setText(statistics.getAveragePurchaseCost());
            if (statistics.getMostPurchasedCategoryName() != 0) {
                mostPurchasedCatTxvi.setText(statistics.getMostPurchasedCategoryName());
            }
            numberOfPurchasesTxvi.setText(statistics.getNumberOfPurchases());
            maxPurchaseCostTxvi.setText(statistics.getMaxPurchaseCost());
            minPurchaseCostTxvi.setText(statistics.getMinPurchaseCost());
            weekdayWithMaxPurchaseTxvi.setText(statistics.getWeekdayNameResWithMaxPurchases());
            storeWithMaxPurchaseTxvi.setText(statistics.getStoreNameWithMaxPurchaseCount());
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

        if (viewModel.getPeriod().length <= 20) {
            dataSet.setDotsColor(ContextCompat.getColor(getContext(), R.color.colorPrimary));
            dataSet.setDotsRadius(3);
        }
        dataSet.setSmooth(false); // TODO: Add an options in settings for the user to toggle this
        dataSet.setColor(ContextCompat.getColor(getContext(), R.color.colorPrimaryDark));
        dataSet.setThickness(2.5f);

        DecimalFormat moneyFormat = new DecimalFormat(getString(R.string.currency_format));
        lineChart.setLabelsFormat(moneyFormat);

        int[] colors = getResources().getIntArray(R.array.lineChartGradient);
        float[] steps = {0.0f, 0.5f, 1.0f};
        dataSet.setGradientFill(colors, steps);
        lineChart.addData(dataSet);
        lineChart.setXLabels(NONE);
        lineChart.show(new Animation(500));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        getActivity().unregisterReceiver(timeReceiver);
        unbinder.unbind();
    }

    public StatisticsViewModel.Period getPeriod() {
        return viewModel.getPeriod();
    }

    public void togglePeriod() {
        viewModel.togglePeriod();
        String caption = getString(R.string.chart_caption, viewModel.getPeriod().length);
        chartCaption.setText(caption);

        showStatistics();
    }

    public Category getFilter() {
        return viewModel.getFilter();
    }

    public void setFilter(Category filter) {
        viewModel.setFilter(filter);

        showStatistics();
    }
}
