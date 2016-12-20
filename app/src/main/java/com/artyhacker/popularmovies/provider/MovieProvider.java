package com.artyhacker.popularmovies.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import com.artyhacker.popularmovies.common.MovieContract;
import com.artyhacker.popularmovies.db.MovieListDatabaseOpenHelper;

public class MovieProvider extends ContentProvider {

    public static final int MOVIE_DIR = 0;
    public static final int MOVIE_ITEM = 1;
    private static UriMatcher uriMatcher;
    private MovieListDatabaseOpenHelper dbHelper;

    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(MovieContract.CONTENT_AUTHORITY, "movie", MOVIE_DIR);
        uriMatcher.addURI(MovieContract.CONTENT_AUTHORITY, "movie/#", MOVIE_ITEM);
    }

    public MovieProvider() {
    }



    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        int deletedRows = 0;
        if(null == selection) selection = "1";
        switch (uriMatcher.match(uri)) {
            case MOVIE_DIR:
                deletedRows = db.delete(MovieContract.MovieEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case MOVIE_ITEM:
                String movieId = uri.getPathSegments().get(1);
                deletedRows = db.delete(MovieContract.MovieEntry.TABLE_NAME, "id=?", new String[]{movieId});
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (deletedRows != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return deletedRows;
    }

    @Override
    public String getType(Uri uri) {
        switch (uriMatcher.match(uri)) {
            case MOVIE_DIR:
                return "vnd.android.cursor.dir/vnd.com.artyhacker.popularmovies.movie";
            case MOVIE_ITEM:
                return "vnd.android.cursor.item/vnd.com.artyhacker.popularmovies.movie";
        }
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Uri uriReturn = null;
        switch (uriMatcher.match(uri)) {
            case MOVIE_DIR:
            case MOVIE_ITEM:
                long newMovieId = db.insert(MovieContract.MovieEntry.TABLE_NAME, null, values);
                uriReturn = Uri.parse("content://" + MovieContract.CONTENT_AUTHORITY + "/movie/" + newMovieId);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        db.close();
        return uriReturn;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        final SQLiteDatabase db = dbHelper.getReadableDatabase();
        final int match = uriMatcher.match(uri);
        switch (match) {
            case MOVIE_DIR:
                db.beginTransaction();
                int resultCount = 0;
                try{
                    //db.delete(MovieContract.MovieEntry.TABLE_NAME, null, null);
                    for (ContentValues value : values) {
                        long _id = db.insert(MovieContract.MovieEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            resultCount ++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri,null);
                return resultCount;
            default:
                return super.bulkInsert(uri, values);
        }
    }

    @Override
    public boolean onCreate() {
        dbHelper = new MovieListDatabaseOpenHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = null;
        switch (uriMatcher.match(uri)) {
            case MOVIE_DIR:
                cursor = db.query(MovieContract.MovieEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case MOVIE_ITEM:
                String movieId = uri.getPathSegments().get(1);
                cursor = db.query(MovieContract.MovieEntry.TABLE_NAME, projection, "_id=?", new String[]{movieId},
                        null, null, sortOrder);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        return cursor;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        int updateRows = 0;
        switch (uriMatcher.match(uri)) {
            case MOVIE_DIR:
                updateRows = db.update(MovieContract.MovieEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            case MOVIE_ITEM:
                String movieId = uri.getPathSegments().get(1);
                updateRows = db.update(MovieContract.MovieEntry.TABLE_NAME, values, "_id=?", new String[]{movieId});
                break;
            default:
                throw new UnsupportedOperationException("Not yet implemented");
        }
        if (updateRows != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return updateRows;
    }
}
