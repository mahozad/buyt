package com.pleon.buyt.viewmodel;

import android.app.Application;

import com.pleon.buyt.database.repository.StoreRepository;
import com.pleon.buyt.model.Store;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

public class CreateStoreViewModel extends AndroidViewModel {

    private StoreRepository mStoreRepository;

    public CreateStoreViewModel(Application application) {
        super(application);
        mStoreRepository = StoreRepository.getInstance(application);
    }

    public LiveData<Store> addStore(Store store) {
        return mStoreRepository.insert(store);
    }
}
