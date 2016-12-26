package com.artyhacker.popularmovies;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.Fragment;
import android.app.LoaderManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.content.SharedPreferences;
import android.content.SyncRequest;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
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

import com.artyhacker.popularmovies.adapter.MovieListAdapter;
import com.artyhacker.popularmovies.bean.MovieBean;
import com.artyhacker.popularmovies.common.ApiConfig;
import com.artyhacker.popularmovies.common.MovieContract;
import com.artyhacker.popularmovies.sync.MovieSyncAdapter;

import java.util.ArrayList;


public class MovieListFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, AdapterView.OnItemClickListener {

    private int mPosition = GridView.INVALID_POSITION;
    private static final String SELECTED_KEY = "selected_position";
    private static String movieType = "";
    public static final int MOVIE_LOADER_ID = 0;
    public static boolean isFavoriteList = false;

    public static final String IS_FIRST_SWICH = "ifs"; //标记是否第一次切换POP/TOP


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
            MovieContract.MovieEntry.COLUMN_POPULARITY,
            MovieContract.MovieEntry.COLUMN_GET_TYPE,
            MovieContract.MovieEntry.COLUMN_RUNTIME,
            MovieContract.MovieEntry.COLUMN_VIDEOS,
            MovieContract.MovieEntry.COLUMN_REVIEWS
    };

    public static final int COL_MOVIE_ID = 0;
    public static final int COL_MOVIE_TITLE = 1;
    public static final int COL_MOVIE_IMAGE = 2;
    public static final int COL_MOVIE_OVERVIEW = 3;
    public static final int COL_MOVIE_VOTE_AVERAGE = 4;
    public static final int COL_MOVIE_RELEASE_DATE = 5;
    public static final int COL_MOVIE_POPULARITY = 6;
    public static final int COL_MOVIE_GET_TYPE = 7;
    public static final int COL_MOVIE_RUNTIME = 8;
    public static final int COL_MOVIE_VIDEOS = 9;
    public static final int COL_MOVIE_REVIEWS = 10;

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
        getMovieListInLocal();
        return rootView;
    }

    private void getMovieListInLocal() {
        movieType = ApiConfig.getMovieType(getActivity());
        getLoaderManager().restartLoader(MOVIE_LOADER_ID, null, this);
    }


    @Override
    public void onItemClick(AdapterView adapterView, View view, int position, long id) {
        Cursor cursor = (Cursor) adapterView.getItemAtPosition(position);
        if (cursor != null) {
            ((mCallback) getActivity())
                    .onItemSelect(Uri.parse(MovieContract.CONTENT_BASE_URI + "/" + id));
        }
        mPosition = position;
    }


    private void getMoviesList() {

        /*requestSync*/
        Bundle settingsBundle = new Bundle();
        settingsBundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        settingsBundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);

        settingsBundle.putString("movieType", ApiConfig.getMovieType(getActivity()));
        settingsBundle.putString("movieUrl", ApiConfig.getMovieListUrl(getActivity()).toString());

        ContentResolver.requestSync(MovieListActivity.mAccount, MovieContract.CONTENT_AUTHORITY, settingsBundle);
    }

    public void onSortTypeChanged(){
        mPosition = 0;
        SharedPreferences sp = getActivity().getSharedPreferences(IS_FIRST_SWICH, Context.MODE_PRIVATE);
        boolean isFirstSwitch = sp.getBoolean(IS_FIRST_SWICH, true);
        if (isFirstSwitch) {
            sp.edit().putBoolean(IS_FIRST_SWICH, false).apply();
            getMoviesList();
            Log.d("MovieListFragment", "第一次切换");
        } else {
            getMovieListInLocal();
            Log.d("MovieListFragment", "不是第一次切换");
        }
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

        if (isFavoriteList) {
            Uri uri = Uri.parse(MovieContract.CONTENT_FAVORITE_BASE_URI);
            CursorLoader loader = new CursorLoader(getActivity(), uri, MOVIE_COLUMNS, null, null, null);
            return loader;
        } else {
            Uri uri = Uri.parse(MovieContract.CONTENT_BASE_URI);
            if (movieType != null) {
                CursorLoader loader = new CursorLoader(getActivity(), uri, MOVIE_COLUMNS,
                        MovieContract.MovieEntry.COLUMN_GET_TYPE + "=?", new String[]{movieType}, null);
                return loader;
            }
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


    /**
     * Receiver
     */
    public class MsgReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            boolean loadFinishedFlag = intent.getBooleanExtra(MovieSyncAdapter.LOAD_FINISHED_FLAG, false);
            movieType = intent.getStringExtra(MovieSyncAdapter.MOVIE_TYPE_FLAG);
            Log.d("MovieListFragment", "receive broadcast! loadFinished: " + loadFinishedFlag + " - movieType: " + movieType);
            Log.d("MovieListFragment", "Actrally movieType: " + ApiConfig.getMovieType(getActivity()));
            if (loadFinishedFlag) {
                getLoaderManager().restartLoader(MOVIE_LOADER_ID, null, MovieListFragment.this);
            }
        }
    }

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
            /*
            case R.id.menu_refresh:
                getMoviesList();
                break;
                */
            case R.id.menu_setting:
                Intent intent = new Intent(getActivity(), SettingActivity.class);
                startActivity(intent);
                break;
            case R.id.menu_collect:
                onlyFavorite();
                break;
            case R.id.menu_all:
                notOnlyFavorite();
        }
        return super.onOptionsItemSelected(item);
    }



    /**
     * 收藏功能
     */

    private void onlyFavorite() {
        isFavoriteList = true;
        getLoaderManager().restartLoader(MOVIE_LOADER_ID, null, this);
    }
    private void notOnlyFavorite() {
        isFavoriteList = false;
        getLoaderManager().restartLoader(MOVIE_LOADER_ID, null, this);
    }



    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (mPosition != GridView.INVALID_POSITION) {
            outState.putInt(SELECTED_KEY, mPosition);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onDestroy() {
        getActivity().unregisterReceiver(msgReceiver);
        super.onDestroy();
    }
}
