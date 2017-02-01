package com.example.jbt.mymovies;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by Elena Fainleib on 11/21/2016.
 *
 * This class manages the presentations of the movies in the list of the main activity
 * It shows whether the movie was watched or not and marks its rating with color
 */
public class MoviesAdapter extends ArrayAdapter<Movie>{
    public MoviesAdapter(Context context, int resource) {
        super(context, resource);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        // Converting the list item xml into Java
        if(convertView==null){
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.movie_list_item,null);
        }

        // Finding the views I will be working with
        ImageView watched = (ImageView) convertView.findViewById(R.id.watched_image);
        TextView movieDetails = (TextView) convertView.findViewById(R.id.movie_details_text);

        Movie currMovie = getItem(position);

        // If the movie is marked as watched - show the eye icon
        if (currMovie.isWatched() == true) {
            watched.setVisibility(View.VISIBLE);
        } else {
            watched.setVisibility(View.INVISIBLE);
        }

        movieDetails.setText(currMovie.getTitle());

        // Checking the rating to get color the item background. If there is no rating, the background stays transparent.
        // If it's bigger than 2.5 the background becomes red, else it becomes green
        if (currMovie.getRating() > 0.0) {
            if (currMovie.getRating() < 2.5) {
                convertView.setBackgroundColor(Color.parseColor("#6FC3B7"));
            } else {
                convertView.setBackgroundColor(Color.parseColor("#FF7372"));
            }
        } else {
            convertView.setBackgroundColor(Color.TRANSPARENT);
        }

        return convertView;
    }
}
