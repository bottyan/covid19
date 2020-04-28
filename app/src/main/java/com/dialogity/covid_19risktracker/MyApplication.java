package com.dialogity.covid_19risktracker;

import android.app.Application;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseSettings;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.RemoteException;
import android.util.Log;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.BeaconTransmitter;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;
import org.altbeacon.beacon.powersave.BackgroundPowerSaver;
import org.altbeacon.beacon.startup.BootstrapNotifier;
import org.altbeacon.beacon.startup.RegionBootstrap;

import java.util.Arrays;
import java.util.Collection;

public class MyApplication extends Application {

    public static final String MY_UUID_KEY = "my_uuid";
    public static final String MY_PREF_NAME = "my_preferences";

    private static Context context;

    private static final String TAG = MyApplication.class.getSimpleName();
    private RegionBootstrap regionBootstrap;
    private BackgroundPowerSaver backgroundPowerSaver; // TODO: check if necessary

    @Override
    public void onCreate() {
        super.onCreate();
        MyApplication.context = getApplicationContext();
        String uuid = DataAccess.get(this).getMyUUID();
        try {

            MyBeaconHandler beaconHandler = new MyBeaconHandler();
            beaconHandler.startBleutooth();
        } catch (Exception e) {
            Log.e(TAG, "Error enabling Bluetooth.", e);
        }
        try {
            startListeningFCM();
        } catch (Exception e) {
            Log.e(TAG, "Error starting FCM listener.", e);
        }
    }

    public static Context getAppContext() {
        return MyApplication.context;
    }

    public void startListeningFCM() {
        MyFirebaseMessagingService myFCM = new MyFirebaseMessagingService(this);
        myFCM.subscribeToTopic();
    }

}
