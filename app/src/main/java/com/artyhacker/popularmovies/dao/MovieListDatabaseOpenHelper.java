package com.artyhacker.popularmovies.dao;

import android.content.Context;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by dh on 16-12-7.
 */

public class MovieListDatabaseOpenHelper extends SQLiteOpenHelper {
    public MovieListDatabaseOpenHelper(Context context) {
        super(context, "moveList", null, 1);
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
        String sql = "create table movielist(_id integer primary key, title varchar(100), image varchar(200), " +
                "overview varchar(500), voteAverage real, releaseDate varchar(20), popularity real)";
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
