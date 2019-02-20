package com.pleon.buyt.database.repository;

import android.app.Application;
import android.os.AsyncTask;

import com.pleon.buyt.database.AppDatabase;
import com.pleon.buyt.database.dao.ItemDao;
import com.pleon.buyt.database.dao.PurchaseDao;
import com.pleon.buyt.model.Item;
import com.pleon.buyt.model.Purchase;
import com.pleon.buyt.model.Store;

import java.util.Date;

import androidx.lifecycle.LiveData;

public class AddItemRepository {

    private ItemDao itemDao;
    private PurchaseDao purchaseDao;
    private LiveData<String[]> itemNames;

    public AddItemRepository(Application application) {
        itemDao = AppDatabase.getDatabase(application).itemDao();
        purchaseDao = AppDatabase.getDatabase(application).purchaseDao();
        itemNames = itemDao.getItemNames();
    }

    public void addItem(Item item) {
        new AddItemTask(itemDao).execute(item);
    }

    public void addPurchasedItem(Item item, Store store, Date purchaseDate) {
        new AddPurchasedItemTask(itemDao, purchaseDao, store, purchaseDate).execute(item);
    }

    public LiveData<String[]> getItemNames() {
        return itemNames;
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

    private static class AddPurchasedItemTask extends AsyncTask<Item, Void, Void> {

        private ItemDao itemDao;
        private PurchaseDao purchaseDao;
        private Store store;
        private Date date;

        AddPurchasedItemTask(ItemDao itemDao, PurchaseDao purchaseDao, Store store, Date date) {
            this.itemDao = itemDao;
            this.purchaseDao = purchaseDao;
            this.store = store;
            this.date = date;
        }

        @Override
        protected Void doInBackground(Item... items) {
            Purchase purchase = new Purchase(store.getStoreId(), date);
            long purchaseId = purchaseDao.insert(purchase);

            items[0].setPurchaseId(purchaseId);
            itemDao.insert(items[0]);

            return null;
        }
    }
}
