package com.artyhacker.popularmovies.adapter;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.artyhacker.popularmovies.R;
import com.artyhacker.popularmovies.bean.MovieTrailer;

import java.net.URL;
import java.util.ArrayList;

/**
 * Created by dh on 16-12-14.
 */

public class MovieTrailerAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<MovieTrailer> movieTrailerArrayList;

    public MovieTrailerAdapter(Context context, ArrayList<MovieTrailer> movieTrailers) {
        this.context = context;
        this.movieTrailerArrayList = movieTrailers;
    }

    @Override
    public int getCount() {
        return movieTrailerArrayList.size();
    }

    @Override
    public Object getItem(int position) {
        return movieTrailerArrayList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    private class ViewHolder{
        TextView tvType;
        TextView tvName;
        TextView tvSize;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.list_item_trailer, parent, false);
            holder = new ViewHolder();
            holder.tvName = (TextView) convertView.findViewById(R.id.trailer_name_tv);
            holder.tvSize = (TextView) convertView.findViewById(R.id.trailer_size_tv);
            holder.tvType = (TextView) convertView.findViewById(R.id.trailer_type_tv);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        MovieTrailer trailer = movieTrailerArrayList.get(position);
        holder.tvName.setText(trailer.name);
        holder.tvType.setText("(" + trailer.type + ")");
        holder.tvSize.setText(trailer.size);

        return convertView;
    }
}
