package com.pleon.buyt.model;

import android.location.Location;

import static java.lang.Math.PI;
import static java.lang.Math.cos;
import static java.lang.Math.sin;

public class Coordinates {

    private double latitude;
    private double longitude;
    private double cosLat;
    private double sinLat;
    private double cosLng;
    private double sinLng;

    public Coordinates(Location location) {
        this(location.getLatitude(), location.getLongitude());
    }

    public Coordinates(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.cosLat = cos(latitude * PI / 180);
        this.cosLng = cos(longitude * PI / 180);
        this.sinLat = sin(latitude * PI / 180);
        this.sinLng = sin(longitude * PI / 180);
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getCosLat() {
        return cosLat;
    }

    public void setCosLat(double cosLat) {
        this.cosLat = cosLat;
    }

    public double getSinLat() {
        return sinLat;
    }

    public void setSinLat(double sinLat) {
        this.sinLat = sinLat;
    }

    public double getCosLng() {
        return cosLng;
    }

    public void setCosLng(double cosLng) {
        this.cosLng = cosLng;
    }

    public double getSinLng() {
        return sinLng;
    }

    public void setSinLng(double sinLng) {
        this.sinLng = sinLng;
    }
}
