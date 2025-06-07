package org.example.classes;

import org.example.database.ConnectionBD;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Agent implements ServiceAuthentification {
    private static String id;
    private String login;
    private String motDePasseHash;

    // Constructeur pour un nouvel agent
    public Agent(String login, String motDePasseHash) {
        this.login = login;
        this.motDePasseHash = motDePasseHash;
    }

    // Constructeur pour récupérer un agent existant de la base de données

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

    public void setId(String id) {
        this.id = id;
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
            System.out.println("Authentification de l'agent " + login + " réussie.");
            // Log de l'action d'authentification
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
                Journal.logAction("Bloquer Compte", LocalDateTime.now(), this.login, "Compte " + compteNumero + " bloqué.");
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
                Journal.logAction("Débloquer Compte", LocalDateTime.now(), this.login, "Compte " + compteNumero + " débloqué.");
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors du déblocage du compte " + compteNumero + ": " + e.getMessage());
        }
        return false;
    }

    public boolean ajouterClient(String nom, String prenom, String email, String telephone, String adresse, String motDePasseHash) {

        String sql = "INSERT INTO clients (id, nom, prenom, email, telephone, adresse, mot_de_passe_hash) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = ConnectionBD.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            pstmt.setString(2, nom);
            pstmt.setString(3, prenom);
            pstmt.setString(4, email);
            pstmt.setString(5, telephone);
            pstmt.setString(6, adresse);
            pstmt.setString(7, motDePasseHash);
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                System.out.println("Client " + nom + " " + prenom + " ajouté avec succès par l'agent " + this.login + ".");
                Journal.logAction("Ajouter Client", LocalDateTime.now(), this.login, "Client " + id + " ajouté.");
                return true;
            }
        } catch (SQLException e) {
            // Gérer le cas où l'email est déjà existant (clé unique)
            if (e.getErrorCode() == 1062) { // Erreur de duplicata pour MySQL
                System.err.println("Erreur: L'email '" + email + "' est déjà utilisé.");
            } else {
                System.err.println("Erreur lors de l'ajout du client: " + e.getMessage());
            }
        }
        return false;
    }

   // Supprime un client du système, ainsi que ses comptes et transactions associées.

    public boolean supprimerClient(String clientId) {
        Connection conn = null;
        try {
            conn = ConnectionBD.getConnection();
            conn.setAutoCommit(false); // Démarre une transaction

            // 1. Supprimer les messages envoyés par/reçus par le client
            String deleteMessagesSql = "DELETE FROM Message WHERE client_id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(deleteMessagesSql)) {
                pstmt.setString(1, clientId);
                pstmt.executeUpdate();
            }

            // 2. Récupérer les numéros de compte du client
            List<String> accountNumbers = new ArrayList<>();
            String getAccountsSql = "SELECT numero FROM compte WHERE client_id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(getAccountsSql)) {
                pstmt.setString(1, clientId);
                ResultSet rs = pstmt.executeQuery();
                while (rs.next()) {
                    accountNumbers.add(rs.getString("numero"));
                }
            }

            // 3. Supprimer les transactions liées à ces comptes
            if (!accountNumbers.isEmpty()) {
                String deleteTransactionsSql = "DELETE FROM transaction WHERE compte_source_numero IN (" + String.join(",", java.util.Collections.nCopies(accountNumbers.size(), "?")) + ") OR compte_destination_numero IN (" + String.join(",", java.util.Collections.nCopies(accountNumbers.size(), "?")) + ")";
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
                pstmt.setString(1, clientId);
                pstmt.executeUpdate();
            }

            // 5. Supprimer le client
            String deleteClientSql = "DELETE FROM clients WHERE id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(deleteClientSql)) {
                pstmt.setString(1, clientId);
                int affectedRows = pstmt.executeUpdate();

                if (affectedRows > 0) {
                    conn.commit(); // Valide la transaction
                    System.out.println("Client " + clientId + " et toutes ses données associées supprimés avec succès par l'agent " + this.login + ".");
                    Journal.logAction("Supprimer Client", LocalDateTime.now(), this.login, "Client " + clientId + " supprimé.");
                    return true;
                } else {
                    conn.rollback(); // Annule la transaction
                    return false;
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la suppression du client " + clientId + ": " + e.getMessage());
            if (conn != null) {
                try {
                    conn.rollback(); // Annule la transaction en cas d'erreur
                } catch (SQLException ex) {
                    System.err.println("Erreur lors du rollback: " + ex.getMessage());
                }
            }
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true); // Rétablit l'auto-commit
                    conn.close();
                } catch (SQLException ex) {
                    System.err.println("Erreur lors de la fermeture de la connexion: " + ex.getMessage());
                }
            }
        }
        return false;
    }


    public boolean marquerMessageLu(String messageId) {
        String sql = "UPDATE Message SET lu = TRUE WHERE id = ?";
        try (Connection conn = ConnectionBD.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, messageId);
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                System.out.println("Message " + messageId + " marqué comme lu par l'agent " + this.login + ".");
                Journal.logAction("Marquer Message Lu", LocalDateTime.now(), this.login, "Message " + messageId + " marqué lu.");
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors du marquage du message " + messageId + " comme lu: " + e.getMessage());
        }
        return false;
    }


}
