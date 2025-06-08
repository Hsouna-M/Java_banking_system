package org.example;

import org.example.classes.Agent;
import org.example.classes.Client;
import org.example.classes.Compte; // Importez Compte pour utiliser getCompteByNumero
import org.example.classes.Message;
import org.example.database.ConnectionBD;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class Main {

    // Méthode utilitaire pour vérifier l'existence d'un client
    public static boolean doesClientExist(int clientId) {
        String sql = "SELECT COUNT(*) FROM clients WHERE id = ?";
        try (Connection conn = ConnectionBD.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, clientId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la vérification de l'existence du client " + clientId + ": " + e.getMessage());
        }
        return false;
    }

    // Méthode utilitaire pour insérer un compte directement (pour le test)
    public static void insertTestAccount(String numero, double solde, int clientId, String typeCompte) {
        String sql = "INSERT INTO compte (numero, solde, date_ouverture, client_id, type_compte, estBlockee, tauxInteret) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = ConnectionBD.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, numero);
            pstmt.setDouble(2, solde);
            pstmt.setDate(3, java.sql.Date.valueOf(LocalDate.now()));
            pstmt.setInt(4, clientId);
            pstmt.setString(5, typeCompte);
            pstmt.setBoolean(6, false); // Not blocked by default
            pstmt.setObject(7, null); // No interest rate for all types
            pstmt.executeUpdate();
            System.out.println("Compte de test '" + numero + "' inséré pour le client ID " + clientId);
        } catch (SQLException e) {
            System.err.println("Erreur lors de l'insertion du compte de test: " + e.getMessage());
        }
    }

    // Méthode utilitaire pour obtenir l'ID d'un client par email (maintenant retourne int)
    public static int getClientIdByEmail(String email) {
        String sql = "SELECT id FROM clients WHERE email = ?";
        try (Connection conn = ConnectionBD.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, email);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("id");
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération de l'ID client par email: " + e.getMessage());
        }
        return 0; // Retourne 0 si non trouvé ou erreur
    }

    // Méthode utilitaire pour obtenir l'ID d'un client par nom et prénom
    public static int getClientIdByNameAndPrenom(String nom, String prenom) {
        String sql = "SELECT id FROM clients WHERE nom = ? AND prenom = ?";
        try (Connection conn = ConnectionBD.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, nom);
            pstmt.setString(2, prenom);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("id");
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération de l'ID client par nom et prénom: " + e.getMessage());
        }
        return 0; // Retourne 0 si non trouvé ou erreur
    }


    // Méthode utilitaire pour vérifier l'état de blocage d'un compte
    public static boolean isCompteBlocked(String numeroCompte) {
        String sql = "SELECT estBlockee FROM compte WHERE numero = ?";
        try (Connection conn = ConnectionBD.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, numeroCompte);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getBoolean("estBlockee");
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la vérification de l'état du compte " + numeroCompte + ": " + e.getMessage());
        }
        return false; // Par défaut, considérer non bloqué ou erreur
    }


    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        // --- Test de connexion à la base de données ---
        System.out.println("--- Test de connexion à la base de données ---");
        try (Connection conn = ConnectionBD.getConnection()) {
            if (conn != null) {
                System.out.println("Connexion à la base de données réussie !");
            } else {
                System.out.println("Échec de la connexion à la base de données. Veuillez vérifier votre configuration.");
                return;
            }
        } catch (SQLException e) {
            System.err.println("Erreur SQL lors du test de connexion : " + e.getMessage());
            return;
        }
        System.out.println("----------------------------------------------\n");

        Agent agent = null;
        boolean authenticated = false;
        int maxAttempts = 3;
        int currentAttempt = 0;

        // Boucle d'authentification de l'agent
        while (!authenticated && currentAttempt < maxAttempts) {
            System.out.println("--- Authentification de l'agent ---");
            System.out.print("Entrez votre login : ");
            String login = scanner.nextLine();
            System.out.print("Entrez votre mot de passe : ");
            String motDePasse = scanner.nextLine();

            Agent tempAgent = new Agent(login, motDePasse);
            authenticated = tempAgent.sauthentifier(login, motDePasse);

            if (authenticated) {
                agent = tempAgent;
                System.out.println("Authentification réussie ! Bienvenue, " + agent.getLogin() + ".");
            } else {
                currentAttempt++;
                System.out.println("Login ou mot de passe incorrect. Tentative " + currentAttempt + "/" + maxAttempts + ".");
                if (currentAttempt == maxAttempts) {
                    System.out.println("Trop de tentatives infructueuses. Le programme va s'arrêter.");
                    scanner.close();
                    return;
                }
            }
        }

        // Si l'authentification est réussie, afficher le menu des options
        if (authenticated) {
            int choix;
            do {
                System.out.println("\n--- Menu Agent ---");
                System.out.println("1. Ajouter un nouveau client");
                System.out.println("2. Bloquer un compte client");
                System.out.println("3. Débloquer un compte client");
                System.out.println("4. Supprimer un client");
                System.out.println("5. Consulter les clients");
                System.out.println("6. Consulter les comptes");
                System.out.println("7. Consulter les transactions");
                System.out.println("8. Consulter les messages");
                System.out.println("0. Quitter");
                System.out.print("Entrez votre choix : ");

                while (!scanner.hasNextInt()) {
                    System.out.println("Entrée invalide. Veuillez entrer un numéro.");
                    scanner.next();
                    System.out.print("Entrez votre choix : ");
                }
                choix = scanner.nextInt();
                scanner.nextLine();

                switch (choix) {
                    case 1:
                        System.out.println("\n--- Ajout d'un nouveau client ---");
                        System.out.print("Nom du client : ");
                        String nom = scanner.nextLine();
                        System.out.print("Prénom du client : ");
                        String prenom = scanner.nextLine();
                        System.out.print("Email du client : ");
                        String email = scanner.nextLine();
                        System.out.print("Téléphone du client : ");
                        String telephone = scanner.nextLine();
                        System.out.print("Adresse du client : ");
                        String adresse = scanner.nextLine();
                        System.out.print("Mot de passe du client (à hacher en production) : ");
                        String clientMdp = scanner.nextLine();

                        boolean clientAdded = agent.ajouterClient(nom, prenom, email, telephone, adresse, clientMdp);
                        if (clientAdded) {
                            System.out.println("Client " + nom + " " + prenom + " ajouté avec succès.");
                        } else {
                            System.out.println("Échec de l'ajout du client " + nom + " " + prenom + ".");
                        }
                        break;
                    case 2:
                        System.out.println("\n--- Blocage d'un compte ---");
                        System.out.print("Entrez le numéro du compte à bloquer : ");
                        String numCompteBloquer = scanner.nextLine();
                        System.out.println("État actuel du compte " + numCompteBloquer + " (avant blocage): " + (isCompteBlocked(numCompteBloquer) ? "Bloqué" : "Débloqué"));
                        boolean blocked = agent.bloquerCompte(numCompteBloquer);
                        if (blocked) {
                            System.out.println("Compte " + numCompteBloquer + " bloqué avec succès.");
                            System.out.println("Nouvel état du compte " + numCompteBloquer + ": " + (isCompteBlocked(numCompteBloquer) ? "Bloqué" : "Débloqué"));
                        } else {
                            System.out.println("Échec du blocage du compte " + numCompteBloquer + ".");
                        }
                        break;
                    case 3:
                        System.out.println("\n--- Déblocage d'un compte ---");
                        System.out.print("Entrez le numéro du compte à débloquer : ");
                        String numCompteDebloquer = scanner.nextLine();
                        System.out.println("État actuel du compte " + numCompteDebloquer + " (avant déblocage): " + (isCompteBlocked(numCompteDebloquer) ? "Bloqué" : "Débloqué"));
                        boolean unblocked = agent.debloquerCompte(numCompteDebloquer);
                        if (unblocked) {
                            System.out.println("Compte " + numCompteDebloquer + " débloqué avec succès.");
                            System.out.println("Nouvel état du compte " + numCompteDebloquer + ": " + (isCompteBlocked(numCompteDebloquer) ? "Bloqué" : "Débloqué"));
                        } else {
                            System.out.println("Échec du déblocage du compte " + numCompteDebloquer + ".");
                        }
                        break;
                    case 4:
                        System.out.println("\n--- Suppression d'un client ---");
                        System.out.print("Entrez l'ID du client à supprimer : ");
                        while (!scanner.hasNextInt()) {
                            System.out.println("Entrée invalide. Veuillez entrer un numéro d'ID valide.");
                            scanner.next();
                            System.out.print("Entrez l'ID du client à supprimer : ");
                        }
                        int clientIdToDelete = scanner.nextInt();
                        scanner.nextLine();

                        System.out.println("\nTentative de suppression du client avec ID : " + clientIdToDelete);
                        System.out.println("Existence du client avant suppression (ID " + clientIdToDelete + ") : " + doesClientExist(clientIdToDelete));

                        boolean clientSuppressed = agent.supprimerClient(clientIdToDelete);

                        if (clientSuppressed) {
                            System.out.println("Client avec ID " + clientIdToDelete + " et ses données associées supprimés avec succès !");
                            System.out.println("Existence du client après suppression (ID " + clientIdToDelete + ") : " + doesClientExist(clientIdToDelete));
                        } else {
                            System.out.println("Échec de la suppression du client avec ID " + clientIdToDelete + ".");
                        }
                        break;
                    case 5:
                        System.out.println("\n--- Consultation des clients ---");
                        List<Map<String, Object>> clients = agent.consulterClients();
                        if (clients.isEmpty()) {
                            System.out.println("Aucun client trouvé.");
                        } else {
                            clients.forEach(System.out::println);
                        }
                        break;
                    case 6:
                        System.out.println("\n--- Consultation des comptes ---");
                        List<Map<String, Object>> comptes = agent.consulterComptes();
                        if (comptes.isEmpty()) {
                            System.out.println("Aucun compte trouvé.");
                        } else {
                            comptes.forEach(System.out::println);
                        }
                        break;
                    case 7:
                        System.out.println("\n--- Consultation des transactions ---");
                        List<Map<String, Object>> transactions = agent.consulterTransactions();
                        if (transactions.isEmpty()) {
                            System.out.println("Aucune transaction trouvée.");
                        } else {
                            transactions.forEach(System.out::println);
                        }
                        break;
                    case 8:
                        System.out.println("\n--- Consultation des messages ---");
                        List<Map<String, Object>> messages = agent.consulterMessages(); // Appelle la méthode renommée
                        if (messages.isEmpty()) {
                            System.out.println("Aucun message trouvé.");
                        } else {
                            messages.forEach(System.out::println);
                        }
                        break;
                    case 0:
                        System.out.println("Déconnexion et fermeture du programme.");
                        break;
                    default:
                        System.out.println("Choix invalide. Veuillez réessayer.");
                }
            } while (choix != 0);
        }

        scanner.close();
    }
}
