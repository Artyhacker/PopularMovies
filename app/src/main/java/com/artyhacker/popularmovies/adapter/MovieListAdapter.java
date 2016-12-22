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
    private Cursor cursor;

    public MovieListAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
        cursor = c;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {

        View view = LayoutInflater.from(context).inflate(R.layout.grid_item_movies, parent, false);
        ViewHolder holder = new ViewHolder(view);
        view.setTag(holder);
        return view;
    }

    public static class ViewHolder{
        public final ImageView imageView;
        public final TextView tvTitle;
        public final RatingBar rbScore;
        public final TextView tvScore;

        public ViewHolder(View view) {
            imageView = (ImageView) view.findViewById(R.id.item_image_iv);
            tvTitle = (TextView) view.findViewById(R.id.item_title_tv);
            rbScore = (RatingBar)view.findViewById(R.id.item_score_rb);
            tvScore = (TextView) view.findViewById(R.id.item_score_tv);
        }
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder holder = (ViewHolder) view.getTag();

        holder.tvTitle.setText(cursor.getString(MovieListFragment.COL_MOVIE_TITLE));
        holder.tvScore.setText(cursor.getString(MovieListFragment.COL_MOVIE_VOTE_AVERAGE));
        float score = Float.parseFloat(cursor.getString(MovieListFragment.COL_MOVIE_VOTE_AVERAGE));
        holder.rbScore.setRating(score/2);
        String urlStr = cursor.getString(MovieListFragment.COL_MOVIE_IMAGE);
        String imageUrl = ApiConfig.IMAGE_BASE_URL + urlStr;
        Picasso.with(context)
                .load(imageUrl)
                .placeholder(R.drawable.bg_loading)
                .error(R.drawable.bg_error)
                .into(holder.imageView);
    }

}
