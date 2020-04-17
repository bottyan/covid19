package com.dialogity.covid_19risktracker;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.ContactsContract;

import java.util.UUID;

public class DataAccess {

    public static class Report {
        public static final int CODE_OK = 0;
        public static final int CODE_WARNING = 1;
        public static final int CODE_INFECTED = 2;
        public static final int CODE_SELF_DIAGNOSE = 4;
        public static final int CODE_REVOKE = 10;

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
        Long now = System.currentTimeMillis()/1000;
        MySQLiteDBHelper helper = new MySQLiteDBHelper(this.context);
        SQLiteDatabase database = helper.getWritableDatabase();
        Cursor cursor = database.query(
                MySQLiteDBHelper.OTHERS_TABLE_NAME,
                new String[] {MySQLiteDBHelper.OTHERS_COLUMN_OTHER_ID},
                MySQLiteDBHelper.OTHERS_COLUMN_OTHER_ID + " = ? and " + MySQLiteDBHelper.OTHERS_COLUMN_TIMESTAMP + " < ?",                                // The columns for the WHERE clause
                new String[] {other_uuid, now.toString()},
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
            // do nothing ID already recorded
        }
    }

    public String getMyUUID() {
        Long now = System.currentTimeMillis()/1000;
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
        if (cursor.moveToNext()) {
            String uuid = cursor.getString(cursor.getColumnIndex(h.MYIDS_COLUMN_MY_ID));
            return uuid;
        } else {
            UUID _uuid = UUID.randomUUID();
            String uuid = _uuid.toString();
            ContentValues values = new ContentValues();
            values.put(h.MYIDS_COLUMN_MY_ID, uuid);
            values.put(h.MYIDS_COLUMN_TIMESTAMP, now);
            long newRowId = database.insert(h.MYIDS_TABLE_NAME, null, values);
            return uuid;
        }
    }

    public Report report(String status, String data) {
        // TODO: implement saving and creating a report
        return null;
    }

    public Status getMyStatus() {
        // TODO: implement status calculation
        Status status = new Status();
        status.status_code = Status.CODE_OK;
        status.status_details = "";
        return status;
    }

    public void addAlert(long timestamp, String data, String[] contacts_to_alert) {
        // Save alert
    }

    public Alert getLastAlert() {
        // get last saved alert, to be able to revoke it
        return null;
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

//    private void saveToDB() {
//        SQLiteDatabase database = new SampleSQLiteDBHelper(this).getWritableDatabase();
//        ContentValues values = new ContentValues();
//        values.put(SampleSQLiteDBHelper.PERSON_COLUMN_NAME, activityBinding.nameEditText.getText().toString());
//        values.put(SampleSQLiteDBHelper.PERSON_COLUMN_AGE, activityBinding.ageEditText.getText().toString());
//        values.put(SampleSQLiteDBHelper.PERSON_COLUMN_GENDER, activityBinding.genderEditText.getText().toString());
//        long newRowId = database.insert(SampleSQLiteDBHelper.PERSON_TABLE_NAME, null, values);
//
//        Toast.makeText(this, "The new Row Id is " + newRowId, Toast.LENGTH_LONG).show();
//    }

//    private void readFromDB() {
//        String name = activityBinding.nameEditText.getText().toString();
//        String gender = activityBinding.genderEditText.getText().toString();
//        String age = activityBinding.ageEditText.getText().toString();
//        if(age.isEmpty())
//            age = "0";
//
//        SQLiteDatabase database = new SampleSQLiteDBHelper(this).getReadableDatabase();
//
//        String[] projection = {
//                SampleSQLiteDBHelper.PERSON_COLUMN_ID,
//                SampleSQLiteDBHelper.PERSON_COLUMN_NAME,
//                SampleSQLiteDBHelper.PERSON_COLUMN_AGE,
//                SampleSQLiteDBHelper.PERSON_COLUMN_GENDER
//        };
//
//        String selection =
//                SampleSQLiteDBHelper.PERSON_COLUMN_NAME + " like ? and " +
//                        SampleSQLiteDBHelper.PERSON_COLUMN_AGE + " > ? and " +
//                        SampleSQLiteDBHelper.PERSON_COLUMN_GENDER + " like ?";
//
//        String[] selectionArgs = {"%" + name + "%", age, "%" + gender + "%"};
//
//        Cursor cursor = database.query(
//                SampleSQLiteDBHelper.PERSON_TABLE_NAME,   // The table to query
//                projection,                               // The columns to return
//                selection,                                // The columns for the WHERE clause
//                selectionArgs,                            // The values for the WHERE clause
//                null,                                     // don't group the rows
//                null,                                     // don't filter by row groups
//                null                                      // don't sort
//        );
//
//        Log.d("TAG", "The total cursor count is " + cursor.getCount());
//        activityBinding.recycleView.setAdapter(new MyRecyclerViewCursorAdapter(this, cursor));
//    }
}
