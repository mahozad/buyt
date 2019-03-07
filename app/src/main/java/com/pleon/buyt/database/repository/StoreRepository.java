package com.pleon.buyt.database.repository;

import android.content.Context;
import android.os.AsyncTask;

import com.pleon.buyt.database.AppDatabase;
import com.pleon.buyt.database.SingleLiveEvent;
import com.pleon.buyt.database.dao.StoreDao;
import com.pleon.buyt.model.Store;

import java.util.List;

import androidx.lifecycle.LiveData;

public class StoreRepository {

    private static volatile StoreRepository sInstance;
    private static StoreDao mStoreDao;
    private LiveData<List<Store>> allStores;
    private SingleLiveEvent<Store> createdStore = new SingleLiveEvent<>();

    private StoreRepository(Context context) {
        mStoreDao = AppDatabase.getDatabase(context).storeDao();
        allStores = mStoreDao.getAll();
    }

    public static StoreRepository getInstance(Context context) {
        if (sInstance == null) {
            synchronized (StoreRepository.class) {
                if (sInstance == null) {
                    sInstance = new StoreRepository(context);
                }
            }
        }
        return sInstance;
    }

    public LiveData<List<Store>> getAll() {
        return allStores;
    }

    public LiveData<Store> insert(Store store/*, boolean publishRequired*/) {
        new InsertAsyncTask(mStoreDao, this/*, publishRequired*/).execute(store);
        return createdStore;
    }

    private void setLatestCreatedStore(Store store) {
        createdStore.setValue(store);
    }

    public void updateStore(Store store) {
        new UpdateStoreTask(mStoreDao).execute(store);
    }

    public void deleteStore(Store store) {
        new DeleteStoreTask(mStoreDao).execute(store);
    }

    private static class InsertAsyncTask extends AsyncTask<Store, Void, Store> {

        private StoreDao mAsyncTaskDao;
        private StoreRepository delegate;
        // private boolean publishRequired;

        InsertAsyncTask(StoreDao mAsyncTaskDao, StoreRepository delegate/*, boolean publishRequired*/) {
            this.mAsyncTaskDao = mAsyncTaskDao;
            this.delegate = delegate;
            // this.publishRequired = publishRequired;
        }

        @Override
        protected Store doInBackground(Store... stores) {
            long storeId = mAsyncTaskDao.insert(stores[0]);
            stores[0].setStoreId(storeId);
            return stores[0];
        }

        @Override
        protected void onPostExecute(Store store) {
        // if (publishRequired) {
            delegate.setLatestCreatedStore(store);
        // }
        }
    }

    private static class UpdateStoreTask extends AsyncTask<Store, Void, Void> {

        private StoreDao storeDao;

        UpdateStoreTask(StoreDao storeDao) {
            this.storeDao = storeDao;
        }

        @Override
        protected Void doInBackground(Store... stores) {
            storeDao.update(stores[0]);
            return null;
        }
    }

    private static class DeleteStoreTask extends AsyncTask<Store, Void, Void> {

        private StoreDao storeDao;

        DeleteStoreTask(StoreDao storeDao) {
            this.storeDao = storeDao;
        }

        @Override
        protected Void doInBackground(Store... stores) {
            storeDao.delete(stores[0]);
            return null;
        }
    }
}
