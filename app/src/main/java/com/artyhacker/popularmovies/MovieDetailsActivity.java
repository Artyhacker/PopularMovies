package com.artyhacker.popularmovies;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

/**
 * Created by dh on 16-12-5.
 */
public class MovieDetailsActivity extends AppCompatActivity {
    private static final String BASE_IMAGE_URL = "https://image.tmdb.org/t/p/w185";
    private ImageView movieImage;
    private TextView movieTitle;
    private TextView movieDate;
    private TextView movieScore;
    private TextView movieContent;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_details);
        Intent intent = getIntent();
        String title = intent.getStringExtra("title");
        String image = BASE_IMAGE_URL + intent.getStringExtra("image");
        String date = intent.getStringExtra("date");
        double score = intent.getDoubleExtra("score", 0);
        String content = intent.getStringExtra("content");

        movieImage = (ImageView) findViewById(R.id.movie_image_iv);
        movieTitle = (TextView) findViewById(R.id.movie_title_tv);
        movieDate = (TextView) findViewById(R.id.movie_date_tv);
        movieScore = (TextView) findViewById(R.id.movie_score_tv);
        movieContent = (TextView) findViewById(R.id.movie_content_tv);

        movieTitle.setText(title);
        movieDate.setText(date);
        movieScore.setText(score + "/10");
        movieContent.setText(content);
        Picasso.with(this).load(image).into(movieImage);
    }
}
