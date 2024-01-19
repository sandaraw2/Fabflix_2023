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
import java.sql.ResultSet;


@WebServlet(name = "AddStarServlet", urlPatterns = "/_dashboard/api/add-star")
public class AddStarServlet extends HttpServlet {

    //Connection pool
    private DataSource dataSource;
    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedbMaster");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }


    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json"); // Response mime type
        response.setCharacterEncoding("UTF-8");

        String star_name = request.getParameter("star_name");
        String star_birth_year = request.getParameter("star_birth_year");

        PrintWriter out = response.getWriter();
        JsonObject jsonObject = new JsonObject();

        try(Connection conn = dataSource.getConnection()){
            String new_id  = getId(conn);
            PreparedStatement insert_statement = getInsertStatement(conn, new_id, star_name, star_birth_year);
            insert_statement.executeUpdate();
            //confirm new addition by printing to console
            printAddedStarInfo(conn, new_id);
            //send new id back to print success message
            jsonObject.addProperty("status", "success");
            jsonObject.addProperty("message", "Added Star with ID: " + new_id);

            // set response status to 200 (OK)
            response.setStatus(200);
            insert_statement.close();
        }catch(Exception e){
            jsonObject.addProperty("status", "failed");
            jsonObject.addProperty("message", e.getMessage());
            out.write(jsonObject.toString());
            // Set response status to 500 (Internal Server Error)
            response.setStatus(500);
        }
        // write JSON string to output
        out.write(jsonObject.toString());
    }

    public String getId(Connection conn) throws java.sql.SQLException{
        String id_query = "SELECT max(substring(id, 3)) from stars";
        PreparedStatement prep = conn.prepareStatement(id_query);
        ResultSet id_results = prep.executeQuery();
        id_results.next();
        String id_string = id_results.getString(1);
        Integer id_int = Integer.parseInt(id_string) + 1;
        String new_id = "nm" + id_int.toString();
        //closing
        prep.close();
        id_results.close();
        return new_id;
    }

    public PreparedStatement getInsertStatement(Connection conn, String id, String star_name, String star_birth_year) throws java.sql.SQLException{
        //create prepared statement for Insert statement
        PreparedStatement insertPrep =
                conn.prepareStatement("INSERT INTO stars (id, name, birthYear) VALUES( ? , ? , ?)");

        //Set parameters with id, star_name, and birth_year
        insertPrep.setString(1, id);
        insertPrep.setString(2,star_name);
        if(star_birth_year != null && !star_birth_year.isEmpty()){
            insertPrep.setInt(3, Integer.parseInt(star_birth_year));
        }else{
            insertPrep.setString(3, null);
        }

        return insertPrep;
    }

    public void printAddedStarInfo(Connection conn, String star_id) throws java.sql.SQLException{
        PreparedStatement prep = conn.prepareStatement("SELECT * FROM stars WHERE id = ?");
        prep.setString(1, star_id);
        ResultSet rs = prep.executeQuery();
        rs.next();
        System.out.println("NEW STAR ADDED :");
        System.out.println("ID: " + rs.getString("id"));
        System.out.println("NAME: " + rs.getString("name"));
        System.out.println("BIRTH YEAR: " + rs.getString("birthYear"));

        //close
        prep.close();
        rs.close();
    }
}