package com.kahl.silir.databasehandler;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.github.mikephil.charting.data.Entry;
import com.kahl.silir.entity.MeasurementResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
    private final String DATA_VOLUME_TIME = "dataVolumeTime";
    private final String DATA_FLOW_VOLUME = "dataFlowVolume";

    private final String DELIMITER_ENTRY = "/";
    private final String DELIMITER_X_Y = ";";

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
                + DATA_VOLUME_TIME + " TEXT, "
                + DATA_FLOW_VOLUME + " TEXT, "
                + "FOREIGN KEY(" + RESULT_PROFILE + ") REFERENCES "
                + ProfileDbHandler.TABLE_PROFILES + "(" + ProfileDbHandler.PROFILE_ID + "))";
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
        SQLiteDatabase db = getReadableDatabase();
        final String query = "SELECT count(*) FROM " + TABLE_RESULTS;
        Cursor cursor = db.rawQuery(query, null);
        int result = cursor.moveToFirst() ? cursor.getInt(0) : 0;
        cursor.close();
        db.close();
        return result;
    }

    final String[] allColumns = {RESULT_ORDER, RESULT_FEV1, RESULT_FVC, RESULT_PEV, RESULT_TIME};

    public void addResult(MeasurementResult measurementResult, String date) {
        SQLiteDatabase db = getReadableDatabase();

        ContentValues values = new ContentValues();
        values.put(RESULT_PROFILE, measurementResult.getProfileId());
        values.put(RESULT_FVC, measurementResult.getFvc());
        values.put(RESULT_FEV1, measurementResult.getFev1());
        values.put(RESULT_PEV, measurementResult.getPef());
        values.put(RESULT_TIME, date);

        StringBuilder volumeTime = new StringBuilder();
        for (Entry value : measurementResult.getVolumeTimeCurve())
            volumeTime.append(value.getX()).append(DELIMITER_X_Y)
                    .append(value.getY()).append(DELIMITER_ENTRY);

        StringBuilder flowVolume = new StringBuilder();
        for (Entry value : measurementResult.getFlowVolumeCurve())
            flowVolume.append(value.getX()).append(DELIMITER_X_Y)
                    .append(value.getY()).append(DELIMITER_ENTRY);

        values.put(DATA_VOLUME_TIME, volumeTime.toString());
        values.put(DATA_FLOW_VOLUME, flowVolume.toString());

        db.insert(TABLE_RESULTS, RESULT_ORDER, values);
        db.close();
    }

    public void deleteResult(String time) {
        SQLiteDatabase db = getWritableDatabase();
        final String QUERY = "DELETE FROM " + TABLE_RESULTS + " WHERE " + RESULT_TIME + " = \'" + time + "\'";
        db.execSQL(QUERY);
        db.close();
    }

    public MeasurementResult getCurrentMeasurement() {
        MeasurementResult retval = null;
        SQLiteDatabase db = getReadableDatabase();
        final String subQuery = "SELECT max(" + RESULT_ORDER + ") FROM " + TABLE_RESULTS;
        final String query = "SELECT * FROM " + TABLE_RESULTS + " WHERE " + RESULT_ORDER + " = ("
                + subQuery + ")";

        Cursor cursor = db.rawQuery(query, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                String profileId = cursor.getString(1);
                float fvc = cursor.getFloat(2);
                float fev1 = cursor.getFloat(3);
                float pef = cursor.getFloat(4);
                String time = cursor.getString(5);
                String volumeTimeData = cursor.getString(6);
                String flowVolumeData = cursor.getString(7);

                List<Entry> volumeTime = new ArrayList<>();
                for (String value : volumeTimeData.split(DELIMITER_ENTRY)) {
                    if (!value.isEmpty()) {
                        String[] entry = value.split(DELIMITER_X_Y);
                        float valueX = Float.parseFloat(entry[0]);
                        float valueY = Float.parseFloat(entry[1]);
                        volumeTime.add(new Entry(valueX, valueY));
                    }
                }

                List<Entry> flowVolume = new ArrayList<>();
                for (String value : flowVolumeData.split(DELIMITER_ENTRY)) {
                    if (!value.isEmpty()) {
                        String[] entry = value.split(DELIMITER_X_Y);
                        float valueX = Float.parseFloat(entry[0]);
                        float valueY = Float.parseFloat(entry[1]);
                        flowVolume.add(new Entry(valueX, valueY));
                    }
                }

                retval = new MeasurementResult(fvc, fev1, pef, time, profileId, volumeTime, flowVolume);
            }
        }

        cursor.close();
        db.close();
        return retval;
    }

    public List<MeasurementResult> getAllResult() {
        SQLiteDatabase db = getReadableDatabase();

        final String query = "SELECT * FROM " + TABLE_RESULTS;
        Cursor cursor = db.rawQuery(query, null);

        List<MeasurementResult> result = new ArrayList<>();
        if (cursor != null) {
             if (cursor.moveToFirst()) {
                do {
                    String profileId = cursor.getString(1);
                    float fvc = cursor.getFloat(2);
                    float fev1 = cursor.getFloat(3);
                    float pef = cursor.getFloat(4);
                    String time = cursor.getString(5);
                    String volumeTimeData = cursor.getString(6);
                    String flowVolumeData = cursor.getString(7);

                    List<Entry> volumeTime = new ArrayList<>();
                    for (String value : volumeTimeData.split(DELIMITER_ENTRY)) {
                        if (!value.isEmpty()) {
                            String[] entry = value.split(DELIMITER_X_Y);
                            float valueX = Float.parseFloat(entry[0]);
                            float valueY = Float.parseFloat(entry[1]);
                            volumeTime.add(new Entry(valueX, valueY));
                        }
                    }

                    List<Entry> flowVolume = new ArrayList<>();
                    for (String value : flowVolumeData.split(DELIMITER_ENTRY)) {
                        if (!value.isEmpty()) {
                            String[] entry = value.split(DELIMITER_X_Y);
                            float valueX = Float.parseFloat(entry[0]);
                            float valueY = Float.parseFloat(entry[1]);
                            flowVolume.add(new Entry(valueX, valueY));
                        }
                    }

                    result.add(new MeasurementResult(fvc, fev1, pef, time, profileId, volumeTime, flowVolume));
                } while (cursor.moveToNext());
            }
        }

        cursor.close();
        db.close();

        return result;
    }

    public MeasurementResult getMeasurementResult(String time) {
        final String QUERY = "SELECT * FROM " + TABLE_RESULTS + " WHERE " + RESULT_TIME + " = \'" + time + "\'";
        MeasurementResult result = null;

        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.rawQuery(QUERY, null);
        if (cursor != null) {
            cursor.moveToFirst();
            String profileId = cursor.getString(1);
            float fvc = cursor.getFloat(2);
            float fev1 = cursor.getFloat(3);
            float pef = cursor.getFloat(4);
            String tm = cursor.getString(5);
            String volumeTimeData = cursor.getString(6);
            String flowVolumeData = cursor.getString(7);

            List<Entry> volumeTime = new ArrayList<>();
            for (String value : volumeTimeData.split(DELIMITER_ENTRY)) {
                if (!value.isEmpty()) {
                    String[] entry = value.split(DELIMITER_X_Y);
                    float valueX = Float.parseFloat(entry[0]);
                    float valueY = Float.parseFloat(entry[1]);
                    volumeTime.add(new Entry(valueX, valueY));
                }
            }

            List<Entry> flowVolume = new ArrayList<>();
            for (String value : flowVolumeData.split(DELIMITER_ENTRY)) {
                if (!value.isEmpty()) {
                    String[] entry = value.split(DELIMITER_X_Y);
                    float valueX = Float.parseFloat(entry[0]);
                    float valueY = Float.parseFloat(entry[1]);
                    flowVolume.add(new Entry(valueX, valueY));
                }
            }

            result = new MeasurementResult(fvc, fev1, pef, tm, profileId, volumeTime, flowVolume);
        }

        cursor.close();
        db.close();

        return result;
    }
}

