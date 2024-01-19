package edu.uci.ics.fabflixmobile.ui.singlemovie;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import edu.uci.ics.fabflixmobile.data.NetworkManager;
import edu.uci.ics.fabflixmobile.databinding.ActivitySinglemovieBinding;
import edu.uci.ics.fabflixmobile.ui.movielist.MovieListActivity;
import edu.uci.ics.fabflixmobile.ui.search.SearchActivity;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class SingleMovieActivity extends AppCompatActivity {

    private TextView title;
    private TextView year;
    private TextView director;
    private TextView rating;
    private TextView genres;
    private TextView stars;

    private final String host = "54.219.184.15";
    private final String port = "8443";
    private final String domain = "2023-fall-cs122b-spin-mop";
    private final String baseURL = "https://" + host + ":" + port + "/" + domain;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivitySinglemovieBinding binding = ActivitySinglemovieBinding.inflate(getLayoutInflater());

        setContentView(binding.getRoot());

        title = binding.title;
        year = binding.year;
        director = binding.director;
        rating = binding.rating;
        genres = binding.genres;
        stars = binding.stars;

        String id = getIntent().getStringExtra("id");
        displayMovie(id);
    }

    @SuppressLint("SetTextI18n")
    public void displayMovie(String id) {
        final RequestQueue queue = NetworkManager.sharedManager(this).queue;

        final StringRequest searchRequest = new StringRequest(
                Request.Method.GET,
                baseURL + "/api/single-movie?id=" + id ,
                response -> {
                    try {
                        JSONObject jsonResponse = new JSONObject(response);

                        String movie_title = jsonResponse.getString("movie_title");
                        String movie_year = jsonResponse.getString("movie_year");
                        String movie_director = jsonResponse.getString("movie_director");
                        String movie_rating = jsonResponse.getString("rating");
                        StringBuilder genre_name = new StringBuilder();
                        StringBuilder stars_name = new StringBuilder();

                        JSONArray jsonStars = jsonResponse.getJSONArray("stars_name");
                        for (int j = 0; j < jsonStars.length(); j++) {
                            stars_name.append(jsonStars.getString(j));
                            if (j != jsonStars.length() - 1)
                                stars_name.append(", ");
                        }
                        JSONArray jsonGenres = jsonResponse.getJSONArray("genre_name");
                        for (int j = 0; j < jsonGenres.length(); j++) {
                            genre_name.append(jsonGenres.getString(j));
                            if (j != jsonGenres.length() - 1)
                                genre_name.append(", ");
                        }



                        runOnUiThread(() -> {
                            title.setText(movie_title);
                            year.setText("Year: " + movie_year);
                            director.setText("Director: " + movie_director);
                            rating.setText("Rating: " + movie_rating);
                            genres.setText("Genres: " + genre_name.toString());
                            stars.setText("Stars: " + stars_name.toString());
                        });
                        Log.d("single-movie.success", "Single Movie Loaded");

                    } catch (JSONException e) {
                        Log.d("single-movie.error", e.toString());
                        throw new RuntimeException(e);
                    }
                },
                error -> {
                    Log.d("single-movie.error", error.toString());
                });
        queue.add(searchRequest);
    }



}
