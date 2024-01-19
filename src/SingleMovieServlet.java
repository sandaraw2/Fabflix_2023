import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// tester: http://localhost:8080/cs122b_project1_api_example_war/single-movie.html?id=tt0395642
// quick overview: how does this work? so instead of multiple jsons in a json array, there is one big json.
//      big json includes the movie, ratings, then the lists for genres and stars
//          Q: Why one Json? A: I'm too smooth brained to figure out a buncha JSONs cause the combos of
//                              genres and stars and the repeats destroy my immune system
// JSON overview: {movie id, movie title, ..., ratings ..., genre_id {list of ids}, genre_name...}
@WebServlet(name = "SingleMovieServlet", urlPatterns = "/api/single-movie")
public class SingleMovieServlet extends HttpServlet {
    private static final long serialVersionUID = 3L;

    //Connection pool
    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");

        String id = request.getParameter("id");
        request.getServletContext().log("getting id: " + id);
        PrintWriter out = response.getWriter();

        try (Connection conn = dataSource.getConnection()) {

            // rs to get movie and ratings info (only one of each)
            String query = "SELECT m.id AS movieId, m.title, m.year, m.director, COALESCE(r.rating, 0) AS rating, COALESCE(r.numVotes, 0) AS numVotes " +
                            "FROM movies AS m " +
                            "LEFT JOIN ratings AS r ON r.movieId = m.id " +
                            "WHERE m.id = ?";
            PreparedStatement statementMR = conn.prepareStatement(query);
            statementMR.setString(1, id);

            ResultSet rsMovieRating = statementMR.executeQuery();

            rsMovieRating.next();

            String movieId = rsMovieRating.getString("movieId");
            String movieTitle = rsMovieRating.getString("title");
            String movieYear = rsMovieRating.getString("year");
            String movieDirector = rsMovieRating.getString("director");
            String rating = rsMovieRating.getString("rating");
            String numVotes = rsMovieRating.getString("numVotes");

            rsMovieRating.close();
            statementMR.close();


            // rs to get genre info (id and name)
            query = "SELECT gim.genreId , g.name as genreName from movies as m, genres_in_movies as gim, genres as g" +
            " where m.id = gim.movieId and gim.genreId = g.id and m.id = ?";
            PreparedStatement statementG = conn.prepareStatement(query);
            statementG.setString(1, id);

            ResultSet rsGenre = statementG.executeQuery();
            List<String> genre_name_set = new ArrayList<String>() ; //protip i thought of AFTER typing this trash out: can do map instead

            while (rsGenre.next()) {
                genre_name_set.add(rsGenre.getString("genreName"));}
            Collections.sort(genre_name_set);

            rsGenre.close();
            statementG.close();

            // rs to get star info (id and name)
            query = "SELECT  s.id, s.name from movies as m, stars_in_movies as sim, stars as s" +
                    " where m.id = sim.movieId and sim.starId = s.id and m.id = ?";
            PreparedStatement statementS = conn.prepareStatement(query);
            statementS.setString(1, id);

            ResultSet rsStars = statementS.executeQuery();

            List<Object[]> stars_set = new ArrayList<>();

            //List<String> stars_id_set = new ArrayList<String>() ;
            //List<String> stars_name_set = new ArrayList<String>() ;

            while (rsStars.next()) {
                String star_id = rsStars.getString("id");
                String star_name = rsStars.getString("name");

                query = "SELECT COUNT(*) as sCount from stars_in_movies " +
                        "where starID = ? group by starID";

                PreparedStatement statementSM = conn.prepareStatement(query);
                statementSM.setString(1, star_id);
                ResultSet rsStarsM = statementSM.executeQuery();

                rsStarsM.next();
                int starCount =  rsStarsM.getInt("sCount");
                stars_set.add(new Object[]{starCount, star_id, star_name});

                statementSM.close();
                rsStarsM.close();
            }

            stars_set.sort((x, y) -> {
                int int1 = (int) x[0];
                int int2 = (int) y[0];
                int comparison = Integer.compare(int2, int1);
                if (comparison == 0) {
                    String str1 = x[2].toString();
                    String str2 = y[2].toString();
                    return str1.compareTo(str2);
                }
                return comparison;
            });


            rsStars.close();
            statementS.close();

            // converting all sets to json... because this language hates me more than i hate myself
            JsonArray genreNameJsonArray = new JsonArray();
            for (String value : genre_name_set) {
                genreNameJsonArray.add(value);}
            JsonArray starsIdJsonArray = new JsonArray();
            JsonArray starsNameJsonArray = new JsonArray();
            for (Object[] value : stars_set) {
                starsIdJsonArray.add(value[1].toString());
                starsNameJsonArray.add(value[2].toString());}

            // Instead of multiple JsonObjects, we just create a big one with everything.
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("movie_id", movieId);
            jsonObject.addProperty("movie_title", movieTitle);
            jsonObject.addProperty("movie_year", movieYear);
            jsonObject.addProperty("movie_director", movieDirector);
            jsonObject.addProperty("rating", rating);
            jsonObject.addProperty("num_votes", numVotes);
            jsonObject.add("genre_name",genreNameJsonArray);
            jsonObject.add("stars_id",starsIdJsonArray);
            jsonObject.add("stars_name",starsNameJsonArray);

            //After all rs are opened and closed.
            request.getServletContext().log("getting result");
            out.write(jsonObject.toString());
            response.setStatus(200);

        } catch (Exception e) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            out.write(jsonObject.toString());
            System.out.println(e.getMessage());//so i can see
            response.setStatus(500);
        } finally {
            out.close();
        }
        // Always remember to close db connection after usage. Here it's done by try-with-resources

    }
}
