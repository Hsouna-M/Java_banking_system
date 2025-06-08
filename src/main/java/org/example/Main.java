package org.example;

import org.example.classes.Agent;
import org.example.database.ConnectionBD;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Scanner;

public class Main {

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

    public static void insertTestAccount(String numero, double solde, int clientId, String typeCompte) {
        String sql = "INSERT INTO compte (numero, solde, date_ouverture, client_id, type_compte, estBlockee, tauxInteret) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = ConnectionBD.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, numero);
            pstmt.setDouble(2, solde);
            pstmt.setDate(3, java.sql.Date.valueOf(LocalDate.now()));
            pstmt.setInt(4, clientId);
            pstmt.setString(5, typeCompte);
            pstmt.setBoolean(6, false);
            pstmt.setObject(7, null);
            pstmt.executeUpdate();
            System.out.println("Compte de test '" + numero + "' inséré pour le client ID " + clientId);
        } catch (SQLException e) {
            System.err.println("Erreur lors de l'insertion du compte de test: " + e.getMessage());
        }
    }

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
        return 0;
    }

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
        return false;
    }


    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("----------------------------------------------\n");

        Agent agent = null;
        boolean authenticated = false;
        int maxAttempts = 3;
        int currentAttempt = 0;

        while (!authenticated && currentAttempt < maxAttempts) {
            System.out.println("--- Authentification de l'agent ---");
            System.out.print("Entrez votre login : ");
            String login = scanner.nextLine();
            System.out.print("Entrez votre mot de passe : ");
            String motDePasse = scanner.nextLine();

            Agent tempAgent = new Agent(login, motDePasse); // L'ID sera hydraté par sauthentifier
            authenticated = tempAgent.sauthentifier(login, motDePasse);

            if (authenticated) {
                agent = tempAgent; // Assignation de l'agent authentifié
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

        if (authenticated) {
            int choix;
            do {
                System.out.println("\n--- Menu Agent ---");
                System.out.println("1. Ajouter un nouveau client");
                System.out.println("2. Bloquer un compte client");
                System.out.println("3. Débloquer un compte client");
                System.out.println("4. Supprimer un client");
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
                        // Demande directement l'ID du client à supprimer
                        System.out.print("Entrez l'ID du client à supprimer : ");
                        while (!scanner.hasNextInt()) {
                            System.out.println("Entrée invalide. Veuillez entrer un numéro d'ID valide.");
                            scanner.next(); // Consomme l'entrée invalide
                            System.out.print("Entrez l'ID du client à supprimer : ");
                        }
                        int clientIdToDelete = scanner.nextInt();
                        scanner.nextLine(); // Consomme la nouvelle ligne

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
