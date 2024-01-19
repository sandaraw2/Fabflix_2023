import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

import jakarta.servlet.http.HttpSession;
import types.Genre;
import types.Movie;
import types.Star;

// Declaring a WebServlet called StarsServlet, which maps to url "/api/stars"
@WebServlet(name = "MovieListServlet", urlPatterns = "/api/movies")
public class MovieListServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    // Connection pool
    private DataSource dataSource;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
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
        HttpSession session = request.getSession();
        long queryStartTime = System.nanoTime();

        String newNumMovies = request.getParameter("newNumMovies");
        if (newNumMovies != null){
            session.setAttribute("pageNumber", 1);
            session.setAttribute("numberOfMovies", Integer.parseInt(newNumMovies));
        }

        String changePage =  request.getParameter("changePage");
        if (changePage != null){
            int newCount = (int) session.getAttribute("pageNumber") + Integer.parseInt(changePage);
            if (newCount > 0)
                session.setAttribute("pageNumber", newCount );
        }


        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        //Build onto this string as we go
        String query_string = "SELECT * FROM movies LEFT JOIN ratings ON movies.id = ratings.movieID ";
        // Get a connection from dataSource
        try (Connection conn = dataSource.getConnection()) {
            //last page generator to make sure we dont go overrrrrrrrrrr
            int page = (int) session.getAttribute("pageNumber");
            String numberOfMovies = session.getAttribute("numberOfMovies").toString();
            Statement countStatement = conn.createStatement();
            String offset = Integer.toString((page-1)*Integer.parseInt(numberOfMovies));

            //Build query
            String title = null ,director = null ,star = null;

            if(session.getAttribute("title") != null){

               title = session.getAttribute("title").toString();
               director = session.getAttribute("director").toString();
               star = session.getAttribute("star").toString();
               if(session.getAttribute("fulltext") == "ON"){
                   query_string = "SELECT DISTINCT m.*, COALESCE(rating, 0) AS rating FROM movies AS m " +
                           "LEFT JOIN ratings AS r ON m.id = r.movieId " +
                           "JOIN stars_in_movies as sim ON sim.movieId = m.id " +
                           "JOIN stars as s ON s.id = sim.starId " +
                           "WHERE MATCH(m.title) AGAINST (? IN BOOLEAN MODE) " +
                           "AND LOWER(m.director) LIKE ? "  +
                           "AND LOWER(s.name) LIKE ?";
               }else{
                   query_string = "SELECT DISTINCT m.*, COALESCE(rating, 0) AS rating FROM movies AS m " +
                           "LEFT JOIN ratings AS r ON m.id = r.movieId " +
                           "JOIN stars_in_movies as sim ON sim.movieId = m.id " +
                           "JOIN stars as s ON s.id = sim.starId " +
                           "WHERE LOWER(m.title) LIKE ? " +
                           "AND LOWER(m.director) LIKE ? "  +
                           "AND LOWER(s.name) LIKE ?";
               }

            }else{
                query_string = "SELECT DISTINCT m.*, COALESCE(rating, 0) AS rating FROM movies AS m " +
                        "LEFT JOIN ratings AS r ON m.id = r.movieId " +
                        "JOIN stars_in_movies as sim ON sim.movieId = m.id " +
                        "JOIN stars as s ON s.id = sim.starId";

            }

            String year = null;
            if(session.getAttribute("year") != null){
                year = session.getAttribute("year").toString();
                query_string += "AND m.year = ?";
            }


            String genre = null;
            if(session.getAttribute("genre")!=null){
                genre = session.getAttribute("genre").toString();
                query_string = "SELECT m.*, COALESCE(rating, 0) AS rating " +
                        "FROM movies AS m " +
                        "LEFT JOIN ratings AS r ON m.id = r.movieId " +
                        "JOIN genres_in_movies AS gim ON gim.movieId = m.id " +
                        "JOIN genres AS g ON gim.genreId = g.id " +
                        "WHERE g.name LIKE ?";
            }

            String letter = null;
            if(session.getAttribute("letter") != null){
                letter = session.getAttribute("letter").toString();
                query_string = "SELECT m.*, COALESCE(rating, 0) AS rating " +
                        "FROM movies AS m " +
                        "LEFT JOIN ratings AS r ON m.id = r.movieId " +
                        "WHERE m.title LIKE ?";
            }

            String order = session.getAttribute("order").toString();
            query_string = query_string + " ORDER BY " + order + " LIMIT ? OFFSET ?";


            long JDBCStartTime = System.nanoTime();
            //create prepared statement from query string
            PreparedStatement query_statement = conn.prepareStatement(query_string);

            //set parameters in prepared query statement
            int numParams = 1;
            if(title != null){
                query_statement.setString(numParams++, title);
                query_statement.setString(numParams++, director);
                query_statement.setString(numParams++, star);
                //clear all attributes
                session.setAttribute("title", null);
                session.setAttribute("director", null);
                session.setAttribute("star", null);

            }

            if(year!= null){
                query_statement.setString(numParams++, year);
                //clear all attributes
                session.setAttribute("year", null);
            }

            if(genre!=null){
                query_statement.setString(numParams++, genre);
                //clear all attributes
                session.setAttribute("genre", null);
            }
            if(letter!= null){
                query_statement.setString(numParams++, letter);
                //clear all attributes
                session.setAttribute("letter", null);
            }
            //set limit page parmaeter values
            query_statement.setInt(numParams++, Integer.parseInt(numberOfMovies));
            query_statement.setInt(numParams, Integer.parseInt(offset));

            System.out.println(query_statement);
            ResultSet rs = query_statement.executeQuery();
            //Process Movie List results

            JsonArray jsonArray = new JsonArray();
            ArrayList<Movie> movies = new ArrayList<>();
            // Iterate through each movie (row)
            while (rs.next()) {
                //get information about current movie
                String movie_id = rs.getString("id");
                String movie_title = rs.getString("title");
                String movie_year = rs.getString("year");
                String movie_director = rs.getString("director");
                String movie_rating = rs.getString("rating");

                //create movie instance
                Movie movie = new Movie(movie_id,movie_title,Integer.parseInt(movie_year),movie_director,
                        Float.parseFloat(movie_rating));

                //Query string for genres
                String genreQuery = "SELECT * FROM genres AS g, genres_in_movies AS gim WHERE gim.movieId = ? " +
                        "AND gim.genreId = g.id " +
                        "ORDER BY g.name ASC";

                //prepared statement
                PreparedStatement genreStatement = conn.prepareStatement(genreQuery);
                genreStatement.setString(1, movie_id);
                //execute genreQuery
                ResultSet genreRS = genreStatement.executeQuery();

                while(genreRS.next()){
                    //Create genre instance
                    Integer genre_id = genreRS.getInt("id");
                    String genre_name = genreRS.getString("name");

                    //Add into genre to movie
                    Genre test_genre = new Genre(genre_id,genre_name);
                    movie.addGenre(test_genre);
                }

                //close genre Result Set and statement
                genreRS.close();
                genreStatement.close();


                //Query String for Stars

                String starQuery = "SELECT s.* FROM stars AS s " +
                        "JOIN ( SELECT starId, COUNT(movieId) AS movieCount " +
                        "FROM stars_in_movies WHERE movieId = ? " +
                        "GROUP BY starId) AS sim ON sim.starId = s.id " +
                        "ORDER BY sim.movieCount DESC, s.name ASC";

                //prepared statement
                PreparedStatement starStatement = conn.prepareStatement(starQuery);
                starStatement.setString(1, movie_id);
                //execute genreQuery
                ResultSet starRS = starStatement.executeQuery();

                int count = 0;
                while(starRS.next() && count < 3){
                    String star_id = starRS.getString("id");
                    String star_name = starRS.getString("name");
                    Integer star_birthYear = starRS.getInt("birthYear");

                    //Add star into movie
                    movie.addStar(new Star(star_id,star_name, star_birthYear));
                    count++;
                }
                //close star Result set and statement
                starRS.close();
                starStatement.close();
                //Add movie to ArrayList
                movies.add(movie);
            }
            rs.close();
            query_statement.close();
            conn.close();

            long JDBCTotalTime = System.nanoTime() - JDBCStartTime;

            for(Movie movie: movies){
                JsonObject jsonObject = new JsonObject();
                // Create a JsonObject based on the data we retrieve from rs
                jsonObject.addProperty("movie_id", movie.getId());
                jsonObject.addProperty("movie_title", movie.getTitle());
                jsonObject.addProperty("movie_year", movie.getYear());
                jsonObject.addProperty("movie_director", movie.getDirector());

                JsonArray genres = new JsonArray();
                for (int i = 0; i < Math.min(3, movie.getGenres().size()); i++) {
                    genres.add(movie.getGenres().get(i).getName());
                }

                jsonObject.add("movie_genres", genres);

                JsonArray stars_name = new JsonArray();
                JsonArray stars_id = new JsonArray();
                for (int i = 0; i < Math.min(3, movie.getStars().size()); i++) {
                    stars_name.add(movie.getStars().get(i).getName());
                    stars_id.add(movie.getStars().get(i).getId());
                }

                jsonObject.add("stars_name", stars_name);
                jsonObject.add("stars_id", stars_id);
                jsonObject.addProperty("movie_rating", movie.getRating());

                jsonArray.add(jsonObject);
            }

            long queryTotalTime = System.nanoTime() - queryStartTime;

            String contextPath = getServletContext().getRealPath("/");

            String queryFilePath =contextPath+"queryTime.csv";

            System.out.println(queryFilePath);

            File csvFileQueryTime = new File(queryFilePath);

            try (PrintWriter outQuery = new PrintWriter(new FileWriter(csvFileQueryTime,true))){

                outQuery.println(String.valueOf(queryTotalTime) + " | " + String.valueOf(JDBCTotalTime));
            } catch (FileNotFoundException e){
                System.out.println(e.toString());
            }

            request.getServletContext().log("getting " + jsonArray.size() + " results");
            out.write(jsonArray.toString());
            response.setHeader("pageNumber", Integer.toString(page));
            response.setStatus(200);
        } catch (Exception e) {

            // Write error message JSON object to output
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            out.write(jsonObject.toString());
            System.out.println("Error Message: " + e.getMessage());
            // Set response status to 500 (Internal Server Error)
            response.setStatus(500);
        } finally {
            out.close();
        }
    }
}
