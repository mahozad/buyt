package com.pleon.buyt.database.repository;

import android.app.Application;
import android.os.AsyncTask;

import com.pleon.buyt.database.AppDatabase;
import com.pleon.buyt.database.dao.PurchaseDao;
import com.pleon.buyt.model.Purchase;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

public class PurchaseRepository {

    private MutableLiveData<Long> mInsertedPurchaseId = new MutableLiveData<>();
    private PurchaseDao mPurchaseDao;
//    private LiveData<List<Purchase>> mAllPurchases;

    public PurchaseRepository(Application application) {
        mPurchaseDao = AppDatabase.getDatabase(application).purchaseDao();
//        mAllPurchases = mPurchaseDao.getAll();
    }

    // this does not need to be run in separate thread because it returns LiveData
//    public LiveData<List<Purchase>> getAll() {
//        return mAllPurchases;
//    }

    public MutableLiveData<Long> insert(Purchase purchase) {
        new PurchaseRepository.InsertAsyncTask(mPurchaseDao, this).execute(purchase);
        return mInsertedPurchaseId;
    }

    private void setInsertedStoreId(long id) {
        mInsertedPurchaseId.postValue(id);
    }

    private static class InsertAsyncTask extends AsyncTask<Purchase, Void, Long> {

        private PurchaseDao mAsyncTaskDao;
        private PurchaseRepository delegate;

        InsertAsyncTask(PurchaseDao dao, PurchaseRepository repository) {
            mAsyncTaskDao = dao;
            delegate = repository;
        }

        @Override
        protected Long doInBackground(Purchase... params) {
            // returns id of the new inserted purchase
            return mAsyncTaskDao.insert(params[0]);
        }

        @Override
        protected void onPostExecute(Long id) {
            delegate.setInsertedStoreId(id);
        }
    }
}
