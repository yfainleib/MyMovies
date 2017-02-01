package com.example.jbt.mymovies;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;

import java.util.ArrayList;

/**
 * Created by Elena Fainleib on 11/17/2016
 *
 * This is the main activity of the project, that manages the movie library. It executes calls to the add movie activities,
 * (manually or from the web) the update movie activities, deletes specific movies or the  entire library.
 *
 */

public class MainActivity extends AppCompatActivity implements View.OnClickListener, DialogInterface.OnClickListener, AdapterView.OnItemLongClickListener, AdapterView.OnItemClickListener {

    private AlertDialog deleteAllDialog, exitDialog, addMovieDialog, editDeleteMovieDialog;
    private ListView moviesList;
    private MoviesAdapter moviesAdapter;
    private MovieDBHelper helper;
    private long currSelectedMovieId = Constants.NULL_CODE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Finding the views I will be working with and initializing class members
        helper = new MovieDBHelper(this);
        ((ImageButton) findViewById(R.id.add_button)).setOnClickListener(this);
        moviesList = (ListView)findViewById(R.id.movies_list);

        // Setting up the movies list
        moviesAdapter = new MoviesAdapter(this, R.layout.movie_list_item);

        moviesList.setAdapter(moviesAdapter);
        moviesList.setOnItemClickListener(this);
        moviesList.setOnItemLongClickListener(this);

    }

    @Override
    protected void onStart() {
        super.onStart();

        // reload the list here every time the activity (re)starts
        LoadMoviesTask loadMoviesTask = new LoadMoviesTask();
        loadMoviesTask.execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // inflate the menu resource file into menu object
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // This method activates the menu items: delete all items or exit the application

        switch (item.getItemId()){
            case R.id.delete_all_menu_item:
                // Show dialog that asks if to delete all the movies from the database
                deleteAllDialog = new AlertDialog.Builder(this)
                        .setTitle(R.string.delete_all_dialog_title)
                        .setMessage(R.string.delete_all_dialog_message)
                        .setPositiveButton(R.string.dialog_positive_button, this)
                        .setNegativeButton(R.string.dialog_negative_button, this)
                        .create();
                deleteAllDialog.show();
                break;

            case R.id.exit_menu_item:
                // Show dialog that asks if to exit the application
                exitDialog = new AlertDialog.Builder(this)
                    .setTitle(R.string.exit_dialog_title)
                    .setMessage(R.string.exit_dialog_message)
                    .setPositiveButton(R.string.dialog_positive_button, this)
                    .setNegativeButton(R.string.dialog_negative_button, this)
                    .create();
               exitDialog.show();
                break;


        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        // Belongs to the Add button (+), displays the dialog on whether to add the movie from the web or manually
        addMovieDialog = new AlertDialog.Builder(this)
                .setTitle(R.string.add_movie_dialog_title)
                .setMessage(R.string.add_movie_dialog_message)
                .setPositiveButton(R.string.add_movie_dialog_manually_button, this)
                .setNegativeButton(R.string.add_movie_dialog_web_button, this)
                .create();
        addMovieDialog.show();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        //Opens the Movies Activity in in the update mode with the details of the chosen movie

        currSelectedMovieId = moviesAdapter.getItem(position).getId();
        Intent addIntent = new Intent(this, MovieActivity.class);
        addIntent.putExtra(Constants.ACTIVITY_CODE, Constants.MAIN_ACTIVITY_CODE);
        addIntent.putExtra(Constants.ACTION_TYPE, Constants.UPDATE_CODE);
        addIntent.putExtra(Constants.MOVIE_ID_KEY, currSelectedMovieId);
        startActivity(addIntent);
        currSelectedMovieId = Constants.NULL_CODE;
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

        // Shows a dialog to let the user chose what to do on the long click: edit or delete the movie

        editDeleteMovieDialog = new AlertDialog.Builder(this)
                .setTitle(R.string.edit_delete_movie_dialog_title)
                .setMessage(R.string.edit_delete_movie_dialog_message)
                .setPositiveButton(R.string.edit_delete_movie_dialog_delete_button, this)
                .setNegativeButton(R.string.edit_delete_movie_dialog_edit_button, this)
                .create();
        editDeleteMovieDialog.show();
        currSelectedMovieId = moviesAdapter.getItem(position).getId();
        return true;
    }

    @Override
    public void onClick(DialogInterface dialog, int button) {

        // This method deals with the clicks on the buttons of the various dialogs of the  main activity

        if (dialog == deleteAllDialog) {
            // The user clicked delete all option from the menu: execute the delete all command
            if (button == DialogInterface.BUTTON_POSITIVE) {
                DeleteAllTask deleteAllTask = new DeleteAllTask();
                deleteAllTask.execute();
            }
        } else if (dialog == exitDialog) {
            // The user clicked the exit option from the menu:
            if (button == DialogInterface.BUTTON_POSITIVE) {
                finish();
            }
        } else if (dialog == addMovieDialog) {
            // The user clicked the add movie button
            switch (button) {
                case DialogInterface.BUTTON_POSITIVE:
                    // The user chose to add a movie manually
                    // Call the Movie Activity in the add new movie from scratch mode
                    Intent addIntent = new Intent(this, MovieActivity.class);
                    addIntent.putExtra(Constants.ACTIVITY_CODE, Constants.MAIN_ACTIVITY_CODE);
                    addIntent.putExtra(Constants.ACTION_TYPE, Constants.SAVE_CODE);
                    startActivity(addIntent);
                    break;
                case DialogInterface.BUTTON_NEGATIVE:
                    // The user chose to add a movie from web
                    // Calls  Web activity which allows to add movies from web
                    startActivity(new Intent(this, WebActivity.class));
                    break;
            }
        } else if (dialog == editDeleteMovieDialog){
            // The user long clicked on the item
            switch (button) {
                case DialogInterface.BUTTON_POSITIVE:
                    // The user chose to delete the movie - executing delete selected movie task
                    DeleteMovieTask deleteMovieTask = new DeleteMovieTask();
                    deleteMovieTask.execute(currSelectedMovieId);
                    break;
                case DialogInterface.BUTTON_NEGATIVE:
                    // The user chose to edit the movie - calling the movie activity in the update mode
                    // with the details of the chosen movie
                    Intent addIntent = new Intent(this, MovieActivity.class);
                    addIntent.putExtra(Constants.ACTIVITY_CODE, Constants.MAIN_ACTIVITY_CODE);
                    addIntent.putExtra(Constants.ACTION_TYPE, Constants.UPDATE_CODE);
                    addIntent.putExtra(Constants.MOVIE_ID_KEY, currSelectedMovieId);
                    startActivity(addIntent);
                    break;
                default:
                    // Nullifying the current selected movie cause done dealing with its value
                    currSelectedMovieId = Constants.NULL_CODE;
                    break;
            }
        }
    }


    public class LoadMoviesTask extends AsyncTask<Void, Void, ArrayList<Movie>> {

        // This task loads all the available movies from the database to the list

        @Override
        protected ArrayList<Movie> doInBackground(Void... params) {
            ArrayList<Movie> savedMovies = new ArrayList<>();
            savedMovies = helper.getAllMovies();
            return savedMovies;
        }

        @Override
        protected void onPostExecute(ArrayList<Movie> movies) {
            moviesAdapter.clear();
            moviesAdapter.addAll(movies);
        }
    }

    public class DeleteAllTask extends AsyncTask<Void, Void, Void> {

        // This task deletes all the movies from the database

        @Override
        protected Void doInBackground(Void... params) {
            helper.deleteAll();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            // Clear the available movies list
            moviesAdapter.clear();
            moviesAdapter.addAll();
        }
    }

    public class DeleteMovieTask extends AsyncTask<Long, Void, Void> {

        // This task deletes a specific movie from the database

        @Override
        protected Void doInBackground(Long... params) {
            helper.deleteMovie(params[0]);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            // Refresh the available movies list
            moviesAdapter.clear();
            moviesAdapter.addAll(helper.getAllMovies());
        }
    }
}
