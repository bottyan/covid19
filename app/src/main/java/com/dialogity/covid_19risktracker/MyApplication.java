package com.dialogity.covid_19risktracker;

import android.app.Application;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
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

public class MyApplication extends Application implements BootstrapNotifier, RangeNotifier {

    public static final String MY_UUID_KEY = "my_uuid";
    public static final String MY_PREF_NAME = "my_preferences";

    private static final String TAG = MyApplication.class.getSimpleName();
    private RegionBootstrap regionBootstrap;
    private BackgroundPowerSaver backgroundPowerSaver; // TODO: check if necessary

    @Override
    public void onCreate() {
        super.onCreate();
        String uuid = DataAccess.get(this).getMyUUID();
        startBluetooth(uuid);
    }

    public void startBluetooth(String uuid) {
        Log.d(TAG, "App started up");
        backgroundPowerSaver = new BackgroundPowerSaver(this); // TODO: check if necessary
        BeaconManager beaconManager = BeaconManager.getInstanceForApplication(this);

        // The following code block sets up the foreground service

        Notification.Builder builder = new Notification.Builder(this);
        builder.setSmallIcon(R.drawable.ic_shield_virus_solid);
        builder.setContentTitle(getString(R.string.noti_contact_tracing_on));
        //Intent intent = new Intent(this, MonitoringActivity.class);
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT
        );
        builder.setContentIntent(pendingIntent);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(getString(R.string.default_notification_channel_id),
                    getString(R.string.default_notification_channel_name), NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription(getString(R.string.default_notification_channel_description));
            NotificationManager notificationManager = (NotificationManager) getSystemService(
                    Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(channel);
            builder.setChannelId(channel.getId());
        }
        beaconManager.enableForegroundServiceScanning(builder.build(), 456);
        beaconManager.setEnableScheduledScanJobs(false);

//        // The following code block effectively disables beacon scanning in the foreground service
//        // to save battery.  Do not include this code block if you want to detect beacons
//
//        beaconManager.getBeaconParsers().clear(); // clearning all beacon parsers ensures nothing matches
//        beaconManager.setBackgroundBetweenScanPeriod(Long.MAX_VALUE);
//        beaconManager.setBackgroundScanPeriod(0);
//        beaconManager.setForegroundBetweenScanPeriod(Long.MAX_VALUE);
//        beaconManager.setForegroundScanPeriod(0);

        // To detect proprietary beacons, you must add a line like below corresponding to your beacon
        // type.  Do a web search for "setBeaconLayout" to get the proper expression.
        beaconManager.getBeaconParsers().add(new BeaconParser()
                //setBeaconLayout("m:2-3=beac,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25"));
                .setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"));

        // wake up the app when any beacon is seen (you can specify specific id filers in the parameters below)
        Region region = new Region("com.dialogity.covid_19risktracker.boostrapRegion", null, null, null);
        regionBootstrap = new RegionBootstrap(this, region);

        // TODO: connect range notifier only after didEnterRegion ? and discontect in 10 sec ?
        // beaconManager.removeAllRangeNotifiers();
        beaconManager.addRangeNotifier(this);
        try {
            beaconManager.startRangingBeaconsInRegion(region);
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        Beacon beacon = new Beacon.Builder()
                //.setId1("2f234454-cf6d-4a0f-adf2-f4911ba9ffa6") // TODO: set my unique ID (/what is the region?)
                .setId1(uuid)
                .setId2("1")
                .setId3("2")
                .setManufacturer(0x004c) // Radius Networks.  Change this for other beacon layouts
                .setTxPower(-59)
                .setDataFields(Arrays.asList(new Long[]{0l})) // Remove this for beacon layouts without d: fields
                .build();

        // Change the layout below for other beacon types
        BeaconParser beaconParser = new BeaconParser().setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24");
        BeaconTransmitter beaconTransmitter = new BeaconTransmitter(getApplicationContext(), beaconParser);
        beaconTransmitter.startAdvertising(beacon, new AdvertiseCallback() {
            @Override
            public void onStartFailure(int errorCode) {
                Log.e(TAG, "Advertisement start failed with code: " + errorCode);
            }

            @Override
            public void onStartSuccess(AdvertiseSettings settingsInEffect) {
                Log.i(TAG, "Advertisement start succeeded.");
            }
        });
    }

    @Override
    public void didDetermineStateForRegion(int arg0, Region arg1) {
        // Don't care
    }

    @Override
    public void didEnterRegion(Region region) {
        // TODO: don't start the app, but log the event
        Log.w(TAG, "Got a didEnterRegion call");
        Log.w(TAG, "Region details: " + region.getUniqueId() + ";" + region.getId1() + ";"
                + region.getId2() + ";" + region.getId3() + ";" + region.toString() + ";" + region.getBluetoothAddress());

        BeaconManager beaconManager = BeaconManager.getInstanceForApplication(this);
        try {
            beaconManager.startMonitoringBeaconsInRegion(region);
            beaconManager.startRangingBeaconsInRegion(region);
        } catch (RemoteException e) {
            Log.e(TAG, "ERROR while start ranging.", e);
        }

        // This call to disable will make it so the activity below only gets launched the first time
        // a beacon is seen (until the next time the app is launched)
        // if you want the Activity to launch every single time beacons come into view, remove this call.
        //      regionBootstrap.disable();
        Intent intent = new Intent(this, MainActivity.class);
        // IMPORTANT: in the AndroidManifest.xml definition of this activity, you must set android:launchMode="singleInstance" or you will get two instances
        // created when a user launches the activity manually and it gets launched from here.
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        this.startActivity(intent);
    }

    @Override
    public void didExitRegion(Region region) {
        // TODO: log this too
        Log.w(TAG, "Got didExitRegion call");
        Log.w(TAG, "Region details: " + region.getUniqueId() + ";" + region.getId1() + ";"
                + region.getId2() + ";" + region.getId3() + ";");
    }

    @Override
    public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
        for (Beacon beacon : beacons) {
            Log.i(TAG, "The first beacon I see is about "+beacon.getDistance()+" meters away.");
            //Log.i(TAG, b.toString());
            String other_id = beacon.getId1().toString();
            DataAccess.get(this).addTracking(other_id);
        }
    }
}
