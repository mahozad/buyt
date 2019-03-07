package com.pleon.buyt.viewmodel;

import android.app.Application;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;

import com.pleon.buyt.database.repository.MainRepository;
import com.pleon.buyt.model.Coordinates;
import com.pleon.buyt.model.Item;
import com.pleon.buyt.model.Store;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import androidx.annotation.DrawableRes;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.preference.PreferenceManager;

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

    private static final double EARTH_RADIUS = 6371000; // In meters

    public enum State {
        IDLE, FINDING, SELECTING
    }

    private SharedPreferences preferences;
    private MainRepository mMainRepository;
    private volatile State state = IDLE;
    private Location location;
    private boolean findingSkipped;
    private List<Store> foundStores = new ArrayList<>();
    private boolean shouldCompletePurchase;
    private boolean shouldAnimateNavIcon;
    @DrawableRes private int storeIcon;
    @StringRes private int storeTitle;

    // TODO: Use paging library architecture component
    private LiveData<List<Item>> mAllItems;

    public MainViewModel(Application application) {
        super(application);
        preferences = PreferenceManager.getDefaultSharedPreferences(application);
        mMainRepository = new MainRepository(application);
        mAllItems = mMainRepository.getAllItems();
    }

    public LiveData<List<Item>> getAllItems() {
        return mAllItems;
    }

    @SuppressWarnings("ConstantConditions")
    public LiveData<List<Store>> findNearStores(Coordinates origin) {
        int distInMeters = Integer.parseInt(preferences.getString("distance", "50"));
        double nearStoresDistance = cos(distInMeters / EARTH_RADIUS);
        return mMainRepository.findNearStores(origin, nearStoresDistance);
    }

    public LiveData<List<Store>> getAllStores() {
        return mMainRepository.getAllStores();
    }

    public void updateItems(Collection<Item> items) {
        mMainRepository.updateItems(items);
    }

    public void buy(Collection<Item> items, Store store, Date purchaseDate) {
        mMainRepository.buy(items, store, purchaseDate);
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

    public boolean isFindingSkipped() {
        return findingSkipped;
    }

    public void setFindingSkipped(boolean findingSkipped) {
        this.findingSkipped = findingSkipped;
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

    public boolean shouldCompletePurchase() {
        return shouldCompletePurchase;
    }

    public void setShouldCompletePurchase(boolean shouldCompletePurchase) {
        this.shouldCompletePurchase = shouldCompletePurchase;
    }

    public boolean shouldAnimateNavIcon() {
        return shouldAnimateNavIcon;
    }

    public void setShouldAnimateNavIcon(boolean shouldAnimateNavIcon) {
        this.shouldAnimateNavIcon = shouldAnimateNavIcon;
    }

    public int getStoreIcon() {
        return storeIcon;
    }

    public void setStoreIcon(int storeIcon) {
        this.storeIcon = storeIcon;
    }

    public String getStoreTitle() {
        return foundStores.size() == 1 ?
                foundStores.get(0).getName() : getApplication().getString(storeTitle);
    }

    public void setStoreTitle(int storeTitle) {
        this.storeTitle = storeTitle;
    }
}
