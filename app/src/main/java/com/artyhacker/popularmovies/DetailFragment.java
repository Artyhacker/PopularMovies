package com.artyhacker.popularmovies;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
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
import com.artyhacker.popularmovies.service.DetailService;
import com.artyhacker.popularmovies.ui.UnScrollListView;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by dh on 16-12-16.
 */

public class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, View.OnClickListener {

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

            if(!DetailActivity.DETAIL_ACITIVTY_IS_STOP) {  //avoid NullPoint Error
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
        Bundle arguments = getArguments();
        if (arguments != null) {
            mUri = arguments.getParcelable(DetailFragment.DETAIL_URI);
        }

        View view = inflater.inflate(R.layout.fragment_movie_details, container, false);
        tvRuntime = (TextView) view.findViewById(R.id.movie_runtime_tv);
        lvTrailers = (UnScrollListView) view.findViewById(R.id.movie_trailers_lv);
        lvReviews = (UnScrollListView) view.findViewById(R.id.movie_reviews_lv);
        btnCollect = (Button) view.findViewById(R.id.movie_collect_btn);

        btnCollect.setOnClickListener(this);


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

    /**
     * Loader
     */
    private DetailMsgReceiver detailMsgReceiver;
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(DETAIL_LOADER, null, this);
        collectOpenHelper = new MovieCollectOpenHelper(getActivity());
        collectDaoUtils = new MovieCollectDaoUtils(getActivity());

        detailMsgReceiver = new DetailMsgReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.artyhacker.popularmovies.DETAIL_LOAD_FINISHED");
        getActivity().registerReceiver(detailMsgReceiver, intentFilter);

        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onDestroy() {
        getActivity().stopService(detailServiceIntent);
        getActivity().unregisterReceiver(detailMsgReceiver);
        super.onDestroy();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (null != mUri) {
            return new CursorLoader(getActivity(), mUri, MOVIE_COLUMNS, null, null, null);
        }
        return null;
    }

    private Intent detailServiceIntent;
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (!data.moveToFirst()) {
            return;
        }
        id = data.getString(COL_MOVIE_ID);

        /**
         * start DetailService
         */
        detailServiceIntent = new Intent(getActivity(), DetailService.class);
        detailServiceIntent.putExtra(DetailService.MOVIE_ID_EXTRA, id);
        getActivity().startService(detailServiceIntent);

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
                .placeholder(R.drawable.bg_loading)
                .error(R.drawable.bg_error)
                .into(movieImage);

        if (collectDaoUtils.isCollected(id)) {
            btnCollect.setText("Cancel Collect");
        } else {
            btnCollect.setText("Collect");
        }

        Log.d("MovieDetails", "id: " + id);

        /*
        String movieDetailsUrlStr = ApiConfig.getMovieDetailsUrl(id);
        try {
            URL movieDetailsUrl = new URL(movieDetailsUrlStr);
            getMovieDetails(movieDetailsUrl);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }*/
    }
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    /**
     * FetchMovieDetailTask
     */
    /*
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
    }*/
/*
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
    }*/


    /**
     * onClick
     */
    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.movie_collect_btn) {
            if (collectDaoUtils.isCollected(id)) {
                collectDaoUtils.unCollectMovie(id);
                btnCollect.setText("Collect");
            } else {
                collectDaoUtils.collectMovie(id);
                btnCollect.setText("Cancel Collect");
            }
        }
    }

    public class DetailMsgReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String receiveId = intent.getStringExtra(DetailService.MOVIE_ID_EXTRA);
            getVideosAndReviews(receiveId);
        }
    }

    private void getVideosAndReviews(final String receiveId) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Uri uri = Uri.parse(MovieContract.CONTENT_BASE_URI + "/" + receiveId);
                ContentResolver resolver = getActivity().getContentResolver();
                Cursor cursor = resolver.query(uri,
                        new String[]{MovieContract.MovieEntry.COLUMN_RUNTIME,
                                MovieContract.MovieEntry.COLUMN_VIDEOS,
                                MovieContract.MovieEntry.COLUMN_REVIEWS}, null, null, null);
                if (cursor != null) {
                    cursor.moveToFirst();
                    movieRuntime = cursor.getString(cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_RUNTIME));
                    String videosStr = cursor.getString(cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_VIDEOS));
                    String reviewsStr = cursor.getString(cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_REVIEWS));

                    try {
                        JSONArray videosJson = new JSONArray(videosStr);
                        for(int i = 0; i < videosJson.length(); i++) {
                            MovieTrailer trailer = new MovieTrailer();
                            JSONObject trailerJson = videosJson.getJSONObject(i);
                            trailer.name = trailerJson.getString("name");
                            trailer.size = trailerJson.getString("size");
                            trailer.source = trailerJson.getString("source");
                            trailer.type = trailerJson.getString("type");
                            movieTrailerList.add(trailer);
                        }

                        JSONArray reviewsJson = new JSONArray(reviewsStr);
                        for(int j = 0; j < reviewsJson.length(); j++) {
                            JSONObject reviewJson = reviewsJson.getJSONObject(j);
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
        }).start();
    }


}
