package com.artyhacker.popularmovies;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by dh on 16-12-5.
 */
public class DetailActivity extends AppCompatActivity {
    public static boolean DETAIL_ACITIVTY_IS_STOP;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details_container);
        DETAIL_ACITIVTY_IS_STOP = false;

        if (savedInstanceState == null) {
            Bundle arguments = new Bundle();
            arguments.putParcelable(DetailFragment.DETAIL_URI, getIntent().getData());
            DetailFragment fragment = new DetailFragment();
            fragment.setArguments(arguments);

            getFragmentManager().beginTransaction()
                    .add(R.id.movie_detail_container, fragment)
                    .commit();
        }
    }

    @Override
    protected void onResume() {
        DETAIL_ACITIVTY_IS_STOP = false;
        super.onResume();
    }

    @Override
    protected void onStop() {
        DETAIL_ACITIVTY_IS_STOP = true;
        super.onStop();
    }
}
