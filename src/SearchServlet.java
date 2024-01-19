import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
@WebServlet(name = "SearchServlet", urlPatterns = "/api/search")
public class SearchServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    //Connection pool
    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession();
        //get request info
        String title = request.getParameter("title");
        //get mysql stopwords from session
        ArrayList<String> stopwords = (ArrayList<String>)session.getAttribute("stopwords");
        //tokenize string
        String[] tokenized_title = title.split(" ");

        //create token_string for match against
        String token_string = "";
        for(String token : tokenized_title){
            //add to string if not stopword
            if(!stopwords.contains(token)){
                token_string = token_string + "+" + token + "* ";
            }
        }
        //set the type of response
        response.setContentType("application/json");
        //set up response writer
        PrintWriter out = response.getWriter();

        try (Connection conn = dataSource.getConnection()){
            //create fulltext index for movie_title
            String dropIndexSQL = "DROP INDEX movie_title ON movies;";
            String createIndexSQL = "CREATE FULLTEXT INDEX movie_title ON movies(title);";
            try{
                conn.prepareStatement(dropIndexSQL).execute();
            }catch(Exception e){
                System.out.println("Index does not exist, error ignored");
            }
            conn.prepareStatement(createIndexSQL).execute();

            String query = "SELECT * " +
                    "FROM movies AS m, ratings AS r " +
                    "WHERE m.id = r.movieId " +
                    "AND MATCH(m.title) AGAINST (? IN BOOLEAN MODE)";

            //set session attrubute to movie
            session.setAttribute("title",  token_string);
            session.setAttribute("fulltext", "ON");
            session.setAttribute("year", null);
            session.setAttribute("director", "%");
            session.setAttribute("star", "%");
            session.setAttribute("genre", null);
            session.setAttribute("letter", null);

            //Execute query
            PreparedStatement query_statement = conn.prepareStatement(query);
            query_statement.setString(1, token_string);
            System.out.println(query_statement);
            System.out.println(query_statement);
            ResultSet movieRS = query_statement.executeQuery();
            JsonArray jsmovieArray = new JsonArray();

            // just display top 10
            int counter = 1;
            while (movieRS.next() && counter <=10) {
                // process result set
                String movie_id = movieRS.getString("id");
                String movie_title = movieRS.getString("title");
                JsonObject movieObject = new JsonObject();
                // Create a JsonObject based on the data we retrieve from rs
                movieObject.addProperty("movie_id", movie_id);
                movieObject.addProperty("movie_title", movie_title);
                jsmovieArray.add(movieObject);
                counter++;
            }
            movieRS.close();
            conn.close();

            // Write JSON string to output
            out.write(jsmovieArray.toString());

            // Set response status to 200 (OK)
            response.setStatus(200);

        } catch (Exception e) {
            // Write error message JSON object to output
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            out.write(jsonObject.toString());
            System.out.println("SearchServlet: Error Message: " + e.getMessage());
            // Set response status to 500 (Internal Server Error)
            response.setStatus(500);
        }
    }
}