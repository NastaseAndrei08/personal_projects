package com.ticketzone.servlets;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/admin/*")
public class AdminServlet extends HttpServlet {

    private static final String DB_URL = "jdbc:mysql://localhost:3306/ticketzone_db";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String path = request.getPathInfo();

        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            if ("/events".equals(path)) {
                String query = "SELECT * FROM events";
                Statement stmt = connection.createStatement();
                ResultSet rs = stmt.executeQuery(query);

                List<String> events = new ArrayList<>();
                while (rs.next()) {
                    events.add(String.format(
                            "{\"id\": \"%d\", \"title\": \"%s\", \"date\": \"%s\", \"location\": \"%s\", \"description\": \"%s\", \"price\": \"%.2f\", \"image\": \"%s\"}",
                            rs.getInt("id"),
                            escapeJson(rs.getString("title")),
                            rs.getDate("date") != null ? rs.getDate("date").toString() : "",
                            escapeJson(rs.getString("location")),
                            escapeJson(rs.getString("description")),
                            rs.getDouble("price"),
                            escapeJson(rs.getString("image"))
                    ));
                }
                response.setContentType("application/json");
                PrintWriter out = response.getWriter();
                out.write("[" + String.join(",", events) + "]");
            } else if ("/event".equals(path)) {
                int eventId = Integer.parseInt(request.getParameter("id"));
                String query = "SELECT * FROM events WHERE id = ?";
                PreparedStatement stmt = connection.prepareStatement(query);
                stmt.setInt(1, eventId);
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    response.setContentType("application/json");
                    PrintWriter out = response.getWriter();
                    out.write(String.format(
                            "{\"id\": \"%d\", \"title\": \"%s\", \"date\": \"%s\", \"location\": \"%s\", \"description\": \"%s\", \"price\": \"%.2f\", \"image\": \"%s\"}",
                            rs.getInt("id"),
                            escapeJson(rs.getString("title")),
                            rs.getDate("date") != null ? rs.getDate("date").toString() : "",
                            escapeJson(rs.getString("location")),
                            escapeJson(rs.getString("description")),
                            rs.getDouble("price"),
                            escapeJson(rs.getString("image"))
                    ));
                } else {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"message\": \"Internal server error\"}");
        }
    }

    /**
     * Escapes special characters in JSON strings.
     */
    private String escapeJson(String value) {
        if (value == null) return "";
        return value.replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r");
    }


    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String id = request.getParameter("id");
        String title = request.getParameter("title");
        String date = request.getParameter("date");
        String location = request.getParameter("location");
        String description = request.getParameter("description");
        String image = request.getParameter("image");
        String price = request.getParameter("price");

        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            if (id == null || id.isEmpty()) {
                String query = "INSERT INTO events (title, date, location, description, image, price) VALUES (?, ?, ?, ?, ?, ?)";
                PreparedStatement statement = connection.prepareStatement(query);
                statement.setString(1, title);
                statement.setDate(2, date.isEmpty() ? null : Date.valueOf(date));
                statement.setString(3, location);
                statement.setString(4, description);
                statement.setString(5, image);
                statement.setDouble(6, Double.parseDouble(price));
                statement.executeUpdate();
            } else {
                String query = "UPDATE events SET title=?, date=?, location=?, description=?, image=?, price=? WHERE id=?";
                PreparedStatement statement = connection.prepareStatement(query);
                statement.setString(1, title);
                statement.setDate(2, date.isEmpty() ? null : Date.valueOf(date));
                statement.setString(3, location);
                statement.setString(4, description);
                statement.setString(5, image);
                statement.setDouble(6, Double.parseDouble(price));
                statement.setInt(7, Integer.parseInt(id));
                statement.executeUpdate();
            }
            response.getWriter().write("{\"success\":true}");
        } catch (Exception e) {
            e.printStackTrace();
            response.getWriter().write("{\"success\":false, \"message\":\"Failed to save event.\"}");
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String eventId = request.getParameter("id");

        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            // Delete from cart
            String deleteCartQuery = "DELETE FROM cart WHERE event_id = ?";
            PreparedStatement deleteCartStmt = connection.prepareStatement(deleteCartQuery);
            deleteCartStmt.setInt(1, Integer.parseInt(eventId));
            deleteCartStmt.executeUpdate();

            // Check for existing tickets
            String checkTicketsQuery = "SELECT COUNT(*) FROM tickets WHERE event_id = ?";
            PreparedStatement checkStmt = connection.prepareStatement(checkTicketsQuery);
            checkStmt.setInt(1, Integer.parseInt(eventId));
            ResultSet rs = checkStmt.executeQuery();
            rs.next();
            int ticketCount = rs.getInt(1);

            if (ticketCount > 0) {
                response.setStatus(HttpServletResponse.SC_CONFLICT);
                response.getWriter().write("{\"success\":false, \"message\":\"Cannot delete event with existing tickets.\"}");
            } else {
                // Delete event
                String deleteQuery = "DELETE FROM events WHERE id = ?";
                PreparedStatement deleteStmt = connection.prepareStatement(deleteQuery);
                deleteStmt.setInt(1, Integer.parseInt(eventId));
                deleteStmt.executeUpdate();

                response.getWriter().write("{\"success\":true, \"message\":\"Event deleted successfully.\"}");
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"success\":false, \"message\":\"Error deleting event.\"}");
        }
    }
}

