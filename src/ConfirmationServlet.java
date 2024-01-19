import com.google.gson.JsonArray;
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

@WebServlet(name = "ConfirmationServlet", urlPatterns = "/api/confirmation")
public class ConfirmationServlet extends HttpServlet {
    private static final long serialVersionUID = 10L;

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

        int startID = Integer.parseInt(request.getParameter("startID"));
        int itemCount = Integer.parseInt(request.getParameter("itemCount"));
        int endID = startID + itemCount - 1;

        try (Connection conn = dataSource.getConnection()) {
            JsonArray jsonArray = new JsonArray();

            String query = "SELECT s.id, s.movieId, m.title FROM sales as s INNER JOIN "+
                    "movies as m ON m.id = s.movieId WHERE s.id BETWEEN ? AND ?";
            PreparedStatement statement = conn.prepareStatement(query);
            statement.setInt(1, startID);
            statement.setInt(2, endID);
            ResultSet rs = statement.executeQuery();

            int totalPrice = 0;
            while(rs.next()){
                JsonObject jsonObject = new JsonObject();
                String movieID = rs.getString("movieId");
                String saleID = rs.getString("id");
                String movieTitle = rs.getString("title");
                int quantity = cart.get(movieID);
                int price = quantity*(Math.abs(movieID.hashCode()) % 20 + 3);

                jsonObject.addProperty("sale_id", saleID);
                jsonObject.addProperty("movie_title", movieTitle);
                jsonObject.addProperty("quantity", quantity);
                jsonObject.addProperty("price", price);

                totalPrice += price;
                jsonArray.add(jsonObject);
            }

            rs.close();
            statement.close();

            // reset cart
            session.setAttribute("cart", new HashMap<String, Integer>());

            out.write(jsonArray.toString());
            response.setHeader("totalPrice", Integer.toString(totalPrice));
            response.setStatus(200);
            System.out.println(jsonArray.toString());
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