package com.pleon.buyt.viewmodel;

import android.app.Application;

import com.pleon.buyt.model.Store;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

public class StoreViewModel extends AndroidViewModel {

    private MutableLiveData<Store> createdStore;

    public StoreViewModel(Application application) {
        super(application);
        createdStore = new MutableLiveData<>();
    }

    public LiveData<Store> getCreatedStore() {
        return createdStore;
    }

    public void setCreatedStore(Store store) {
        createdStore.setValue(store);
    }
}
