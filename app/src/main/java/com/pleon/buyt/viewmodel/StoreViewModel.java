package com.pleon.buyt.viewmodel;

import android.app.Application;

import com.pleon.buyt.database.repository.StoreRepository;
import com.pleon.buyt.model.Store;

import java.util.List;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

public class StoreViewModel extends AndroidViewModel {

    private StoreRepository mStoreRepository;
    private LiveData<List<Store>> allStores;

    public StoreViewModel(Application application) {
        super(application);
        mStoreRepository = StoreRepository.getInstance(application);
        allStores = mStoreRepository.getAll();
    }

    public void insertForObserver(Store store) {
        mStoreRepository.insert(store, true);
    }

    public void insert(Store store) {
        mStoreRepository.insert(store, false);
    }

    public LiveData<List<Store>> getAll() {
        return allStores;
    }
}
