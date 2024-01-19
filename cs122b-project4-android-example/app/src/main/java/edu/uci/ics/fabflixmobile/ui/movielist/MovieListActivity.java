package edu.uci.ics.fabflixmobile.ui.movielist;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import edu.uci.ics.fabflixmobile.R;
import edu.uci.ics.fabflixmobile.data.NetworkManager;
import edu.uci.ics.fabflixmobile.data.model.Movie;
import edu.uci.ics.fabflixmobile.ui.search.SearchActivity;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class MovieListActivity extends AppCompatActivity {
    private ArrayList<Movie> movies;

    private int changePage;
    private final String host = "54.219.184.15";
    private final String port = "8443";
    private final String domain = "2023-fall-cs122b-spin-mop";
    private final String baseURL = "https://" + host + ":" + port + "/" + domain;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movielist);

        Button prevButton = findViewById(R.id.prevButton);
        Button nextButton = findViewById(R.id.nextButton);

        changePage = 0;

        movies = new ArrayList<>();
        getMovieList();
    }

    public void onPrevButtonClick(View view){
            changePage = -1;
            getMovieList();
        Log.d("movielist.page", Integer.toString(changePage));
    }

    public void onNextButtonClick(View view){
        changePage = 1;
        Log.d("movielist.page", Integer.toString(changePage));
        getMovieList();
    }

    @SuppressLint("SetTextI18n")
    public void getMovieList() {
        final RequestQueue queue = NetworkManager.sharedManager(this).queue;
        final StringRequest moviesRequest = new StringRequest(
                Request.Method.GET,
                baseURL + "/api/movies?changePage=" + Integer.toString(changePage),
                response -> {
                    try {
                        movies.clear();
                        JSONArray jsonArray = new JSONArray(response);

                        for (int i = 0; i < jsonArray.length(); i++){
                            JSONObject jsonMovie = jsonArray.getJSONObject(i);

                            String movie_id = jsonMovie.getString("movie_id");
                            String movie_title = jsonMovie.getString("movie_title");
                            int movie_year = jsonMovie.getInt("movie_year");
                            String movie_director = jsonMovie.getString("movie_director");
                            Float rating = Float.parseFloat(jsonMovie.getString("movie_rating"));

                            Movie tempMovie = new Movie(movie_id, movie_title, movie_year, movie_director, rating);


                            JSONArray jsonStars = jsonMovie.getJSONArray("stars_name");
                            for (int j = 0; j < jsonStars.length(); j++) {
                                String star = jsonStars.getString(j);
                                tempMovie.addStar(star);
                            }
                            JSONArray jsonGenres = jsonMovie.getJSONArray("movie_genres");
                            for (int j = 0; j < jsonGenres.length(); j++) {
                                String genre = jsonGenres.getString(j);
                                tempMovie.addGenre(genre);
                            }
                            movies.add(tempMovie);
                        }

                        MovieListViewAdapter adapter = new MovieListViewAdapter(this, movies);
                        ListView listView = findViewById(R.id.list);
                        listView.setAdapter(adapter);
                        listView.setOnItemClickListener((parent, view, position, id) -> {
                            Movie movie = movies.get(position);
                            @SuppressLint("DefaultLocale") String message = String.format("Clicked on position: %d, name: %s, %d", position, movie.getTitle(), movie.getYear());
                            Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                        });



                        Log.d("movielist.success", response);
                    } catch (JSONException e){
                        Log.d("movielist.error", "Error parsing JSON: " + e.getMessage());
                    }
                },
                error -> {
                    // error
                    Log.d("movielist.error", error.toString());
                });
        // important: queue.add is where the login request is actually sent
        queue.add(moviesRequest);
    }
}