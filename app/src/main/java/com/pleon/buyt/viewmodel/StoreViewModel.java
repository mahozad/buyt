package com.pleon.buyt.viewmodel;

import android.app.Application;

import com.pleon.buyt.database.repository.StoreRepository;
import com.pleon.buyt.model.Store;

import androidx.lifecycle.AndroidViewModel;

public class StoreViewModel extends AndroidViewModel {

    private StoreRepository mStoreRepository;

    public StoreViewModel(Application application) {
        super(application);
        mStoreRepository = StoreRepository.getInstance(application);
    }

    public void insertForObserver(Store store) {
        mStoreRepository.insert(store, true);
    }

    public void insert(Store store) {
        mStoreRepository.insert(store, false);
    }
}
