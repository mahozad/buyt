package com.pleon.buyt.database.repository;

import android.app.Application;
import android.os.AsyncTask;

import com.pleon.buyt.database.AppDatabase;
import com.pleon.buyt.database.dao.StoreDao;
import com.pleon.buyt.model.Store;

import androidx.lifecycle.MutableLiveData;

public class StoreRepository {

    private MutableLiveData<Long> mInsertedStoreId = new MutableLiveData<>();
    private StoreDao mStoreDao;
//    private LiveData<List<Store>> mAllStores;

    public StoreRepository(Application application) {
        mStoreDao = AppDatabase.getDatabase(application).storeDao();
//        mAllStores = mStoreDao.getAll();
    }

    // this does not need to be run in separate thread because it returns LiveData
//    public LiveData<List<Store>> getAll() {
//        return mAllStores;
//    }

    public MutableLiveData<Long> insert(Store store) {
        new InsertAsyncTask(mStoreDao, this).execute(store);
        return mInsertedStoreId;
    }

    private void setInsertedStoreId(long id) {
        mInsertedStoreId.postValue(id);
    }

    private static class InsertAsyncTask extends AsyncTask<Store, Void, Long> {

        private StoreDao mAsyncTaskDao;
        private StoreRepository delegate;

        InsertAsyncTask(StoreDao dao, StoreRepository repository) {
            mAsyncTaskDao = dao;
            delegate = repository;
        }

        @Override
        protected Long doInBackground(Store... params) {
            // returns id of the new inserted store
            return mAsyncTaskDao.insert(params[0]);
        }

        @Override
        protected void onPostExecute(Long id) {
            delegate.setInsertedStoreId(id);
        }
    }
}
