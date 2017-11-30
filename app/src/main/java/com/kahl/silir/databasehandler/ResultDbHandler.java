package com.kahl.silir.databasehandler;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.kahl.silir.entity.MeasurementResult;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

/**
 * Created by Paskahlis Anjas Prabowo on 04/09/2017.
 */

public class ResultDbHandler extends DatabaseHandler {
    private static final String DATABASE_NAME = "resultDatabase";
    private final String TABLE_RESULTS = "results";

    private final String RESULT_ORDER = "_order";
    private final String RESULT_PROFILE = "resultProfileId";
    private final String RESULT_FVC = "fvc";
    private final String RESULT_FEV1 = "fev1";
    private final String RESULT_PEV = "pev";
    private final String RESULT_TIME = "time";
    private final String RESULT_ARRAY_FLOW = "arrayFlow";
    private final String RESULT_ARRAY_VOLUME = "arrayVolume";

    SQLiteDatabase db;


    public ResultDbHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String query = "CREATE TABLE " + TABLE_RESULTS + " ("
                + RESULT_ORDER + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + RESULT_PROFILE + " TEXT, "
                + RESULT_FVC + " REAL, "
                + RESULT_FEV1 + " REAL, "
                + RESULT_PEV + " REAL, "
                + RESULT_TIME + " TEXT, "
                + RESULT_ARRAY_FLOW + " TEXT, "
                + RESULT_ARRAY_VOLUME + " TEXT, "
                + "FOREIGN KEY(" + RESULT_PROFILE + ") REFERENCES "
                + ProfileDbHandler.TABLE_PROFILES + "(" + ProfileDbHandler.PROFILE_ID + "));";

        db.execSQL(query);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RESULTS);
        onCreate(db);
    }

    @Override
    public boolean isDbExist() {
        return countRecord() > 0;
    }

    @Override
    public int countRecord() {
        db = getReadableDatabase();
        final String query = "SELECT count(*) FROM " + TABLE_RESULTS;
        Cursor cursor = db.rawQuery(query, null);
        int result = cursor.moveToFirst() ? cursor.getInt(0) : 0;
        cursor.close();
        db.close();
        return result;
    }

    final String[] allColumns = {RESULT_ORDER, RESULT_FEV1, RESULT_FVC, RESULT_PEV, RESULT_TIME,
            RESULT_ARRAY_FLOW, RESULT_ARRAY_VOLUME};

    public void addResult(MeasurementResult measurementResult) {
        db = getReadableDatabase();

        ContentValues values = new ContentValues();
        values.put(RESULT_PROFILE, measurementResult.getProfileId());
        values.put(RESULT_FVC, measurementResult.getFvc());
        values.put(RESULT_FEV1, measurementResult.getFev1());
        values.put(RESULT_PEV, measurementResult.getPef());
        values.put(RESULT_TIME, measurementResult.getTime());
        values.put(RESULT_ARRAY_FLOW, measurementResult.getArrayFlow());
        values.put(RESULT_ARRAY_VOLUME, measurementResult.getArrayVolume());

        db.insert(TABLE_RESULTS, RESULT_ORDER, values);
        db.close();
    }

    public void deleteResult(String order, String profileId) {
        db = getReadableDatabase();
        db.delete(TABLE_RESULTS, RESULT_ORDER + " =? AND " + RESULT_PROFILE + " =?", new String[]{order, profileId});
        db.close();
    }

    public MeasurementResult getCurrentMeasurement() {
        MeasurementResult retval = null;
        SQLiteDatabase db = getReadableDatabase();
        //final String subQuery = "SELECT max(" + RESULT_ORDER + ") FROM " + TABLE_RESULTS;
        //final String query = "SELECT * FROM (" + subQuery + ")";
        final String query = "SELECT * FROM " + TABLE_RESULTS;

        Cursor cursor = db.rawQuery(query, null);
        if (cursor != null) {
            if (cursor.moveToLast())
                retval = new MeasurementResult(
                        cursor.getFloat(2),
                        cursor.getFloat(3),
                        cursor.getFloat(4),
                        cursor.getString(5),
                        cursor.getString(1),
                        cursor.getString(6),
                        cursor.getString(7));
        }
        cursor.close();
        db.close();
        return retval;
    }

    public HashMap<String, MeasurementResult> getAllResult() {
        HashMap<String, MeasurementResult> result = new HashMap<>();

        /*to be continued*/

        return result;
    }
}

