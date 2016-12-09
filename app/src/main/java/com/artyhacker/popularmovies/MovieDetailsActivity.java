package com.artyhacker.popularmovies;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by dh on 16-12-5.
 */
public class MovieDetailsActivity extends AppCompatActivity {
    private static final String BASE_IMAGE_URL = "https://image.tmdb.org/t/p/w185";

    @BindView(R.id.movie_image_iv) ImageView movieImage;
    @BindView(R.id.movie_title_tv) TextView movieTitle;
    @BindView(R.id.movie_date_tv) TextView movieDate;
    @BindView(R.id.movie_score_tv) TextView movieScore;
    @BindView(R.id.movie_content_tv) TextView movieOverview;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_details);
        ButterKnife.bind(this);

        Intent intent = getIntent();
        movieTitle.setText(intent.getStringExtra("title"));
        movieDate.setText(intent.getStringExtra("date"));
        movieScore.setText(intent.getDoubleExtra("score", 0) + "/10");
        movieOverview.setText(intent.getStringExtra("content"));
        Picasso.with(this)
                .load(BASE_IMAGE_URL + intent.getStringExtra("image"))
                .into(movieImage);
    }
}
