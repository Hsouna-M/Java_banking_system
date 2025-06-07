package org.example.classes;

import org.example.database.ConnectionBD;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Message {
    private int id; // L'ID du message, auto-incrémenté par la base de données
    private String sujet;
    private String contenu;
    private LocalDateTime dateMessage; // La date et l'heure de l'envoi du message
    private int clientId; // L'ID du client qui a envoyé le message (référence à clients.id de type INT)
    private boolean lu; // Indique si le message a été lu ou non

    public Message(String sujet, String contenu, int clientId) {
        this.sujet = sujet;
        this.contenu = contenu;
        this.dateMessage = LocalDateTime.now(); // Définit la date et l'heure actuelles
        this.clientId = clientId;
        this.lu = false; // Un nouveau message est par défaut non lu
    }

    public Message(int id, String sujet, String contenu, LocalDateTime dateMessage, int clientId, boolean lu) {
        this.id = id;
        this.sujet = sujet;
        this.contenu = contenu;
        this.dateMessage = dateMessage;
        this.clientId = clientId;
        this.lu = lu;
    }

    // --- Getters pour accéder aux propriétés du message ---
    public int getId() {
        return id;
    }

    public String getSujet() {
        return sujet;
    }

    public String getContenu() {
        return contenu;
    }

    public LocalDateTime getDateMessage() {
        return dateMessage;
    }

    public int getClientId() {
        return clientId;
    }

    public boolean isLu() {
        return lu;
    }

    public void setSujet(String sujet) {
        this.sujet = sujet;
    }

    public void setContenu(String contenu) {
        this.contenu = contenu;
    }

    public void setLu(boolean lu) {
        this.lu = lu;
    }

    public static boolean envoyerMessage(String sujet, String contenu, int clientId) {
        String sql = "INSERT INTO Message (sujet, contenu, date_message, client_id, lu) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = ConnectionBD.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            LocalDateTime now = LocalDateTime.now();
            pstmt.setString(1, sujet);
            pstmt.setString(2, contenu);
            pstmt.setTimestamp(3, Timestamp.valueOf(now));
            pstmt.setInt(4, clientId);
            pstmt.setBoolean(5, false);

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                // Journalisation de l'action d'envoi de message
                Journal.logAction("Envoi Message", now, "Client ID: " + clientId, "Message envoyé par le client " + clientId + " (Sujet: " + sujet + ")");
                System.out.println("Message envoyé avec succès par le client " + clientId + ".");
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de l'envoi du message: " + e.getMessage());
        }
        return false;
    }

    public static Message getMessageById(int messageId) {
        String sql = "SELECT id, sujet, contenu, date_message, client_id, lu FROM Message WHERE id = ?";
        try (Connection conn = ConnectionBD.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, messageId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new Message(
                        rs.getInt("id"),
                        rs.getString("sujet"),
                        rs.getString("contenu"),
                        rs.getTimestamp("date_message").toLocalDateTime(), // Conversion de Timestamp en LocalDateTime
                        rs.getInt("client_id"),
                        rs.getBoolean("lu")
                );
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération du message par ID " + messageId + ": " + e.getMessage());
        }
        return null;
    }

    public static List<Message> getMessagesByClientId(int clientId) {
        List<Message> messages = new ArrayList<>();
        String sql = "SELECT id, sujet, contenu, date_message, client_id, lu FROM Message WHERE client_id = ? ORDER BY date_message DESC";
        try (Connection conn = ConnectionBD.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, clientId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                messages.add(new Message(
                        rs.getInt("id"),
                        rs.getString("sujet"),
                        rs.getString("contenu"),
                        rs.getTimestamp("date_message").toLocalDateTime(),
                        rs.getInt("client_id"),
                        rs.getBoolean("lu")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des messages pour le client " + clientId + ": " + e.getMessage());
        }
        return messages;
    }

    public static List<Message> getAllMessages() {
        List<Message> messages = new ArrayList<>();
        String sql = "SELECT id, sujet, contenu, date_message, client_id, lu FROM Message ORDER BY date_message DESC";
        try (Connection conn = ConnectionBD.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                messages.add(new Message(
                        rs.getInt("id"),
                        rs.getString("sujet"),
                        rs.getString("contenu"),
                        rs.getTimestamp("date_message").toLocalDateTime(),
                        rs.getInt("client_id"),
                        rs.getBoolean("lu")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération de tous les messages: " + e.getMessage());
        }
        return messages;
    }
}
