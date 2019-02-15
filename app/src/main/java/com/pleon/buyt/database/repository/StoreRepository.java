package com.pleon.buyt.database.repository;

import android.content.Context;
import android.os.AsyncTask;

import com.pleon.buyt.database.AppDatabase;
import com.pleon.buyt.database.SingleLiveEvent;
import com.pleon.buyt.database.dao.StoreDao;
import com.pleon.buyt.model.Store;

import androidx.lifecycle.LiveData;

public class StoreRepository {

    private static volatile StoreRepository sInstance;
    private static StoreDao mStoreDao;
    private SingleLiveEvent<Store> mLatestCreatedStore = new SingleLiveEvent<>();

    public static StoreRepository getInstance(Context appContext) {
        if (sInstance == null) {
            synchronized (StoreRepository.class) {
                if (sInstance == null) {
                    mStoreDao = AppDatabase.getDatabase(appContext).storeDao();
                    sInstance = new StoreRepository();
                }
            }
        }
        return sInstance;
    }

    public void insert(Store store, boolean publishRequired) {
        new InsertAsyncTask(mStoreDao, this, publishRequired).execute(store);
    }

    public LiveData<Store> getLatestCreatedStore() {
        return mLatestCreatedStore;
    }

    private void setLatestCreatedStore(Store store) {
        mLatestCreatedStore.setValue(store);
    }

    private static class InsertAsyncTask extends AsyncTask<Store, Void, Store> {

        private StoreDao mAsyncTaskDao;
        private StoreRepository delegate;
        private boolean publishRequired;

        InsertAsyncTask(StoreDao mAsyncTaskDao, StoreRepository delegate, boolean publishRequired) {
            this.mAsyncTaskDao = mAsyncTaskDao;
            this.delegate = delegate;
            this.publishRequired = publishRequired;
        }

        @Override
        protected Store doInBackground(Store... stores) {
            // returns id of the new inserted store
            long storeId = mAsyncTaskDao.insert(stores[0]);
            stores[0].setStoreId(storeId);
            return stores[0];
        }

        @Override
        protected void onPostExecute(Store store) {
            if (publishRequired) {
                delegate.setLatestCreatedStore(store);
            }
        }
    }
}
