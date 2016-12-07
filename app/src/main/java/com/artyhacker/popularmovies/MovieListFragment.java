package com.artyhacker.popularmovies;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
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
import com.artyhacker.popularmovies.dao.MovieListDaoUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;


public class MovieListFragment extends Fragment implements AdapterView.OnItemClickListener {

    private static final String MOVIES_BASE_URL_POPULAR = "https://api.themoviedb.org/3/movie/popular?";
    private static final String MOVIES_BASE_URL_TOP_RATED = "https://api.themoviedb.org/3/movie/top_rated?";
    private String moviesBaseUrl = "";

    private ArrayList<MovieBean> movieBeanArray;
    private GridView gridView;
    private MovieListAdapter adapter;
    private MovieListDaoUtils movieListDaoUtils;

    public MovieListFragment() {

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        setHasOptionsMenu(true);
        movieBeanArray = new ArrayList<MovieBean>();
        movieListDaoUtils = new MovieListDaoUtils(getContext());

        getMoviesList();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_popular_movies, container, false);
        gridView = (GridView) rootView.findViewById(R.id.fragment_grid_layout);
        gridView.setOnItemClickListener(this);
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
                break;
            case R.id.menu_setting:
                Intent intent = new Intent(getActivity(), SettingActivity.class);
                startActivityForResult(intent, 0);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (resultCode) {
            case 1:
                Log.d("RESULT", "resultCode: " + requestCode);
                getMoviesList();
                break;
            default:
                break;
        }
    }

    public void getMoviesList() {
        movieListDaoUtils.deleteDatebase();
        movieBeanArray = new ArrayList<MovieBean>();
        new RefreshMoviesTask().execute();
        movieListDaoUtils.saveMovieList(movieBeanArray);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        MovieBean movie = movieBeanArray.get(position);
        Intent intent = new Intent(getActivity(), MovieDetailsActivity.class);
        intent.putExtra("title", movie.title);
        intent.putExtra("image", movie.image);
        intent.putExtra("date", movie.releaseDate);
        intent.putExtra("score", movie.voteAverage);
        intent.putExtra("content", movie.overview);
        startActivity(intent);
    }

    public class RefreshMoviesTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            HttpURLConnection connection = null;
            BufferedReader reader = null;
            String moviesJsonStr = "";

            final String API_KEY_PARAM = "api_key";
            final String PAGE_PARAM = "page";
            final String LANGUAGE_PARAM = "language";



            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
            String sortType = prefs.getString(getString(R.string.pref_sortType_key), getString(R.string.pref_sortType_default));
            if ("0".equals(sortType)) {
                moviesBaseUrl = MOVIES_BASE_URL_POPULAR;
            }
            else if ("1".equals(sortType)) {
                moviesBaseUrl = MOVIES_BASE_URL_TOP_RATED;
            }
            Uri builtUri = Uri.parse(moviesBaseUrl).buildUpon()
                    .appendQueryParameter(API_KEY_PARAM, BuildConfig.API_KEY)
                    .appendQueryParameter(PAGE_PARAM, "1")
                    .appendQueryParameter(LANGUAGE_PARAM, "zh-cn")
                    .build();

            try {
                URL url = new URL(builtUri.toString());
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setReadTimeout(5000);
                connection.setConnectTimeout(5000);
                InputStream is = connection.getInputStream();
                String strLine = "";
                reader = new BufferedReader(new InputStreamReader(is));
                StringBuilder builder = new StringBuilder();
                while((strLine = reader.readLine()) != null) {
                    builder.append(strLine);
                }
                moviesJsonStr = builder.toString();

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if(reader != null)
                    try {
                        reader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                if(connection != null)
                    connection.disconnect();
            }
            if(!moviesJsonStr.isEmpty()) {
                getMoviesListFromJson(moviesJsonStr);
            }
            return null;
        }


        @Override
        protected void onPostExecute(Void aVoid) {
            /*
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
            String sortType = prefs.getString(getString(R.string.pref_sortType_key), getString(R.string.pref_sortType_default));
            if ("0".equals(sortType)) {
                new MovieListSortUtil().sortByPopularity(movieBeanArray);
            }
            else if ("1".equals(sortType)) {
                new MovieListSortUtil().sortByScore(movieBeanArray);
            }
            */
            adapter = new MovieListAdapter(getActivity(), movieBeanArray, gridView);
            gridView.setAdapter(adapter);

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

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);

        if (savedInstanceState != null) {
            movieBeanArray = new ArrayList<MovieBean>();
            movieBeanArray = movieListDaoUtils.getMovieListfromDB();
            new RefreshMoviesTask().execute();
        }
    }
}
