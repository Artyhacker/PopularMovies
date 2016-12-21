package com.artyhacker.popularmovies.service;

import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.artyhacker.popularmovies.bean.MovieBean;
import com.artyhacker.popularmovies.common.ApiConfig;
import com.artyhacker.popularmovies.common.MovieContract;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Vector;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by dh on 16-12-19.
 */

public class MovieService extends IntentService {
    public static final String GET_MOVIE_URL_EXTRA = "gmue";
    private ArrayList<MovieBean> movieBeanArray;
    public static String LOAD_FINISHED_FLAG = "lff";
    public static String MOVIE_TYPE_FLAG = "mtf";
    private String movieType = "";
    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     */
    public MovieService(){
        super("MovieService");
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        movieType = ApiConfig.getMovieType(this);
        URL url = ApiConfig.getMovieListUrl(getApplicationContext());
        getMovieListFromNetwork(url);
    }

    public static class AlarmReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Intent sendIntent = new Intent(context, MovieService.class);
            sendIntent.putExtra(MovieService.GET_MOVIE_URL_EXTRA, intent.getStringExtra(MovieService.GET_MOVIE_URL_EXTRA));
            context.startService(sendIntent);
        }
    }

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
                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "请确认网络并刷新", Toast.LENGTH_SHORT).show();
                    }
                });
                Intent intent = new Intent("com.artyhacker.popularmovies.LOAD_FINISHED");
                intent.putExtra(LOAD_FINISHED_FLAG, true);
                intent.putExtra(MOVIE_TYPE_FLAG, movieType);
                sendBroadcast(intent);
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

                ContentResolver resolver = this.getContentResolver();
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

            Intent intent = new Intent("com.artyhacker.popularmovies.LOAD_FINISHED");
            intent.putExtra(LOAD_FINISHED_FLAG, true);
            intent.putExtra(MOVIE_TYPE_FLAG, movieType);
            sendBroadcast(intent);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


}
