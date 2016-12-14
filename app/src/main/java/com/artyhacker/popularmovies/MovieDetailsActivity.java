package com.artyhacker.popularmovies;

import android.app.Fragment;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.artyhacker.popularmovies.adapter.MovieTrailerAdapter;
import com.artyhacker.popularmovies.adapter.MovieTrailerRecyclerAdapter;
import com.artyhacker.popularmovies.bean.MovieReview;
import com.artyhacker.popularmovies.bean.MovieTrailer;
import com.artyhacker.popularmovies.common.ApiConfig;
import com.artyhacker.popularmovies.common.MovieContract;
import com.artyhacker.popularmovies.ui.UnScrollListView;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

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

        private ArrayList<MovieTrailer> movieTrailerList = new ArrayList<>();
        private ArrayList<MovieReview> movieReviewsList = new ArrayList<>();
        private String movieRuntime = "";
        private MovieTrailerAdapter trailerAdapter;
        //private MovieTrailerRecyclerAdapter trailerAdapter;

        private TextView tvRuntime;
        private UnScrollListView lvTrailers;
        private ListView lvReviews;
        private RecyclerView rvTrailers;

        private Handler handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {

                tvRuntime.setText(movieRuntime + "分钟");
                trailerAdapter = new MovieTrailerAdapter(getActivity(), movieTrailerList);
                lvTrailers.setAdapter(trailerAdapter);
                //trailerAdapter = new MovieTrailerRecyclerAdapter(getActivity(), movieTrailerList);
                //rvTrailers.setAdapter(trailerAdapter);
            }
        };

        public DetailFragment(){
            setHasOptionsMenu(true);
        }

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.fragment_movie_details, container, false);
            tvRuntime = (TextView) view.findViewById(R.id.movie_runtime_tv);
            lvTrailers = (UnScrollListView) view.findViewById(R.id.movie_trailers_lv);
            lvReviews = (ListView) view.findViewById(R.id.movie_reviews_lv);
            //rvTrailers = (RecyclerView) view.findViewById(R.id.movie_trailers_rv);
            //rvTrailers.setLayoutManager(new LinearLayoutManager(getActivity()));

            return view;
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

            String movieDetailsUrlStr = ApiConfig.getMovieDetailsUrl(idString);
            try {
                URL movieDetailsUrl = new URL(movieDetailsUrlStr);
                getMovieDetails(movieDetailsUrl);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }
        @Override
        public void onLoaderReset(Loader<Cursor> loader) {

        }

        private void getMovieDetails(URL movieDetailsUrl) {

            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(movieDetailsUrl)
                    .build();
            Call call = client.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String responseJson = response.body().string();
                    parseMovieDetailsJson(responseJson);
                }
            });
        }

        private void parseMovieDetailsJson(String responseJson) {

            try {
                JSONObject object = new JSONObject(responseJson);
                movieRuntime = object.getString("runtime");

                JSONArray movieTrailers = object.getJSONObject("trailers").getJSONArray("youtube");
                for(int i = 0; i < movieTrailers.length(); i++) {
                    MovieTrailer trailer = new MovieTrailer();
                    JSONObject trailerJson = movieTrailers.getJSONObject(i);
                    trailer.name = trailerJson.getString("name");
                    trailer.size = trailerJson.getString("size");
                    trailer.source = trailerJson.getString("source");
                    trailer.type = trailerJson.getString("type");
                    movieTrailerList.add(trailer);
                }

                JSONArray movieReviews = object.getJSONObject("reviews").getJSONArray("results");
                for(int j = 0; j < movieReviews.length(); j++) {
                    JSONObject reviewJson = movieReviews.getJSONObject(j);
                    MovieReview review = new MovieReview();
                    review.author = reviewJson.getString("author");
                    review.content = reviewJson.getString("content");
                    review.urlStr = reviewJson.getString("url");
                    movieReviewsList.add(review);
                }

                handler.sendEmptyMessage(0);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }




    }
}
