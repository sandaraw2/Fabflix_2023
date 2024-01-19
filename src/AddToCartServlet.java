import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.HashMap;

// Declaring a WebServlet called SessionServlet, which maps to url "/session"
@WebServlet(name = "AddToCartServlet", urlPatterns = "/api/addToCart")
public class AddToCartServlet extends HttpServlet {
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // Get an instance of current session on the request
        HttpSession session = request.getSession(true);
        HashMap<String, Integer> cart = (HashMap<String, Integer>) session.getAttribute("cart");

        String movieID = request.getParameter("id");
        boolean decrease = Boolean.parseBoolean(request.getParameter("decrease"));
        boolean deleteID = Boolean.parseBoolean(request.getParameter("deleteID"));

        if (deleteID){
            cart.remove(movieID);
        } else if (decrease) {
            if (cart.get(movieID) > 1){
                cart.put(movieID, cart.get(movieID) - 1);}
            else{
                cart.remove(movieID);
            }
        }
        else{
            int oldQ = 1;
            if (cart.containsKey(movieID)){
                oldQ += cart.get(movieID);
            }
            cart.put(movieID, oldQ);
        }

        session.setAttribute("cart", cart);
    }
}

