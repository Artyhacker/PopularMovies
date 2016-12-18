package com.artyhacker.popularmovies;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.artyhacker.popularmovies.adapter.MovieReviewAdapter;
import com.artyhacker.popularmovies.adapter.MovieTrailerAdapter;
import com.artyhacker.popularmovies.bean.MovieReview;
import com.artyhacker.popularmovies.bean.MovieTrailer;
import com.artyhacker.popularmovies.common.ApiConfig;
import com.artyhacker.popularmovies.common.MovieContract;
import com.artyhacker.popularmovies.db.MovieCollectDaoUtils;
import com.artyhacker.popularmovies.db.MovieCollectOpenHelper;
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
 * Created by dh on 16-12-16.
 */

public class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

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
    private MovieReviewAdapter reviewAdapter;
    private MovieCollectOpenHelper collectOpenHelper;
    private MovieCollectDaoUtils collectDaoUtils;
    private String id;

    public static final String DETAIL_URI = "URI";
    private Uri mUri;
    private static final int DETAIL_LOADER = 0;
    private TextView tvRuntime;
    private UnScrollListView lvTrailers;
    private UnScrollListView lvReviews;
    private Button btnCollect;

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {

            if(!DetailActivity.DETAIL_ACITIVTY_FINISHED) {  //avoid NullPoint Error
                //tvRuntime.setText(movieRuntime + "分钟");
                tvRuntime.setText(getActivity().getString(R.string.format_runtime, movieRuntime));
                trailerAdapter = new MovieTrailerAdapter(getActivity(), movieTrailerList);
                lvTrailers.setAdapter(trailerAdapter);
                reviewAdapter = new MovieReviewAdapter(getActivity(), movieReviewsList);
                lvReviews.setAdapter(reviewAdapter);
            }
        }
    };

    public static DetailFragment newInstance(int index) {
        DetailFragment f = new DetailFragment();
        Bundle args = new Bundle();
        args.putInt("index", index);
        f.setArguments(args);
        return f;
    }

    public int getShownIndex() {
        return getArguments().getInt("index", 0);
    }

    public DetailFragment(){
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        /*if (container == null) {
            return null;
        }*/
        Bundle arguments = getArguments();
        if (arguments != null) {
            mUri = arguments.getParcelable(DetailFragment.DETAIL_URI);
        }

        View view = inflater.inflate(R.layout.fragment_movie_details, container, false);
        tvRuntime = (TextView) view.findViewById(R.id.movie_runtime_tv);
        lvTrailers = (UnScrollListView) view.findViewById(R.id.movie_trailers_lv);
        lvReviews = (UnScrollListView) view.findViewById(R.id.movie_reviews_lv);
        btnCollect = (Button) view.findViewById(R.id.movie_collect_btn);

        btnCollect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (collectDaoUtils.isCollected(id)) {
                    collectDaoUtils.unCollectMovie(id);
                    btnCollect.setText("Collect");
                } else {
                    collectDaoUtils.collectMovie(id);
                    btnCollect.setText("Cancel Collect");
                }
            }
        });

        lvTrailers.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Uri playUri = Uri.parse(ApiConfig.PLAY_VIDEO_BASE_URL + movieTrailerList.get(position).source);
                Intent intent = new Intent(Intent.ACTION_VIEW, playUri);
                startActivity(intent);
            }
        });

        lvReviews.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //Uri reviewUri = Uri.parse(movieReviewsList.get(position).urlStr);
                //Intent intent = new Intent(Intent.ACTION_VIEW, reviewUri);
                //startActivity(intent);
                View dialogView = View.inflate(getActivity(), R.layout.dialog_review_details, null);
                TextView tvDialogContent = (TextView) dialogView.findViewById(R.id.dialog_content_tv);
                tvDialogContent.setText(movieReviewsList.get(position).content);
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                        .setTitle("Review Details")
                        .setCancelable(true)
                        .setView(dialogView)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                builder.show();
            }
        });

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(DETAIL_LOADER, null, this);
        collectOpenHelper = new MovieCollectOpenHelper(getActivity());
        collectDaoUtils = new MovieCollectDaoUtils(getActivity());
        super.onActivityCreated(savedInstanceState);
    }
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        //Intent intent = getActivity().getIntent();
        //if (intent == null || intent.getData() == null) {
        //    return null;
        //}
        //return new CursorLoader(getActivity(), intent.getData(), MOVIE_COLUMNS, null, null, null);
        if (null != mUri) {
            return new CursorLoader(getActivity(), mUri, MOVIE_COLUMNS, null, null, null);
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (!data.moveToFirst()) {
            return;
        }
        id = data.getString(COL_MOVIE_ID);
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

        if (collectDaoUtils.isCollected(id)) {
            btnCollect.setText("Cancel Collect");
        } else {
            btnCollect.setText("Collect");
        }

        Log.d("MovieDetails", "id: " + id);

        String movieDetailsUrlStr = ApiConfig.getMovieDetailsUrl(id);
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
