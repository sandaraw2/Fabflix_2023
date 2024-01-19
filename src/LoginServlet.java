import com.google.gson.JsonObject;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.*;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.sql.*;
import java.util.HashMap;
import jakarta.servlet.http.HttpSession;
import org.jasypt.util.password.StrongPasswordEncryptor;
import java.util.ArrayList;
@WebServlet(name = "LoginServlet", urlPatterns = "/api/login")
public class LoginServlet extends HttpServlet {
    private static final long serialVersionUID = 5L;
    public static final String SITE_VERIFY_URL = "https://www.google.com/recaptcha/api/siteverify";

    //Connection pool
    private DataSource dataSource;
    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");

        String email = request.getParameter("email");
        String password = request.getParameter("password");

        request.getServletContext().log("getting email: " + email);
        PrintWriter out = response.getWriter();
        JsonObject responseJsonObject = new JsonObject();

        String gRecaptchaResponse = request.getParameter("g-recaptcha-response");
        String androidBypass = request.getParameter("androidBypass");

        if(androidBypass == null ){
            try{
                RecaptchaVerifyUtils.verify(gRecaptchaResponse);
            } catch(Exception e){
                responseJsonObject.addProperty("status", "fail");
                responseJsonObject.addProperty("message", "ReCaptcha Failed");

                out.write(responseJsonObject.toString());
                response.setStatus(200);
                return;
            }
        }

        try (Connection conn = dataSource.getConnection()){
            // Generate a SQL query
            String query = "SELECT c.password, c.ccId, c.id from customers AS c WHERE c.email = ?";
            request.getServletContext().log("query：" + query);

            PreparedStatement statement = conn.prepareStatement(query);
            statement.setString(1, email);
            ResultSet rs = statement.executeQuery();

            //get Stopword list from mysql
            String stopwords_query = "SELECT * FROM information_schema.innodb_ft_default_stopword;";
            PreparedStatement stopwords_statement = conn.prepareStatement(stopwords_query);
            ResultSet stopwords_rs = stopwords_statement.executeQuery();

            ArrayList<String> stopwords = new ArrayList<>();
            while(stopwords_rs.next()){
                String word = stopwords_rs.getString("value");
                stopwords.add(word);
            }

            stopwords_rs.close();
            stopwords_statement.close();

            if (rs.next()){
                boolean success = verifyPassword(password,rs);
                if (success){
                    HttpSession session = request.getSession();
                    // query attribute will be the query string we actually modify and execute
                    //                    session.setAttribute("query", "");
                    session.setAttribute("id", rs.getString("id"));
                    session.setAttribute("ccId", rs.getString("ccId"));
                    session.setAttribute("title", null);
                    session.setAttribute("fulltext", "OFF");
                    session.setAttribute("year", null);
                    session.setAttribute("director", null);
                    session.setAttribute("star", null);
                    session.setAttribute("pageNumber", 1);
                    session.setAttribute("numberOfMovies", 10);
                    session.setAttribute("cart", new HashMap<String, Integer>());
                    session.setAttribute("order",  "title ASC, COALESCE(rating, 0) DESC");
                    session.setAttribute("stopwords", stopwords);
                    responseJsonObject.addProperty("status", "success");
                    responseJsonObject.addProperty("message", "success");
                }
                else{ //Case: Invalid Password
                    responseJsonObject.addProperty("status", "fail");
                    responseJsonObject.addProperty("message", "Password is incorrect.");
                }
            }
            else{ //Case: Invalid email
                responseJsonObject.addProperty("status", "fail");
                responseJsonObject.addProperty("message", "Email " + email + " doesn't exist");
            }

            out.write(responseJsonObject.toString());

            rs.close();
            statement.close();
            response.setStatus(200);

        } catch (SQLException e) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            out.write(jsonObject.toString());
            System.out.println(e.getMessage());//so i can see
            response.setStatus(500);
        } finally {
            out.close();
        }
    }

    public Boolean verifyPassword(String password, ResultSet rs) throws SQLException{
        // get the encrypted password from the database
        String encryptedPassword = rs.getString("password");

        // use the same encryptor to compare the user input password with encrypted password stored in DB
        return new StrongPasswordEncryptor().checkPassword(password, encryptedPassword);

    }
}
