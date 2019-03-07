package com.pleon.buyt.viewmodel;

import android.app.Application;

import com.pleon.buyt.database.repository.StoreRepository;
import com.pleon.buyt.model.Store;

import java.util.List;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

public class StoreListViewModel extends AndroidViewModel {

    private LiveData<List<Store>> allStores;
    private StoreRepository storeRepository;

    public StoreListViewModel(Application application) {
        super(application);
        storeRepository = StoreRepository.getInstance(application);
        allStores = storeRepository.getAll();
    }

    public LiveData<List<Store>> getAllStores() {
        return allStores;
    }

    public void updateStore(Store store) {
        storeRepository.updateStore(store);
    }

    public void deleteStore(Store store) {
        storeRepository.deleteStore(store);
    }
}
