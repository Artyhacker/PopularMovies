package com.artyhacker.popularmovies;

import android.app.Fragment;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import com.artyhacker.popularmovies.adapter.MovieListAdapter;
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

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class MovieListFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, AdapterView.OnItemClickListener {

    private static final int REQUEST_SUCCESS = 1;
    private static final int REQUEST_FAIL = 0;
    private String moviesBaseUrl = "";

    private static final int MOVIE_LOADER_ID = 0;

    private ArrayList<MovieBean> movieBeanArray;
    private GridView gridView;

    private MovieListAdapter adapter;

    private static final String[] MOVIE_COLUMNS = {
            MovieContract.MovieEntry.COLUMN_ID,
            MovieContract.MovieEntry.COLUME_TITLE,
            MovieContract.MovieEntry.COLUME_IMAGE,
            MovieContract.MovieEntry.COLUMN_OVERVIEW,
            MovieContract.MovieEntry.COLUMN_VOTE_AVERAGE,
            MovieContract.MovieEntry.COLUMN_RELASE_DATE,
            MovieContract.MovieEntry.COLUMN_POPULARITY
    };

    public static final int COL_MOVIE_ID = 0;
    public static final int COL_MOVIE_TITLE = 1;
    public static final int COL_MOVIE_IMAGE = 2;
    public static final int COL_MOVIE_OVERVIEW = 3;
    public static final int COL_MOVIE_VOTE_AVERAGE = 4;
    public static final int COL_MOVIE_RELASE_DATE = 5;
    public static final int COL_MOVIE_POPULARITY = 6;

    public MovieListFragment() {

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        setHasOptionsMenu(true);
        movieBeanArray = new ArrayList<MovieBean>();
        getMoviesList();
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_popular_movies, container, false);
        gridView = (GridView) rootView.findViewById(R.id.fragment_grid_layout);
        gridView.setOnItemClickListener(this);

        //Loader
        Uri movieForLocationUri = Uri.parse("content://com.artyhacker.popularmovies/movie");
        Cursor cursor = getActivity().getContentResolver().query(movieForLocationUri, null, null, null, null);
        adapter = new MovieListAdapter(getActivity(), cursor, 0, gridView);
        gridView.setAdapter(adapter);
        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_popularmoviesfragment, menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.menu_refresh:
                getMoviesList();
                //getLoaderManager().restartLoader(MOVIE_LOADER_ID, null, this);
                break;
            case R.id.menu_setting:
                Intent intent = new Intent(getActivity(), SettingActivity.class);
                startActivity(intent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void onSortTypeChanged(){
        getMoviesList();
    }

    private void getMoviesList() {
        movieBeanArray = new ArrayList<MovieBean>();
        getMovieListFromNetwork();
    }

    private URL getMovieListUrl(){

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String sortType = prefs.getString(getString(R.string.pref_sortType_key), getString(R.string.pref_sortType_default));
        if ("0".equals(sortType)) {
            moviesBaseUrl = ApiConfig.GET_MOVIES_POPULAR_BASE_URL;
        }
        else if ("1".equals(sortType)) {
            moviesBaseUrl = ApiConfig.GET_MOVIES_TOP_RATED_BASE_URL;
        }
        Uri builtUri = Uri.parse(moviesBaseUrl).buildUpon()
                .appendQueryParameter(ApiConfig.API_KEY_PARAM, ApiConfig.API_KEY)
                .appendQueryParameter(ApiConfig.PAGE_PARAM, "1")
                .appendQueryParameter(ApiConfig.LANGUAGE_PARAM, ApiConfig.LANGUAGE_VALUE_ZH)
                .build();
        URL url = null;
        try {
            url = new URL(builtUri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return url;
    }

    private void getMovieListFromNetwork() {

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                    .url(getMovieListUrl())
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


    @Override
    public void onItemClick(AdapterView adapterView, View view, int position, long id) {

        Cursor cursor = (Cursor) adapterView.getItemAtPosition(position);
        if (cursor != null) {
            Intent intent = new Intent(getActivity(), MovieDetailsActivity.class);
            intent.setData(Uri.parse("content://com.artyhacker.popularmovies/movie/" + cursor.getString(COL_MOVIE_ID)));
            startActivity(intent);
        }

    }

    private void getMoviesListFromJson(String moviesJsonStr) {
        try {
            JSONObject object = new JSONObject(moviesJsonStr);
            JSONArray jsonArray = object.getJSONArray("results");
            for(int i = 0; i < jsonArray.length(); i++) {
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
            new MovieListDaoUtils(getActivity()).saveMovieList(movieBeanArray);
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    getLoaderManager().restartLoader(MOVIE_LOADER_ID, null, MovieListFragment.this);
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        getLoaderManager().initLoader(MOVIE_LOADER_ID, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Uri uri = Uri.parse("content://com.artyhacker.popularmovies/movie");
        CursorLoader loader = new CursorLoader(getActivity(), uri, MOVIE_COLUMNS, null, null, null);
        return loader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        adapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        adapter.swapCursor(null);
    }

}
