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

@WebServlet(name = "SortingServlet", urlPatterns = "/api/sortBy")
public class SortingServlet extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        //set the type of response
        response.setContentType("application/json");
        //set up response writer
        PrintWriter out = response.getWriter();
        JsonObject jsonObject = new JsonObject();

        String sortBy = request.getParameter("sortBy");
        HttpSession session = request.getSession();
        try {
            switch(sortBy){
                case ("title_ASC-rating_DESC"):
                    session.setAttribute("order", "title ASC, rating DESC");
                    break;
                case ("title_DESC-rating_DESC"):
                    session.setAttribute("order", "title DESC, rating DESC");
                    break;
                case ("title_ASC-rating_ASC"):
                    session.setAttribute("order", "title ASC, rating ASC");
                    break;
                case ("title_DESC-rating_ASC"):
                    session.setAttribute("order", "title DESC, rating ASC");
                    break;
                case ("rating_ASC-title_DESC"):
                    session.setAttribute("order", "rating ASC, title DESC");
                    break;
                case ("rating_DESC-title_DESC"):
                    session.setAttribute("order", "rating DESC, title DESC");
                    break;
                case ("rating_ASC-title_ASC"):
                    session.setAttribute("order", "rating ASC, title ASC");
                    break;
                case("rating_DESC-title_ASC"):
                    session.setAttribute("order", "rating DESC, title ASC");
                    break;
                default:
                    session.setAttribute("order", "title ASC, rating DESC");
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
