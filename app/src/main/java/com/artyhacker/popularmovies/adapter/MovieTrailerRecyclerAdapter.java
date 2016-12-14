package com.artyhacker.popularmovies.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.artyhacker.popularmovies.R;
import com.artyhacker.popularmovies.bean.MovieTrailer;

import java.util.ArrayList;

/**
 * Created by dh on 16-12-14.
 */

public class MovieTrailerRecyclerAdapter extends RecyclerView.Adapter<MovieTrailerRecyclerAdapter.MyViewHolder> {

    private Context context;
    private ArrayList<MovieTrailer> movieTrailerArrayList;
    public MovieTrailerRecyclerAdapter(Context context, ArrayList<MovieTrailer> movieTrailers) {
        this.context = context;
        this.movieTrailerArrayList = movieTrailers;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        MyViewHolder holder = new MyViewHolder(LayoutInflater.from(context).inflate(R.layout.list_item_trailer, parent, false));
        return holder;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        MovieTrailer trailer = movieTrailerArrayList.get(position);
        holder.tvName.setText(trailer.name);
        holder.tvType.setText("(" + trailer.type + ")");
        holder.tvSize.setText(trailer.size);
    }

    @Override
    public int getItemCount() {
        return movieTrailerArrayList.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView tvType;
        TextView tvName;
        TextView tvSize;

        public MyViewHolder(View itemView) {
            super(itemView);
            tvName = (TextView) itemView.findViewById(R.id.trailer_name_tv);
            tvSize = (TextView) itemView.findViewById(R.id.trailer_size_tv);
            tvType = (TextView) itemView.findViewById(R.id.trailer_type_tv);
        }
    }

}
