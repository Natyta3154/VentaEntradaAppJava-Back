package com.example.venta_entrada;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class DbCheck {
    public static void main(String[] args) {
        String url = "jdbc:mysql://localhost:3306/venta_entradas_db";
        String user = "root";
        String pass = "";

        try (Connection conn = DriverManager.getConnection(url, user, pass);
             Statement stmt = conn.createStatement()) {

            System.out.println("--- USERS ---");
            ResultSet rs = stmt.executeQuery("SELECT id, nombre, email, rol_id FROM usuarios");
            while (rs.next()) {
                System.out.println("ID: " + rs.getInt("id") + ", Nombre: " + rs.getString("nombre") + 
                                   ", Email: " + rs.getString("email") + ", Rol ID: " + rs.getInt("rol_id"));
            }
            
            System.out.println("--- ROLES ---");
            ResultSet rs2 = stmt.executeQuery("SELECT * FROM roles");
            while (rs2.next()) {
                System.out.println("ID: " + rs2.getInt("id") + ", Nombre: " + rs2.getString("nombre"));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
