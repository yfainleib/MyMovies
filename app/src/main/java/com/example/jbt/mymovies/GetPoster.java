package com.example.jbt.mymovies;

/**
 * Created by Elena Fainleib on 11/14/2016.
 * This activity receives a String representing a URL of the movie poster.
 * It downloads the image from this URL
 */

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class GetPoster extends AppCompatActivity implements View.OnClickListener, DialogInterface.OnClickListener {

    private ImageView poster;
    private String posterUrl;
    private ProgressBar progressBar;
    private AlertDialog noInternetDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_poster);

        // Finding the views I will be working with
        ((Button) findViewById(R.id.image_close_btn)).setOnClickListener(this);
        poster = (ImageView) findViewById(R.id.poster_view);
        progressBar = (ProgressBar) findViewById(R.id.poster_loading_progress_bar);

        // Creating the noInternet alert dialog case it would have to be called from a task
        noInternetDialog = new AlertDialog.Builder(this)
                .setTitle(R.string.no_internet_dialog_title)
                .setMessage(R.string.no_internet_dialog_message)
                .setPositiveButton(R.string.dialog_positive_button, this)
                .create();

        // Get the intent that called this activity and get the url from it.
        Intent currIntent = getIntent();
        posterUrl = currIntent.getStringExtra(Constants.POSTER_URL_KEY);

        // Call the task to download the poster
        GetPosterTask getPosterTask = new GetPosterTask();
        getPosterTask.execute(posterUrl);
    }

    @Override
    public void onClick(View v) {
        // Belongs to the "Close" button, closes the activity.
        finish();
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        // Belongs to the NoInternet alert, nothing to do here, the button press will simply
        // close the alert
    }

    public class GetPosterTask extends AsyncTask<String, Integer, Bitmap> {

        // This task downloads and returns an image from the url that was passed to it.

        @Override
        protected void onPreExecute() {
            // Showing the progress bar before poster download starts
            progressBar.setVisibility(ProgressBar.VISIBLE);
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            // Taking care of downloading a picture from the url that was passed

            Bitmap image = null;
            HttpURLConnection connection = null;

            try {

                // Connecting to the internet
                URL url = new URL(params[0]);
                connection = (HttpURLConnection) url.openConnection();

                // Checking the response code:
                // If there is no poster at the url - load a "404 error" pic
                // If there is a poster at the url - download it
                // Else - assuming Internet connection problems, return null
                if (connection.getResponseCode() == HttpURLConnection.HTTP_NOT_FOUND)
                {
                    image = BitmapFactory.decodeResource(getResources(), R.drawable.error404_small);
                } else if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    image = BitmapFactory.decodeStream(connection.getInputStream());
                } else {
                    return null;
                }

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
            return image;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            // Checking if a picture is received
            // if not - alert the user of the Internet connection problems
            // if yes - put the image into its view
            progressBar.setVisibility(ProgressBar.GONE);
            if (bitmap == null) {
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
                super.onPostExecute(bitmap);
                poster.setImageBitmap(bitmap);
                poster.setVisibility(ImageView.VISIBLE);

            }
        }
    }
}
