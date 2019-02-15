package com.pleon.buyt.database.repository;

import android.app.Application;
import android.os.AsyncTask;

import com.pleon.buyt.database.AppDatabase;
import com.pleon.buyt.database.SingleLiveEvent;
import com.pleon.buyt.database.dao.PurchaseDao;
import com.pleon.buyt.model.Category;
import com.pleon.buyt.model.Statistics;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

public class StatisticsRepository { // TODO: make this class singleton

    private PurchaseDao mPurchaseDao;
    private SingleLiveEvent<Statistics> averagePurchaseCost;

    public StatisticsRepository(Application application) {
        mPurchaseDao = AppDatabase.getDatabase(application).purchaseDao();
        averagePurchaseCost = new SingleLiveEvent<>();
    }

    public LiveData<Statistics> getStatistics(int period, @Nullable Category filter) {
        new GetStatisticsTask(mPurchaseDao, averagePurchaseCost, period, filter).execute();
        return averagePurchaseCost;
    }

    private static class GetStatisticsTask extends AsyncTask<Void, Void, Statistics> {

        private PurchaseDao purchaseDao;
        private MutableLiveData<Statistics> statistics;
        private int period;
        private Category filter;

        GetStatisticsTask(PurchaseDao purchaseDao, MutableLiveData<Statistics> statistics,
                          int period, @Nullable Category filter) {
            this.purchaseDao = purchaseDao;
            this.statistics = statistics;
            this.period = period;
            this.filter = filter;
        }

        @Override
        protected Statistics doInBackground(Void... voids) {
            return purchaseDao.getStats(period, filter);
        }

        @Override
        protected void onPostExecute(Statistics statistics) {
            this.statistics.setValue(statistics);
        }
    }
}
