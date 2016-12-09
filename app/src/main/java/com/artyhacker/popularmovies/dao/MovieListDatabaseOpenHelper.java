package com.artyhacker.popularmovies.dao;

import android.content.Context;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.artyhacker.popularmovies.MovieContract;

/**
 * Created by dh on 16-12-7.
 */

public class MovieListDatabaseOpenHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;
    static final String DATABASE_NAME = "moveList";

    public MovieListDatabaseOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * public String image;
     public String title;
     public int id;
     public String overview;
     public double voteAverage;
     public String releaseDate;
     public double popularity;
     * @param db
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        //String sql1 = "create table movielist(_id integer primary key, title varchar(100), image varchar(200), " +
               // "overview varchar(500), voteAverage real, releaseDate varchar(20), popularity real)";

        String sql = "CREATE TABLE " + MovieContract.TABLE_NAME + " (" +
                MovieContract.COLUMN_ID + " INTEGER PRIMARY KEY, " +
                MovieContract.COLUME_TITLE + " TEXT NOT NULL, " +
                MovieContract.COLUME_IMAGE + " TEXT NOT NULL, " +
                MovieContract.COLUMN_OVERVIEW + " TEXT NOT NULL, " +
                MovieContract.COLUMN_VOTE_AVERAGE + " REAL NOT NULL, " +
                MovieContract.COLUMN_RELASE_DATE + " TEXT NOT NULL, " +
                MovieContract.COLUMN_POPULARITY + " REAL NOT NULL " +
                ");";
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
