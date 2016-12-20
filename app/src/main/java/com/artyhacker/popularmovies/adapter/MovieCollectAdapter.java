package com.artyhacker.popularmovies.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.artyhacker.popularmovies.MovieListFragment;
import com.artyhacker.popularmovies.R;
import com.artyhacker.popularmovies.bean.MovieBean;
import com.artyhacker.popularmovies.common.ApiConfig;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by dh on 16-12-15.
 */

public class MovieCollectAdapter extends BaseAdapter {
    private GridView gridView;
    private Context context;
    private ArrayList<MovieBean> movieBeanArrayList;

    public MovieCollectAdapter(Context context, ArrayList<MovieBean> movieBeanArrayList, GridView gridView) {
        this.context = context;
        this.movieBeanArrayList = movieBeanArrayList;
        this.gridView = gridView;
    }

    @Override
    public int getCount() {
        return movieBeanArrayList.size();
    }

    @Override
    public Object getItem(int position) {
        return movieBeanArrayList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return movieBeanArrayList.get(position).id;
    }

    private class ViewHolder{
        ImageView imageView;
        TextView tvTitle;
        RatingBar rbScore;
        TextView tvScore;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.grid_item_movies, parent, false);
            //convertView.setLayoutParams(new GridView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    //gridView.getHeight()/2));
            holder = new ViewHolder();
            holder.imageView = (ImageView) convertView.findViewById(R.id.item_image_iv);
            holder.tvTitle = (TextView) convertView.findViewById(R.id.item_title_tv);
            holder.rbScore = (RatingBar)convertView.findViewById(R.id.item_score_rb);
            holder.tvScore = (TextView) convertView.findViewById(R.id.item_score_tv);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        MovieBean movie = movieBeanArrayList.get(position);
        holder.tvTitle.setText(movie.title);
        holder.tvScore.setText(String.valueOf(movie.voteAverage));
        float score = (float)movie.voteAverage;
        holder.rbScore.setRating(score/2);
        String urlStr = movie.image;
        String imageUrl = ApiConfig.IMAGE_BASE_URL + urlStr;
        Picasso.with(context).load(imageUrl)
                .placeholder(R.drawable.bg_loading)
                .error(R.drawable.bg_error)
                .into(holder.imageView);

        return convertView;
    }
}
