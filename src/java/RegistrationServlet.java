import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.RequestDispatcher;
import java.sql.*;

public class RegistrationServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");

        // Get form parameters
        String name = request.getParameter("name");
        String password = request.getParameter("password");

        // Database credentials
        String url = "jdbc:mysql://localhost:3306/new_schema";
        String username = "root";
        String dbPassword = "123456";

        Connection conn = null;
        try {
            // Load MySQL JDBC Driver
            Class.forName("com.mysql.cj.jdbc.Driver");

            // Establish connection
            conn = DriverManager.getConnection(url, username, dbPassword);

            // Check if the username already exists
            String checkSql = "SELECT * FROM login2 WHERE name = ?";
            PreparedStatement checkStmt = conn.prepareStatement(checkSql);
            checkStmt.setString(1, name);
            ResultSet rs = checkStmt.executeQuery();

            if (rs.next()) {
                // Username already exists
                request.setAttribute("message", "Username already exists. Please choose a different name.");
                RequestDispatcher dispatcher = request.getRequestDispatcher("register.html");
                dispatcher.forward(request, response);
            } else {
                // Prepare SQL statement to insert new user
                String sql = "INSERT INTO login2 (name, password) VALUES (?, ?)";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setString(1, name);
                stmt.setString(2, password);

                // Execute the statement
                int rowsInserted = stmt.executeUpdate();
                if (rowsInserted > 0) {
                    // Registration successful, redirect to login page
                    response.sendRedirect("login.html");
                } else {
                    // Registration failed, forward to registration page
                    request.setAttribute("message", "Registration failed. Please try again.");
                    RequestDispatcher dispatcher = request.getRequestDispatcher("register.html");
                    dispatcher.forward(request, response);
                }

                stmt.close();
            }

            // Close resources
            rs.close();
            checkStmt.close();
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("message", "An error occurred. Please try again.");
            RequestDispatcher dispatcher = request.getRequestDispatcher("register.jsp");
            dispatcher.forward(request, response);
        } finally {
            try {
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
