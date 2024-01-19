import com.google.gson.JsonObject;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.*;
import java.util.HashMap;
import jakarta.servlet.http.HttpSession;
import org.jasypt.util.password.StrongPasswordEncryptor;

@WebServlet(name = "DashboardLoginServlet", urlPatterns = "/_dashboard/api/dashboard-login")
public class DashboardLoginServlet extends HttpServlet {
    private static final long serialVersionUID = 5L;

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

        request.getServletContext().log("getting employee email: " + email);
        PrintWriter out = response.getWriter();
        JsonObject responseJsonObject = new JsonObject();

        String gRecaptchaResponse = request.getParameter("g-recaptcha-response");
        try{
            RecaptchaVerifyUtils.verify(gRecaptchaResponse);
        } catch(Exception e){
            responseJsonObject.addProperty("status", "fail");
            responseJsonObject.addProperty("message", "ReCaptcha Failed");

            out.write(responseJsonObject.toString());
            response.setStatus(200);
            return;
        }

        try (Connection conn = dataSource.getConnection()){
            // Generate a SQL query
            String query = "SELECT e.password, e.email from employees AS e WHERE e.email = ?";
            request.getServletContext().log("query：" + query);

            PreparedStatement statement = conn.prepareStatement(query);
            statement.setString(1, email);
            ResultSet rs = statement.executeQuery();

            if (rs.next()){
                boolean success = verifyPassword(password,rs);
                if (success){
                    HttpSession session = request.getSession();
                    // query attribute will be the query string we actually modify and execute
//                    session.setAttribute("query", "");
                    session.setAttribute("email", rs.getString("email"));
                    session.setAttribute("pageNumber", 1);
                    session.setAttribute("numberOfMovies", 10);
                    session.setAttribute("order",  "title ASC, rating DESC");
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
            //System.out.printf("ive sent it" + responseJsonObject.toString());

            rs.close();
            statement.close();
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
    }

    public Boolean verifyPassword(String password, ResultSet rs) throws Exception{
        // get the encrypted password from the database
        String encryptedPassword = rs.getString("password");

        // use the same encryptor to compare the user input password with encrypted password stored in DB
        return new StrongPasswordEncryptor().checkPassword(password, encryptedPassword);

    }
}