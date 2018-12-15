package com.pleon.buyt.viewmodel;

import android.app.Application;
import android.location.Location;
import android.os.AsyncTask;

import com.pleon.buyt.database.repository.MainRepository;
import com.pleon.buyt.model.Coordinates;
import com.pleon.buyt.model.Item;
import com.pleon.buyt.model.Purchase;
import com.pleon.buyt.model.Store;

import java.util.Date;
import java.util.List;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

//The ViewModel's role is to provide data to the UI and survive configuration changes.
// A ViewModel acts as a communication center between the Repository and the UI.
// You can also use a ViewModel to share data between fragments

//Warning: Never pass context into ViewModel instances. Do not store Activity, Fragment, or View instances or their Context in the ViewModel.
// For example, an Activity can be destroyed and created many times during the lifecycle of a ViewModel as the device is rotated.
// If you store a reference to the Activity in the ViewModel, you end up with references that point to the destroyed Activity. This is a memory leak.
public class MainViewModel extends AndroidViewModel {

    private MainRepository mRepository;

    // cache the list of Items
    // TODO: Use paging library architecture component
    private LiveData<List<Item>> mAllItems;

    public MainViewModel(Application application) {
        super(application);
        mRepository = new MainRepository(application);
        mAllItems = mRepository.getAll();
    }

    public LiveData<List<Item>> getAll() {
        return mAllItems;
    }

    public LiveData<List<Store>> findNearStores(Coordinates origin, double maxDistance) {
        return mRepository.findNearStores(origin, maxDistance);
    }

    public void insertItem(Item item) {
        mRepository.insertItem(item);
    }

    public void buy(Item item, Store store) {
        mRepository.buy(item, store);
    }
}
