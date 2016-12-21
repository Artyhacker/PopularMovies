package com.artyhacker.popularmovies;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
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
            MovieContract.MovieEntry.COLUMN_POPULARITY,
            MovieContract.MovieEntry.COLUMN_GET_TYPE,
            MovieContract.MovieEntry.COLUMN_RUNTIME,
            MovieContract.MovieEntry.COLUMN_VIDEOS,
            MovieContract.MovieEntry.COLUMN_REVIEWS
    };

    private static final int COL_MOVIE_ID = 0;
    private static final int COL_MOVIE_TITLE = 1;
    private static final int COL_MOVIE_IMAGE = 2;
    private static final int COL_MOVIE_OVERVIEW = 3;
    private static final int COL_MOVIE_VOTE_AVERAGE = 4;
    private static final int COL_MOVIE_RELEASE_DATE = 5;
    private static final int COL_MOVIE_POPULARITY = 6;
    private static final int COL_MOVIE_GET_TYPE = 7;
    private static final int COL_MOVIE_RUNTIME = 8;
    private static final int COL_MOVIE_VIDEOS = 9;
    private static final int COL_MOVIE_REVIEWS = 10;


    private ArrayList<MovieTrailer> movieTrailerList = new ArrayList<>();
    private ArrayList<MovieReview> movieReviewsList = new ArrayList<>();
    private String movieRuntime = "";
    private MovieTrailerAdapter trailerAdapter;
    private MovieReviewAdapter reviewAdapter;
    private String id = "";
    private Uri favoriteUri;
    private Uri contentUri;

    public static final String DETAIL_URI = "URI";
    private Uri mUri;
    private static final int DETAIL_LOADER = 0;
    private TextView tvRuntime;
    private UnScrollListView lvTrailers;
    private UnScrollListView lvReviews;
    private Button btnFavorite;
    private boolean isFavorite;
    private ContentResolver resolver;

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {

            if(!DetailActivity.DETAIL_ACITIVTY_IS_STOP) {
                if (null != movieRuntime) {
                    tvRuntime.setText(getActivity().getString(R.string.format_runtime, movieRuntime));
                }
                trailerAdapter = new MovieTrailerAdapter(getActivity(), movieTrailerList);
                lvTrailers.setAdapter(trailerAdapter);
                reviewAdapter = new MovieReviewAdapter(getActivity(), movieReviewsList);
                lvReviews.setAdapter(reviewAdapter);
            }
        }
    };

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
        btnFavorite = (Button) view.findViewById(R.id.movie_collect_btn);

        btnFavorite.setOnClickListener(this);


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

        resolver = getActivity().getContentResolver();

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
        contentUri = Uri.parse(MovieContract.CONTENT_BASE_URI + "/" + id);
        favoriteUri = Uri.parse(MovieContract.CONTENT_FAVORITE_BASE_URI + "/" + id);
        isFavorite = isFavorite(id);
        Log.d("DetailFragment", "isFavorite? " + isFavorite);
        if (isFavorite) {
            btnFavorite.setText(R.string.btn_text_unfavorite);
        } else {
            btnFavorite.setText(R.string.btn_text_favorite);
        }

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
        String dateString = data.getString(COL_MOVIE_RELEASE_DATE);
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




        Log.d("MovieDetails", "id: " + id);

    }
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }




    /**
     * onClick
     */
    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.movie_collect_btn) {
            if (isFavorite) {
                resolver.delete(favoriteUri, null, null);
                btnFavorite.setText(R.string.btn_text_favorite);
            } else {
                insertFavorite(resolver, contentUri, favoriteUri);
                btnFavorite.setText(R.string.btn_text_unfavorite);
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
                        if(null != videosStr) {
                            JSONArray videosJson = new JSONArray(videosStr);
                            for (int i = 0; i < videosJson.length(); i++) {
                                MovieTrailer trailer = new MovieTrailer();
                                JSONObject trailerJson = videosJson.getJSONObject(i);
                                trailer.name = trailerJson.getString("name");
                                trailer.size = trailerJson.getString("size");
                                trailer.source = trailerJson.getString("source");
                                trailer.type = trailerJson.getString("type");
                                movieTrailerList.add(trailer);
                            }
                        }

                        if(null != reviewsStr) {
                            JSONArray reviewsJson = new JSONArray(reviewsStr);
                            for (int j = 0; j < reviewsJson.length(); j++) {
                                JSONObject reviewJson = reviewsJson.getJSONObject(j);
                                MovieReview review = new MovieReview();
                                review.author = reviewJson.getString("author");
                                review.content = reviewJson.getString("content");
                                review.urlStr = reviewJson.getString("url");
                                movieReviewsList.add(review);
                            }
                        }

                        handler.sendEmptyMessage(0);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }


    public boolean isFavorite(String id) {
        Uri uri = Uri.parse(MovieContract.CONTENT_FAVORITE_BASE_URI + "/" + id);
        Cursor cursor = getActivity().getContentResolver().query(uri, null, null, null, null);
        Log.d("DetailFragment", "cursor count: " + cursor.getCount());
        return (cursor.getCount() != 0);
    }

    private void insertFavorite(final ContentResolver resolver, final Uri contentUri, final Uri favoriteUri) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Cursor cursor = resolver.query(contentUri, null, null, null, null);
                if (cursor != null) {
                    cursor.moveToFirst();
                    ContentValues values = new ContentValues();
                    values.put(MOVIE_COLUMNS[0], cursor.getString(COL_MOVIE_ID));
                    values.put(MOVIE_COLUMNS[1], cursor.getString(COL_MOVIE_TITLE));
                    values.put(MOVIE_COLUMNS[2], cursor.getString(COL_MOVIE_IMAGE));
                    values.put(MOVIE_COLUMNS[3], cursor.getString(COL_MOVIE_OVERVIEW));
                    values.put(MOVIE_COLUMNS[4], cursor.getString(COL_MOVIE_VOTE_AVERAGE));
                    values.put(MOVIE_COLUMNS[5], cursor.getString(COL_MOVIE_RELEASE_DATE));
                    values.put(MOVIE_COLUMNS[6], cursor.getString(COL_MOVIE_POPULARITY));
                    values.put(MOVIE_COLUMNS[7], cursor.getString(COL_MOVIE_GET_TYPE));
                    values.put(MOVIE_COLUMNS[8], cursor.getString(COL_MOVIE_RUNTIME));
                    values.put(MOVIE_COLUMNS[9], cursor.getString(COL_MOVIE_VIDEOS));
                    values.put(MOVIE_COLUMNS[10],cursor.getString(COL_MOVIE_REVIEWS));
                    resolver.insert(favoriteUri, values);
                }
            }
        }).start();
    }
}
