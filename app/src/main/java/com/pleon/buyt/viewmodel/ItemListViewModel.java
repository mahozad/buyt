package com.pleon.buyt.viewmodel;

import android.app.Application;

import com.pleon.buyt.database.repository.MainRepository;
import com.pleon.buyt.database.repository.StoreRepository;
import com.pleon.buyt.model.Coordinates;
import com.pleon.buyt.model.Item;
import com.pleon.buyt.model.Store;

import java.util.List;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

// The ViewModel's role is to provide data to the UI and survive configuration changes.
// Every screen in the app (an activity with all its fragments) has one corresponding viewModel for itself.
// A ViewModel acts as a communication center between the Repository and the UI.
// You can also use a ViewModel to share data between fragments

// Warning: Never pass context into ViewModel instances. Do not store Activity, Fragment, or View instances or their Context in the ViewModel.
// For example, an Activity can be destroyed and created many times during the lifecycle of a ViewModel as the device is rotated.
// If you store a reference to the Activity in the ViewModel, you end up with references that point to the destroyed Activity. This is a memory leak.
public class ItemListViewModel extends AndroidViewModel {

    private MainRepository mMainRepository;
    private StoreRepository mStoreRepository;

    // cache the list of Items
    // TODO: Use paging library architecture component
    private LiveData<List<Item>> mAllItems;

    public ItemListViewModel(Application application) {
        super(application);
        mMainRepository = new MainRepository(application);
        mStoreRepository = StoreRepository.getInstance(application);
        mAllItems = mMainRepository.getAll();
    }

    public LiveData<List<Item>> getAll() {
        return mAllItems;
    }

    public LiveData<List<Store>> findNearStores(Coordinates origin, double maxDistance) {
        return mMainRepository.findNearStores(origin, maxDistance);
    }

    public void insertItem(Item item) {
        mMainRepository.insertItem(item);
    }

    public LiveData<Store> getLatestCreatedStore() {
        return mStoreRepository.getLatestCreatedStore();
    }

    public void buy(Item item, Store store) {
        mMainRepository.buy(item, store);
    }
}
