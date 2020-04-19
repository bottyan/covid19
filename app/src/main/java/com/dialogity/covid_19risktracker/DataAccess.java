package com.dialogity.covid_19risktracker;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.ContactsContract;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public class DataAccess {

    private static final String TAG = DataAccess.class.getSimpleName();

    public static class Report {
        public static final int CODE_OK = 0;
        public static final int CODE_WARNING = 1;
        public static final int CODE_INFECTED = 2;
        public static final int CODE_SELF_DIAGNOSE = 4;
        public static final int CODE_REVOKE = 10;

        String id = null;
        int report_code = 0;
        String report_details = "";
    }

    public static class Status {
        public static final int CODE_OK = 0;
        public static final int CODE_WARNING = 1;
        public static final int CODE_INFECTED = 2;
        public static final int CODE_DANGER = 3;

        int status_code = 0;
        String status_details = "";
    }

    public static class Alert {
        String alertId;
        long timestamp;
        String data;
        String[] contacts_to_alert;
    }

    private Context context;

    public static DataAccess get(Context context) {
        DataAccess da = new DataAccess(context);
        return da;
    }

    private DataAccess(Context context) {
        this.context = context;
    }

    public void addTracking(String other_uuid) {
        // check when the last time this ID was added, if it's older than 5 min, add again
        Long now = System.currentTimeMillis();
        Long nowMinus3Min = now-3*60000;
        MySQLiteDBHelper helper = new MySQLiteDBHelper(this.context);
        SQLiteDatabase database = helper.getWritableDatabase();
        Cursor cursor = database.query(
                MySQLiteDBHelper.OTHERS_TABLE_NAME,
                new String[] {MySQLiteDBHelper.OTHERS_COLUMN_OTHER_ID},
                MySQLiteDBHelper.OTHERS_COLUMN_OTHER_ID + " = ? and " + MySQLiteDBHelper.OTHERS_COLUMN_TIMESTAMP + " > ?",                                // The columns for the WHERE clause
                new String[] {other_uuid, nowMinus3Min.toString()},
                null,
                null,
                null
        );
        if (cursor.getCount() <= 0) {
            ContentValues values = new ContentValues();
            values.put(MySQLiteDBHelper.OTHERS_COLUMN_OTHER_ID, other_uuid);
            values.put(MySQLiteDBHelper.OTHERS_COLUMN_TIMESTAMP, now);
            long newRowId = database.insert(MySQLiteDBHelper.OTHERS_TABLE_NAME, null, values);
        } else {
            // do nothing ID already recorded in the last 3 Min
        }
        cursor.close();
    }

    public String getMyUUID() {
        Long now = System.currentTimeMillis();
        Long nowMinus30Min = now-30*60000;
        MySQLiteDBHelper h = new MySQLiteDBHelper(this.context);
        SQLiteDatabase database = h.getWritableDatabase();
        Cursor cursor = database.query(
                h.MYIDS_TABLE_NAME,
                new String[] {h.MYIDS_COLUMN_MY_ID, h.MYIDS_COLUMN_TIMESTAMP},
                 h.MYIDS_COLUMN_TIMESTAMP + " > ?",                                // The columns for the WHERE clause
                new String[] {nowMinus30Min.toString()},
                null,
                null,
                null
        );
        String uuid;
        if (cursor.moveToNext()) {
            uuid = cursor.getString(cursor.getColumnIndex(h.MYIDS_COLUMN_MY_ID));
        } else {
            UUID _uuid = UUID.randomUUID();
            uuid = _uuid.toString();
            ContentValues values = new ContentValues();
            values.put(h.MYIDS_COLUMN_MY_ID, uuid);
            values.put(h.MYIDS_COLUMN_TIMESTAMP, now);
            long newRowId = database.insert(h.MYIDS_TABLE_NAME, null, values);
        }
        cursor.close();
        return  uuid;
    }

    public Status getMyStatus() {
        Log.i(TAG, "Calculate status...");
        Status status = new Status();
        status.status_code = Status.CODE_OK;
        status.status_details = "";

        Report r = getLastReport();
        if (r != null && (r.report_code == Report.CODE_INFECTED || r.report_code == Report.CODE_SELF_DIAGNOSE)) {
            Log.i(TAG, "I have a self report...");
            status.status_code = Status.CODE_INFECTED;
            status.status_details = r.report_details;
        } else {
            Log.i(TAG, "No report, calculate from alert history.");
            Long now = System.currentTimeMillis();
            Long nowMinus2Weeks = now-7*86400000;
            MySQLiteDBHelper h = new MySQLiteDBHelper(this.context);
            SQLiteDatabase database = h.getWritableDatabase();
            Cursor cursor = database.query(
                    h.ALERTS_TABLE_NAME,
                    new String[] {h.ALERTS_COLUMN_DATA, h.ALERTS_COLUMN_HIT_COUNT, h.ALERTS_COLUMN_ALERT_ID},
                    h.ALERTS_COLUMN_TIMESTAMP + " > ?",
                    new String[] {nowMinus2Weeks.toString()},
                    null,
                    null,
                    null
            );
            Map<String, Integer> result = new HashMap<>();
            int points = 0;
            HashMap<String, Double> scoresAdded = new HashMap<>();
            HashSet<String> toRemove = new HashSet<>();
            while (cursor.moveToNext()) {
                String data_str = "";
                try {
                    data_str = cursor.getString(cursor.getColumnIndex(h.ALERTS_COLUMN_DATA));
                    int hit_count = cursor.getInt(cursor.getColumnIndex(h.ALERTS_COLUMN_HIT_COUNT));
                    JSONObject json = new JSONObject(data_str);
                    int alert_code = json.getInt("code");
                    String reportId = "";
                    if (json.has("reportId")) {
                        reportId = json.getString("reportId");
                    }
                    if (alert_code == Report.CODE_INFECTED) {
                        double toAdd = hit_count * 5;
                        scoresAdded.put(reportId, toAdd);
                        points += toAdd;
                    } else if (alert_code == Report.CODE_SELF_DIAGNOSE) {
                        JSONObject symptoms = json.getJSONObject("data").getJSONObject("symptoms");
                        // TODO get more accurate probability estimate
                        double probability = 0.0;
                        if (symptoms.getBoolean("s1")) { probability += 0.1; }
                        if (symptoms.getBoolean("s2")) { probability += 0.1; }
                        if (symptoms.getBoolean("s3")) { probability += 0.1; }
                        if (symptoms.getBoolean("s4")) { probability += 0.1; }
                        if (symptoms.getBoolean("s5")) { probability += 0.1; }
                        if (symptoms.getBoolean("s6")) { probability += 0.1; }
                        if (symptoms.getBoolean("s7")) { probability += 0.1; }
                        if (symptoms.getBoolean("s8")) { probability += 0.1; }
                        double toAdd = hit_count * 2.5 * probability;
                        scoresAdded.put(reportId, toAdd);
                        points += toAdd;
                    } else if (alert_code == Report.CODE_REVOKE) {
                        String revokedAlert = json.getString("revoked");
                        toRemove.add(revokedAlert);
                    }
                } catch (JSONException e) {
                    Log.e(TAG, "ERROR parsing alert data: \"" + data_str + "\"", e);
                }
            }
            cursor.close();
            for(String revokedId : toRemove) {
                if(scoresAdded.containsKey(revokedId)) {
                    points -= scoresAdded.get(revokedId);
                } else {
                    Log.w(TAG, "Revoked ID not found! " + revokedId);
                }
            }
            Log.i(TAG, "I have: " + points + " points.");
            if (points >= 15 && points <= 50) {
                status.status_code = Status.CODE_WARNING;
                status.status_details = "" + points;
            } else if (points > 50) {
                status.status_code = Status.CODE_DANGER;
                status.status_details = "" + points;
            }
        }

        return status;
    }

    public Report getLastReport() {
        MySQLiteDBHelper h = new MySQLiteDBHelper(this.context);
        SQLiteDatabase database = h.getReadableDatabase();
        Cursor cursor = database.query(
                h.MYREPORTS_TABLE_NAME,
                new String[] {h.MYREPORTS_COLUMN_STATUS, h.MYREPORTS_COLUMN_DATA, h.MYREPORTS_COLUMN_ID},
                null,
                null,
                null,
                null,
                h.MYREPORTS_COLUMN_TIMESTAMP + " DESC"
        );
        if (cursor.moveToNext()) {
            Report r = new Report();
            r.report_details = cursor.getString(cursor.getColumnIndex(h.MYREPORTS_COLUMN_DATA));
            r.report_code = Integer.parseInt(cursor.getString(cursor.getColumnIndex(h.MYREPORTS_COLUMN_STATUS)));
            r.id = cursor.getString(cursor.getColumnIndex(h.MYREPORTS_COLUMN_ID));
            cursor.close();
            return r;
        }
        cursor.close();
        return null;
    }

    public String addReport(Report report) {
        Log.i(TAG, "Adding report.");
        MySQLiteDBHelper h = new MySQLiteDBHelper(this.context);
        SQLiteDatabase database = h.getWritableDatabase();
        ContentValues values = new ContentValues();
        String reportId = UUID.randomUUID().toString();
        values.put(h.MYREPORTS_COLUMN_DATA, report.report_details);
        values.put(h.MYREPORTS_COLUMN_ID, reportId);
        values.put(h.MYREPORTS_COLUMN_STATUS, ""+report.report_code);
        values.put(h.MYREPORTS_COLUMN_TIMESTAMP, System.currentTimeMillis());
        long newRowId = database.insert(MySQLiteDBHelper.MYREPORTS_TABLE_NAME, null, values);
        return  reportId;
    }

    public void addAlert(long alertTimestamp, String alertId, String data, Map<String, Integer> contacts_to_alert) {
        Log.i(TAG, "Checking alert");
        MySQLiteDBHelper h = new MySQLiteDBHelper(this.context);
        SQLiteDatabase database = h.getWritableDatabase();
        Cursor cursor = database.query(
                h.MYIDS_TABLE_NAME,
                new String[] {h.MYIDS_COLUMN_MY_ID},
                h.MYIDS_COLUMN_MY_ID + " IN (" + h.getPlaceholders(contacts_to_alert.size()) + ")",
                contacts_to_alert.keySet().toArray(new String[0]),
                null,
                null,
                null
        );
        int hit_count = 0;
        while (cursor.moveToNext()) {
            String id = cursor.getString(cursor.getColumnIndex(h.MYIDS_COLUMN_MY_ID));
            hit_count += contacts_to_alert.get(id);
        }
        cursor.close();
        if (hit_count > 0) {
            Log.i(TAG, "MATCH FOUND");
            ContentValues values = new ContentValues();
            values.put(h.ALERTS_COLUMN_ALERT_ID, alertId);
            values.put(h.ALERTS_COLUMN_DATA, data);
            values.put(h.ALERTS_COLUMN_TIMESTAMP, alertTimestamp);
            values.put(h.ALERTS_COLUMN_HIT_COUNT, hit_count);
            long newRowId = database.insert(MySQLiteDBHelper.ALERTS_TABLE_NAME, null, values);
        } else {
            // my ID is not mentioned do nothing
            Log.i(TAG, "NO MATCH FOUND");
        }
    }

    public Map<String, Integer> getContacts() {
        Long now = System.currentTimeMillis();
        Long nowMinus2Weeks = now-7*86400000;
        MySQLiteDBHelper h = new MySQLiteDBHelper(this.context);
        SQLiteDatabase database = h.getWritableDatabase();
        Cursor cursor = database.query(
                h.OTHERS_TABLE_NAME,
                new String[] {h.OTHERS_COLUMN_OTHER_ID, "COUNT(1) AS count"},
                h.OTHERS_COLUMN_TIMESTAMP + " > ?",
                new String[] {nowMinus2Weeks.toString()},
                h.OTHERS_COLUMN_OTHER_ID,
                null,
                null
        );
        Map<String, Integer> result = new HashMap<>();
        while (cursor.moveToNext()) {
            result.put(
                    cursor.getString(cursor.getColumnIndex(h.OTHERS_COLUMN_OTHER_ID)),
                    cursor.getInt(cursor.getColumnIndex("count"))
            );
        }
        cursor.close();
        return result;
    }

    public void resetAll() {
        MySQLiteDBHelper helper = new MySQLiteDBHelper(this.context);
        SQLiteDatabase database = helper.getWritableDatabase();
        database.execSQL("DROP TABLE IF EXISTS " + MySQLiteDBHelper.OTHERS_TABLE_NAME);
        database.execSQL("DROP TABLE IF EXISTS " + MySQLiteDBHelper.MYREPORTS_TABLE_NAME);
        database.execSQL("DROP TABLE IF EXISTS " + MySQLiteDBHelper.MYIDS_TABLE_NAME);
        database.execSQL("DROP TABLE IF EXISTS " + MySQLiteDBHelper.ALERTS_TABLE_NAME);
        helper.onCreate(database);
    }

    public static String stringListToJsonString(String[] list) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < list.length; i++) {
            if (i == 0) {
                sb.append("\"").append(list[i]).append("\"");
            } else {
                sb.append(", \"").append(list[i]).append("\"");
            }
        }
        sb.append("]");
        return sb.toString();
    }

}
