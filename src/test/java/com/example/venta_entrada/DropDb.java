package com.example.venta_entrada;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class DropDb {
    public static void main(String[] args) {
        String url = "jdbc:mysql://localhost:3306/";
        String user = "root";
        String pass = "";

        try (Connection conn = DriverManager.getConnection(url, user, pass);
             Statement stmt = conn.createStatement()) {

            System.out.println("Dropping and recreating database...");
            stmt.executeUpdate("DROP DATABASE IF EXISTS venta_entradas_db");
            stmt.executeUpdate("CREATE DATABASE venta_entradas_db");
            System.out.println("Database reset successfully!");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
