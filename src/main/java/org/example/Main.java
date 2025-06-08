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

    // Méthode utilitaire pour obtenir l'ID d'un client par email
    public static String getClientIdByEmail(String email) {
        String sql = "SELECT id FROM clients WHERE email = ?";
        try (Connection conn = ConnectionBD.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, email);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getString("id");
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération de l'ID client par email: " + e.getMessage());
        }
        return null;
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in); // Création du Scanner

        // --- Test de connexion à la base de données ---
        System.out.println("--- Test de connexion à la base de données ---");
        try (Connection conn = ConnectionBD.getConnection()) {
            if (conn != null) {
                System.out.println("Connexion à la base de données réussie !");
            } else {
                System.out.println("Échec de la connexion à la base de données. Veuillez vérifier votre configuration.");
                return; // Arrêter si la connexion échoue
            }
        } catch (SQLException e) {
            System.err.println("Erreur SQL lors du test de connexion : " + e.getMessage());
            return;
        }
        System.out.println("----------------------------------------------\n");

        Agent agent = null; // L'objet agent sera créé après authentification
        boolean authenticated = false;
        int maxAttempts = 3; // Nombre maximum de tentatives d'authentification
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
                agent = tempAgent; // Assignation de l'agent authentifié
                System.out.println(" Bienvenue, " + agent.getLogin() + ".");
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
                System.out.println("0. Quitter");
                System.out.print("Entrez votre choix : ");

                while (!scanner.hasNextInt()) {
                    System.out.println("Entrée invalide. Veuillez entrer un numéro.");
                    scanner.next(); // Consomme l'entrée invalide
                    System.out.print("Entrez votre choix : ");
                }
                choix = scanner.nextInt();
                scanner.nextLine(); // Consomme la nouvelle ligne restante après nextInt()

                switch (choix) {
                    case 1:
                        System.out.println("\n--- Ajout d'un nouveau client ---");
                        System.out.print("Nom du client : ");
                        String nom = scanner.nextLine();
                        System.out.print("Prénom du client : ");
                        String prenom = scanner.nextLine();
                        // L'attribut agence n'existe pas dans votre DB pour les clients.
                        // Je le laisse ici pour coller à votre constructeur Client si vous l'utilisez,
                        // mais il n'est pas utilisé par agent.ajouterClient.
                        // Supprimez cette ligne si vous modifiez le constructeur Client.
                        // String agence = "Non renseignée"; // ou demander à l'utilisateur
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
                        boolean blocked = agent.bloquerCompte(numCompteBloquer);
                        if (blocked) {
                            System.out.println("Compte " + numCompteBloquer + " bloqué avec succès.");
                        } else {
                            System.out.println("Échec du blocage du compte " + numCompteBloquer + ".");
                        }
                        break;
                    case 3:
                        System.out.println("\n--- Déblocage d'un compte ---");
                        System.out.print("Entrez le numéro du compte à débloquer : ");
                        String numCompteDebloquer = scanner.nextLine();
                        boolean unblocked = agent.debloquerCompte(numCompteDebloquer);
                        if (unblocked) {
                            System.out.println("Compte " + numCompteDebloquer + " débloqué avec succès.");
                        } else {
                            System.out.println("Échec du déblocage du compte " + numCompteDebloquer + ".");
                        }
                        break;
                    case 4:
                        System.out.println("\n--- Suppression d'un client ---");
                        System.out.print("Entrez l'ID du client à supprimer : ");
                        while (!scanner.hasNextInt()) {
                            System.out.println("Entrée invalide. Veuillez entrer un numéro d'ID valide.");
                            scanner.next(); // Consomme l'entrée invalide
                            System.out.print("Entrez l'ID du client à supprimer : ");
                        }
                        int clientIdToDelete = scanner.nextInt();
                        scanner.nextLine(); // Consomme la nouvelle ligne

                        boolean clientSuppressed = agent.supprimerClient(clientIdToDelete);
                        if (clientSuppressed) {
                            System.out.println("Client avec ID " + clientIdToDelete + " et ses données associées supprimés.");
                        } else {
                            System.out.println("Échec de la suppression du client avec ID " + clientIdToDelete + ". Vérifiez l'ID.");
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
