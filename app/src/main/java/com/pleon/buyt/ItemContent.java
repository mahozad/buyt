package com.pleon.buyt;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// this is just a holder class to store list of items
public class ItemContent {

    public static final List<Item> ITEMS = new ArrayList<>();

    /**
     * A map of sample (dummy) items, by ID.
     */
    public static final Map<String, Item> ITEM_MAP = new HashMap<>();

    private static final int COUNT = 250;

    /*static {
        // Add some sample items.
        for (int i = 1; i <= COUNT; i++) {
            addItem(new Item(String.valueOf(i), LocalDateTime.now().toString(), "225 toman"));
        }
    }*/

    private static void addItem(Item item) {
        ITEMS.add(item);
        ITEM_MAP.put(item.id, item);
    }

    private static Item getItem(String id) {
        for (Item item : ITEMS) {
            if (item.id.equals(id)) {
                return item;
            }
        }
        return null; // TODO: throw a not found exception
    }

    /**
     * An item to buy.
     */
    public static class Item {

        public final String id;
        public final String name;
        public final String price;

        public Item(String id, String name, String price) {
            this.id = id;
            this.name = name;
            this.price = price;
        }

        @Override
        public String toString() {
            return "Item{" +
                    "name='" + name + '\'' +
                    ", price='" + price + '\'' +
                    '}';
        }
    }
}
