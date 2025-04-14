package com.ticketzone.servlets;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        String email = request.getParameter("email");
        String password = request.getParameter("password");

        String url = "jdbc:mysql://localhost:3306/ticketzone_db";
        String user = "root";
        String dbPassword = ""; // Replace with your MySQL password

        try (Connection connection = DriverManager.getConnection(url, user, dbPassword)) {
            String query = "SELECT id, is_admin FROM users WHERE email = ? AND password = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, email);
            statement.setString(2, password);

            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                int userId = resultSet.getInt("id");
                boolean isAdmin = resultSet.getBoolean("is_admin");
                out.println("{\"success\": true, \"userId\": " + userId + ", \"isAdmin\": " + isAdmin + "}");
            } else {
                out.println("{\"success\": false, \"message\": \"Invalid email or password\"}");
            }
        } catch (Exception e) {
            e.printStackTrace();
            out.println("{\"success\": false, \"message\": \"An error occurred.\"}");
        }
    }
}
