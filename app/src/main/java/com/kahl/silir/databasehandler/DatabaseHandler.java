package com.kahl.silir.databasehandler;

import android.content.Context;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.File;

/**
 * Created by Paskahlis Anjas Prabowo on 03/09/2017.
 */

public abstract class DatabaseHandler extends SQLiteOpenHelper {
    protected static final int DATABASE_VERSION = 1;

    protected DatabaseHandler(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    protected DatabaseHandler(Context context, String name, SQLiteDatabase.CursorFactory factory, int version, DatabaseErrorHandler errorHandler) {
        super(context, name, factory, version, errorHandler);
    }

    public abstract boolean isDbExist();
    public abstract int countRecord();
}
