package com.ticketzone.servlets;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;

@WebServlet("/cart")
public class CartServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");
        int userId = Integer.parseInt(request.getParameter("userId"));
        int eventId = Integer.parseInt(request.getParameter("eventId"));

        response.setContentType("application/json");
        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/ticketzone_db", "root", "");
             Statement statement = connection.createStatement()) {

            if ("add".equals(action)) {
                String query = "INSERT INTO cart (user_id, event_id) VALUES (?, ?)";
                try (PreparedStatement ps = connection.prepareStatement(query)) {
                    ps.setInt(1, userId);
                    ps.setInt(2, eventId);
                    ps.executeUpdate();
                    response.getWriter().write("{\"success\":true, \"message\":\"Event added to cart.\"}");
                }
            } else if ("remove".equals(action)) {
                String query = "DELETE FROM cart WHERE user_id = ? AND event_id = ?";
                try (PreparedStatement ps = connection.prepareStatement(query)) {
                    ps.setInt(1, userId);
                    ps.setInt(2, eventId);
                    ps.executeUpdate();
                    response.getWriter().write("{\"success\":true, \"message\":\"Event removed from cart.\"}");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.getWriter().write("{\"success\":false, \"message\":\"Error occurred.\"}");
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        int userId = Integer.parseInt(request.getParameter("userId"));
        response.setContentType("application/json");

        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/ticketzone_db", "root", "");
             PreparedStatement ps = connection.prepareStatement("SELECT events.id, events.title, events.date, events.location, events.price FROM cart JOIN events ON cart.event_id = events.id WHERE cart.user_id = ?")) {
            ps.setInt(1, userId);
            ResultSet resultSet = ps.executeQuery();

            PrintWriter out = response.getWriter();
            out.print("[");
            boolean first = true;
            while (resultSet.next()) {
                if (!first) out.print(",");
                out.print("{");
                out.print("\"id\":" + resultSet.getInt("id") + ",");
                out.print("\"title\":\"" + resultSet.getString("title") + "\",");
                out.print("\"date\":\"" + resultSet.getString("date") + "\",");
                out.print("\"location\":\"" + resultSet.getString("location") + "\",");
                out.print("\"price\":" + resultSet.getDouble("price"));
                out.print("}");
                first = false;
            }
            out.print("]");
        } catch (Exception e) {
            e.printStackTrace();
            response.getWriter().write("[]");
        }
    }

}
