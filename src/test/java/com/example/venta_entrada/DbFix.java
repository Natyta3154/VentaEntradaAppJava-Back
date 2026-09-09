package com.example.venta_entrada;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class DbFix {
    public static void main(String[] args) {
        String url = "jdbc:mysql://localhost:3306/venta_entradas_db";
        String user = "root";
        String pass = "";

        try (Connection conn = DriverManager.getConnection(url, user, pass);
             Statement stmt = conn.createStatement()) {

            System.out.println("Updating user to Admin...");
            int rows = stmt.executeUpdate("UPDATE usuarios SET rol_id = 1, email = 'herny@gmail.com', password = '$2b$12$OEEJW7xTvbJzdD2rq8n7peocYt6DcuwqiYu2NVGw07oL66f2vrQBS' WHERE id = 1");
            System.out.println("Rows updated: " + rows);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
