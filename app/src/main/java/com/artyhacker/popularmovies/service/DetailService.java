package com.artyhacker.popularmovies.service;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import com.artyhacker.popularmovies.bean.MovieReview;
import com.artyhacker.popularmovies.bean.MovieTrailer;
import com.artyhacker.popularmovies.common.ApiConfig;
import com.artyhacker.popularmovies.common.MovieContract;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by dh on 16-12-20.
 */

public class DetailService extends IntentService {
    public static final String MOVIE_ID_EXTRA = "mie";
    private String id = "";
    private String urlStr = "";

    public DetailService(){

        super("DetailService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        id = intent.getStringExtra(MOVIE_ID_EXTRA);
        Log.d("DetailService", "start service! id: " + id);
        if (id != null) {
            urlStr = ApiConfig.getMovieDetailsUrl(id);
            try {
                URL url = new URL(urlStr);
                getMovieDetails(url);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }
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
                Log.d("DetailService", "responseJson: " + responseJson);
                saveMovieDetails(responseJson);
            }
        });
    }

    private void saveMovieDetails(String responseJson) {

        try {
            JSONObject object = new JSONObject(responseJson);
            String movieRuntime = object.getString("runtime");
            JSONArray movieTrailers = object.getJSONObject("trailers").getJSONArray("youtube");
            JSONArray movieReviews = object.getJSONObject("reviews").getJSONArray("results");
            ContentValues values = new ContentValues();
            values.put(MovieContract.MovieEntry.COLUMN_RUNTIME, movieRuntime);
            values.put(MovieContract.MovieEntry.COLUMN_VIDEOS, movieTrailers.toString());
            values.put(MovieContract.MovieEntry.COLUMN_REVIEWS, movieReviews.toString());
            Uri uri = Uri.parse(MovieContract.CONTENT_BASE_URI + "/" + id);
            int updateRows = getContentResolver().update(uri, values, null, null);

            Log.d("DetailService", "updateRows: " + updateRows);
            Intent intent = new Intent("com.artyhacker.popularmovies.DETAIL_LOAD_FINISHED");
            intent.putExtra(MOVIE_ID_EXTRA, id);
            sendBroadcast(intent);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
