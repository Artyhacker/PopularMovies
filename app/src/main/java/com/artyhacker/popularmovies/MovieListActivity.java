package com.artyhacker.popularmovies;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.NotificationManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SyncRequest;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.artyhacker.popularmovies.common.ApiConfig;
import com.artyhacker.popularmovies.common.MovieContract;


public class MovieListActivity extends AppCompatActivity implements MovieListFragment.mCallback {

    private Context mContext;
    private String mSortType;
    private SharedPreferences prefs;
    private static final String DETAILFRAGMENT_TAG = "DFTAG";
    private static long pollFrequency = 1;


    public static Account mAccount;

    public static boolean mTwoPane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_list);
        mContext = this;

        if (findViewById(R.id.movie_detail_container) != null) {
            mTwoPane = true;
            Toast.makeText(this, "选择电影条目查看详细信息", Toast.LENGTH_LONG).show();
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




        /**
         * add Account
         */
        mAccount = createSyncAccount(this);

    }

    private Account createSyncAccount(Context context) {
        Account newAccount = new Account(context.getString(R.string.app_name), "artyhacker.com");
        AccountManager accountManager = (AccountManager) context.getSystemService(ACCOUNT_SERVICE);
        if (!accountManager.addAccountExplicitly(newAccount, null, null)) {
            return null;
        }
        return newAccount;
    }

    @Override
    protected void onResume() {
        super.onResume();

        /**
         * load MovieList is Popular or TopRates
         */
        String sortType = prefs.getString(getString(R.string.pref_sortType_key), getString(R.string.pref_sortType_default));
        Log.d("MovieListActivity", "movieType: " + sortType + " - " + ApiConfig.getMovieType(mContext));

        if (sortType != null && !sortType.equals(mSortType)) {
            MovieListFragment movieListFragment = (MovieListFragment) getFragmentManager().findFragmentById(R.id.fragment_movie_list);
            if (null != movieListFragment) {
                movieListFragment.onSortTypeChanged();
            }
            mSortType = sortType;
        }

        /**
         * notify switch
         */
        boolean notifyIsOpen = prefs.getBoolean(getString(R.string.pref_notify_key), true);
        if (!notifyIsOpen) {
            NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            manager.cancel(1);
        }


        /**
         * load Poll Frequency
         */
        pollFrequency = Long.parseLong(prefs.getString(getString(R.string.pref_pollFrequency_key),
                getString(R.string.pref_pollFrequency_default)));
        long pollFrequencyWithSecond = 60*60*pollFrequency;
        AccountManager accountManager = (AccountManager) getSystemService(ACCOUNT_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.GET_ACCOUNTS) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(mContext, "请在'设置-账户'中打开同步功能！", Toast.LENGTH_LONG).show();
            return;
        }
        Account account = accountManager.getAccountsByType("artyhacker.com")[0];
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            SyncRequest request = new SyncRequest.Builder()
                    .syncPeriodic(pollFrequencyWithSecond, pollFrequencyWithSecond / 3)
                    .setSyncAdapter(account, MovieContract.CONTENT_AUTHORITY)
                    .setExtras(Bundle.EMPTY).build();
            ContentResolver.requestSync(request);
        } else {
            ContentResolver.addPeriodicSync(account, MovieContract.CONTENT_AUTHORITY, Bundle.EMPTY,
                    pollFrequencyWithSecond);
        }
        ContentResolver.setSyncAutomatically(account, MovieContract.CONTENT_AUTHORITY, true);
        Log.d("MovieListActivity", "同步频率： " + pollFrequency + " 小时");

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
