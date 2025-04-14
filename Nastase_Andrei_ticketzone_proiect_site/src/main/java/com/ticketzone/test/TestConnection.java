package com.ticketzone.test;

import com.ticketzone.utils.DatabaseConnection;

import java.sql.Connection;

public class TestConnection {
    public static void main(String[] args) {
        try (Connection connection = DatabaseConnection.getConnection()) {
            if (connection != null) {
                System.out.println("Connected to the database successfully!");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
