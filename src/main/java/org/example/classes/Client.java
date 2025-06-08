package org.example.classes;

import java.sql.SQLException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import org.example.database.ConnectionBD;
import java.util.ArrayList;
import java.util.Date;

public class Client implements ServiceAuthentification{
    private int id;
    private String nom;
    private String prenom;
    private String agence;
    private String email;
    private String numTel;
    private String adresse;
    private ArrayList<Compte> listComptes;

    public Client(String nom, String prenom, String agence, String email, String numTel, String adresse) {
        this.nom = nom;
        this.prenom = prenom;
        this.agence = agence;
        this.email = email;
        this.numTel = numTel;
        this.adresse = adresse;
        this.listComptes = new ArrayList<Compte>();
    }

    public int getId() {
        return this.id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public String getAgence() {
        return agence;
    }

    public void setAgence(String agence) {
        this.agence = agence;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getNumTel() {
        return numTel;
    }

    public void setNumTel(String numTel) {
        this.numTel = numTel;
    }

    public String getAdresse() {
        return adresse;
    }

    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }

    public ArrayList<Compte> getListComptes() {
        return listComptes;
    }

    public void fectchComtes() {
        this.listComptes = listComptes;
    }

    @Override
    public boolean sauthentifier (String email, String mdp) {
        Connection conn = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            conn = ConnectionBD.getConnection();
            String sql = "SELECT mot_de_passe_hash FROM clients WHERE email = ?";
            preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setString(1, email);
            resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                String storedPasswordHash = resultSet.getString("mot_de_passe_hash");

                // we might need to encrypt to hash the password live and comprare it to the sotred hash in the database

                return mdp.equals(storedPasswordHash);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {

            try {
                if (resultSet != null) resultSet.close();
                if (preparedStatement != null) preparedStatement.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    // this is a comment to test the hsouna branch
    /**
     * Adds a new account for the client by specifying its details.
     *
     * @param numero       The account number.
     * @param typeCompte   The type of account (e.g., "Courant", "Epargne").
     * @param soldeInitial The initial balance of the account.
     * @return true if the account was added successfully, false otherwise.
     */
    public boolean ajouterCompte(String numero, String typeCompte, double soldeInitial) {
        // The client must be authenticated and have an ID to add an account
        String sql = "INSERT INTO compte (numero, solde, date_ouverture, client_id, type_compte, estBlockee, tauxInteret) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = ConnectionBD.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, numero);
            pstmt.setDouble(2, soldeInitial);
            pstmt.setDate(3, new java.sql.Date(new java.util.Date().getTime())); // Set current date
            pstmt.setInt(4, this.getId());
            pstmt.setString(5, typeCompte);
            pstmt.setBoolean(6, false); // New accounts are not blocked by default

            //Compte newAccount; // Will hold the instance of the new account

            // Set interest rate and create the correct account type instance
            if ("Epargne".equalsIgnoreCase(typeCompte)) {
                // Assumes CompteEpargne has a static getter for the interest rate
                pstmt.setDouble(7, new CompteEpargne(numero,"Ep").getTauxInteret());
                newAccount = new CompteEpargne(numero);
            } else if ("Courant".equalsIgnoreCase(typeCompte)) {
                pstmt.setNull(7, Types.DOUBLE);
                // CompteCourant constructor requires an overdraft value, defaulting to 0.0
                newAccount = new CompteCourant(numero, typeCompte, 0.0);
            } else {
                // For a generic account type that might not be 'Courant' or 'Epargne'
                pstmt.setNull(7, Types.DOUBLE);
                newAccount = new Compte(numero, typeCompte);
            }

            // Set the balance for the new account object
            newAccount.setSolde(soldeInitial);

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                this.listComptes.add(newAccount); // Add the new account to the in-memory list
                System.out.println("Account " + numero + " of type " + typeCompte + " added successfully for client " + this.nom + ".");
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error adding account: " + e.getMessage());
        }
        return false;
    }
}