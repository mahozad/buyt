package com.pleon.buyt.viewmodel;

import android.app.Application;

import com.pleon.buyt.database.repository.PurchaseRepository;
import com.pleon.buyt.model.Purchase;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

public class PurchaseViewModel extends AndroidViewModel {

    private PurchaseRepository mRepository;

//    private LiveData<List<Purchase>> mAllPurchases;

    public PurchaseViewModel(Application application) {
        super(application);
        mRepository = new PurchaseRepository(application);
//        mAllPurchases = mRepository.getAll();
    }

//    public LiveData<List<Purchase>> getAll() {
//        return mAllPurchases;
//    }

    public MutableLiveData<Long> insert(Purchase purchase) {
        return mRepository.insert(purchase);
    }
}
