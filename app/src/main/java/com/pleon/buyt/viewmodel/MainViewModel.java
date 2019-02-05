package com.pleon.buyt.viewmodel;

import android.app.Application;
import android.location.Location;
import android.os.Bundle;

import com.pleon.buyt.database.repository.MainRepository;
import com.pleon.buyt.database.repository.StoreRepository;
import com.pleon.buyt.model.Coordinates;
import com.pleon.buyt.model.Item;
import com.pleon.buyt.model.Purchase;
import com.pleon.buyt.model.Store;
import com.pleon.buyt.model.WeekdayCost;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import androidx.annotation.DrawableRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import static com.pleon.buyt.viewmodel.MainViewModel.State.IDLE;
import static java.lang.Math.cos;

// The ViewModel's role is to provide data to the UI and survive configuration changes.
// Every screen in the app (an activity with all its fragments) has one corresponding viewModel for itself.
// A ViewModel acts as a communication center between the Repository and the UI.
// You can also use a ViewModel to share data between fragments

// Warning: Never pass context into ViewModel instances. Do not store Activity, Fragment, or View instances or their Context in the ViewModel.
// For example, an Activity can be destroyed and created many times during the lifecycle of a ViewModel as the device is rotated.
// If you store a reference to the Activity in the ViewModel, you end up with references that point to the destroyed Activity. This is a memory leak.

/**
 * {@link androidx.lifecycle.ViewModel ViewModels} only survive configuration changes and not
 * force-kills. So to survive process stops, implement
 * {@link  AppCompatActivity#onSaveInstanceState(Bundle)} method in your activity/fragment.
 */
public class MainViewModel extends AndroidViewModel {

    // equals 100m (6371km is the radius of the Earth)
    private static final double NEAR_STORES_DISTANCE = cos(0.1 / 6371);

    public enum State {
        IDLE, FINDING, SELECTING
    }

    private MainRepository mMainRepository;
    private StoreRepository mStoreRepository;
    private volatile State state = IDLE;
    private Location location;
    private boolean findingStateSkipped;
    private List<Store> foundStores = new ArrayList<>();
    @DrawableRes private int storeIcon;

    // TODO: Use paging library architecture component
    private LiveData<List<Item>> mAllItems;
    private LiveData<List<Purchase>> allPurchases;

    public MainViewModel(Application application) {
        super(application);
        mMainRepository = new MainRepository(application);
        mStoreRepository = StoreRepository.getInstance(application);
        mAllItems = mMainRepository.getAllItems();
        allPurchases = mMainRepository.getAllPurchases();
    }

    public LiveData<List<Item>> getAllItems() {
        return mAllItems;
    }

    public LiveData<List<Store>> findNearStores(Coordinates origin) {
        return mMainRepository.findNearStores(origin, NEAR_STORES_DISTANCE);
    }

    public LiveData<List<Store>> getAllStores() {
        return mMainRepository.getAllStores();
    }

    public void addItem(Item item) {
        mMainRepository.insertItem(item);
    }

    public void updateItems(Collection<Item> items) {
        mMainRepository.updateItems(items);
    }

    public void buy(Collection<Item> items, Store store, Date purchaseDate) {
        mMainRepository.buy(items, store, purchaseDate);
    }

    public LiveData<Store> getLatestCreatedStore() {
        return mStoreRepository.getLatestCreatedStore();
    }

    public LiveData<List<WeekdayCost>> getTotalWeekdayCosts(long from, long to) {
        return mMainRepository.getTotalWeekdayCosts(from, to);
    }

    public LiveData<List<Purchase>> getAllPurchases() {
        return allPurchases;
    }

    public void deleteItem(Item item) {
        mMainRepository.deleteItem(item);
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public boolean isFindingStateSkipped() {
        return findingStateSkipped;
    }

    public void setFindingStateSkipped(boolean findingStateSkipped) {
        this.findingStateSkipped = findingStateSkipped;
    }

    public void resetFoundStores() {
        foundStores.clear();
    }

    public List<Store> getFoundStores() {
        return foundStores;
    }

    public void setFoundStores(List<Store> foundStores) {
        this.foundStores = foundStores;
    }

    public int getStoreIcon() {
        return storeIcon;
    }

    public void setStoreIcon(int storeIcon) {
        this.storeIcon = storeIcon;
    }
}
