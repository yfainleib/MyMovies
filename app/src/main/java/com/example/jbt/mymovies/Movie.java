package com.example.jbt.mymovies;

/**
 * Created by Elena Fainleib on 11/14/2016.
 *
 * This class represents the movie and its properties:
 * id - local id of the movie in the movies database
 * title - movie title
 * plot - movie plot short summary
 * posterURL - the url to the movie poster
 * imdbID - the id of the movie in IMDB
 * watched - whether the movie was watched
 * rating - rating of the movie from 1 to 5
 *
 */
public class Movie {
    private long id;
    private String title, plot, posterURL, imdbID;
    private boolean watched;
    private float rating;

    // CONSTRUCTORS
    public Movie(long id, String title, String plot, String posterURL, boolean watched, float rating) {
        this.id = id;
        this.title = title;
        this.plot = plot;
        this.posterURL = posterURL;
        this.watched = watched;
        this.rating = rating;
    }

    public Movie(String title, String plot, String posterURL, boolean watched, float rating) {
        this.title = title;
        this.plot = plot;
        this.posterURL = posterURL;
        this.watched = watched;
        this.rating = rating;
    }

    public Movie(String title, String plot, String posterURL, float rating) {
        this.title = title;
        this.plot = plot;
        this.posterURL = posterURL;
        this.rating = rating;
    }

    public Movie(String title, String imdbID) {
        this.title = title;
        this.imdbID = imdbID;
    }

    // GETTERS AND SETTERS
    public long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPlot() {
        return plot;
    }

    public void setPlot(String plot) {
        this.plot = plot;
    }

    public String getPosterURL() {
        return posterURL;
    }

    public void setPosterURL(String posterURL) {
        this.posterURL = posterURL;
    }

    public String getImdbID() {
        return imdbID;
    }

    public void setImdbID(String imdbID) {
        this.imdbID = imdbID;
    }

    public boolean isWatched() {
        return watched;
    }

    public void setWatched(boolean watched) {
        this.watched = watched;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    // TO_STRING
    @Override
    public String toString() {
        return title;
    }
}
