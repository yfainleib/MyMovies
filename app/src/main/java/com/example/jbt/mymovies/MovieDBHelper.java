package com.example.jbt.mymovies;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.net.URL;
import java.util.ArrayList;

/**
 * Created by Elena Fainleib on 11/14/2016.
 *
 * This class implements a movie database helper which manages different actions in the  database:
 * adding a movie, selecting a specific movie, updating movie data, deleting a specific movie and
 * deleting all movies
 */

public class MovieDBHelper extends SQLiteOpenHelper {


    private static final String TABLE_NAME = "my_movies";
    private static final String ID_COL = "id";
    private static final String TITLE_COL = "title";
    private static final String PLOT_COL = "plot";
    private static final String URL_COL = "url";
    private static final String WATCHED_COL = "watched";
    private static final String RATING_COL = "rating";

    public MovieDBHelper(Context context) {
        super(context, "MyMovies.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createMoviesTable = String.format("CREATE TABLE %s ( %s INTEGER PRIMARY KEY AUTOINCREMENT, %s TEXT NOT NULL, %s TEXT, %s TEXT, %s INTEGER, %s REAL )",
                TABLE_NAME, ID_COL, TITLE_COL, PLOT_COL, URL_COL, WATCHED_COL, RATING_COL);
        db.execSQL(createMoviesTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Nothing to do here in the meantime
    }


    public void addMovie(Movie movieToAdd) {
        // Adds a movie to the database
        ContentValues values = new ContentValues();
        values.put(TITLE_COL, movieToAdd.getTitle());
        values.put(PLOT_COL, movieToAdd.getPlot());
        values.put(URL_COL, movieToAdd.getPosterURL());
        values.put(WATCHED_COL, booleanToInt(movieToAdd.isWatched()));
        values.put(RATING_COL, movieToAdd.getRating());

        SQLiteDatabase db = getWritableDatabase();
        db.insert(TABLE_NAME, null, values);
        db.close();
    }

    public void updateMovie(Movie movieToUpdate) {
        // Updates the details of the movie
        ContentValues values = new ContentValues();
        values.put(TITLE_COL, movieToUpdate.getTitle());
        values.put(PLOT_COL, movieToUpdate.getPlot());
        values.put(URL_COL, movieToUpdate.getPosterURL());
        values.put(WATCHED_COL, booleanToInt(movieToUpdate.isWatched()));
        values.put(RATING_COL, movieToUpdate.getRating());

        SQLiteDatabase db = getWritableDatabase();
        db.update(TABLE_NAME, values, ID_COL + "=" + movieToUpdate.getId(),null);
        db.close();
    }

    public void deleteMovie(long id) {
        // Deletes a specific movie
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_NAME, ID_COL + "=" + id, null );
        db.close();
    }

    public void deleteAll() {
        // Deletes all the movies from the database
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_NAME, null, null);
        db.close();
    }

    public Movie getMovie(long id) {
        // Gets the details of a specific movie by the id it receives

        Movie currMovie = null;
        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.query(TABLE_NAME, null,ID_COL + "=" + id, null, null, null, null);

        if (cursor.moveToNext()) {
            currMovie =  new Movie(cursor.getLong(cursor.getColumnIndex(ID_COL)),
                    cursor.getString(cursor.getColumnIndex(TITLE_COL)),
                    cursor.getString(cursor.getColumnIndex(PLOT_COL)),
                    cursor.getString(cursor.getColumnIndex(URL_COL)),
                    intToBoolean(cursor.getInt(cursor.getColumnIndex(WATCHED_COL))),
                    (cursor.getFloat(cursor.getColumnIndex(RATING_COL))));
            db.close();
            return currMovie;
        }
        return null;
    }

    public ArrayList<Movie> getAllMovies () {
        // Brings all the movies from the database

        ArrayList<Movie> movies = new ArrayList();
        SQLiteDatabase db = getReadableDatabase();

        long id = 0;
        String title, plot, url;
        boolean watched;
        float rating;

        Cursor cursor = db.query(TABLE_NAME, null, null, null, null, null,null);
        while (cursor.moveToNext()) {
            id = cursor.getLong(cursor.getColumnIndex(ID_COL));
            title = cursor.getString(cursor.getColumnIndex(TITLE_COL));
            plot = cursor.getString(cursor.getColumnIndex(PLOT_COL));
            url = cursor.getString(cursor.getColumnIndex(URL_COL));
            watched = intToBoolean(cursor.getInt(cursor.getColumnIndex(WATCHED_COL)));

            rating = (cursor.getFloat(cursor.getColumnIndex(RATING_COL)));


            movies.add(new Movie(id, title, plot, url, watched, rating));
        }

        db.close();
        return movies;
    }

    private int booleanToInt(boolean value) {
        //Converts a boolean value into integer
        return (value == true) ? 1:0;
    }

    private boolean intToBoolean(int value) {
        // Coverts an integer value into boolean
        return (value == 1) ? true: false;
    }
}
