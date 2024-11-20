package murach.sql;

import java.io.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;

import java.sql.*;

public class SQLGatewayServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        String sqlStatement = request.getParameter("sqlStatement");
        String sqlResult = "";

        try {
            // Load the driver
            Class.forName("com.mysql.jdbc.Driver");

            // Get a connection
            String dbURL = "jdbc:mysql://exam12-2-student-0a77.d.aivencloud.com:24134/Exam12_2";
            String username = "avnadmin";
            String password = "AVNS_7U85DYpLE0bX8upMDZF";
            Connection connection = DriverManager.getConnection(
                    dbURL, username, password);

            // Parse and validate SQL statement
            sqlStatement = sqlStatement.trim();
            if (sqlStatement.length() >= 6) {
                String sqlType = sqlStatement.substring(0, 6).toLowerCase();

                if (sqlType.equals("select")) {
                    // Use PreparedStatement for SELECT statements
                    PreparedStatement preparedStatement = connection.prepareStatement(sqlStatement);
                    ResultSet resultSet = preparedStatement.executeQuery();

                    // Convert ResultSet to HTML table
                    sqlResult = SQLUtil.getHtmlTable(resultSet);
                    resultSet.close();
                    preparedStatement.close();

                } else {
                    // Execute DDL or DML statements with Statement
                    Statement statement = connection.createStatement();
                    int rowsAffected = statement.executeUpdate(sqlStatement);

                    if (rowsAffected == 0) {
                        sqlResult = "<p>The statement executed successfully.</p>";
                    } else {
                        sqlResult = "<p>The statement executed successfully.<br>" + rowsAffected + " row(s) affected.</p>";
                    }
                    statement.close();
                }
            }

            connection.close();
        } catch (ClassNotFoundException e) {
            sqlResult = "<p>Error loading the database driver: <br>" + e.getMessage() + "</p>";
        } catch (SQLException e) {
            sqlResult = "<p>Error executing the SQL statement: <br>" + e.getMessage() + "</p>";
        }

        // Store results in session
        HttpSession session = request.getSession();
        session.setAttribute("sqlResult", sqlResult);
        session.setAttribute("sqlStatement", sqlStatement);

        // Forward to JSP page
        String url = "/index.jsp";
        getServletContext()
                .getRequestDispatcher(url)
                .forward(request, response);
    }
}
