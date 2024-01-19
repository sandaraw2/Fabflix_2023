import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
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
import java.util.HashMap;
import java.util.Map;

@WebServlet(name = "ShoppingCartServlet", urlPatterns = "/api/shopping-cart")
public class ShoppingCartServlet extends HttpServlet {
    private static final long serialVersionUID = 8L;

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
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        HttpSession session = request.getSession(true);
        HashMap<String, Integer> cart = (HashMap<String, Integer>) session.getAttribute("cart");

        try (Connection conn = dataSource.getConnection()) {

            JsonArray jsonArray = new JsonArray();

            int totalPrice = 0;
            for (Map.Entry<String, Integer> entry : cart.entrySet()) {
                JsonObject jsonObject = new JsonObject();

                String movieID = entry.getKey();
                int quantity = entry.getValue();
                int price = Math.abs(movieID.hashCode()) % 20 + 3;

                String query = "SELECT title FROM movies WHERE id = ?";
                PreparedStatement statement = conn.prepareStatement(query);
                statement.setString(1, movieID);
                ResultSet rs = statement.executeQuery();

                rs.next();
                String movieName = rs.getString("title");

                jsonObject.addProperty("movie_title", movieName);
                jsonObject.addProperty("movie_id", movieID);
                jsonObject.addProperty("quantity", quantity);
                jsonObject.addProperty("price", price);

                totalPrice += quantity*price;

                jsonArray.add(jsonObject);
                rs.close();
                statement.close();
            }


            out.write(jsonArray.toString());

            response.setHeader("totalPrice", Integer.toString(totalPrice));
            response.setStatus(200);
        } catch (Exception e) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            out.write(jsonObject.toString());
            System.out.println(e.getMessage());
            response.setStatus(500);
        } finally {
            out.close();
        }
    }
}