package com.example.jbt.mymovies;

/**
 * Created by Elena Fainleib on 11/14/2016.
 *
 * This class contains constants and values which are used by more than one activity
 * and values used in external services
 */

public final class Constants {
    public static final int NULL_CODE = -1;
    public static final int MAIN_ACTIVITY_CODE = 1;
    public static final int WEB_ACTIVITY_CODE = 2;

    public static final int SAVE_CODE = 3;
    public static final int UPDATE_CODE = 4;

    public static final String ACTIVITY_CODE = "activityCode";
    public static final String ACTION_TYPE = "actionType";
    public static final String MOVIE_ID_KEY = "movieId";
    public static final String IMDB_ID_KEY = "imdbId";
    public static final String POSTER_URL_KEY = "posterUrl";

    public static final String OMDB_URL = "https://www.omdbapi.com/";
    public static final String OMDB_TITLE_SEARCH = "?s=";
    public static final String OMDB_IMDBID_SEARCH = "?i=";
    public static final String OMDB_SEARCH_ARRAY_NAME = "Search";
    public static final String OMDB_SEARCH_TITLE_TAG = "Title";
    public static final String OMDB_SEARCH_IMDBID_TAG = "imdbID";
    public static final String OMDB_SEARCH_PLOT_TAG = "Plot";
    public static final String OMDB_SEARCH_POSTER_TAG = "Poster";
    public static final String OMDB_SEARCH_RATING_TAG = "imdbRating";
    public static final String OMDB_RESPONSE_TAG = "Response";

}
