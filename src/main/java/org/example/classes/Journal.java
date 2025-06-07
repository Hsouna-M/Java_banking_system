package org.example.classes;

import org.example.database.ConnectionBD;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;

public class Journal {

    public static boolean logAction(String actionType, LocalDateTime date, String actor, String details) {
        String sql = "INSERT INTO JournalAction (action_type, action_date, actor, details) VALUES (?, ?, ?, ?)";
        try (Connection conn = ConnectionBD.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, actionType);
            pstmt.setTimestamp(2, Timestamp.valueOf(date));
            pstmt.setString(3, actor);
            pstmt.setString(4, details);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Erreur lors de la journalisation de l'action: " + e.getMessage());
        }
        return false;
    }
}
