package com.artyhacker.popularmovies;

import android.app.Fragment;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.artyhacker.popularmovies.common.ApiConfig;
import com.artyhacker.popularmovies.common.MovieContract;
import com.squareup.picasso.Picasso;

/**
 * Created by dh on 16-12-5.
 */
public class MovieDetailsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
    }

    public static class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

        private String movieDetailsUrl = "";
        private String movieVideosUrl = "";
        private String movieReviewUrl = "";

        private static final String[] MOVIE_COLUMNS = {
                MovieContract.MovieEntry.COLUMN_ID,
                MovieContract.MovieEntry.COLUME_TITLE,
                MovieContract.MovieEntry.COLUME_IMAGE,
                MovieContract.MovieEntry.COLUMN_OVERVIEW,
                MovieContract.MovieEntry.COLUMN_VOTE_AVERAGE,
                MovieContract.MovieEntry.COLUMN_RELASE_DATE,
                MovieContract.MovieEntry.COLUMN_POPULARITY
        };

        private static final int COL_MOVIE_ID = 0;
        private static final int COL_MOVIE_TITLE = 1;
        private static final int COL_MOVIE_IMAGE = 2;
        private static final int COL_MOVIE_OVERVIEW = 3;
        private static final int COL_MOVIE_VOTE_AVERAGE = 4;
        private static final int COL_MOVIE_RELASE_DATE = 5;
        private static final int COL_MOVIE_POPULARITY = 6;

        public DetailFragment(){
            setHasOptionsMenu(true);
        }

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_movie_details, container, false);
        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            getLoaderManager().initLoader(0, null, this);
            super.onActivityCreated(savedInstanceState);
        }

        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            Intent intent = getActivity().getIntent();
            if (intent == null) {
                return null;
            }
            return new CursorLoader(getActivity(), intent.getData(), MOVIE_COLUMNS, null, null, null);
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            if (!data.moveToFirst()) {
                return;
            }
            String idString = data.getString(COL_MOVIE_ID);
            String titleString = data.getString(COL_MOVIE_TITLE);
            String imageString = data.getString(COL_MOVIE_IMAGE);
            String overviewString = data.getString(COL_MOVIE_OVERVIEW);
            String voteString = data.getString(COL_MOVIE_VOTE_AVERAGE);
            String dateString = data.getString(COL_MOVIE_RELASE_DATE);
            String popularString = data.getString(COL_MOVIE_POPULARITY);

            ImageView movieImage = (ImageView) getView().findViewById(R.id.movie_image_iv);
            TextView movieTitle = (TextView) getView().findViewById(R.id.movie_title_tv);
            TextView movieDate = (TextView) getView().findViewById(R.id.movie_date_tv);
            TextView movieScore = (TextView) getView().findViewById(R.id.movie_score_tv);
            TextView movieOverview = (TextView) getView().findViewById(R.id.movie_content_tv);
            RatingBar movieRatingBar = (RatingBar) getView().findViewById(R.id.movie_score_rb);
            movieRatingBar.setRating(Float.parseFloat(voteString)/2);

            movieTitle.setText(titleString);
            movieDate.setText(dateString);
            movieScore.setText(voteString);
            movieOverview.setText(overviewString);
            Picasso.with(getActivity())
                    .load(ApiConfig.IMAGE_BASE_URL + imageString)
                    .into(movieImage);


            Log.d("MovieDetails", "id: " + idString);

            movieDetailsUrl = ApiConfig.getMovieDetailsUrl(idString);
            movieVideosUrl = ApiConfig.getMovieVideosUrl(idString);
            movieReviewUrl = ApiConfig.getMovieReviewsUrl(idString);
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {

        }
    }
}
