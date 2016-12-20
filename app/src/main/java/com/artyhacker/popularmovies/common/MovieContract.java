package com.artyhacker.popularmovies.common;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by dh on 16-12-8.
 */

public class MovieContract implements BaseColumns {

    public static final String CONTENT_AUTHORITY = "com.artyhacker.popularmovies";
    public static final String CONTENT_BASE_URI = "content://com.artyhacker.popularmovies/movie";

    public static final class MovieEntry implements BaseColumns{

        public static final String DATABASE_NAME = "movie.db";
        public static final String TABLE_NAME = "movieList";
        public static final String COLUMN_ID = "_id";
        public static final String COLUME_TITLE = "title";
        public static final String COLUME_IMAGE = "image";
        public static final String COLUMN_VOTE_AVERAGE = "voteAverage";
        public static final String COLUMN_POPULARITY = "popularity";
        public static final String COLUMN_RELASE_DATE = "releaseDate";
        public static final String COLUMN_OVERVIEW = "overview";

        public static final String COLUMN_GET_TYPE = "getType";
        public static final String GET_TYPE_VALUE_POP = "pop";
        public static final String GET_TYPE_VALUE_TOP = "top";

        public static final String COLUMN_RUNTIME = "runtime";
        public static final String COLUMN_VIDEOS = "videos";
        public static final String COLUMN_REVIEWS = "reviews";


    }
}
