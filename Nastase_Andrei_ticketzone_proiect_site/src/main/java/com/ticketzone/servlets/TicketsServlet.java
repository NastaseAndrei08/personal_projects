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
import org.json.JSONArray;
import org.json.JSONObject;

@WebServlet("/tickets")
public class TicketsServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String userId = request.getParameter("userId");
        if (userId == null || userId.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"success\":false, \"message\":\"User not logged in.\"}");
            return;
        }

        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/ticketzone_db", "root", "")) {
            String query = "SELECT t.event_id, e.title, e.date, e.location, t.access_code " +
                    "FROM tickets t " +
                    "JOIN events e ON t.event_id = e.id " +
                    "WHERE t.user_id = ?";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setInt(1, Integer.parseInt(userId));
            ResultSet resultSet = stmt.executeQuery();

            JSONArray ticketsArray = new JSONArray();

            while (resultSet.next()) {
                JSONObject ticket = new JSONObject();
                ticket.put("title", resultSet.getString("title"));
                ticket.put("date", resultSet.getString("date"));
                ticket.put("location", resultSet.getString("location"));
                ticket.put("accessCode", resultSet.getString("access_code")); // Include the access code
                ticketsArray.put(ticket);
            }

            response.setContentType("application/json");
            PrintWriter out = response.getWriter();
            out.write(ticketsArray.toString());
        } catch (Exception e) {
            e.printStackTrace();
            response.getWriter().write("{\"success\":false, \"message\":\"Error loading tickets.\"}");
        }
    }
}
