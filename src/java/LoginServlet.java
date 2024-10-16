import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.sql.*;


public class LoginServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();

        // Get form parameters
        String name = request.getParameter("name");
        String password = request.getParameter("password");

        // Database credentials
        String url = "jdbc:mysql://localhost:3306/new_schema";
        String dbUsername = "root";
        String dbPassword = "123456";

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            // Load MySQL JDBC Driver
            Class.forName("com.mysql.cj.jdbc.Driver");

            // Establish connection
            conn = DriverManager.getConnection(url, dbUsername, dbPassword);

            // Prepare SQL statement to retrieve user_id
            String sql = "SELECT id FROM login2 WHERE name = ? AND password = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, name);
            stmt.setString(2, password);

            // Execute query
            rs = stmt.executeQuery();

            // Check if user exists
            if (rs.next()) {
                // User authenticated, store user_id in session
                int userId = rs.getInt("id");
                HttpSession session = request.getSession();
                session.setAttribute("userId", userId);
                response.sendRedirect("plot.html"); // Redirect to the page with the plot form
            } else {            
                response.sendRedirect("login.html?error=Invalid%20username%20or%20password.");

            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // Close resources
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
