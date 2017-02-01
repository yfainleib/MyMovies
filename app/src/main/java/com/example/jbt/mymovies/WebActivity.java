package com.example.jbt.mymovies;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by Elena Fainleib on 11/14/2016
 *
 * This activity allows the user to search for movies in the OMD Api service and to select which ones
 * they want to add to the database
 */

public class WebActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemClickListener, DialogInterface.OnClickListener {


    private static final String DUMMY_ID = "dummid";

    private ListView webMoviesList;
    private EditText searchText;
    private ArrayAdapter<Movie> webMoviesAdapter;
    private ProgressBar progressBar;
    private AlertDialog noInternetDialog, searchTextEmpty, searchNoResultsDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web);

        // Finding the views I will be working with
        ((Button) findViewById(R.id.web_cancel_button)).setOnClickListener(this);
        ((ImageButton) findViewById(R.id.web_search_button)).setOnClickListener(this);
        progressBar = (ProgressBar) findViewById(R.id.search_loading_progress_bar);
        webMoviesList = (ListView) findViewById(R.id.web_movies_list);
        searchText = (EditText) findViewById(R.id.search_movie_text);

        // Creating the alert dialogs to communicate various problems to user
        noInternetDialog = new AlertDialog.Builder(this)
                .setTitle(R.string.no_internet_dialog_title)
                .setMessage(R.string.no_internet_dialog_message)
                .setPositiveButton(R.string.dialog_positive_button, this)
                .create();

        searchTextEmpty = new AlertDialog.Builder(this)
                .setTitle(R.string.search_text_empty_dialog_title)
                .setMessage(R.string.search_text_empty_dialog_message)
                .setPositiveButton(R.string.dialog_positive_button, this)
                .create();

        searchNoResultsDialog = new AlertDialog.Builder(this)
                .setTitle(R.string.search_no_results)
                .setMessage(R.string.search_no_results_dialog_message)
                .setPositiveButton(R.string.dialog_positive_button, this)
                .create();


        // Connecting the adapter that will hold the movie search results to the list view
        // that will display them
        webMoviesAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1);
        webMoviesList.setAdapter(webMoviesAdapter);
        webMoviesList.setOnItemClickListener(this);
    }


    @Override
    public void onClick(View v) {

        // Responds to the button clicks in the activity
        // In case of search button click calls an Internet search task with the url+search string
        // and the field names to get from the JSON result.
        // In case of cancel button the activity will close

        switch (v.getId()) {
            case R.id.web_search_button:
                if (searchText.getText().toString().isEmpty()) {
                    searchTextEmpty.show();
                } else {
                    String searchStr = Constants.OMDB_URL
                            + Constants.OMDB_TITLE_SEARCH + searchText.getText().toString().replace(" ", "%20");
                    GetMovieSearchTask searchTask = new GetMovieSearchTask();
                    searchTask.execute(searchStr);
                }
                break;
            case R.id.web_cancel_button:
                finish();
                break;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        // Getting the imdbID from the item and passing it to the MovieActivity to save in the database
        Intent addIntent = new Intent(this, MovieActivity.class);
        addIntent.putExtra(Constants.ACTIVITY_CODE, Constants.WEB_ACTIVITY_CODE);
        addIntent.putExtra(Constants.ACTION_TYPE, Constants.SAVE_CODE);
        addIntent.putExtra(Constants.IMDB_ID_KEY, webMoviesAdapter.getItem(position).getImdbID());
        startActivity(addIntent);

    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        // Nothing to do here, the button press will simply
        // close the alerts
    }

    public class GetMovieSearchTask extends AsyncTask<String, Integer, ArrayList<Movie>> {

        // This task searches for movies with specific words in their titles and returns
        // those that fit the search criteria

        @Override
        protected void onPreExecute() {
            progressBar.setVisibility(ProgressBar.VISIBLE);
        }

        @Override
        protected ArrayList<Movie> doInBackground(String... params) {
            ArrayList<Movie> searchResults = new ArrayList();
            HttpURLConnection connection = null;
            BufferedReader reader;
            StringBuilder stringResult = new StringBuilder();
            JSONObject jsonResult, currMovie;
            JSONArray moviesArray;

            try {

                // Opening an Internet connection
                URL url = new URL(params[0]);
                connection = (HttpURLConnection) url.openConnection();

                // Checking if connection was successful
                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line = reader.readLine();

                // If there are search results reading them
                while (line != null) {
                    stringResult.append(line);
                    line = reader.readLine();
                }

                // Getting the actual results array from the entire results object
                jsonResult = new JSONObject(stringResult.toString());

                // Checking the case no results were found. Returning an array with one dummy movie object
                if (jsonResult.getString(Constants.OMDB_RESPONSE_TAG).toString().equalsIgnoreCase("false")) {
                    searchResults.add(new Movie((getString(R.string.search_no_results)), DUMMY_ID));
                    return searchResults;
                }

                moviesArray = jsonResult.getJSONArray(Constants.OMDB_SEARCH_ARRAY_NAME);

                // Looping over the search results and adding the movies to the local array
                for (int i=0; i < moviesArray.length(); i++) {
                    currMovie = moviesArray.getJSONObject(i);
                    // PUT THESE STRINGS INTO CONSTANTS AS WELL. ALSO PUT OMDB RELATED CONSTANTS HERE
                    searchResults.add(new Movie(currMovie.getString(Constants.OMDB_SEARCH_TITLE_TAG)
                            , currMovie.getString(Constants.OMDB_SEARCH_IMDBID_TAG)));
                }

                return searchResults;

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
        protected void onPostExecute(ArrayList<Movie> movies) {

            webMoviesAdapter.clear();
            progressBar.setVisibility(ProgressBar.GONE);

            // Checking if the movies search was successful
            // No data returned - Internet connection problem
            // First item's id equals "dummyid" - the search produced no results
            // Otherwise - load the search results into the list
            if (movies == null) {
                try {
                    noInternetDialog.show();
                } catch (Exception e) {
                    // Case the user chooses to close the activity before
                    // the async task finishes running in which case android.view.WindowManager$BadTokenException
                    // is thrown. But Android Studio doesn't recognize this specific exception, and the only solution
                    // I found was to catch a general exception instead
                    e.printStackTrace();
                }
            }
            else if (movies.get(0).getImdbID().equalsIgnoreCase(DUMMY_ID)){
                searchNoResultsDialog.show();
            } else {
                webMoviesAdapter.addAll(movies);
            }

        }
    }
}
