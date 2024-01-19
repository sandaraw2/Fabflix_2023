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
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.ArrayList;

import jakarta.servlet.http.HttpSession;


@WebServlet(name = "AdvancedSearchServlet", urlPatterns = "/api/advancedSearch")
public class AdvancedSearchServlet extends HttpServlet {
    private static final long serialVersionUID = 11L;

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
        //get request info
        String title = request.getParameter("title");
        String year = request.getParameter("year");
        String director = request.getParameter("director");
        String star = request.getParameter("star");

        //set the type of response
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        JsonObject responseJsonObject = new JsonObject();

        try(Connection conn = dataSource.getConnection()){
            HttpSession session = request.getSession();
            //get query frm helper function
            //grabs default_query template, fills it in and places
            // in query attribute for movieList to execute
            setQuery(title, year, director, star, conn, session);
            responseJsonObject.addProperty("status", "success");
            responseJsonObject.addProperty("message", "success");
            out.write(responseJsonObject.toString());
            response.setStatus(200);

        }catch(Exception e){
            // Write error message JSON object to output
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            out.write(jsonObject.toString());

            // Set response status to 500 (Internal Server Error)
            response.setStatus(500);
        } finally {
            out.close();
        }

    }

    //Fills in default_query template based on the search parameters
    private void setQuery(String t, String y, String d, String s, Connection c, HttpSession sesh){
        if(t!=null && !t.isEmpty()){

            //get mysql stopwords from session
            ArrayList<String> stopwords = (ArrayList<String>)sesh.getAttribute("stopwords");

            //tokenize string
            String[] tokenized_title = t.split(" ");

            //create token_string for match against
            String token_string = "";
            for(String token : tokenized_title){
                //add to string if not stopword
                if(!stopwords.contains(token)){
                    token_string = token_string + "+" + token + "* ";
                }
            }

            sesh.setAttribute("title",  token_string);
            sesh.setAttribute("fulltext", "ON");
        }else{
            sesh.setAttribute("title", "%");
            sesh.setAttribute("fulltext", "OFF");

        }
        if(y!=null && !y.isEmpty()){
            sesh.setAttribute("year", y);
        }else{
            sesh.setAttribute("year", null);
        }
        if(d!=null && !d.isEmpty()){
            sesh.setAttribute("director", "%"+d.toLowerCase()+"%");
        }else{
            sesh.setAttribute("director", "%");
        }
        if(s!=null && !s.isEmpty()){
            sesh.setAttribute("star", "%"+s.toLowerCase()+"%");
        }else{
            sesh.setAttribute("star", "%");
        }
        sesh.setAttribute("genre", null);
        sesh.setAttribute("letter", null);

    }
}

