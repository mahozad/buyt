package com.pleon.buyt.database;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

@Entity
public class Purchase {

    @PrimaryKey(autoGenerate = true)
    public long id;

    @ForeignKey(entity = Shop.class, parentColumns = "id", childColumns = "shopId")
    public long shopId;

    public String date;

    public Purchase(long shopId, String date) {
        this.shopId = shopId;
        this.date = date;
    }
}
