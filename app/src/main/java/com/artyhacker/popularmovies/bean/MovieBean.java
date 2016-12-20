package com.artyhacker.popularmovies.bean;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by dh on 16-12-4.
 */

public class MovieBean {
    public int id;
    public String title;
    public String image;
    public String overview;
    public String releaseDate;
    public double voteAverage;
    public double popularity;
    public String getType;

    public int runtime;
    public String videosJson;
    public String reviewsJson;

    public MovieBean(){}

}
