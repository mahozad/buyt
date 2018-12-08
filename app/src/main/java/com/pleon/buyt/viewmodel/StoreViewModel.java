package com.pleon.buyt.viewmodel;

import android.app.Application;

import com.pleon.buyt.database.repository.StoreRepository;
import com.pleon.buyt.model.Store;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

public class StoreViewModel extends AndroidViewModel {

    private StoreRepository mRepository;

//    private LiveData<List<Store>> mAllStores;

    public StoreViewModel(Application application) {
        super(application);
        mRepository = new StoreRepository(application);
//        mAllStores = mRepository.getAll();
    }

//    public LiveData<List<Store>> getAll() {
//        return mAllStores;
//    }

    public MutableLiveData<Long> insert(Store store) {
        return mRepository.insert(store);
    }
}
