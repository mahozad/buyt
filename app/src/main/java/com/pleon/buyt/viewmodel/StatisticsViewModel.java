package com.pleon.buyt.viewmodel;

import android.app.Application;

import com.pleon.buyt.R;
import com.pleon.buyt.database.repository.StatisticsRepository;
import com.pleon.buyt.model.Category;
import com.pleon.buyt.model.Statistics;

import androidx.annotation.DrawableRes;
import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import static com.pleon.buyt.viewmodel.StatisticsViewModel.Period.NARROW;

public class StatisticsViewModel extends AndroidViewModel {

    public enum Period {

        NARROW(7, R.drawable.avd_period_wid_nar), MEDIUM(15, R.drawable.avd_period_nar_med),
        EXTENDED(30, R.drawable.avd_period_med_ext), WIDE(90, R.drawable.avd_period_ext_wid);

        public int length;
        @DrawableRes private int imageRes;

        Period(int length, int imageRes) {
            this.length = length;
            this.imageRes = imageRes;
        }

        public int getImageRes() {
            return imageRes;
        }
    }

    private StatisticsRepository statisticsRepository;
    private Period period = NARROW;
    private @Nullable Category filter = null;

    public StatisticsViewModel(Application application) {
        super(application);
        statisticsRepository = new StatisticsRepository(application);
    }

    public LiveData<Statistics> getStatistics() {
        return statisticsRepository.getStatistics(period.length, filter);
    }

    public Period getPeriod() {
        return period;
    }

    public void togglePeriod() {
        int currentIndex = Period.valueOf(period.name()).ordinal();
        int index = (currentIndex + 1) % Period.values().length;
        period = Period.values()[index];
    }

    public Category getFilter() {
        return filter;
    }

    public void setFilter(@Nullable Category filter) {
        this.filter = filter;
    }
}
