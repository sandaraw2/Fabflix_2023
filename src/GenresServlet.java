import com.google.gson.Gson;
import com.google.gson.JsonObject;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;


@WebServlet(name = "GenresServlet", urlPatterns = "/api/genres")
public class GenresServlet extends HttpServlet {
    //Connection pool
    private DataSource dataSource;
    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException{
        response.setContentType("application/json");
        //set up response writer
        PrintWriter out = response.getWriter();
        try( Connection conn = dataSource.getConnection()) {
            //create connection to dataSource
            Statement genreStatement = conn.createStatement();
            //get query frm helper function
            String query = "SELECT name FROM genres";
            //execute query
            ResultSet genreResult = genreStatement.executeQuery(query);
            //process results
            ArrayList<String> genres = new ArrayList<>();
            while (genreResult.next()) {
                genres.add(genreResult.getString("name"));
            }
            //convert to json
            out.write(new Gson().toJson(genres));
            genreResult.close();
            genreStatement.close();

        }catch (Exception e) {
            // Write error message JSON object to output
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            out.write(jsonObject.toString());

            // Set response status to 500 (Internal Server Error)
            response.setStatus(500);
        }finally{
            out.close();
        }
    }
}
