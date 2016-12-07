package com.artyhacker.popularmovies.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

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

    /**
     * "_id integer primary key, title varchar(100), image varchar(200), " +
     "overview varchar(500), voteAverage real, releaseDate varchar(20), popularity real";
     * @param arrayList
     */
    public void saveMovieList(ArrayList<MovieBean> arrayList) {
        SQLiteDatabase db = helper.getReadableDatabase();
        for(MovieBean bean: arrayList) {
            ContentValues values = new ContentValues();
            values.put("_id", bean.id);
            values.put("title", bean.title);
            values.put("image", bean.image);
            values.put("overview", bean.overview);
            values.put("voteAverage", bean.voteAverage);
            values.put("releaseDate", bean.releaseDate);
            values.put("popularity", bean.popularity);
            db.insert("movieList", null, values);
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

                MovieBean bean = new MovieBean();
                bean.id = id;
                bean.title = title;
                bean.image = image;
                bean.overview = overview;
                bean.voteAverage = voteAverage;
                bean.releaseDate = releaseDate;
                bean.popularity = popularity;
                arrayList.add(bean);
            }
        }
        db.close();
        return arrayList;
    }

    public void deleteDatebase() {
        SQLiteDatabase db = helper.getReadableDatabase();
        db.delete("movieList", null, null);
        db.close();
    }

}
