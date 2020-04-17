package com.dialogity.covid_19risktracker;

public class DataAccess {

    public static class Report {
        public static final int CODE_OK = 0;
        public static final int CODE_WARNING = 1;
        public static final int CODE_INFECTED = 2;
        public static final int CODE_SELF_DIAGNOSE = 3;
        public static final int CODE_REVOKE = 10;

        int report_code = 0;
        String report_details = "";
    }

    public static class Status {
        int status_code = 0;
        String status_details = "";
    }

    public static class Alert {
        String alertId;
        long timestamp;
        String data;
        String[] contacts_to_alert;
    }

    public static void addTracking(String other_uuid, String my_uuid) {

    }

    public static String getMyUUID() {
        return null;
    }

    public static Report report(String status, String data) {
        // TODO: implement saving and creating a report
        return null;
    }

    public static Status getMyStatus() {
        // TODO: implement status calculation
        return null;
    }

    public static void addAlert(Alert alert) {
        // TODO: implement alert processing
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
