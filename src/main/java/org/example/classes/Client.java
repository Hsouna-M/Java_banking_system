package org.example.classes;

import org.example.database.ConnectionBD;

import java.sql.*;
import java.util.ArrayList;

public class Client implements ServiceAuthentification {
    private int id;
    private String nom;
    private String prenom;
    private String email;
    private String numTel;
    private String adresse;
    private ArrayList<String> listCompteNumeros;

    /**
     * Constructor for creating a new client instance.
     */
    public Client(String nom, String prenom, String email, String numTel, String adresse) {
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.numTel = numTel;
        this.adresse = adresse;
        this.listCompteNumeros = new ArrayList<>();
    }

    /**
     * Default constructor.
     */
    public Client() {
        this.listCompteNumeros = new ArrayList<>();
    }

    // Getters and Setters
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

    public ArrayList<String> getListCompteNumeros() {
        return listCompteNumeros;
    }

    /**
     * Fetches the account numbers for this client from the database.
     */
    public void fetchCompteNumeros() {
        if (this.id == 0) {
            System.out.println("Client ID is not set. Cannot fetch accounts.");
            return;
        }
        this.listCompteNumeros.clear();
        String sql = "SELECT numero FROM compte WHERE client_id = ?";
        try (Connection conn = ConnectionBD.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, this.id);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    this.listCompteNumeros.add(rs.getString("numero"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean sauthentifier(String email, String mdp) {
        String sql = "SELECT id, nom, prenom, telephone, adresse, mot_de_passe_hash FROM clients WHERE email = ?";
        try (Connection conn = ConnectionBD.getConnection();
             PreparedStatement preparedStatement = conn.prepareStatement(sql)) {
            preparedStatement.setString(1, email);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    String storedPasswordHash = resultSet.getString("mot_de_passe_hash");

                    // In a real application, you would use a secure password hashing library like BCrypt.
                    if (mdp.equals(storedPasswordHash)) {
                        // Populate client object with data from DB
                        this.id = resultSet.getInt("id");
                        this.nom = resultSet.getString("nom");
                        this.prenom = resultSet.getString("prenom");
                        this.email = email;
                        this.numTel = resultSet.getString("telephone");
                        this.adresse = resultSet.getString("adresse");
                        fetchCompteNumeros(); // Fetch associated account numbers
                        return true;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public int getId(){
        return this.id;
    }

    /**
     * Creates a new bank account for the client.
     *
     * @param typeCompte   The type of the account (e.g., "Courant", "Epargne").
     * @param initialSolde The initial balance of the account.
     * @return true if the account was created successfully, false otherwise.
     */


    public boolean createAccount(String typeCompte, double initialSolde) {
        if (this.getId() == 0) {
            System.out.println("Cannot create account. Client is not saved or identified in the database.");
            return false;
        }

        // Generate a unique account number
        String numero = "TN" + System.currentTimeMillis();

        String sql = "INSERT INTO compte (numero, solde, date_ouverture, client_id, type_compte, estBlockee, tauxInteret) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = ConnectionBD.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, numero);
            pstmt.setDouble(2, initialSolde);
            pstmt.setDate(3, new java.sql.Date(System.currentTimeMillis()));
            pstmt.setInt(4, this.getId());
            pstmt.setString(5, typeCompte);
            pstmt.setBoolean(6, false); // Not blocked by default

            if ("Epargne".equalsIgnoreCase(typeCompte)) {
                // Using the constant from CompteEpargne class for the interest rate
                pstmt.setDouble(7, CompteEpargne.TAUX_INTERET);
            } else {
                pstmt.setNull(7, Types.DOUBLE);
            }

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                // Add the new account number to the list in the object
                this.listCompteNumeros.add(numero);
                System.out.println("Account created successfully with number: " + numero);
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error creating account: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Creates and sends a new message from the client.
     *
     * @param sujet   The subject of the message.
     * @param contenu The content of the message.
     * @return true if the message was sent successfully, false otherwise.
     */
    public boolean createMessage(String sujet, String contenu) {
        if (this.id == 0) {
            System.out.println("Cannot send message. Client is not identified in the database.");
            return false;
        }
        // Calls the static method from the Message class to send the message
        return Message.envoyerMessage(sujet, contenu, this.id);
    }

    /**
     * Transfers a specified amount from a source account to a destination account.
     * The source account must belong to the client.
     *
     * @param compteSourceNumero      The account number of the source account.
     * @param compteDestinataireNumero The account number of the destination account.
     * @param montant                 The amount to transfer.
     * @return true if the transfer was successful, false otherwise.
     */
    public boolean effectuerVirement(String compteSourceNumero, String compteDestinataireNumero, double montant) {
        // Security check: Ensure the source account belongs to this client
        if (!this.listCompteNumeros.contains(compteSourceNumero)) {
            System.err.println("Transaction failed: The source account does not belong to this client.");
            return false;
        }

        // Fetch Compte objects from the database using the new helper method
        Compte compteSource = Compte.getCompteByNumero(compteSourceNumero);
        Compte compteDestinataire = Compte.getCompteByNumero(compteDestinataireNumero);

        // Validate that accounts exist
        if (compteSource == null) {
            System.err.println("Transaction failed: Source account not found.");
            return false;
        }
        if (compteDestinataire == null) {
            System.err.println("Transaction failed: Destination account not found.");
            return false;
        }

        // Check if the source account is blocked
        if (compteSource.isEstBlockee()) {
            System.err.println("Transaction failed: Source account is blocked.");
            return false;
        }

        // Execute the transaction
        try {
            Transaction transaction = new Transaction(compteSourceNumero, compteDestinataireNumero, montant);
            // The executerTransaction method handles the withdrawal, deposit, and DB logging
            return transaction.executerTransaction(compteSource, compteDestinataire);
        } catch (SQLException e) {
            System.err.println("Error creating the transaction object: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
// Add this new static method to the Client.java class

    /**
     * Retrieves a Client object from the database by its ID.
     *
     * @param clientId The ID of the client.
     * @return A Client object, or null if not found.
     */
    public static Client getClientById(int clientId) {
        String sql = "SELECT id, nom, prenom, email, telephone, adresse FROM clients WHERE id = ?";
        try (Connection conn = ConnectionBD.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, clientId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                Client client = new Client();
                client.id = rs.getInt("id"); // Assuming 'id' can be accessed
                client.setNom(rs.getString("nom"));
                client.setPrenom(rs.getString("prenom"));
                client.setEmail(rs.getString("email"));
                client.setNumTel(rs.getString("telephone"));
                client.setAdresse(rs.getString("adresse"));
                client.fetchCompteNumeros(); // Fetch associated accounts
                return client;
            }
        } catch (SQLException e) {
            System.err.println("Error fetching client by ID: " + e.getMessage());
        }
        return null;
    }
}