package com.pleon.buyt.database.repository;

import android.app.Application;
import android.os.AsyncTask;

import com.pleon.buyt.database.AppDatabase;
import com.pleon.buyt.database.SingleLiveEvent;
import com.pleon.buyt.database.dao.ItemDao;
import com.pleon.buyt.database.dao.PurchaseDao;
import com.pleon.buyt.database.dao.StoreDao;
import com.pleon.buyt.model.Coordinates;
import com.pleon.buyt.model.Item;
import com.pleon.buyt.model.Purchase;
import com.pleon.buyt.model.Store;
import com.pleon.buyt.model.WeekdayCost;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

// A Repository class handles data operations. It provides a clean API to the rest of the app for app data
// A Repository manages query threads and allows you to use multiple back-ends.
// In the most common example, the Repository implements the logic for deciding whether
// to fetch data from a network or use results cached in a local database.
public class MainRepository { // TODO: make this class singleton

    private ItemDao mItemDao;
    private StoreDao mStoreDao;
    private PurchaseDao mPurchaseDao;
    private LiveData<List<Item>> mAllItems;
    private LiveData<List<Purchase>> allPurchases;
    private SingleLiveEvent<List<Store>> mNearStores;
    private SingleLiveEvent<List<Store>> allStores;
    private SingleLiveEvent<List<WeekdayCost>> weekdayCosts;
    private SingleLiveEvent<List<WeekdayCost>> last30DayCosts;

    public MainRepository(Application application) {
        mItemDao = AppDatabase.getDatabase(application).itemDao();
        mStoreDao = AppDatabase.getDatabase(application).storeDao();
        mPurchaseDao = AppDatabase.getDatabase(application).purchaseDao();
        mAllItems = mItemDao.getAll();
        allPurchases = mPurchaseDao.getAll();
        mNearStores = new SingleLiveEvent<>();
        allStores = new SingleLiveEvent<>();
        weekdayCosts = new SingleLiveEvent<>();
        last30DayCosts = new SingleLiveEvent<>();
    }

    // this does not need to be run in separate thread because it just returns LiveData
    public LiveData<List<Item>> getAllItems() {
        return mAllItems;
    }

    public LiveData<List<Purchase>> getAllPurchases() {
        return allPurchases;
    }

    public void insertItem(Item item) {
        new AddItemTask(mItemDao).execute(item);
    }

    public void updateItems(Collection<Item> items) {
        new UpdateItemsTask(mItemDao, items).execute();
    }

    public void deleteItem(Item item) {
        new DeleteItemTask(mItemDao).execute(item);
    }

    public LiveData<List<Store>> findNearStores(Coordinates origin, double maxDistance) {
        new FindNearStoresAsyncTask(mStoreDao, origin, maxDistance, mNearStores).execute();
        return mNearStores;
    }

    public LiveData<List<Store>> getAllStores() {
        new GetAllStoresAsyncTask(mStoreDao, allStores).execute();
        return allStores;
    }

    public LiveData<List<WeekdayCost>> getWeekdayCosts() {
        new GetWeekdayCostsTask(mPurchaseDao, weekdayCosts).execute();
        return weekdayCosts;
    }

    public LiveData<List<WeekdayCost>> getLast30DayCosts() {
        new Get30dayCostsTask(mPurchaseDao, last30DayCosts).execute();
        return last30DayCosts;
    }

    public void buy(Collection<Item> items, Store store, Date purchaseDate) {
        new BuyAsyncTask(items, store, purchaseDate, mItemDao, mStoreDao, mPurchaseDao).execute();
    }

    private static class AddItemTask extends AsyncTask<Item, Void, Void> {

        private ItemDao itemDao;

        AddItemTask(ItemDao itemDao) {
            this.itemDao = itemDao;
        }

        @Override
        protected Void doInBackground(Item... items) {
            itemDao.insert(items[0]);
            return null;
        }
    }

    private static class DeleteItemTask extends AsyncTask<Item, Void, Void> {

        private ItemDao itemDao;

        DeleteItemTask(ItemDao itemDao) {
            this.itemDao = itemDao;
        }

        @Override
        protected Void doInBackground(Item... items) {
            itemDao.delete(items[0]);
            return null;
        }
    }

    private static class UpdateItemsTask extends AsyncTask<Void, Void, Void> {

        private ItemDao itemDao;
        private Collection<Item> items;

        UpdateItemsTask(ItemDao itemDao, Collection<Item> items) {
            this.itemDao = itemDao;
            this.items = items;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            itemDao.updateAll(items);
            return null;
        }
    }

