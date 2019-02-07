package com.pleon.buyt.viewmodel;

import com.pleon.buyt.model.Item;
import com.pleon.buyt.model.Store;

import java.util.Date;
import java.util.List;

import androidx.lifecycle.ViewModel;

public class AddItemViewModel extends ViewModel {

    private Item.Category itemCategory = Item.Category.GROCERY;
    private List<Store> storeList;
    private Store store;
    private Date purchaseDate = new Date();
    private int itemOrder;

    public Item.Category getItemCategory() {
        return itemCategory;
    }

    public void setItemCategory(Item.Category itemCategory) {
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
