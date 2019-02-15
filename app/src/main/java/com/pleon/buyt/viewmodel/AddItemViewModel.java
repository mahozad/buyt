package com.pleon.buyt.viewmodel;

import android.app.Application;

import com.pleon.buyt.database.repository.AddItemRepository;
import com.pleon.buyt.model.Category;
import com.pleon.buyt.model.Item;
import com.pleon.buyt.model.Store;

import java.util.Date;
import java.util.List;

import androidx.lifecycle.AndroidViewModel;

public class AddItemViewModel extends AndroidViewModel {

    private AddItemRepository repository;
    private Category itemCategory = Category.GROCERY;
    private List<Store> storeList;
    private Store store;
    private Date purchaseDate = new Date();
    private int itemOrder;

    public AddItemViewModel(Application application) {
        super(application);
        repository = new AddItemRepository(application);
    }

    public void addItem(Item item) {
        repository.addItem(item);
    }

    public void addPurchasedItem(Item item, Store store, Date purchaseDate) {
        repository.addPurchasedItem(item, store, purchaseDate);
    }

    public Category getCategory() {
        return itemCategory;
    }

    public void setItemCategory(Category itemCategory) {
        this.itemCategory = itemCategory;
    }

    public List<Store> getStoreList() {
        return storeList;
    }

    public void setStoreList(List<Store> storeList) {
        this.storeList = storeList;
    }

    public Store getStore() {
        return store;
    }

    public void setStore(Store store) {
        this.store = store;
    }

    public Date getPurchaseDate() {
        return purchaseDate;
    }

    public void setPurchaseDate(Date purchaseDate) {
        this.purchaseDate = purchaseDate;
    }

    public int getItemOrder() {
        return itemOrder;
    }

    public void setItemOrder(int itemOrder) {
        this.itemOrder = itemOrder;
    }
}
