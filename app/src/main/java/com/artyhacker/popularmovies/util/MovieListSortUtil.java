package com.artyhacker.popularmovies.util;

import com.artyhacker.popularmovies.bean.MovieBean;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by dh on 16-12-6.
 */

public class MovieListSortUtil {

    Comparator<MovieBean> movieComparatorByPopularity = new Comparator<MovieBean>() {
        @Override
        public int compare(MovieBean lhs, MovieBean rhs) {
            if(lhs.popularity > rhs.popularity)
                return 1;
            return -1;
        }
    };
    Comparator<MovieBean> movieComparatorByScore = new Comparator<MovieBean>() {
        @Override
        public int compare(MovieBean lhs, MovieBean rhs) {
            if(lhs.voteAverage > rhs.voteAverage)
                return -1;
            return 1;
        }
    };

    public void sortByPopularity(ArrayList<MovieBean> movieBeanArray) {
        Collections.sort(movieBeanArray, movieComparatorByPopularity);
    }

    public void sortByScore(ArrayList<MovieBean> movieBeanArray) {
        Collections.sort(movieBeanArray, movieComparatorByScore);
    }
}
