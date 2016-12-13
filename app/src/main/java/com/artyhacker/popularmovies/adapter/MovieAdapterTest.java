package com.artyhacker.popularmovies.adapter;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CursorAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.artyhacker.popularmovies.MovieListFragmentLoaderTest;
import com.artyhacker.popularmovies.R;
import com.artyhacker.popularmovies.bean.MovieBean;
import com.artyhacker.popularmovies.common.ApiConfig;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by dh on 16-12-4.
 */

public class MovieAdapterTest extends CursorAdapter {
    //private ArrayList<MovieBean> movieBeanArray;
    //private Context context;
    private GridView gridView;



/*
    @Override
    public int getCount() {
        return movieBeanArray.size();
    }

    @Override
    public Object getItem(int position) {
        return movieBeanArray.get(position);
    }

    @Override
    public long getItemId(int position) {
        return movieBeanArray.get(position).id;
    }

    static class ViewHolder{
        ImageView image;
    }*/

    /*
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if(convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_movie,null);
            holder = new ViewHolder();
            holder.image = (ImageView) convertView.findViewById(R.id.list_item_image);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        convertView.setLayoutParams(new GridView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                gridView.getHeight()/2));
        //ImageView image = (ImageView) convertView.findViewById(R.id.list_item_image);
        MovieBean bean = movieBeanArray.get(position);
        String imageUrl = ApiConfig.IMAGE_BASE_URL + bean.image;
        Picasso.with(context).load(imageUrl).into(holder.image);

        return convertView;
    }*/

    public MovieAdapterTest(Context context, Cursor c, int flags, GridView gridView) {
        super(context, c, flags);
        this.gridView = gridView;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_movie, parent, false);
        view.setLayoutParams(new GridView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                gridView.getHeight()/2));
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ImageView imageView = (ImageView) view;
        //String urlStr = cursor.getString(cursor.getColumnIndex("image"));
        String urlStr = cursor.getString(MovieListFragmentLoaderTest.COL_MOVIE_IMAGE);
        String imageUrl = ApiConfig.IMAGE_BASE_URL + urlStr;
        Picasso.with(context).load(imageUrl).into(imageView);
    }
}
