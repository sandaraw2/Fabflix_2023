import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;

@WebFilter(filterName = "LoginFilter", urlPatterns = "/*")
public class LoginFilter implements Filter {
    private final ArrayList<String> allowedURIs = new ArrayList<>();

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        System.out.println("LoginFilter: " + httpRequest.getRequestURI());
        if (this.isUrlAllowedWithoutLogin(httpRequest.getRequestURI())) {
            chain.doFilter(request, response);
            return;
        }

        if (httpRequest.getSession().getAttribute("id") != null || httpRequest.getSession().getAttribute("email") != null) {
            chain.doFilter(request, response);
        } else {
            httpResponse.sendRedirect("login.html");
        }
    }

    private boolean isUrlAllowedWithoutLogin(String requestURI) {
        return allowedURIs.stream().anyMatch(requestURI.toLowerCase()::endsWith);
    }

    public void init(FilterConfig fConfig) {
        allowedURIs.add("login.html");
        allowedURIs.add("js/login.js");
        allowedURIs.add("api/login");
        allowedURIs.add("_dashboard");
        allowedURIs.add("_dashboard/dashboard-login.html");
        allowedURIs.add("js/_dashboard/dashboard-login.js");
        allowedURIs.add("api/dashboard-login");
        allowedURIs.add("css/smStyles.css");
        allowedURIs.add("css/styles.css");

    }

    public void destroy() {
    }

}