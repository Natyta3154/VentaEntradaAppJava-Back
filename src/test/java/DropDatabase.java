import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class DropDatabase {
    public static void main(String[] args) {
        String url = "jdbc:mysql://localhost:3306/?user=root&password=";
        try (Connection conn = DriverManager.getConnection(url);
             Statement stmt = conn.createStatement()) {
            
            stmt.executeUpdate("DROP DATABASE IF EXISTS venta_entradas_db");
            System.out.println("Base de datos eliminada.");
            stmt.executeUpdate("CREATE DATABASE venta_entradas_db");
            System.out.println("Base de datos recreada.");
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
