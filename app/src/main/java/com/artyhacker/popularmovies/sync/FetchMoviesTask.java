package com.artyhacker.popularmovies.sync;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Message;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.artyhacker.popularmovies.MovieListActivity;
import com.artyhacker.popularmovies.MovieListFragment;
import com.artyhacker.popularmovies.R;
import com.artyhacker.popularmovies.bean.MovieBean;
import com.artyhacker.popularmovies.common.ApiConfig;
import com.artyhacker.popularmovies.common.MovieContract;
import com.artyhacker.popularmovies.db.MovieListDaoUtils;

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

/**
 * Created by dh on 16-12-19.
 */

public class FetchMoviesTask{
    private Context context;
    private ArrayList<MovieBean> movieBeanArray;

    public FetchMoviesTask(Context context) {
        this.context = context;
        movieBeanArray = new ArrayList<>();
    }

    public void getMovieListFromNetwork(URL url) {
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
                movieBeanArray = getMoviesListFromJson(reponseJson);
                new MovieListDaoUtils(context).saveMovieList(movieBeanArray);
            }
        });
    }

    private ArrayList<MovieBean> getMoviesListFromJson(String moviesJsonStr) {
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
                movieBeanArray.add(bean);
            }

            Vector<ContentValues> cVVector = new Vector<>(movieBeanArray.size());
            for(int j = 0; j < movieBeanArray.size(); j++) {
                MovieBean bean = movieBeanArray.get(j);
                ContentValues values = new ContentValues();
                values.put(MovieContract.MovieEntry.COLUMN_ID, bean.id);
                values.put(MovieContract.MovieEntry.COLUME_TITLE, bean.title);
                values.put(MovieContract.MovieEntry.COLUMN_OVERVIEW, bean.overview);
                values.put(MovieContract.MovieEntry.COLUMN_VOTE_AVERAGE, bean.voteAverage);
                values.put(MovieContract.MovieEntry.COLUMN_RELASE_DATE, bean.releaseDate);
                values.put(MovieContract.MovieEntry.COLUMN_POPULARITY, bean.popularity);
                cVVector.add(values);
            }
            if (cVVector.size() > 0) {
                ContentValues[] cvArray = new ContentValues[cVVector.size()];
                cVVector.toArray(cvArray);
                context.getContentResolver().bulkInsert(ApiConfig.getMovieListUri(context), cvArray);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
}
