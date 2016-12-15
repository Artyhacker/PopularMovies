package com.artyhacker.popularmovies.adapter;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.artyhacker.popularmovies.MovieListFragment;
import com.artyhacker.popularmovies.R;
import com.artyhacker.popularmovies.common.ApiConfig;
import com.squareup.picasso.Picasso;

/**
 * Created by dh on 16-12-4.
 */

public class MovieListAdapter extends CursorAdapter {

    private GridView gridView;

    public MovieListAdapter(Context context, Cursor c, int flags, GridView gridView) {
        super(context, c, flags);
        this.gridView = gridView;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.grid_item_movies, parent, false);
        view.setLayoutParams(new GridView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                gridView.getHeight()/2));
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        ImageView imageView = (ImageView) view.findViewById(R.id.item_image_iv);
        TextView tvTitle = (TextView) view.findViewById(R.id.item_title_tv);
        RatingBar rbScore = (RatingBar) view.findViewById(R.id.item_score_rb);
        TextView tvScore = (TextView) view.findViewById(R.id.item_score_tv);

        tvTitle.setText(cursor.getString(MovieListFragment.COL_MOVIE_TITLE));
        tvScore.setText(cursor.getString(MovieListFragment.COL_MOVIE_VOTE_AVERAGE));
        float score = Float.parseFloat(cursor.getString(MovieListFragment.COL_MOVIE_VOTE_AVERAGE));
        rbScore.setRating(score/2);
        String urlStr = cursor.getString(MovieListFragment.COL_MOVIE_IMAGE);
        String imageUrl = ApiConfig.IMAGE_BASE_URL + urlStr;
        Picasso.with(context).load(imageUrl).into(imageView);
    }
}
