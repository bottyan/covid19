package com.dialogity.covid_19risktracker;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MySQLiteDBHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "covid_db";

    public static final String OTHERS_TABLE_NAME = "others_detected";
    public static final String OTHERS_COLUMN_ID = "id";
    public static final String OTHERS_COLUMN_MY_ID = "my_id";
    public static final String OTHERS_COLUMN_OTHER_ID = "other_id";
    public static final String OTHERS_COLUMN_TIMESTAMP = "timestamp";

    public static final String MYIDS_TABLE_NAME = "my_ids";
    public static final String MYIDS_COLUMN_MY_ID = "my_id";
    public static final String MYIDS_COLUMN_TIMESTAMP = "timestamp";

    public static final String MYREPORTS_TABLE_NAME = "my_reports";
    public static final String MYREPORTS_COLUMN_ID = "id";
    public static final String MYREPORTS_COLUMN_MY_ID = "my_id";
    public static final String MYREPORTS_COLUMN_TIMESTAMP = "timestamp";
    public static final String MYREPORTS_COLUMN_STATUS = "status";
    public static final String MYREPORTS_COLUMN_DATA = "data";

    public MySQLiteDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL("CREATE TABLE " + OTHERS_TABLE_NAME + " (" +
                OTHERS_COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                OTHERS_COLUMN_MY_ID + " TEXT, " +
                OTHERS_COLUMN_OTHER_ID + " TEXT, " +
                OTHERS_COLUMN_TIMESTAMP + " BIGINT " +
                ")");

        sqLiteDatabase.execSQL("CREATE TABLE " + MYIDS_TABLE_NAME + " (" +
                MYIDS_COLUMN_MY_ID + " TEXT PRIMARY KEY, " +
                MYIDS_COLUMN_TIMESTAMP + " BIGINT " +
                ")");

        sqLiteDatabase.execSQL("CREATE TABLE " + MYREPORTS_TABLE_NAME + " (" +
                MYREPORTS_COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                MYREPORTS_COLUMN_MY_ID + " TEXT, " +
                MYREPORTS_COLUMN_STATUS + " TEXT, " +
                MYREPORTS_COLUMN_DATA + " TEXT, " +
                MYREPORTS_COLUMN_TIMESTAMP + " BIGINT " +
                ")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        if (i == i1) {
            return;
        }
        if (i1 == 1) {
            if (i < 1) {
                sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + OTHERS_TABLE_NAME);
                sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + MYREPORTS_TABLE_NAME);
                sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + MYIDS_TABLE_NAME);
                onCreate(sqLiteDatabase);
            }
        }
        // TODO: implement later upgrades
    }
}
