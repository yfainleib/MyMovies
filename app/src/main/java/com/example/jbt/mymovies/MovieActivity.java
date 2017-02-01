package com.example.jbt.mymovies;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.provider.SyncStateContract;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by Elena Fainleib 11/17/2016
 *
 * This activity can save a new movie in a database or update its details.
 * If no movie id is passed it displays empty fields where the user enter movie details to be added to the database
 * If a database movie id is passed it loads the details of this movie from the database for update
 * If an IMDB Id is passed it loads the details of this movie from the web to be added to the database
 */

public class MovieActivity extends AppCompatActivity implements View.OnClickListener, DialogInterface.OnClickListener {

    private Button saveUpdateButton, cancelButton, shareButton;
    private EditText title, plot, url;
    private ImageButton showPoster;
    private CheckBox watched;
    private RatingBar rating;
    private int currAction;
    private MovieDBHelper helper;
    private long currMovieId = Constants.NULL_CODE;
    private Movie movieToUpdate = null;
    private AlertDialog noInternetDialog, titleEmptyDialog, badUrlDialog;
    private ProgressBar progressBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie);


        // Finding the views I will be working with
        saveUpdateButton = (Button) findViewById(R.id.movie_save_update_btn);
        saveUpdateButton.setOnClickListener(this);
        cancelButton = (Button)findViewById(R.id.movie_cancel_button);
        cancelButton.setOnClickListener(this);
        shareButton = (Button)findViewById(R.id.movie_share_button);
        shareButton.setOnClickListener(this);
        showPoster = (ImageButton) findViewById(R.id.show_poster_button);
        showPoster.setOnClickListener(this);

        title = (EditText) findViewById(R.id.movie_title_text);
        plot = (EditText) findViewById(R.id.movie_plot_text);
        url = (EditText) findViewById(R.id.movie_url_text);
        watched = (CheckBox) findViewById(R.id.movie_watched);
        rating = (RatingBar) findViewById(R.id.movieRatingBar);
        progressBar = (ProgressBar) findViewById(R.id.movie_loading_progress_bar);

        // Getting a helper to the database
        helper = new MovieDBHelper(this);

        // Creating the alert dialogs to communicate various problems to the user
        noInternetDialog = new AlertDialog.Builder(this)
                .setTitle(R.string.no_internet_dialog_title)
                .setMessage(R.string.no_internet_dialog_message)
                .setPositiveButton(R.string.dialog_positive_button, this)
                .create();

        titleEmptyDialog = new AlertDialog.Builder(this)
                .setTitle(R.string.empty_title_dialog_title)
                .setMessage(R.string.empty_title_dialog_message)
                .setPositiveButton(R.string.dialog_positive_button, this)
                .create();

        badUrlDialog = new AlertDialog.Builder(this)
                .setTitle(R.string.bad_url_dialog_title)
                .setMessage(R.string.bad_url_dialog_message)
                .setPositiveButton(R.string.dialog_positive_button, this)
                .create();


        // Checking which action the activity was called to perform: add a movie from scratch, add a movie from list or update a movie
        Intent currIntent = getIntent();

        // Checking first if the activity was called to add or update a movie
        if (currIntent.getIntExtra(Constants.ACTION_TYPE, Constants.SAVE_CODE) == Constants.SAVE_CODE) {
            saveUpdateButton.setText(R.string.save_button);
            currAction = Constants.SAVE_CODE;

            // IF THE ACTIVITY WAS CALLED FROM THE WEB SEARCH ACTIVITY IT WAS PASSED AN IMDB ID OF THE MOVIE
            // THAT THE USER WANTS TO ADD TO THE DATABASE.THE ACTIVITY WILL LOAD ITS DATA INTO THE FIELDS AND
            // THE USER CAN SAVE IT.
            if (currIntent.getIntExtra(Constants.ACTIVITY_CODE, Constants.MAIN_ACTIVITY_CODE) == Constants.WEB_ACTIVITY_CODE) {
                String searchString = Constants.OMDB_URL + Constants.OMDB_IMDBID_SEARCH
                        + currIntent.getStringExtra(Constants.IMDB_ID_KEY);
                GetIMDBMovieTask getIMDBMovieTask = new GetIMDBMovieTask();
                getIMDBMovieTask.execute(searchString);
            }
        } else if (currIntent.getIntExtra(Constants.ACTION_TYPE, Constants.SAVE_CODE) == Constants.UPDATE_CODE) {
            saveUpdateButton.setText(R.string.update_button);
            currAction = Constants.UPDATE_CODE;

            // IF THE ACTIVITY WAS CALLED TO UPDATE IT WAS PASSED A MOVIE ID WHICH THE USER WANTS TO UPDATE
            // THE ACTIVITY WILL PRESENT ITS DATA IN THE FIELDS
            currMovieId = currIntent.getLongExtra(Constants.MOVIE_ID_KEY, Constants.NULL_CODE);
            GetMovieTask getMovieTask = new GetMovieTask();
            getMovieTask.execute(currMovieId);
        }

        // If none of the conditions of the above if statement were satisfied, it means that the activity was called
        // to manually add a movie to the database. All its fields should be empty.

    }

    @Override
    public void onClick(View v) {
        // Deals with the button clicks:
        // For the save/update button - either adds the new movie to the database or updates the existing movie details
        // according to the state in which the form was opened
        // The showPoster button calls the getPoster activity to download the poster of the movie and passes it its URL
        // The cancel button closes the activity
        // The share button shares the title of the movie with the user's contacts

        switch (v.getId()) {
            case R.id.movie_save_update_btn:
                switch (currAction) {
                    case Constants.SAVE_CODE:
                        // Open a new task which opens a database connection and inserts the values from the fields into the table
                        // Checking if movie title was entered
                        if (!title.getText().toString().isEmpty()) {
                            AddMovieTask addTask = new AddMovieTask();
                            addTask.execute(new Movie(title.getText().toString(), plot.getText().toString(), url.getText().toString()
                                            ,watched.isChecked(), rating.getRating()));
                        } else {
                            // Alert the user that the movie title field is empty
                            titleEmptyDialog.show();
                        }
                        break;

                    case Constants.UPDATE_CODE:
                        // Open a new task which opens a database connection and updates the details of the movie
                        // Checking if movie title was not erased
                        if (!title.getText().toString().isEmpty()) {
                            movieToUpdate.setTitle(title.getText().toString());
                            movieToUpdate.setPlot(plot.getText().toString());
                            movieToUpdate.setPosterURL(url.getText().toString());
                            movieToUpdate.setWatched(watched.isChecked());
                            movieToUpdate.setRating(rating.getRating());

                            UpdateMovieTask updateMovieTask = new UpdateMovieTask();
                            updateMovieTask.execute(movieToUpdate);
                            movieToUpdate = null;}
                        else {
                            titleEmptyDialog.show();
                        }
                        break;
                }

                break;
            case R.id.show_poster_button:
                // Open the activity that will download the poster into a separate layout
                // also will check the url field for validity and will pass it to the getImage activity
                if (url.getText().toString().startsWith("http://") || url.getText().toString().startsWith("https://")) {
                    Intent getPosterIntent = new Intent(this, GetPoster.class);
                    getPosterIntent.putExtra(Constants.POSTER_URL_KEY, url.getText().toString());
                    startActivity(getPosterIntent);
                } else {
                    badUrlDialog.show();
                }

                break;
            case R.id.movie_cancel_button:
                // No update/save will be made
                finish();
                break;
            case R.id.movie_share_button:
                // If the title field is not empty, share the movie title
                if (!title.getText().toString().isEmpty()) {
                    Intent sendIntent = new Intent();
                    sendIntent.setAction(Intent.ACTION_SEND);
                    sendIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.movie_added) + title.getText().toString());
                    sendIntent.setType("text/plain");
                    startActivity(Intent.createChooser(sendIntent, getString(R.string.choose_service)));
                } else {
                    // Alert the user that the movie title field is empty
                    titleEmptyDialog.show();
                }
                break;
        }
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        // Belongs to the information alerts: no title, bad url
        // Nothing to do here, the button press will simply
        // close the alert
    }

    private class AddMovieTask extends AsyncTask<Movie, Void, Void> {

        // This task adds a movie to the database
        // and closes the activity

        @Override
        protected Void doInBackground(Movie... params) {
            helper.addMovie(params[0]);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            finish();
        }
    }

    private class GetMovieTask extends AsyncTask<Long, Void, Movie> {

        // This task receives a movie database id and brings the rest of its details for update

        @Override
        protected Movie doInBackground(Long... params) {
            Movie currMovie = helper.getMovie(params[0]);
            return currMovie;
        }

        @Override
        protected void onPostExecute(Movie movie) {
            // Presents the details of the movie in the fields

            movieToUpdate = movie;
            title.setText(movieToUpdate.getTitle());
            plot.setText(movieToUpdate.getPlot());
            url.setText(movieToUpdate.getPosterURL());
            watched.setChecked(movieToUpdate.isWatched());
            rating.setRating(movie.getRating());
        }
    }
    private class UpdateMovieTask extends AsyncTask<Movie, Void, Void> {

        // This task updates the details of the movie with a specific id
        // and then closes the activity

        @Override
        protected Void doInBackground(Movie... params) {
            helper.updateMovie(params[0]);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            finish();
        }
    }
    private class GetIMDBMovieTask extends AsyncTask<String, Void, Movie> {

        // This task receives an IMDB movie id and a list of its additional features,
        // which it brings from the internet
        protected void onPreExecute() {
            progressBar.setVisibility(ProgressBar.VISIBLE);
        }

        @Override
        protected Movie doInBackground(String... params) {
            Movie currMovie = null;
            HttpURLConnection connection = null;
            BufferedReader reader;
            StringBuilder stringResult = new StringBuilder();
            String line;
            JSONObject jsonResult;

            try {
                // Get an internet connection
                URL url = new URL(params[0]);
                connection = (HttpURLConnection) url.openConnection();

                // Checking if it is ok
                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    return null;
                }

                // Bringing the movie data
                reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                line = reader.readLine();

                while (line != null) {
                    stringResult.append(line);
                    line = reader.readLine();
                }

                jsonResult = new JSONObject(stringResult.toString());
                currMovie = new Movie(jsonResult.getString(Constants.OMDB_SEARCH_TITLE_TAG), jsonResult.getString(Constants.OMDB_SEARCH_PLOT_TAG)
                        ,jsonResult.getString(Constants.OMDB_SEARCH_POSTER_TAG), (float) (jsonResult.getDouble(Constants.OMDB_SEARCH_RATING_TAG)));

                return currMovie;

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }

            return null;

        }

        @Override
        protected void onPostExecute(Movie movie) {
            // Checking if the movie data was received.
            // If yes - present it, if not assume internet problems and notify the user
            progressBar.setVisibility(ProgressBar.GONE);

            if (movie == null) {
                try {
                    noInternetDialog.show();
                } catch (Exception e) {
                    // Case the user chooses to close the activity before
                    // the async task finishes running in which case android.view.WindowManager$BadTokenException
                    // is thrown. But Android Studio doesn't recognize this specific exception, and the only solution
                    // I found was to catch a general exception instead
                    e.printStackTrace();
                }
            } else {
                title.setText(movie.getTitle());
                plot.setText(movie.getPlot());
                url.setText(movie.getPosterURL());
                watched.setChecked(movie.isWatched());
                rating.setRating(movie.getRating()/2);
            }
        }
    }

}
