package com.ticketzone.servlets;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.UUID;

@WebServlet("/checkout")
public class CheckoutServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String userId = request.getParameter("userId");
        if (userId == null || userId.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"success\":false, \"message\":\"User not logged in.\"}");
            return;
        }

        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/ticketzone_db", "root", "")) {
            // Get all items in the user's cart and fetch their price from the events table
            String getCartQuery = "SELECT c.event_id, e.title, e.date, e.location, e.price " +
                    "FROM cart c " +
                    "JOIN events e ON c.event_id = e.id " +
                    "WHERE c.user_id = ?";
            PreparedStatement getCartStmt = connection.prepareStatement(getCartQuery);
            getCartStmt.setInt(1, Integer.parseInt(userId));
            ResultSet cartItems = getCartStmt.executeQuery();

            if (!cartItems.next()) {
                response.getWriter().write("{\"success\":false, \"message\":\"Cart is empty.\"}");
                return;
            }

            // Add items to the tickets table with unique access codes
            String addTicketQuery = "INSERT INTO tickets (user_id, event_id, price, access_code) VALUES (?, ?, ?, ?)";
            PreparedStatement addTicketStmt = connection.prepareStatement(addTicketQuery);

            do {
                int eventId = cartItems.getInt("event_id");
                double price = cartItems.getDouble("price");
                String accessCode = UUID.randomUUID().toString(); // Generate unique access code

                addTicketStmt.setInt(1, Integer.parseInt(userId));
                addTicketStmt.setInt(2, eventId);
                addTicketStmt.setDouble(3, price);
                addTicketStmt.setString(4, accessCode);
                addTicketStmt.addBatch();
            } while (cartItems.next());
            addTicketStmt.executeBatch();

            // Clear the cart after successful checkout
            String clearCartQuery = "DELETE FROM cart WHERE user_id = ?";
            PreparedStatement clearCartStmt = connection.prepareStatement(clearCartQuery);
            clearCartStmt.setInt(1, Integer.parseInt(userId));
            clearCartStmt.executeUpdate();

            response.getWriter().write("{\"success\":true, \"message\":\"Checkout successful.\"}");
        } catch (Exception e) {
            e.printStackTrace();
            response.getWriter().write("{\"success\":false, \"message\":\"Error processing checkout.\"}");
        }
    }
}
