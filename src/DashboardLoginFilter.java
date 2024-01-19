import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;

@WebFilter(filterName = "DashboardLoginFilter", urlPatterns = "/_dashboard/*")
public class DashboardLoginFilter implements Filter {
    private final ArrayList<String> allowedURIs = new ArrayList<>();

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        System.out.println("DashboardFilter: " + httpRequest.getRequestURI());
        if (this.isUrlAllowedWithoutLogin(httpRequest.getRequestURI())) {
            chain.doFilter(request, response);
            return;
        }

        if (httpRequest.getSession().getAttribute("email") == null) {
            //httpResponse.sendRedirect(httpResponse.encodeRedirectURL(httpRequest.getContextPath() + "/dashboard-login.html"));
            httpResponse.sendRedirect("_dashboard/dashboard-login.html");
        } else {
            chain.doFilter(request, response);
        }
    }

    private boolean isUrlAllowedWithoutLogin(String requestURI) {
        return allowedURIs.stream().anyMatch(requestURI.toLowerCase()::endsWith);
    }

    public void init(FilterConfig fConfig) {
        allowedURIs.add("login.html");
        allowedURIs.add("js/login.js");
        allowedURIs.add("api/login");
        allowedURIs.add("_dashboard/dashboard-login.html");
        allowedURIs.add("js/_dashboard/dashboard-login.js");
        allowedURIs.add("api/dashboard-login");
        allowedURIs.add("css/smStyles.css");
        allowedURIs.add("css/styles.css");
    }

    public void destroy() {
    }

}
