package com.artyhacker.popularmovies;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

public class MovieListActivity extends AppCompatActivity {

    private Context mContext;
    private String mSortType;
    private SharedPreferences prefs;
    private final String MOVIELISTFRAGMENT_TAG = "MLFTAG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_details);
        mContext = this;

        if(savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.activity_main, new MovieListFragment(), MOVIELISTFRAGMENT_TAG)
                    .commit();
        }

        prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        mSortType = prefs.getString(getString(R.string.pref_sortType_key), getString(R.string.pref_sortType_default));
        Log.d("MovieListActivity", "onCreated mSortType: " + mSortType);

    }

    @Override
    protected void onResume() {
        super.onResume();

        String sortType = prefs.getString(getString(R.string.pref_sortType_key), getString(R.string.pref_sortType_default));
        Log.d("MovieListActivity", "Changed before sortType: " + sortType);
        Log.d("MovieListActivity", "Changed before mSortType: " + mSortType);
        if (sortType != null && !sortType.equals(mSortType)) {
            MovieListFragment fragment = (MovieListFragment) getFragmentManager().findFragmentByTag(MOVIELISTFRAGMENT_TAG);
            if (null != fragment) {
                fragment.onSortTypeChanged();
            }
            mSortType = sortType;
        }
        Log.d("MovieListActivity", "Changed after sortType: " + sortType);
        Log.d("MovieListActivity", "Changed after mSortType: " + mSortType);
    }
}
