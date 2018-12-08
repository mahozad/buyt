package com.pleon.buyt.database.repository;

import android.app.Application;
import android.os.AsyncTask;

import com.pleon.buyt.database.AppDatabase;
import com.pleon.buyt.database.dao.ItemDao;
import com.pleon.buyt.database.dao.PurchaseDao;
import com.pleon.buyt.database.dao.StoreDao;
import com.pleon.buyt.model.Item;
import com.pleon.buyt.model.Purchase;
import com.pleon.buyt.model.Store;

import java.util.List;

import androidx.lifecycle.LiveData;

// A Repository class handles data operations. It provides a clean API to the rest of the app for app data
// A Repository manages query threads and allows you to use multiple backends.
// In the most common example, the Repository implements the logic for deciding whether
// to fetch data from a network or use results cached in a local database.
public class MainRepository { // TODO: make this class singleton

    private ItemDao mItemDao;
    private StoreDao mStoreDao;
    private PurchaseDao mPurchaseDao;
    private LiveData<List<Item>> mAllItems;

    public MainRepository(Application application) {
        mItemDao = AppDatabase.getDatabase(application).itemDao();
        mStoreDao = AppDatabase.getDatabase(application).storeDao();
        mPurchaseDao = AppDatabase.getDatabase(application).purchaseDao();
        mAllItems = mItemDao.getAll();
    }

    // this does not need to be run in separate thread because it returns LiveData
    public LiveData<List<Item>> getAll() {
        return mAllItems;
    }

    public void insertItem(Item item) {
        mItemDao.insert(item);
    }

    public long insertStore(Store store) {
        return mStoreDao.insert(store);
    }

    public long insertPurchase(Purchase purchase) {
        return mPurchaseDao.insert(purchase);
    }

    public void updateItem(Item item) {
        mItemDao.update(item);
    }

    private static class InsertAsyncTask extends android.os.AsyncTask<Item, Void, Void> {

        private ItemDao mAsyncTaskDao;

        InsertAsyncTask(ItemDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(Item... params) {
            mAsyncTaskDao.insert(params[0]);
            return null;
        }
    }

    private static class UpdateAsyncTask extends AsyncTask<Item, Void, Void> {

        private ItemDao mAsyncTaskDao;

        UpdateAsyncTask(ItemDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(Item... params) {
            mAsyncTaskDao.update(params[0]);
            return null;
        }
    }

}
