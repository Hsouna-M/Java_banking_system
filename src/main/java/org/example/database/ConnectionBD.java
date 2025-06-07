package org.example.database;

import java.sql.Connection;
import java.sql.DriverManager;

public class ConnectionBD {
    public static Connection getConnection() {
        Connection conn = null;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
<<<<<<< HEAD
            conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/banking_system_db", "root", "");
=======
            conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/banking_system_db", "root", "rootroot");
>>>>>>> 3a986c5dedb08eddb5951a2dbde989a2ab779feb
            System.out.println("connexion reussite");

        } catch (Exception e) {
            e.printStackTrace();
        }
        return conn;
    }
}
