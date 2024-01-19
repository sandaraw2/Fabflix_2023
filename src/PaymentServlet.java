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
import jakarta.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;
import java.text.SimpleDateFormat;
import java.util.Date;

@WebServlet(name = "PaymentServlet", urlPatterns = "/api/payment")
public class PaymentServlet extends HttpServlet {
    private static final long serialVersionUID = 9L;

    //Connection pool
    private DataSource dataSource;
    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            System.out.println(e.getMessage());
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");

        String firstName = request.getParameter("first_name");
        String lastName = request.getParameter("last_name");
        String ccNumber = request.getParameter("cc_number");

        String stringExp = request.getParameter("expiration");
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        Date expiration = new Date();
        try {
            expiration = dateFormat.parse(stringExp);
        } catch (java.text.ParseException e) {
        }



        HttpSession session = request.getSession(true);
        String customerId = session.getAttribute("id").toString();
        String ccId = session.getAttribute("ccId").toString();

        PrintWriter out = response.getWriter();
        JsonObject jsonObject = new JsonObject();
        try (Connection conn = dataSource.getConnection()){
            String query = "SELECT cc.firstName, cc.lastName, cc.expiration " +
                            "FROM creditcards AS cc WHERE cc.id = ?";

            PreparedStatement statement = conn.prepareStatement(query);
            statement.setString(1, ccId);
            ResultSet rs = statement.executeQuery();

            if (rs.next()){
                String trueFName = rs.getString("firstName");
                String trueLName = rs.getString("lastName");
                Date trueExp = rs.getDate("expiration");

                //System.out.println(firstName + " " + lastName + " " + expiration);
                //System.out.println(trueFName + " " + trueLName + " " + trueExp);

                if (trueFName.equals(firstName) && trueLName.equals(lastName) && trueExp.equals(expiration)){
                    //everything works im not a charity not gonna be hinting allthat
                    jsonObject.addProperty("status", "success");
                    jsonObject.addProperty("message", "Success");

                    HashMap<String, Integer> cart = (HashMap<String, Integer>) session.getAttribute("cart");
                    java.util.Date utilDate = new java.util.Date();
                    java.sql.Date currentDate = new java.sql.Date(utilDate.getTime());

                    String salesQuery = "INSERT INTO sales (customerId, movieId, saleDate) VALUES (?,?,?)";
                    PreparedStatement salesStatement = conn.prepareStatement(salesQuery,Statement.RETURN_GENERATED_KEYS);
                    int loop1 = 0;
                    int itemCount = 0;
                    for (Map.Entry<String, Integer> entry : cart.entrySet()) {
                        String movieID = entry.getKey();
                        int quantity = entry.getValue();

                        salesStatement.setString(1, customerId);
                        salesStatement.setString(2, movieID);
                        salesStatement.setDate(3, currentDate);

                        int rowsAffected = salesStatement.executeUpdate();

                        if (loop1 == 0){
                            loop1 = 1;
                            ResultSet generatedKeys = salesStatement.getGeneratedKeys();
                            if (generatedKeys.next()){
                                int generatedID = generatedKeys.getInt(1);
                                response.setHeader("startID", Integer.toString(generatedID));
                            }
                            generatedKeys.close();
                        }

                        itemCount++;
                    }
                    response.setHeader("itemCount", Integer.toString(itemCount));
                    salesStatement.close();
                }
                else{
                    jsonObject.addProperty("status", "fail");
                    jsonObject.addProperty("message", "Incorrect payment information, please try again.");}
            }
            else {
                jsonObject.addProperty("status", "fail");
                jsonObject.addProperty("message", "Incorrect payment information, please try again.");}

            out.write(jsonObject.toString());
            rs.close();
            statement.close();
            response.setStatus(200);
        } catch (Exception e) {
            JsonObject jsonObjectE = new JsonObject();
            jsonObjectE.addProperty("errorMessage", e.getMessage());
            out.write(jsonObjectE.toString());
            System.out.println(e.getMessage());//so i can see
            response.setStatus(500);
        } finally {
            out.close();
        }

    }

}
