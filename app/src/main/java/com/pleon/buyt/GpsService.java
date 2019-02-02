package com.pleon.buyt;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;

import com.pleon.buyt.ui.activity.MainActivity;

import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import static android.location.LocationManager.GPS_PROVIDER;
import static androidx.core.app.NotificationCompat.PRIORITY_LOW;

/**
 * We are not using WorkManager because if we want to run our task instantly
 * without any delay and the task is initiated by the user and supposed to be completed even if
 * the user exits the app (that means Guaranteed execution) then we have to use foreground services.
 * <p>
 * More importantly, because we request location even when the app is in background, and because of
 * the limitation on number of background location requests in Android O (API 26) and higher,
 * foreground services should be used as stated by the android developer site.
 */
public class GpsService extends Service implements LocationListener {

    public static final String ACTION_LOCATION_EVENT = "com.pleon.buyt.broadcast.LOCATION_EVENT";
    public static final String EXTRA_LOCATION = "com.pleon.buyt.extra.LOCATION";
    private static final String PROVIDER = GPS_PROVIDER;

    private LocationManager locationManager;
    private NotificationCompat.Builder notification;

    @Override
    public void onCreate() {
        super.onCreate();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("default", "YOUR_CHANNEL_NAME", NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("YOUR_NOTIFICATION_CHANNEL_DISCRIPTION");
            ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).createNotificationChannel(channel);

            Intent notificationIntent = new Intent(this, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

            // If you want to remove the notification when service stopped, see onDestroy() below
            notification = new NotificationCompat.Builder(this, "default")
                    .setSmallIcon(R.drawable.ic_buyt)
                    .setOngoing(true)
                    .setPriority(PRIORITY_LOW) // set to MIN to hide the icon in notification bar
                    .setContentTitle("My Awesome App")
                    .setContentText("Doing some work...")
                    .setContentIntent(pendingIntent);

            // note that apps targeting android P (API level 28) or later must declare the permission
            // Manifest.permission.FOREGROUND_SERVICE in order to use startForeground()
            startForeground(12421, notification.build());
        }
    }

    @Override
    public IBinder onBind(Intent arg) {
        // This method must be overridden; if you don't want to allow binding, return null.
        return null;
    }

    @Override
    @SuppressLint("MissingPermission")
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        // FIXME: Because the first gps fix does not have a good accuracy, it seems better to call
        // requestLocationUpdates() and then in onLocationFound() check the accuracy and if it's
        // good enough call removeUpdates() there
        locationManager.requestSingleUpdate(PROVIDER, this, null);

        // If we get killed, after returning from here, don't restart
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (locationManager != null) {
            locationManager.removeUpdates(this);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            stopForeground(false); // here notification can be removed as well
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        // TODO: Make the intent explicit by defining the receiver class instead of action
        // For broadcast receivers, the intent simply defines the announcement being broadcast
        Intent result = new Intent(ACTION_LOCATION_EVENT);
        result.putExtra(EXTRA_LOCATION, location);
        LocalBroadcastManager.getInstance(this).sendBroadcast(result);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notification.setContentTitle("Location found");
        }

        stopSelf();
    }

    //<editor-fold desc="Other LocationListener callbacks">
    @Override
    public void onProviderDisabled(String provider) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    //</editor-fold>
}
