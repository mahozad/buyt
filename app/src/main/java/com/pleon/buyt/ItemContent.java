package com.pleon.buyt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

// this is just a holder class to store list of items
public class ItemContent {

    public static final List<Item> ITEMS = new ArrayList<>();

    /**
     * A map of sample (dummy) items, by ID.
     */
    private static final Map<Integer, Item> ITEM_MAP = new HashMap<>();

    public static void addItem(Item item) {
        ITEMS.add(item);
        ITEM_MAP.put(item.id, item);
    }

    public static Item getItem(int id) {
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
        public int id;
        public final String name;
        public final String price;

        public Item(String name, String price) {
            this.name = name;
            this.price = price;
        }
    }
}
