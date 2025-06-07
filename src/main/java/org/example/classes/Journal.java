package org.example.classes;

import org.example.database.ConnectionBD;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;

public class Journal {

        public static boolean logAction(String id , String actionType, LocalDateTime date, String actor, String details) {

            String sql = "INSERT INTO JournalAction (id, action_type, action_date, actor, details) VALUES (?, ?, ?, ?, ?)";
            try (Connection conn = ConnectionBD.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, id);
                pstmt.setString(2, actionType);
                pstmt.setTimestamp(3, Timestamp.valueOf(date));
                pstmt.setString(4, actor);
                pstmt.setString(5, details);
                int affectedRows = pstmt.executeUpdate();
                return affectedRows > 0;
            } catch (SQLException e) {
                System.err.println("Erreur lors de l'enregistrement de l'action dans le journal: " + e.getMessage());
                return false;
            }
        }
    }
