package org.example.classes;

import org.example.database.ConnectionBD;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Message {
    private int id;
    private String sujet;
    private String contenu;
    private LocalDateTime dateMessage;
    private int clientId;
    private boolean lu;

    //Constructeur pour créer une nouvelle instance de message avant de la sauvegarder en BD.

    public Message(String sujet, String contenu, int clientId) {
        this.sujet = sujet;
        this.contenu = contenu;
        this.dateMessage = LocalDateTime.now();
        this.clientId = clientId;
        this.lu = false;
    }

    //Constructeur pour créer une instance de message à partir de données récupérées de la BD

    public Message(int id, String sujet, String contenu, LocalDateTime dateMessage, int clientId, boolean lu) {
        this.id = id;
        this.sujet = sujet;
        this.contenu = contenu;
        this.dateMessage = dateMessage;
        this.clientId = clientId;
        this.lu = lu;
    }

    public static List<Message> getAllUnreadMessages() {
        List<Message> messages = new ArrayList<>();
        // SQL query now filters for lu = FALSE
        String sql = "SELECT id, sujet, contenu, date_message, client_id, lu FROM message WHERE lu = FALSE ORDER BY date_message DESC";
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
            System.err.println("Erreur lors de la récupération de tous les messages non lus: " + e.getMessage());
        }
        return messages;
    }


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
        String sql = "INSERT INTO message (sujet, contenu, date_message, client_id, lu) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = ConnectionBD.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            LocalDateTime now = LocalDateTime.now();
            pstmt.setString(1, sujet);
            pstmt.setString(2, contenu);
            pstmt.setTimestamp(3, Timestamp.valueOf(now));// Conversion de LocalDateTime en Timestamp
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

     //  @return L'objet Message correspondant à l'ID, ou null

    public static Message getMessageById(int messageId) {
        String sql = "SELECT id, sujet, contenu, date_message, client_id, lu FROM message WHERE id = ?";
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
    /* Récupère tous les messages associés à un client spécifique.
      Les messages sont triés par date d'envoi décroissante
      @return Une liste d'objets Message envoyés par ce client.     */

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

    @Override
    public String toString() {
        return "Message{" +
                "id=" + this.getId() +
                ", Subject='" + this.getSujet() + '\'' +
                ", Content=" + this.getContenu() +
                ", date=" + this.getDateMessage() +
                ", clientId=" + this.getClientId() +
                '}';
    }
}
