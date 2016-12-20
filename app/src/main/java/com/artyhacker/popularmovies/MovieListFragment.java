package com.artyhacker.popularmovies;

import android.app.AlarmManager;
import android.app.Fragment;
import android.app.LoaderManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import com.artyhacker.popularmovies.adapter.MovieCollectAdapter;
import com.artyhacker.popularmovies.adapter.MovieListAdapter;
import com.artyhacker.popularmovies.bean.MovieBean;
import com.artyhacker.popularmovies.common.ApiConfig;
import com.artyhacker.popularmovies.common.MovieContract;
import com.artyhacker.popularmovies.db.MovieCollectDaoUtils;
import com.artyhacker.popularmovies.db.MovieListDaoUtils;
import com.artyhacker.popularmovies.service.MovieService;
import com.artyhacker.popularmovies.sync.FetchMoviesTask;

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

    private String moviesBaseUrl = "";
    private int mPosition = GridView.INVALID_POSITION;
    private static final String SELECTED_KEY = "selected_position";
    private String movieType = "";
    public static final int MOVIE_LOADER_ID = 0;

    private ArrayList<MovieBean> movieBeanArray;
    private GridView gridView;

    private MovieListAdapter mMovieListAdapter;

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

    public interface mCallback {
        public void onItemSelect(Uri movieUri);
    }
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        setHasOptionsMenu(true);
        movieBeanArray = new ArrayList<MovieBean>();

        //startServiceIntent = new Intent("com.artyhacker.popularmovies.LOAD_START");
        //getActivity().startService(startServiceIntent);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mMovieListAdapter = new MovieListAdapter(getActivity(), null, 0);
        View rootView = inflater.inflate(R.layout.fragment_popular_movies, container, false);
        gridView = (GridView) rootView.findViewById(R.id.fragment_grid_layout);
        gridView.setAdapter(mMovieListAdapter);
        gridView.setOnItemClickListener(this);

        if (savedInstanceState != null && savedInstanceState.containsKey(SELECTED_KEY)) {
            mPosition = savedInstanceState.getInt(SELECTED_KEY);
        }

        return rootView;
    }

    public void onSortTypeChanged(){
        getMoviesList();
        getLoaderManager().restartLoader(MOVIE_LOADER_ID, null, this);
    }

    @Override
    public void onItemClick(AdapterView adapterView, View view, int position, long id) {
        Cursor cursor = (Cursor) adapterView.getItemAtPosition(position);
        if (cursor != null) {
            ((mCallback) getActivity())
                    .onItemSelect(Uri.parse("content://com.artyhacker.popularmovies/movie/" + id));
        }
        mPosition = position;
    }


    /**
     * Loader
     */
    private MsgReceiver msgReceiver;
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        getLoaderManager().initLoader(MOVIE_LOADER_ID, null, this);


        msgReceiver = new MsgReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.artyhacker.popularmovies.LOAD_FINISHED");
        getActivity().registerReceiver(msgReceiver, intentFilter);


        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Uri uri = Uri.parse(MovieContract.CONTENT_BASE_URI);
        if (movieType != null) {
            CursorLoader loader = new CursorLoader(getActivity(), uri, MOVIE_COLUMNS,
                    MovieContract.MovieEntry.COLUMN_GET_TYPE + "=?", new String[]{movieType}, null);
            return loader;
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mMovieListAdapter.swapCursor(data);
        if (mPosition != GridView.INVALID_POSITION) {
            gridView.smoothScrollToPosition(mPosition);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mMovieListAdapter.swapCursor(null);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (mPosition != GridView.INVALID_POSITION) {
            outState.putInt(SELECTED_KEY, mPosition);
        }
        super.onSaveInstanceState(outState);
    }


    /**
     * Fetch Movies Task
     */
    //private Intent startServiceIntent;
    private void getMoviesList() {
        //movieBeanArray = new ArrayList<MovieBean>();
        //getMovieListFromNetwork(ApiConfig.getMovieListUrl(getActivity()));

        //new FetchMoviesTask(getActivity()).getMovieListFromNetwork();
        //getLoaderManager().restartLoader(MOVIE_LOADER_ID, null, this);

        /*
        Intent intent = new Intent(getActivity(), MovieService.class);
        intent.putExtra(MovieService.GET_MOVIE_URL_EXTRA, ApiConfig.getMovieListUrl(getActivity()));
        getActivity().startService(intent);
        getLoaderManager().restartLoader(MOVIE_LOADER_ID, null, this);*/

        Intent alarmIntent = new Intent(getActivity(), MovieService.AlarmReceiver.class);
        alarmIntent.putExtra(MovieService.GET_MOVIE_URL_EXTRA, MovieContract.CONTENT_BASE_URI);
        PendingIntent pi = PendingIntent.getBroadcast(getActivity(), 0, alarmIntent, PendingIntent.FLAG_ONE_SHOT);
        AlarmManager am = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);
        am.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 3000, pi);

    }

    @Override
    public void onDestroy() {
        //getActivity().stopService(startServiceIntent);
        getActivity().unregisterReceiver(msgReceiver);
        super.onDestroy();
    }

    public class MsgReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            boolean loadFinishedFlag = intent.getBooleanExtra(MovieService.LOAD_FINISHED_FLAG, false);
            movieType = intent.getStringExtra(MovieService.MOVIE_TYPE_FLAG);
            Log.d("MovieListFragment", "receive broadcast! loadFinished: " + loadFinishedFlag);
            if (loadFinishedFlag) {
                getLoaderManager().restartLoader(MOVIE_LOADER_ID, null, MovieListFragment.this);
            }
        }
    }


    /*
    private void getMovieListFromNetwork(URL url) {

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .build();
        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getActivity(), "请检查网络并刷新", Toast.LENGTH_SHORT).show();
                    }
                });
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
*/

    /**
     * Menu
     * @param menu
     */
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
                startActivity(intent);
                break;
            case R.id.menu_collect:
                onlyCollect();
                break;
            case R.id.menu_all:
                notOnlyCollect();
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * 收藏功能
     */
    private void notOnlyCollect() {
        movieBeanArray = new MovieListDaoUtils(getActivity()).getMovieListfromDB();
        MovieCollectAdapter adapter = new MovieCollectAdapter(getActivity(), movieBeanArray, gridView);
        gridView.setAdapter(adapter);
    }

    private void onlyCollect() {
        MovieCollectDaoUtils movieCollectDaoUtils = new MovieCollectDaoUtils(getActivity());
        movieBeanArray = new MovieListDaoUtils(getActivity()).getMovieListfromDB();
        ArrayList<MovieBean> movieCollectArray = new ArrayList<>();
        for (MovieBean movieBean : movieBeanArray) {
            String id = String.valueOf(movieBean.id);
            if (movieCollectDaoUtils.isCollected(id)) {
                movieCollectArray.add(movieBean);
            }
        }
        MovieCollectAdapter adapter = new MovieCollectAdapter(getActivity(), movieCollectArray, gridView);
        gridView.setAdapter(adapter);
    }
}
