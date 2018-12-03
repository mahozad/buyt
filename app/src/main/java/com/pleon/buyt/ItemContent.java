package com.pleon.buyt;

import com.pleon.buyt.database.Purchase;

import java.util.ArrayList;
import java.util.List;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

// this is just a holder class to store list of items
public class ItemContent {

    public static final List<Item> ITEMS = new ArrayList<>();

    public static void addItem(Item item) {
        ITEMS.add(item);
    }

    public static Item getItem(long id) {
        for (Item item : ITEMS) {
            if (item.id == id) {
                return item;
            }
        }
        return null; // TODO: throw a not found exception
    }

    /**
     * An item to buy.
     */
    @Entity
    public static class Item {

        @PrimaryKey(autoGenerate = true)
        public long id;

        @ForeignKey(entity = Purchase.class, parentColumns = "id", childColumns = "purchaseId")
        public long purchaseId;

        public final String name;
        public final String price;
        public double volume;
        public boolean bought;
        public String category;

        public Item(String name, String price) {
            this.name = name;
            this.price = price;
        }
    }
}
