package com.artyhacker.popularmovies.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.artyhacker.popularmovies.common.MovieContract;
import com.artyhacker.popularmovies.bean.MovieBean;

import java.util.ArrayList;

/**
 * Created by dh on 16-12-7.
 */

public class MovieListDaoUtils {
    private MovieListDatabaseOpenHelper helper;
    public MovieListDaoUtils(Context context) {
        helper = new MovieListDatabaseOpenHelper(context);
    }

    public void saveMovieList(ArrayList<MovieBean> arrayList) {
        SQLiteDatabase db = helper.getReadableDatabase();
        deleteDatebase();
        for(MovieBean bean: arrayList) {
            ContentValues values = new ContentValues();
            values.put(MovieContract.MovieEntry.COLUMN_ID, bean.id);
            values.put(MovieContract.MovieEntry.COLUME_TITLE, bean.title);
            values.put(MovieContract.MovieEntry.COLUME_IMAGE, bean.image);
            values.put(MovieContract.MovieEntry.COLUMN_OVERVIEW, bean.overview);
            values.put(MovieContract.MovieEntry.COLUMN_VOTE_AVERAGE, bean.voteAverage);
            values.put(MovieContract.MovieEntry.COLUMN_RELASE_DATE, bean.releaseDate);
            values.put(MovieContract.MovieEntry.COLUMN_POPULARITY, bean.popularity);
            values.put(MovieContract.MovieEntry.COLUMN_GET_TYPE, bean.getType);

            db.insert(MovieContract.MovieEntry.TABLE_NAME, null, values);
        }
        db.close();
    }
    public ArrayList<MovieBean> getMovieListfromDB() {
        ArrayList<MovieBean> arrayList = new ArrayList<>();
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cursor = db.rawQuery("select * from movieList", null);
        if(cursor != null && cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                int id = cursor.getInt(0);
                String title = cursor.getString(1);
                String image = cursor.getString(2);
                String overview = cursor.getString(3);
                double voteAverage = cursor.getDouble(4);
                String releaseDate = cursor.getString(5);
                double popularity = cursor.getDouble(6);
                String getType = cursor.getString(7);

                MovieBean bean = new MovieBean();
                bean.id = id;
                bean.title = title;
                bean.image = image;
                bean.overview = overview;
                bean.voteAverage = voteAverage;
                bean.releaseDate = releaseDate;
                bean.popularity = popularity;
                bean.getType = getType;

                arrayList.add(bean);
            }
        }
        db.close();
        return arrayList;
    }

    public void deleteDatebase() {
        SQLiteDatabase db = helper.getReadableDatabase();
        db.delete(MovieContract.MovieEntry.TABLE_NAME, null, null);
    }

}
