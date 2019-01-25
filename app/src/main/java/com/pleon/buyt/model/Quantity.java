package com.pleon.buyt.model;

import com.pleon.buyt.R;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;

public class Quantity {

    public enum Unit {
        UNIT(R.string.quantity_unit), KILOGRAM(R.string.quantity_kilogram), GRAM(R.string.quantity_gram);

        @StringRes private final int name;

        Unit(int name) {
            this.name = name;
        }

        public int getNameRes() {
            return name;
        }
    }

    private long quantity;
    private Unit unit;

    public Quantity(long quantity, Unit unit) {
        this.quantity = quantity;
        this.unit = unit;
    }

    public long getQuantity() {
        return quantity;
    }

    public void setQuantity(long quantity) {
        this.quantity = quantity;
    }

    public Unit getUnit() {
        return unit;
    }

    public void setUnit(Unit unit) {
        this.unit = unit;
    }

    @NonNull
    @Override
    public String toString() {
        return quantity + " " + unit.toString().toLowerCase();
    }
}
