package com.ticketzone.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/ticketzone_db";
    private static final String USER = "root"; // Default XAMPP username
    private static final String PASSWORD = ""; // Leave empty if no password is set

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
