package com.pleon.buyt

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_DEFAULT
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.location.LocationManager.GPS_PROVIDER
import android.os.Build
import android.os.Bundle
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.PRIORITY_DEFAULT
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.pleon.buyt.ui.activity.MainActivity

const val ACTION_LOCATION_EVENT = "com.pleon.buyt.broadcast.LOCATION_EVENT"
const val EXTRA_LOCATION = "com.pleon.buyt.extra.LOCATION"
private const val PROVIDER = GPS_PROVIDER
private const val NOTIFICATION_ID = 238

/**
 * We are not using WorkManager because if we want to run our task instantly
 * without any delay and the task is initiated by the user and supposed to be completed even if
 * the user exits the app (that means Guaranteed execution) then we have to use foreground services.
 *
 * More importantly, because we request location even when the app is in background, and because of
 * the limitation on number of background location requests in Android O (API 26) and higher,
 * foreground services should be used as stated by the android developer site.
 *
 * See [this post]
 * [https://android-developers.googleblog.com/2018/10/modern-background-execution-in-android.html]
 * for more information.
 */
class GpsService : Service(), LocationListener {

    private lateinit var locationManager: LocationManager
    private lateinit var notificationManager: NotificationManager
    private lateinit var pendingIntent: PendingIntent

    override fun onCreate() {
        super.onCreate()

        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel("Main", getString(R.string.app_name), IMPORTANCE_DEFAULT)
            channel.description = getString(R.string.notif_channel_desc)
            // channel.setShowBadge(false) // Disable the dot on app icon when notification is shown
            notificationManager.createNotificationChannel(channel)
        }

        val notificationIntent = Intent(this, MainActivity::class.java)
        pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0)

        // If you want to remove the notification when service stopped, see onDestroy() below
        val notification = NotificationCompat.Builder(this, "Main")
                .setSmallIcon(R.drawable.ald_buyt_notification)
                .setOngoing(true)
                .setLights(resources.getColor(R.color.colorPrimary), 500, 600)
                .setProgress(0, 0, true)
                .setPriority(PRIORITY_DEFAULT) // set to MIN to hide the icon in notification bar
                .setCategory(NotificationCompat.CATEGORY_PROGRESS)
                .setContentTitle(getString(R.string.notif_title_finding_store))
                // .setContentText("Determining the store...")
                .setContentIntent(pendingIntent)
                // .addAction() // Implement the action to cancel the search
                .build()

        // note that apps targeting android P (API level 28) or later must declare the permission
        // Manifest.permission.FOREGROUND_SERVICE in order to use startForeground()
        startForeground(NOTIFICATION_ID, notification)
    }

    /**
     * This method must be overridden; if you don't want to allow binding, return null.
     */
    override fun onBind(arg: Intent) = null

    @SuppressLint("MissingPermission")
    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        // FIXME: Because the first gps fix does not have a good accuracy, it seems better to call
        // requestLocationUpdates() and then in onLocationFound() check the accuracy and if it's
        // good enough call removeUpdates() there
        locationManager.requestSingleUpdate(PROVIDER, this, null)

        // If we get killed, after returning from here, don't restart
        return Service.START_NOT_STICKY
    }

    override fun onLocationChanged(location: Location) {
        // TODO: Make the intent explicit by defining the receiver class instead of action
        // For broadcast receivers, the intent simply defines the announcement being broadcast
        val result = Intent(ACTION_LOCATION_EVENT)
        result.putExtra(EXTRA_LOCATION, location)
        LocalBroadcastManager.getInstance(this).sendBroadcast(result)

        stopSelf()
        val notification = NotificationCompat.Builder(this, "Main")
                .setSmallIcon(R.drawable.ic_buyt)
                .setContentTitle(getString(R.string.notif_title_store_found))
                .setContentIntent(pendingIntent)
                .setAutoCancel(true) // Dismiss the notification when user taps on it
                .build()
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    override fun onProviderDisabled(provider: String) {}
    override fun onProviderEnabled(provider: String) {}
    override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}

    override fun onDestroy() {
        super.onDestroy()

        locationManager.removeUpdates(this)

        // if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        // stopForeground(false) // here notification can be removed as well
        // }
    }
}
