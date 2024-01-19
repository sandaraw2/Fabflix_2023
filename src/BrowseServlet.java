import com.google.gson.Gson;
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
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;


@WebServlet(name = "BrowseServlet", urlPatterns = "/api/browse")
public class BrowseServlet extends HttpServlet {
    //Connection pool
    private DataSource dataSource;
    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }


    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        //set the type of response
        response.setContentType("application/json");
        //set up response writer
        PrintWriter out = response.getWriter();
        JsonObject jsonObject = new JsonObject();

        String letter = request.getParameter("letter");


        String encodedGenre = request.getParameter("genre");
        String genre = URLDecoder.decode(encodedGenre, StandardCharsets.UTF_8);

        System.out.println(genre);

        try (Connection conn = dataSource.getConnection()) {
            HttpSession session = request.getSession();
            if(letter != null){
                session.setAttribute("letter", letter+'%');
                session.setAttribute("title", null);
                session.setAttribute("year", null);
                session.setAttribute("director", null);
                session.setAttribute("star", null);
                session.setAttribute("genre", null);
            }else{
                session.setAttribute("genre", genre);
                session.setAttribute("letter", null);
                session.setAttribute("title", null);
                session.setAttribute("year", null);
                session.setAttribute("director", null);
                session.setAttribute("star", null);
            }
            jsonObject.addProperty("status", "success");
            out.write(jsonObject.toString());
            response.setStatus(200);
            // after setting session attribute to movieList, we redirect to that page
        }catch (Exception e) {
            jsonObject.addProperty("errorMessage", e.getMessage());
            out.write(jsonObject.toString());
            // Set response status to 500 (Internal Server Error)
            response.setStatus(500);
        } finally{
            out.close();

        }

    }
}
