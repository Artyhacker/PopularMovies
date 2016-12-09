package com.artyhacker.popularmovies.bean;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by dh on 16-12-4.
 */

public class MovieBean implements Parcelable{
    public int id;
    public String title;
    public String image;
    public String overview;
    public String releaseDate;
    public double voteAverage;
    public double popularity;

    protected MovieBean(Parcel in) {
        id = in.readInt();
        title = in.readString();
        image = in.readString();
        overview = in.readString();
        releaseDate = in.readString();
        voteAverage = in.readDouble();
        popularity = in.readDouble();
    }

    public MovieBean(){}

    public static final Creator<MovieBean> CREATOR = new Creator<MovieBean>() {
        @Override
        public MovieBean createFromParcel(Parcel in) {
            return new MovieBean(in);
        }

        @Override
        public MovieBean[] newArray(int size) {
            return new MovieBean[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(title);
        dest.writeString(image);
        dest.writeString(overview);
        dest.writeString(releaseDate);
        dest.writeDouble(voteAverage);
        dest.writeDouble(popularity);
    }
}
