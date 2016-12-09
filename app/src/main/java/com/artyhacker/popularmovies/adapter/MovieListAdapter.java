package com.artyhacker.popularmovies.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.artyhacker.popularmovies.BuildConfig;
import com.artyhacker.popularmovies.bean.MovieBean;
import com.artyhacker.popularmovies.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by dh on 16-12-4.
 */

public class MovieListAdapter extends BaseAdapter {
    private ArrayList<MovieBean> movieBeanArray;
    private Context context;
    private GridView gridView;
    public MovieListAdapter(Context context, ArrayList<MovieBean> movieBeanArray, GridView gridView) {
        this.context = context;
        this.movieBeanArray = movieBeanArray;
        this.gridView = gridView;
    }
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
    }

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
        String imageUrl = BuildConfig.BASE_IMAGE_URL + bean.image;
        Picasso.with(context).load(imageUrl).into(holder.image);

        return convertView;
    }
}
