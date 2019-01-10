package com.pleon.buyt;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import static android.content.Context.LOCATION_SERVICE;

public class GpsListener implements LocationListener {

    private Context context;
    private Callback callback;

    public GpsListener(Context context, Callback callback) {
        this.context = context;
        this.callback = callback;
    }

    @Override
    public void onLocationChanged(Location location) {
        // stop using GPS
        ((LocationManager) context.getSystemService(LOCATION_SERVICE)).removeUpdates(this);
        callback.onLocationFound(location);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onProviderDisabled(String provider) {
        // TODO: handle gps disabled
    }

    public interface Callback {
        void onLocationFound(Location location);
    }
}
