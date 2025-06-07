package org.example.database;

import java.sql.Connection;
import java.sql.DriverManager;

public class ConnectionBD {
    public static Connection getConnection() {
        Connection conn = null;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");

            conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/banking_system_db", "root", "rootroot");

            System.out.println("connexion reussite");

        } catch (Exception e) {
            e.printStackTrace();
        }
        return conn;
    }
}
