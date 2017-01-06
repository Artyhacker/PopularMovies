package com.artyhacker.popularmovies.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SyncRequest;
import android.content.SyncResult;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.text.TextUtils;
import android.util.Log;

import com.artyhacker.popularmovies.DetailActivity;
import com.artyhacker.popularmovies.R;
import com.artyhacker.popularmovies.bean.MovieBean;
import com.artyhacker.popularmovies.common.ApiConfig;
import com.artyhacker.popularmovies.common.MovieContract;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Vector;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static android.content.Context.NOTIFICATION_SERVICE;

/**
 * Created by dh on 16-12-24.
 */

public class MovieSyncAdapter extends AbstractThreadedSyncAdapter {

    public static String LOAD_FINISHED_FLAG = "lff";
    public static String MOVIE_TYPE_FLAG = "mtf";
    private static String NOTIFY_SP_NAME = "nsn";
    private static String LAST_ID_KEY = "lik";
    private ContentResolver resolver;
    private static String movieType;
    private ArrayList<MovieBean> movieBeanArray;

    public MovieSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
    }


    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        movieBeanArray = new ArrayList<>();
        resolver = getContext().getContentResolver();
        String typeExtras = extras.getString("movieType");
        if (typeExtras != null) {
            try {
                URL url = new URL(extras.getString("movieUrl"));
                movieType = typeExtras;
                getMovieListFromNetwork(url);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        } else {
            movieType = ApiConfig.getMovieType(getContext());
            URL url = ApiConfig.getMovieListUrl(getContext());
            getMovieListFromNetwork(url);
        }
    }

    @Override
    public void onSyncCanceled() {
        super.onSyncCanceled();
    }

    public static void syncImmediately(Context context, Bundle bundle) {
        ContentResolver.requestSync(getSyncAccount(context),
                context.getString(R.string.content_authority), bundle);
    }

    public static Account getSyncAccount(Context context) {
        AccountManager accountManager =
                (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);
        Account newAccount = new Account(
                context.getString(R.string.app_name), context.getString(R.string.sync_account_type));
        if ( null == accountManager.getPassword(newAccount) ) {
            if (!accountManager.addAccountExplicitly(newAccount, "", null)) {
                return null;
            }
            onAccountCreated(newAccount, context);
        }
        return newAccount;
    }

    public static void configurePeriodicSync(Context context) {
        SharedPreferences sp = getDefaultSharedPreferencesMultiprocess(context);
        long pollFrequency = Long.parseLong(sp.getString(context.getString(R.string.pref_pollFrequency_key),
                context.getString(R.string.pref_pollFrequency_default)));
        long syncInterval = 60 * 60 * pollFrequency;
        long flexTime = syncInterval/3;
        Account account = getSyncAccount(context);
        String authority = context.getString(R.string.content_authority);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            SyncRequest request = new SyncRequest.Builder().
                    syncPeriodic(syncInterval, flexTime).
                    setSyncAdapter(account, authority).
                    setExtras(new Bundle()).build();
            ContentResolver.requestSync(request);
        } else {
            ContentResolver.addPeriodicSync(account,
                    authority, new Bundle(), syncInterval);
        }
    }

    private static void onAccountCreated(Account newAccount, Context context) {

        MovieSyncAdapter.configurePeriodicSync(context);
        ContentResolver.setSyncAutomatically(newAccount, context.getString(R.string.content_authority), true);

        Bundle settingsBundle = new Bundle();
        settingsBundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        settingsBundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        syncImmediately(context, settingsBundle);
    }

    public static void initializeSyncAdapter(Context context) {
        getSyncAccount(context);
    }


    /**
     * getMovieList
     */

    private void getMovieListFromNetwork(URL url) {
        movieBeanArray = new ArrayList<>();
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .build();
        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String reponseJson = response.body().string();
                getMoviesListFromJson(reponseJson);
            }
        });
    }

    private void getMoviesListFromJson(String moviesJsonStr) {
        try {
            JSONObject object = new JSONObject(moviesJsonStr);
            JSONArray jsonArray = object.getJSONArray("results");
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonMovie = jsonArray.getJSONObject(i);
                int id = jsonMovie.getInt("id");
                String title = jsonMovie.getString("title");
                String image = jsonMovie.getString("poster_path");
                String overview = jsonMovie.getString("overview");
                double voteAverage = jsonMovie.getDouble("vote_average");
                String releaseDate = jsonMovie.getString("release_date");
                double popularity = jsonMovie.getDouble("popularity");
                MovieBean bean = new MovieBean();
                bean.id = id;
                bean.title = title;
                bean.image = image;
                bean.overview = overview;
                bean.voteAverage = voteAverage;
                bean.releaseDate = releaseDate;
                bean.popularity = popularity;

                if (MovieContract.MovieEntry.GET_TYPE_VALUE_POP.equals(movieType)) {
                    bean.getType = MovieContract.MovieEntry.GET_TYPE_VALUE_POP;
                } else {
                    bean.getType = MovieContract.MovieEntry.GET_TYPE_VALUE_TOP;
                }

                movieBeanArray.add(bean);
            }

            Vector<ContentValues> cVVector = new Vector<>(movieBeanArray.size());
            for(int j = 0; j < movieBeanArray.size(); j++) {
                MovieBean bean = movieBeanArray.get(j);
                ContentValues values = new ContentValues();
                values.put(MovieContract.MovieEntry.COLUMN_ID, bean.id);
                values.put(MovieContract.MovieEntry.COLUME_TITLE, bean.title);
                values.put(MovieContract.MovieEntry.COLUME_IMAGE, bean.image);
                values.put(MovieContract.MovieEntry.COLUMN_OVERVIEW, bean.overview);
                values.put(MovieContract.MovieEntry.COLUMN_VOTE_AVERAGE, bean.voteAverage);
                values.put(MovieContract.MovieEntry.COLUMN_RELASE_DATE, bean.releaseDate);
                values.put(MovieContract.MovieEntry.COLUMN_POPULARITY, bean.popularity);
                values.put(MovieContract.MovieEntry.COLUMN_GET_TYPE, bean.getType);
                cVVector.add(values);
            }
            if (cVVector.size() > 0) {
                ContentValues[] cvArray = new ContentValues[cVVector.size()];
                cVVector.toArray(cvArray);
                Uri uri = Uri.parse(MovieContract.CONTENT_BASE_URI);

                int deleteRows = 0;
                if (MovieContract.MovieEntry.GET_TYPE_VALUE_POP.equals(movieType)) {
                    deleteRows = resolver.delete(uri, MovieContract.MovieEntry.COLUMN_GET_TYPE + "=?",
                            new String[]{MovieContract.MovieEntry.GET_TYPE_VALUE_POP});

                } else if (MovieContract.MovieEntry.GET_TYPE_VALUE_TOP.equals(movieType)) {
                    deleteRows = resolver.delete(uri, MovieContract.MovieEntry.COLUMN_GET_TYPE + "=?",
                            new String[]{MovieContract.MovieEntry.GET_TYPE_VALUE_TOP});
                }
                Log.d("MovieService", "delete " + deleteRows + " rows");
                resolver.bulkInsert(uri, cvArray);
            }
            Log.d("MovieService", "MovieService Complete. " + cVVector.size() + " Inserted");

            getMoviesDetails();

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    /**
     * getMoviesDetails
     */
    private void getMoviesDetails() {

        Cursor cursor = resolver.query(Uri.parse(MovieContract.CONTENT_BASE_URI), new String[]{"_id"},
                null, null, null);

        if (cursor != null) {
            while (cursor.moveToNext()) {
                String id = cursor.getString(0);
                String urlStr = ApiConfig.getMovieDetailsUrl(id);
                try {
                    URL url = new URL(urlStr);
                    getMovieDetails(url, id);
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
            }
            Intent intent = new Intent("com.artyhacker.popularmovies.LOAD_FINISHED");
            intent.putExtra(LOAD_FINISHED_FLAG, true);
            intent.putExtra(MOVIE_TYPE_FLAG, movieType);
            getContext().sendBroadcast(intent);
            cursor.close();
        }

        checkNotify(getContext());
    }

    private void getMovieDetails(URL movieDetailsUrl, final String movieId) {

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
                //Log.d("DetailService", "responseJson: " + responseJson);
                saveMovieDetails(responseJson, movieId);
            }
        });
    }

    private void saveMovieDetails(String responseJson, String movieId) {

        if (TextUtils.isEmpty(responseJson) || TextUtils.isEmpty(movieId)) {
            return;
        }

        try {
            JSONObject object = new JSONObject(responseJson);
            ContentValues values = new ContentValues();

            String movieRuntime = "";
            JSONArray movieTrailers = null;
            JSONArray movieReviews = null;

            if (object.has("runtime")) {
                movieRuntime = object.getString("runtime");
            }
            if (object.has("trailers") && object.getJSONObject("trailers").has("youtube")) {
                movieTrailers = object.getJSONObject("trailers").getJSONArray("youtube");
            }
            if (object.has("reviews") && object.getJSONObject("reviews").has("results")) {
                movieReviews = object.getJSONObject("reviews").getJSONArray("results");
            }

            values.put(MovieContract.MovieEntry.COLUMN_RUNTIME, movieRuntime);
            values.put(MovieContract.MovieEntry.COLUMN_VIDEOS, movieTrailers.toString());
            values.put(MovieContract.MovieEntry.COLUMN_REVIEWS, movieReviews.toString());
            Uri uri = Uri.parse(MovieContract.CONTENT_BASE_URI + "/" + movieId);
            int updateRows = getContext().getContentResolver().update(uri, values, null, null);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    /**
     * notify
     */
    private void checkNotify(Context context) {
        SharedPreferences prefs = getDefaultSharedPreferencesMultiprocess(context);
        String prefNotifyKey = context.getString(R.string.pref_notify_key);
        boolean notifyIsOpen = prefs.getBoolean(prefNotifyKey, Boolean.parseBoolean(context.getString(R.string.pref_notify_default)));
        if (notifyIsOpen) {
            checkPopFirstChanged(context);
        }
    }

    private void checkPopFirstChanged(Context context) {

        Cursor cursor = resolver.query(Uri.parse(MovieContract.CONTENT_BASE_URI), null, "getType=?",
                new String[]{MovieContract.MovieEntry.GET_TYPE_VALUE_POP}, null);
        if (cursor.moveToFirst()) {
            String newId = cursor.getString(cursor.getColumnIndex("_id"));
            String title = cursor.getString(cursor.getColumnIndex("title"));

            SharedPreferences sp = context.getSharedPreferences(NOTIFY_SP_NAME, Context.MODE_PRIVATE);
            String lastId = sp.getString(LAST_ID_KEY, newId);
            if (!lastId.equals(newId)) {
                sp.edit().putString(LAST_ID_KEY, newId).apply();
                notifyMovie(context, newId, title);
            }
            cursor.close();
        }
    }

    private void notifyMovie(Context context, String id, String title) {

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
                .setAutoCancel(true)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("最热电影抢先看！")
                .setContentText(title);
        Intent resultIntent = new Intent(context, DetailActivity.class);
        resultIntent.setData(Uri.parse(MovieContract.CONTENT_BASE_URI + "/" + id));

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addParentStack(DetailActivity.class);
        stackBuilder.addNextIntent(resultIntent);

        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        mNotificationManager.notify(1, mBuilder.build());
    }


    /**
     * 修复在SyncAdapter中获取不到参数的更新值问题
     */
    public static SharedPreferences getDefaultSharedPreferencesMultiprocess(Context context) {
        return context.getSharedPreferences(context.getPackageName() + "_preferences",
                Context.MODE_PRIVATE | Context.MODE_MULTI_PROCESS);
    }



}
