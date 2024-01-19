import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.google.gson.JsonObject;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

@WebServlet(name = "AddMovieServlet", urlPatterns = "/_dashboard/api/add-movie")
public class AddMovieServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    //Connection pool
    private DataSource dataSource;
    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedbMaster");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        //set up response
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        JsonObject jsonObject = new JsonObject();
        String statusMessage ="";
        // get Input parameters
        String movieTitle = request.getParameter("movie_title");
        int movieYear = Integer.parseInt(request.getParameter("movie_year"));
        String movieDirector = request.getParameter("movie_director");
        String starName = request.getParameter("star_name");
        String genreName = request.getParameter("genre_name");
        String starBirthYear = request.getParameter("star_birth_year");


        // Establish the database connection
        try (Connection conn = dataSource.getConnection()) {
            // Prepare the SQL statement to call the stored procedure
            String sql = "{CALL add_movie(?, ?, ?, ?, ?, ?)}";
            try (PreparedStatement statement = conn.prepareCall(sql)) {
                // Set the input parameters
                statement.setString(1, movieTitle);
                statement.setInt(2, movieYear);
                statement.setString(3, movieDirector);
                statement.setString(4, starName);
                if(starBirthYear.isEmpty()){
                    statement.setString(5, null);
                }else{
                    statement.setInt(5, Integer.parseInt(starBirthYear));
                }
                statement.setString(6, genreName);

                // Execute the stored procedure
                statement.execute();

                // get status message from result set
                ResultSet rs = statement.getResultSet();
                while(rs.next()){
                    statusMessage = rs.getString("status_message");
                }

                // Set the status message in the JSON response
                jsonObject.addProperty("status", "success");
                jsonObject.addProperty("message", statusMessage);

            }
        }catch (SQLException e) {
        e.printStackTrace();

        // Set the error message in the JSON response
        jsonObject.addProperty("status", "failed");
        jsonObject.addProperty("message", e.toString());
    }

        // Send the JSON response to the client
        out.println(jsonObject.toString());
    }
}
