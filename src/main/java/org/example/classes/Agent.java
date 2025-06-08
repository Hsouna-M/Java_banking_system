package org.example.classes;

import org.example.database.ConnectionBD;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Agent implements ServiceAuthentification {
    private int id;
    private String login;
    private String motDePasseHash;

    public Agent(String login, String motDePasseHash) {
        this.login = login;
        this.motDePasseHash = motDePasseHash;
    }

    public Agent(int id, String login, String motDePasseHash) {
        this.id = id;
        this.login = login;
        this.motDePasseHash = motDePasseHash;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getLogin() {
        return login;
    }

    public String getMotDePasseHash() {
        return motDePasseHash;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public void setMotDePasseHash(String motDePasseHash) {
        this.motDePasseHash = motDePasseHash;
    }

    public static Agent getAgentByLogin(String login) {
        String sql = "SELECT id, login, mot_de_passe_hash FROM Agent WHERE login = ?";
        try (Connection conn = ConnectionBD.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, login);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new Agent(rs.getInt("id"), rs.getString("login"), rs.getString("mot_de_passe_hash"));
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération de l'agent par login: " + e.getMessage());
        }
        return null;
    }

    @Override
    public boolean sauthentifier(String login, String mdp) {
        Agent agent = getAgentByLogin(login);

        if (agent != null && agent.getMotDePasseHash().equals(mdp)) {
            this.id = agent.getId();
            this.login = agent.getLogin();
            this.motDePasseHash = agent.getMotDePasseHash();

            System.out.println("Authentification de l'agent " + login + " réussie.");
            Journal.logAction("Authentification", LocalDateTime.now(), login, "Agent authentifié");
            return true;
        }
        System.out.println("Échec de l'authentification pour l'agent " + login + ".");
        return false;
    }

    public boolean bloquerCompte(String compteNumero) {
        String sql = "UPDATE compte SET estBlockee = TRUE WHERE numero = ?";
        try (Connection conn = ConnectionBD.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, compteNumero);
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                System.out.println("Compte " + compteNumero + " bloqué avec succès par l'agent " + this.login + ".");
                Journal.logAction("Blocage Compte", LocalDateTime.now(), login, "Compte " + compteNumero + " bloqué");
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors du blocage du compte " + compteNumero + ": " + e.getMessage());
        }
        return false;
    }

    public boolean debloquerCompte(String compteNumero) {
        String sql = "UPDATE compte SET estBlockee = FALSE WHERE numero = ?";
        try (Connection conn = ConnectionBD.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, compteNumero);
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                System.out.println("Compte " + compteNumero + " débloqué avec succès par l'agent " + this.login + ".");
                Journal.logAction("Déblocage Compte", LocalDateTime.now(), login, "Compte " + compteNumero + " débloqué");
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors du déblocage du compte " + compteNumero + ": " + e.getMessage());
        }
        return false;
    }

    public boolean ajouterClient(String nom, String prenom, String email, String telephone, String adresse, String motDePasseHash) {
        String sql = "INSERT INTO clients (nom, prenom, email, telephone, adresse, mot_de_passe_hash) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = ConnectionBD.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, nom);
            pstmt.setString(2, prenom);
            pstmt.setString(3, email);
            pstmt.setString(4, telephone);
            pstmt.setString(5, adresse);
            pstmt.setString(6, motDePasseHash);
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                System.out.println("Client " + nom + " " + prenom + " ajouté avec succès par l'agent " + this.login + ".");
                Journal.logAction("Ajout Client", LocalDateTime.now(), login, "Client ajouté : " + nom + " " + prenom);
                return true;
            }
        } catch (SQLException e) {
            if (e.getErrorCode() == 1062) {
                System.err.println("Erreur: L'email '" + email + "' est déjà utilisé.");
            } else {
                System.err.println("Erreur lors de l'ajout du client: " + e.getMessage());
            }
        }
        return false;
    }


    public boolean supprimerClient(int clientId) {
        Connection conn = null;
        try {
            conn = ConnectionBD.getConnection();
            conn.setAutoCommit(false);

            String deleteMessagesSql = "DELETE FROM Message WHERE client_id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(deleteMessagesSql)) {
                pstmt.setInt(1, clientId);
                pstmt.executeUpdate();
            }

            List<String> accountNumbers = new ArrayList<>();
            String getAccountsSql = "SELECT numero FROM compte WHERE client_id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(getAccountsSql)) {
                pstmt.setInt(1, clientId);
                ResultSet rs = pstmt.executeQuery();
                while (rs.next()) {
                    accountNumbers.add(rs.getString("numero"));
                }
            }

            if (!accountNumbers.isEmpty()) {
                String placeholders = String.join(",", java.util.Collections.nCopies(accountNumbers.size(), "?"));
                String deleteTransactionsSql = "DELETE FROM transaction WHERE compte_source_numero IN (" +
                        placeholders + ") OR compte_destination_numero IN (" + placeholders + ")";
                try (PreparedStatement pstmt = conn.prepareStatement(deleteTransactionsSql)) {
                    int i = 1;
                    for (String accNum : accountNumbers) {
                        pstmt.setString(i++, accNum);
                    }
                    for (String accNum : accountNumbers) {
                        pstmt.setString(i++, accNum);
                    }
                    pstmt.executeUpdate();
                }
            }

            String deleteAccountsSql = "DELETE FROM compte WHERE client_id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(deleteAccountsSql)) {
                pstmt.setInt(1, clientId);
                pstmt.executeUpdate();
            }

            String deleteClientSql = "DELETE FROM clients WHERE id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(deleteClientSql)) {
                pstmt.setInt(1, clientId);
                int affectedRows = pstmt.executeUpdate();

                if (affectedRows > 0) {
                    conn.commit();
                    Journal.logAction("Suppression Client", LocalDateTime.now(), login, "Client supprimé : " + clientId);
                    System.out.println("Client " + clientId + " et ses données associées supprimés avec succès.");
                    return true;
                } else {
                    conn.rollback();
                    System.out.println("Aucun client trouvé avec l'ID : " + clientId + ". La suppression a été annulée.");
                    return false;
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la suppression du client " + clientId + ": " + e.getMessage());
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    System.err.println("Erreur lors du rollback après une erreur de suppression: " + ex.getMessage());
                }
            }
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException ex) {
                    System.err.println("Erreur lors de la fermeture de la connexion dans le bloc finally: " + ex.getMessage());
                }
            }
        }
        return false;
    }

    public boolean marquerMessageLu(String messageId) {
        String sql = "UPDATE Message SET lu = TRUE WHERE id = ?";
        try (Connection conn = ConnectionBD.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, Integer.parseInt(messageId));
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                System.out.println("Message " + messageId + " marqué comme lu par l'agent " + this.login + ".");
                Journal.logAction("Marquer Message Lu", LocalDateTime.now(), this.login, "Message " + messageId + " marqué lu.");
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors du marquage du message " + messageId + " comme lu: " + e.getMessage());
        } catch (NumberFormatException e) {
            System.err.println("Erreur: L'ID du message '" + messageId + "' n'est pas un nombre valide. " + e.getMessage());
        }
        return false;
    }

    // --- Méthodes de consultation mises à jour ---

    public List<Map<String, Object>> consulterClients() {
        List<Map<String, Object>> clients = new ArrayList<>();
        String sql = "SELECT id, nom, prenom, email, telephone, adresse FROM clients";
        try (Connection conn = ConnectionBD.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) { // Utilisation de try-with-resources pour ResultSet
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();

            while (rs.next()) {
                Map<String, Object> clientData = new HashMap<>();
                for (int i = 1; i <= columnCount; i++) {
                    clientData.put(metaData.getColumnLabel(i), rs.getObject(i));
                }
                clients.add(clientData);
            }
            System.out.println("Consultation des clients par l'agent " + this.login + ".");
            Journal.logAction("Consulter Clients", LocalDateTime.now(), this.login, "Liste des clients consultée.");
        } catch (SQLException e) {
            System.err.println("Erreur lors de la consultation des clients: " + e.getMessage());
        }
        return clients;
    }

    public List<Map<String, Object>> consulterComptes() {
        List<Map<String, Object>> comptes = new ArrayList<>();
        String sql = "SELECT numero, solde, date_ouverture, client_id, type_compte, estBlockee, tauxInteret FROM compte";
        try (Connection conn = ConnectionBD.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();

            while (rs.next()) {
                Map<String, Object> compteData = new HashMap<>();
                for (int i = 1; i <= columnCount; i++) {
                    compteData.put(metaData.getColumnLabel(i), rs.getObject(i));
                }
                comptes.add(compteData);
            }
            System.out.println("Consultation des comptes par l'agent " + this.login + ".");
            Journal.logAction("Consulter Comptes", LocalDateTime.now(), this.login, "Liste des comptes consultée.");
        } catch (SQLException e) {
            System.err.println("Erreur lors de la consultation des comptes: " + e.getMessage());
        }
        return comptes;
    }

    public List<Map<String, Object>> consulterTransactions() {
        List<Map<String, Object>> transactions = new ArrayList<>();
        String sql = "SELECT id, montant, date_transaction, compte_source_numero, compte_destination_numero FROM transaction";
        try (Connection conn = ConnectionBD.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();

            while (rs.next()) {
                Map<String, Object> transactionData = new HashMap<>();
                for (int i = 1; i <= columnCount; i++) {
                    transactionData.put(metaData.getColumnLabel(i), rs.getObject(i));
                }
                transactions.add(transactionData);
            }
            System.out.println("Consultation des transactions par l'agent " + this.login + ".");
            Journal.logAction("Consulter Transactions", LocalDateTime.now(), this.login, "Liste des transactions consultée.");
        } catch (SQLException e) {
            System.err.println("Erreur lors de la consultation des transactions: " + e.getMessage());
        }
        return transactions;
    }

    public List<Map<String, Object>> consulterMessages() { // Renommée pour la cohérence
        List<Map<String, Object>> messages = new ArrayList<>();
        String sql = "SELECT id, sujet, contenu, date_message, client_id, lu FROM Message";
        try (Connection conn = ConnectionBD.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();

            while (rs.next()) {
                Map<String, Object> messageData = new HashMap<>();
                for (int i = 1; i <= columnCount; i++) {
                    messageData.put(metaData.getColumnLabel(i), rs.getObject(i));
                }
                messages.add(messageData);
            }
            System.out.println("Consultation des messages par l'agent " + this.login + ".");
            Journal.logAction("Consulter Messages", LocalDateTime.now(), this.login, "Liste des messages consultée.");
        } catch (SQLException e) {
            System.err.println("Erreur lors de la consultation des messages: " + e.getMessage());
        }
        return messages;
    }
}
