import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

@WebServlet(name = "GetMetaDataServlet", urlPatterns = "/_dashboard/api/get-meta-data")
public class GetMetaDataServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    //Connection pool
    private DataSource dataSource;
    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        JsonObject resultJson = new JsonObject();

        try(Connection conn = dataSource.getConnection()){

            // Get database metadata
            DatabaseMetaData metaData = conn.getMetaData();

            // Get tables
            ResultSet tables = metaData.getTables(null, null, null, new String[] { "TABLE" });

            JsonArray tablesArray = new JsonArray();

            while (tables.next()) {
                String tableName = tables.getString("TABLE_NAME");
                JsonObject tableObject = new JsonObject();
                tableObject.addProperty("tableName", tableName);

                // Get columns for each table
                ResultSet columns = metaData.getColumns(null, null, tableName, null);

                JsonArray columnsArray = new JsonArray();
                while (columns.next()) {
                    String columnName = columns.getString("COLUMN_NAME");
                    String columnType = columns.getString("TYPE_NAME");

                    JsonObject columnObject = new JsonObject();
                    columnObject.addProperty("columnName", columnName);
                    columnObject.addProperty("columnType", columnType);

                    columnsArray.add(columnObject);
                }

                tableObject.add("columns", columnsArray);
                tablesArray.add(tableObject);

                // Close resources
                columns.close();
            }

            tables.close();
            conn.close();

            resultJson.add("tables", tablesArray);
            out.println(resultJson.toString());
        } catch (Exception e) {
            e.printStackTrace();
            resultJson.addProperty("error", "An error occurred while fetching metadata.");
            out.println(resultJson.toString());
        }
    }
}
