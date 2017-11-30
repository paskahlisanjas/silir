package com.kahl.silir.databasehandler;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.kahl.silir.entity.MeasurementProfile;
import com.kahl.silir.entity.User;

import java.util.HashMap;

/**
 * Created by Paskahlis Anjas Prabowo on 02/09/2017.
 */


public class ProfileDbHandler extends DatabaseHandler {
    private final static String DATABASE_NAME = "profileDatabase";
    static final String TABLE_PROFILES = "profiles";
    static final String PROFILE_ID = "id";

    private final String PROFILE_NAME = "name";
    private final String PROFILE_DOB = "dob";
    private final String PROFILE_GENDER = "gender";
    private final String PROFILE_HEIGHT = "height";
    private final String PROFILE_WEIGHT = "weight";

    private final String IS_KEY_ID_EQUAL = PROFILE_ID + "=?";

    public ProfileDbHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String CREATE_TABLE = "CREATE TABLE " + TABLE_PROFILES + " ("
                + PROFILE_ID + " TEXT PRIMARY KEY, "
                + PROFILE_NAME + " TEXT, "
                + PROFILE_DOB + " TEXT, "
                + PROFILE_GENDER + " TEXT, "
                + PROFILE_HEIGHT + " INTEGER, "
                + PROFILE_WEIGHT + " INTEGER)";
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PROFILES);
        onCreate(db);
    }

    @Override
    public boolean isDbExist() {
        return countRecord() > 0;
    }

    @Override
    public int countRecord(){
        SQLiteDatabase db = getReadableDatabase();
        final String query = "SELECT count(*) FROM " + TABLE_PROFILES;
        Cursor cursor = db.rawQuery(query, null);
        int result = cursor.moveToFirst() ? cursor.getInt(0) : 0;
        cursor.close();
        db.close();
        return result;
    }

    public void addProfile(MeasurementProfile profile, String id) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(PROFILE_ID, id);
        values.put(PROFILE_NAME, profile.getName());
        values.put(PROFILE_DOB, profile.getDob());
        values.put(PROFILE_GENDER, profile.getGender());
        values.put(PROFILE_HEIGHT, profile.getHeight());
        values.put(PROFILE_WEIGHT, profile.getWeight());

        db.insert(TABLE_PROFILES, null, values);
        db.close();
    }

    public MeasurementProfile getProfile(String id) {
        Log.d("SILIR", "profil_id = " + id);
        final String[] ALL_COLUMNS = new String[]{PROFILE_ID, PROFILE_NAME, PROFILE_DOB, PROFILE_GENDER, PROFILE_HEIGHT, PROFILE_WEIGHT};

        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_PROFILES, ALL_COLUMNS, IS_KEY_ID_EQUAL, new String[]{id},
                null, null, null, null);
        if (cursor != null) cursor.moveToFirst();

        MeasurementProfile profile = new MeasurementProfile(cursor.getString(1), cursor.getString(2),
                cursor.getString(3), cursor.getInt(4), cursor.getInt(5));
        cursor.close();
        db.close();
        return profile;
    }

    public HashMap<String, MeasurementProfile> getAllProfile() {
        final String query = "SELECT * FROM " + TABLE_PROFILES;
        HashMap<String, MeasurementProfile> hashMap = new HashMap<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        while (cursor.moveToNext()) {
            String key = cursor.getString(0);
            MeasurementProfile profile = new MeasurementProfile(cursor.getString(1), cursor.getString(2),
                    cursor.getString(3), cursor.getInt(4), cursor.getInt(5));
            hashMap.put(key, profile);
        }
        cursor.close();
        db.close();
        return hashMap;
    }

    public void updateProfile(MeasurementProfile profile, String id) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(PROFILE_NAME, profile.getName());
        values.put(PROFILE_DOB, profile.getDob());
        values.put(PROFILE_ID, id);
        values.put(PROFILE_GENDER, profile.getGender());
        values.put(PROFILE_HEIGHT, profile.getHeight());
        values.put(PROFILE_WEIGHT, profile.getWeight());
        db.update(TABLE_PROFILES, values, IS_KEY_ID_EQUAL, new String[]{id});
        db.close();
    }

    public void deleteProfile(String id) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_PROFILES, IS_KEY_ID_EQUAL, new String[]{id});
        db.close();
    }

    public boolean isNonDefaultProfileExist() {
        SQLiteDatabase db = getReadableDatabase();
        final String query = "SELECT count(*) FROM " + TABLE_PROFILES + " WHERE " +
                PROFILE_ID + "!= ?";
        Cursor cursor = db.rawQuery(query, new String[]{User.KEY_IN_LOCAL_DB});
        int result =  cursor.moveToFirst() ? cursor.getInt(0) : 0;
        cursor.close();
        db.close();
        return result > 0;
    }
}
