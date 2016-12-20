package com.artyhacker.popularmovies.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.artyhacker.popularmovies.bean.MovieBean;

/**
 * Created by dh on 16-12-15.
 */

public class MovieCollectDaoUtils {
    private MovieCollectOpenHelper helper;
    public MovieCollectDaoUtils(Context context) {
        helper = new MovieCollectOpenHelper(context);
    }

    public void collectMovie(String id) {
        if(id != null) {
            SQLiteDatabase db = helper.getReadableDatabase();
            ContentValues values = new ContentValues();
            values.put("_id", id);
            values.put("collected", 1);
            db.insert("movieCollect", null, values);

            db.close();
        }
    }

    public void unCollectMovie(String id) {
        if(id != null) {
            SQLiteDatabase db = helper.getReadableDatabase();
            ContentValues values = new ContentValues();
            values.put("collected", 0);
            db.update("movieCollect", values, "_id=?", new String[]{id});
            db.close();
        }
    }

    public boolean isCollected(String id) {
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cursor = db.rawQuery("select * from movieCollect", null);
        if (cursor != null && id != null) {
            while (cursor.moveToNext()) {
                if (id.equals(cursor.getString(0)) && "1".equals(cursor.getString(1))) {
                    return true;
                }
            }
        }
        db.close();
        return false;
    }
}
