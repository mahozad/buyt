package com.pleon.buyt.model;

public class Quantity {

    public enum Unit {
        UNIT, KILOGRAM, GRAM
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

    @Override
    public String toString() {
        return quantity + " " + unit.toString().toLowerCase();
    }
}
