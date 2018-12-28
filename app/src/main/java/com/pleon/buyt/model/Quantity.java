package com.pleon.buyt.model;

public class Quantity {

    public enum Unit {
        UNIT, KILOGRAM, GRAM
    }

    private long value;
    private Unit unit;

    public Quantity(long value, Unit unit) {
        this.value = value;
        this.unit = unit;
    }

    public long getValue() {
        return value;
    }

    public void setValue(long value) {
        this.value = value;
    }

    public Unit getUnit() {
        return unit;
    }

    public void setUnit(Unit unit) {
        this.unit = unit;
    }
}
