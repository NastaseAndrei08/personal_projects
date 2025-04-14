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
import java.sql.ResultSet;
import java.sql.Statement;

@WebServlet("/events")
public class EventServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String eventId = request.getParameter("id");

        try (PrintWriter out = response.getWriter()) {
            String url = "jdbc:mysql://localhost:3306/ticketzone_db";
            String user = "root";
            String password = "";

            Connection connection = DriverManager.getConnection(url, user, password);
            Statement statement = connection.createStatement();

            if (eventId != null) {
                // Fetch single event
                String query = "SELECT * FROM events WHERE id = " + eventId;
                ResultSet resultSet = statement.executeQuery(query);
                if (resultSet.next()) {
                    out.print("{");
                    out.print("\"id\":" + resultSet.getInt("id") + ",");
                    out.print("\"title\":\"" + resultSet.getString("title") + "\",");
                    out.print("\"date\":\"" + resultSet.getString("date") + "\",");
                    out.print("\"location\":\"" + resultSet.getString("location") + "\",");
                    out.print("\"image\":\"" + resultSet.getString("image") + "\",");
                    out.print("\"description\":\"" + resultSet.getString("description") + "\"");
                    out.print("}");
                } else {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    out.print("{\"error\":\"Event not found\"}");
                }
            }
            else {
                // Fetch all events
                ResultSet resultSet = statement.executeQuery("SELECT * FROM events");
                StringBuilder json = new StringBuilder("[");
                while (resultSet.next()) {
                    if (json.length() > 1) json.append(",");
                    json.append("{")
                            .append("\"id\":").append(resultSet.getInt("id")).append(",")
                            .append("\"title\":\"").append(resultSet.getString("title")).append("\",")
                            .append("\"date\":\"").append(resultSet.getString("date")).append("\",")
                            .append("\"location\":\"").append(resultSet.getString("location")).append("\"")
                            .append("}");
                }
                json.append("]");
                out.print(json.toString());
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }
}

