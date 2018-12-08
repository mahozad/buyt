package com.pleon.buyt;

import android.app.Application;

import com.facebook.stetho.Stetho;

/*
 * if application runs in debug mode, this file will be the "Application" class of the app
 * (we've defined so in the manifest file of the src/debug).
 * Here the Stetho tool is setup so we can inspect app database, network, layouts and so on
 * with Google chrome in: [chrome://inspect].
 * In Iran, use VPN due to sanctions.
 */
public class BuytApplication extends Application {

    public void onCreate() {
        super.onCreate();
        Stetho.initializeWithDefaults(this);
    }
}
