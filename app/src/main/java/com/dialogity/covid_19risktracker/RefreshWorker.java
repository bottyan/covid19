package com.dialogity.covid_19risktracker;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class RefreshWorker extends Worker {

    private static final String TAG = RefreshWorker.class.getSimpleName();

    public RefreshWorker(
            @NonNull Context context,
            @NonNull WorkerParameters params) {
        super(context, params);
    }

    @Override
    public Result doWork() {
        Log.i(TAG, "Restarting Bluetooth...");
        try {
            MyBeaconHandler beaconHandler = new MyBeaconHandler();
            beaconHandler.startBleutooth();
        } catch (Exception e) {
            Log.e(TAG, "Error restarting Bluetooth.", e);
        }
        return Result.success();
    }
}
