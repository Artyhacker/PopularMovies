package com.artyhacker.popularmovies.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.artyhacker.popularmovies.R;
import com.artyhacker.popularmovies.bean.MovieReview;

import java.util.ArrayList;

/**
 * Created by dh on 16-12-15.
 */

public class MovieReviewAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<MovieReview> movieReviewArrayList;

    public MovieReviewAdapter(Context context,  ArrayList<MovieReview> movieReviews) {
        this.context = context;
        this.movieReviewArrayList = movieReviews;
    }
    @Override
    public int getCount() {
        return movieReviewArrayList.size();
    }

    @Override
    public Object getItem(int position) {
        return movieReviewArrayList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    private class ViewHolder {
        TextView tvReviewAuthor;
        TextView tvReviewContent;
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.list_item_review, parent, false);
            holder = new ViewHolder();
            holder.tvReviewAuthor = (TextView) convertView.findViewById(R.id.review_author_tv);
            holder.tvReviewContent = (TextView) convertView.findViewById(R.id.review_content_tv);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        MovieReview review = movieReviewArrayList.get(position);
        holder.tvReviewAuthor.setText(review.author);
        holder.tvReviewContent.setText(review.content);
        return convertView;
    }
}
