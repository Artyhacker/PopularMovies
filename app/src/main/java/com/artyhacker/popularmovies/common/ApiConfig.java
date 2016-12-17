package com.artyhacker.popularmovies.common;

import android.net.Uri;

/**
 * Created by dh on 16-12-9.
 */

public class ApiConfig {
    public static final String API_KEY = "5039bffadb9e7b63e94b5003fc59af10";
    public static final String IMAGE_BASE_URL = "https://image.tmdb.org/t/p/w185";
    public static final String GET_MOVIES_POPULAR_BASE_URL = "https://api.themoviedb.org/3/movie/popular?";
    public static final String GET_MOVIES_TOP_RATED_BASE_URL = "https://api.themoviedb.org/3/movie/top_rated?";
    public static final String API_KEY_PARAM = "api_key";
    public static final String PAGE_PARAM = "page";
    public static final String LANGUAGE_PARAM = "language";
    public static final String LANGUAGE_VALUE_ZH = "zh-cn";

    public static final String PLAY_VIDEO_BASE_URL = "https://www.youtube.com/watch?v=";

    private static final String MOVIE_DETAILS_BASE_URL = "http://api.themoviedb.org/3/movie/";

    public static String getMovieDetailsUrl(String id) {
        return MOVIE_DETAILS_BASE_URL + id + "?append_to_response=trailers,reviews&api_key=" + API_KEY;
    }

    public static String getIdFromUri(Uri uri) {
        return uri.getPathSegments().get(1);
    }

}
