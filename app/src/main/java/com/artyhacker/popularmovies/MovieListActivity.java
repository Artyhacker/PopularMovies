package com.artyhacker.popularmovies;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SyncRequest;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.artyhacker.popularmovies.common.MovieContract;
import com.artyhacker.popularmovies.sync.MovieSyncAdapter;


public class MovieListActivity extends AppCompatActivity implements MovieListFragment.mCallback {

    private Context mContext;
    private String mSortType;
    private SharedPreferences prefs;
    private static final String DETAILFRAGMENT_TAG = "DFTAG";

    public static boolean mTwoPane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_list);
        mContext = this;

        if (findViewById(R.id.movie_detail_container) != null) {
            mTwoPane = true;
            if (savedInstanceState == null) {
                getFragmentManager().beginTransaction()
                        .replace(R.id.movie_detail_container, new DetailFragment(), DETAILFRAGMENT_TAG)
                        .commit();
            }
        } else {
            mTwoPane = false;
            getSupportActionBar().setElevation(0f);
        }

        prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        mSortType = prefs.getString(getString(R.string.pref_sortType_key), getString(R.string.pref_sortType_default));

        MovieSyncAdapter.initializeSyncAdapter(mContext);

    }

    @Override
    protected void onResume() {
        super.onResume();

        /**
         * load MovieList is Popular or TopRates
         */
        String sortType = prefs.getString(getString(R.string.pref_sortType_key), getString(R.string.pref_sortType_default));

        if (sortType != null && !sortType.equals(mSortType)) {
            MovieListFragment movieListFragment = (MovieListFragment) getFragmentManager().findFragmentById(R.id.fragment_movie_list);
            if (null != movieListFragment) {
                movieListFragment.onSortTypeChanged();
            }
            mSortType = sortType;
        }
    }

    @Override
    public void onItemSelect(Uri movieUri) {
        if (mTwoPane) {
            Bundle args = new Bundle();
            args.putParcelable(DetailFragment.DETAIL_URI, movieUri);
            DetailFragment fragment = new DetailFragment();
            fragment.setArguments(args);

            getFragmentManager().beginTransaction()
                    .replace(R.id.movie_detail_container, fragment, DETAILFRAGMENT_TAG)
                    .commit();
        } else {
            Intent intent = new Intent(this, DetailActivity.class);
            intent.setData(movieUri);
            startActivity(intent);
        }
    }

}
