package edu.uci.ics.fabflixmobile.ui.search;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.util.Log;
import android.widget.SearchView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import edu.uci.ics.fabflixmobile.data.NetworkManager;
import edu.uci.ics.fabflixmobile.databinding.ActivitySearchBinding;
import edu.uci.ics.fabflixmobile.ui.movielist.MovieListActivity;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import android.os.Bundle;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
public class SearchActivity extends AppCompatActivity {
    private SearchView search;
    private TextView message;

    /*
      In Android, localhost is the address of the device or the emulator.
      To connect to your machine, you need to use the below IP address
     */
    private final String host = "54.219.184.15";
    private final String port = "8443";
    private final String domain = "2023-fall-cs122b-spin-mop";
    private final String baseURL = "https://" + host + ":" + port + "/" + domain;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivitySearchBinding binding = ActivitySearchBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        search = binding.search;
        message = binding.message;

        //assign a listener to call a function to handle the user request when clicking a button
        search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Log.d("Query", "Query: " + query);
                doSearch(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                Log.d("search.change", "text changes");
                Log.d("Query", "Query: " + newText);
                return false;
            }
        });
    }

    @SuppressLint("SetTextI18n")
    public void doSearch(String query) {
        message.setText("Searching...");
        final RequestQueue queue = NetworkManager.sharedManager(this).queue;

        String fullURL = baseURL + "/api/search";
        try {
            fullURL += "?title=" + URLEncoder.encode(query, "UTF-8");
        } catch (UnsupportedEncodingException e){
            Log.d("search.error", "error");
        }

        final StringRequest searchRequest = new StringRequest(
                Request.Method.GET,
                fullURL,
                response -> {
                    Log.d("search.success", "yippe");
                    Intent MovieListPage = new Intent(SearchActivity.this, MovieListActivity.class);
                    startActivity(MovieListPage);
                    finish();
                },
                error -> {
                    Log.d("search.error", error.toString());
                });
            queue.add(searchRequest);
        }


    }
