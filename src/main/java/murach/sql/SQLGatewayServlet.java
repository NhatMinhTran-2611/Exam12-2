package murach.sql;

import java.io.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.sql.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class SQLGatewayServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String sqlStatement = request.getParameter("sqlStatement");
        String sqlResult = "";

        try {
            // Load driver (sử dụng driver MySQL 8 hoặc mới hơn)
            Class.forName("com.mysql.cj.jdbc.Driver");

            // Kết nối với cơ sở dữ liệu
            String dbURL = "jdbc:mysql://exam12-2-student-0a77.d.aivencloud.com:24134/Exam12_2";
            String username = "avnadmin";
            String password = "AVNS_7U85DYpLE0bX8upMDZF";
            Connection connection = DriverManager.getConnection(dbURL, username, password);

            // Kiểm tra câu lệnh SQL có phải SELECT không
            if (sqlStatement.trim().toLowerCase().startsWith("select")) {

                // Kiểm tra xem câu lệnh SELECT có chứa điều kiện luôn đúng không
                String errorMessage = checkAlwaysTrueCondition(sqlStatement);

                if (errorMessage != null) {
                    // Nếu có điều kiện luôn đúng, sử dụng PreparedStatement thay vì thực thi câu lệnh SQL trực tiếp
                    String sql = "SELECT * FROM users WHERE email = ?";  // Ví dụ, sử dụng tham số
                    PreparedStatement preparedStatement = connection.prepareStatement(sql);
                    preparedStatement.setString(1, request.getParameter("email"));
                    ResultSet resultSet = preparedStatement.executeQuery();
                    sqlResult = SQLUtil.getHtmlTable(resultSet);  // Chuyển đổi ResultSet thành bảng HTML
                    resultSet.close();
                    preparedStatement.close();
                } else {
                    // Nếu không có điều kiện luôn đúng, thực thi câu lệnh SELECT trực tiếp
                    Statement statement = connection.createStatement();
                    ResultSet resultSet = statement.executeQuery(sqlStatement);
                    sqlResult = SQLUtil.getHtmlTable(resultSet);  // Chuyển đổi ResultSet thành bảng HTML
                    resultSet.close();
                    statement.close();
                }

            } else {
                // Nếu là câu lệnh DML hoặc DDL, thực thi như bình thường
                Statement statement = connection.createStatement();
                int rowsAffected = statement.executeUpdate(sqlStatement);
                if (rowsAffected == 0) {
                    sqlResult = "<p>The statement executed successfully.</p>";
                } else {
                    sqlResult = "<p>The statement executed successfully.<br>" + rowsAffected + " row(s) affected.</p>";
                }
                statement.close();
            }

            // Đóng kết nối
            connection.close();
        } catch (ClassNotFoundException e) {
            sqlResult = "<p>Error loading the database driver: <br>" + e.getMessage() + "</p>";
        } catch (SQLException e) {
            sqlResult = "<p>Error executing the SQL statement: <br>" + e.getMessage() + "</p>";
        }

        // Lưu kết quả vào session
        HttpSession session = request.getSession();
        session.setAttribute("sqlResult", sqlResult);
        session.setAttribute("sqlStatement", sqlStatement);

        // Forward đến trang JSP
        String url = "/index.jsp";
        getServletContext().getRequestDispatcher(url).forward(request, response);
    }

    public String checkAlwaysTrueCondition(String sqlQuery) {
        // Biểu thức chính quy để kiểm tra các điều kiện luôn đúng như '1=1', '2=2', 'true', 'x'='x', x=x, ...
        String regex = "(?i)(where\\s+('[^']*'|\\S+)\\s*=\\s*('[^']*'|\\S+)|or\\s+('[^']*'|\\S+)\\s*=\\s*('[^']*'|\\S+)|\\s*('[^']*'|\\S+)\\s*=\\s*('[^']*'|\\S+)\\s*)|where\\s+true|or\\s+true";

        // Tạo pattern từ biểu thức chính quy
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(sqlQuery);

        // Kiểm tra xem câu lệnh SQL có chứa điều kiện luôn đúng không
        if (matcher.find()) {
            // Nếu có, trả về thông báo lỗi
            return "<p>Error: Query contains always-true condition (e.g., 'WHERE '2' = '2'). Please revise the query.</p>";
        } else {
            // Nếu không có, trả về null
            return null;
        }
    }

}
