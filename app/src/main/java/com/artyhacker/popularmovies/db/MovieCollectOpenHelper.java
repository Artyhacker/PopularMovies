package com.artyhacker.popularmovies.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by dh on 16-12-15.
 */

public class MovieCollectOpenHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;

    public MovieCollectOpenHelper(Context context) {
        super(context, "movieCollect.db", null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        String sql = "CREATE TABLE " + "movieCollect" + " (" +
                "_id" + " INTEGER NOT NULL, " +
                "collected" + " Integer NOT NULL " +
                ");";
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop table if exists movieCollect");
        onCreate(db);
    }
}
