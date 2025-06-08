package org.example.classes;

import org.example.database.ConnectionBD;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


public class Agent implements ServiceAuthentification {
    private String id;
    private String login;
    private String motDePasseHash;

    //  Constructeur pour un nouvel agent

    public Agent(String login, String motDePasseHash) {
        this.login = login;
        this.motDePasseHash = motDePasseHash;
    }

    //Constructeur pour récupérer un agent existant de la base de données

    public Agent(String id, String login, String motDePasseHash) {
        this.id = id;
        this.login = login;
        this.motDePasseHash = motDePasseHash;
    }

    public String getId() {
        return id;
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

    // @return L'objet Agent si trouvé, sinon null.

    public static Agent getAgentByLogin(String login) {
        String sql = "SELECT id, login, mot_de_passe_hash FROM Agent WHERE login = ?";
        try (Connection conn = ConnectionBD.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, login);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new Agent(rs.getString("id"), rs.getString("login"), rs.getString("mot_de_passe_hash"));
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
            // Gérer le cas où l'email est déjà existant (clé unique)
            if (e.getErrorCode() == 1062) { // Code d'erreur pour les duplicata sous MySQL
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
            conn = ConnectionBD.getConnection();  //Toutes les opérations SQL suivantes exécutées sur cette connexion feront partie d'une seule et même transaction.
            conn.setAutoCommit(false);

            // 1. Supprimer les messages envoyés par/reçus par le client
            String deleteMessagesSql = "DELETE FROM Message WHERE client_id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(deleteMessagesSql)) {
                pstmt.setInt(1, clientId);
                pstmt.executeUpdate();
            }

            // 2. Récupérer les numéros de compte du client pour supprimer leurs transactions associées
            List<String> accountNumbers = new ArrayList<>(); // Interface Polymorphique
            String getAccountsSql = "SELECT numero FROM compte WHERE client_id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(getAccountsSql)) {
                pstmt.setInt(1, clientId);
                ResultSet rs = pstmt.executeQuery();
                while (rs.next()) {
                    accountNumbers.add(rs.getString("numero"));
                }
            }
            // 3. Supprimer les transactions où le compte source ou destination est un des comptes du client.

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

            // 4. Supprimer les comptes du client
            String deleteAccountsSql = "DELETE FROM compte WHERE client_id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(deleteAccountsSql)) {
                pstmt.setInt(1, clientId);
                pstmt.executeUpdate();
            }

            // 5. Supprimer le client lui-même
            String deleteClientSql = "DELETE FROM clients WHERE id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(deleteClientSql)) {
                pstmt.setInt(1, clientId);
                int affectedRows = pstmt.executeUpdate();

                if (affectedRows > 0) {
                    conn.commit(); // Valide toutes les opérations de la transaction
                    Journal.logAction("Suppression Client", LocalDateTime.now(), login, "Client supprimé : " + clientId);
                    System.out.println("Client " + clientId + " et ses données associées supprimés avec succès.");
                    return true;
                } else {
                    conn.rollback(); // Annule la transaction si aucun client n'a été trouvé/supprimé
                    System.out.println("Aucun client trouvé avec l'ID : " + clientId + ". La suppression a été annulée.");
                    return false;
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la suppression du client " + clientId + ": " + e.getMessage());
            if (conn != null) {
                try {
                    conn.rollback(); // Annule la transaction en cas d'erreur
                } catch (SQLException ex) {
                    System.err.println("Erreur lors du rollback après une erreur de suppression: " + ex.getMessage());
                }
            }
        } finally {
            // Rétablit l'auto-commit et ferme la connexion dans tous les cas
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

    public ResultSet consulterClients() {
        String sql = "SELECT * FROM clients";
        Connection conn = null;
        Statement stmt = null;
        try {
            conn = ConnectionBD.getConnection();
            stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            System.out.println("Consultation des clients par l'agent " + this.login + ".");
            Journal.logAction("Consulter Clients", LocalDateTime.now(), this.login, "Liste des clients consultée.");
            return rs;
        } catch (SQLException e) {
            System.err.println("Erreur lors de la consultation des clients: " + e.getMessage());
            try {
                if (stmt != null) stmt.close();
                if (conn != null) conn.close();
            } catch (SQLException ex) {
                System.err.println("Erreur lors de la fermeture des ressources après une erreur de consultation des clients: " + ex.getMessage());
            }
        }
        return null;
    }

    public ResultSet consulterComptes() {
        String sql = "SELECT * FROM compte";
        Connection conn = null;
        Statement stmt = null;
        try {
            conn = ConnectionBD.getConnection();
            stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            System.out.println("Consultation des comptes par l'agent " + this.login + ".");
            Journal.logAction("Consulter Comptes", LocalDateTime.now(), this.login, "Liste des comptes consultée.");
            return rs;
        } catch (SQLException e) {
            System.err.println("Erreur lors de la consultation des comptes: " + e.getMessage());
            try {
                if (stmt != null) stmt.close();
                if (conn != null) conn.close();
            } catch (SQLException ex) {
                System.err.println("Erreur lors de la fermeture des ressources après une erreur de consultation des comptes: " + ex.getMessage());
            }
        }
        return null;
    }

    public ResultSet consulterTransactions() {
        String sql = "SELECT * FROM transaction";
        Connection conn = null;
        Statement stmt = null;
        try {
            conn = ConnectionBD.getConnection();
            stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            System.out.println("Consultation des transactions par l'agent " + this.login + ".");
            Journal.logAction("Consulter Transactions", LocalDateTime.now(), this.login, "Liste des transactions consultée.");
            return rs;
        } catch (SQLException e) {
            System.err.println("Erreur lors de la consultation des transactions: " + e.getMessage());
            try {
                if (stmt != null) stmt.close();
                if (conn != null) conn.close();
            } catch (SQLException ex) {
                System.err.println("Erreur lors de la fermeture des ressources après une erreur de consultation des transactions: " + ex.getMessage());
            }
        }
        return null;
    }

    public ResultSet consulterMessage() {
        String sql = "SELECT * FROM Message";
        Connection conn = null;
        Statement stmt = null;
        try {
            conn = ConnectionBD.getConnection();
            stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            System.out.println("Consultation des messages par l'agent " + this.login + ".");
            Journal.logAction("Consulter Messages", LocalDateTime.now(), this.login, "Liste des messages consultée.");
            return rs;
        } catch (SQLException e) {
            System.err.println("Erreur lors de la consultation des messages: " + e.getMessage());
            try {
                if (stmt != null) stmt.close();
                if (conn != null) conn.close();
            } catch (SQLException ex) {
                System.err.println("Erreur lors de la fermeture des ressources après une erreur de consultation des messages: " + ex.getMessage());
            }
        }
        return null;
    }
}