    private static class BuyAsyncTask extends AsyncTask<Void, Void, Void> {

        private Collection<Item> items;
        private Store store;
        private PurchaseDao mPurchaseDao;
        private ItemDao mItemDao;
        private StoreDao mStoreDao;
        private Date purchaseDate;

        BuyAsyncTask(Collection<Item> items, Store store, Date purchaseDate, ItemDao mItemDao,
                     StoreDao mStoreDao, PurchaseDao mPurchaseDao) {
            this.items = items;
            this.store = store;
            this.purchaseDate = purchaseDate;
            this.mItemDao = mItemDao;
            this.mStoreDao = mStoreDao;
            this.mPurchaseDao = mPurchaseDao;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            long storeId = store.getId();
            if (storeId == 0) { // then this is a new Store so persist it
                storeId = mStoreDao.insert(store);
            }

            long totalCost = 0;
            for (Item item : items) {
                totalCost += item.getTotalPrice();
            }
            Purchase purchase = new Purchase(storeId, purchaseDate, totalCost);
            long purchaseId = mPurchaseDao.insert(purchase);

            for (Item item : items) {
                item.setPurchaseId(purchaseId);
                item.setBought(true);
            }
            mItemDao.updateAll(items);

            return null;
        }
    }

    private static class FindNearStoresAsyncTask extends AsyncTask<Void, Void, List<Store>> {

        private StoreDao mDao;
        private Coordinates origin;
        private double maxDistance;
        private MutableLiveData<List<Store>> mNearStores;

        FindNearStoresAsyncTask(StoreDao mDao, Coordinates origin, double maxDistance, MutableLiveData<List<Store>> mNearStores) {
            this.mDao = mDao;
            this.origin = origin;
            this.maxDistance = maxDistance;
            this.mNearStores = mNearStores;
        }

        @Override
        protected List<Store> doInBackground(Void... voids) {
            double sinLat = origin.getSinLat();
            double cosLat = origin.getCosLat();
            double sinLng = origin.getSinLng();
            double cosLng = origin.getCosLng();
            return mDao.findNearStores(sinLat, cosLat, sinLng, cosLng, maxDistance);
        }

        @Override
        protected void onPostExecute(List<Store> nearStores) {
            mNearStores.setValue(nearStores);
        }
    }

    private static class GetAllStoresAsyncTask extends AsyncTask<Void, Void, List<Store>> {

        private StoreDao mDao;
        private MutableLiveData<List<Store>> allStores;

        GetAllStoresAsyncTask(StoreDao mDao, MutableLiveData<List<Store>> allStores) {
            this.mDao = mDao;
            this.allStores = allStores;
        }

        @Override
        protected List<Store> doInBackground(Void... voids) {
            return mDao.getAll();
        }

        @Override
        protected void onPostExecute(List<Store> stores) {
            allStores.setValue(stores);
        }
    }

    private static class GetWeekdayCostsTask extends AsyncTask<Void, Void, List<WeekdayCost>> {

        private PurchaseDao purchaseDao;
        private MutableLiveData<List<WeekdayCost>> totalWeekdayCosts;

        GetWeekdayCostsTask(PurchaseDao purchaseDao, MutableLiveData<List<WeekdayCost>> totalWeekdayCosts) {
            this.purchaseDao = purchaseDao;
            this.totalWeekdayCosts = totalWeekdayCosts;
        }

        @Override
        protected List<WeekdayCost> doInBackground(Void... voids) {
            return purchaseDao.getWeekdayCosts();
        }

        @Override
        protected void onPostExecute(List<WeekdayCost> costs) {
            totalWeekdayCosts.setValue(costs);
        }
    }

    private static class Get30dayCostsTask extends AsyncTask<Void, Void, List<WeekdayCost>> {

        private PurchaseDao purchaseDao;
        private MutableLiveData<List<WeekdayCost>> last30DayCosts;

        Get30dayCostsTask(PurchaseDao purchaseDao, MutableLiveData<List<WeekdayCost>> last30DayCosts) {
            this.purchaseDao = purchaseDao;
            this.last30DayCosts = last30DayCosts;
        }

        @Override
        protected List<WeekdayCost> doInBackground(Void... voids) {
            return purchaseDao.getLast30DayCosts();
        }

        @Override
        protected void onPostExecute(List<WeekdayCost> costs) {
            last30DayCosts.setValue(costs);
        }
    }
}
