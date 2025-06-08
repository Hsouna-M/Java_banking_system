package org.example.classes;

import java.sql.SQLException;
import java.util.Date;

import org.example.database.ConnectionBD;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;


public class Transaction {
    private String senderID;
    private String receiverID;
    private double montant;
    private Date dateTransaction;

    // Constructor
    public Transaction(String senderID, String receiverID, double montant) throws SQLException {
        this.senderID = senderID;
        this.receiverID = receiverID;
        this.montant = montant;
        this.dateTransaction = new Date(); // current time
    }

    // Getters
    public String getSenderID() {
        return senderID;
    }

    public String getReceiverID() {
        return receiverID;
    }

    public double getMontant() {
        return montant;
    }

    public Date getDateTransaction() {
        return dateTransaction;
    }

    // Setters
    public void setSenderID(String senderID) {
        this.senderID = senderID;
    }

    public void setReceiverID(String receiverID) {
        this.receiverID = receiverID;
    }

    public void setMontant(double montant) {
        this.montant = montant;
    }

    public void setDateTransaction(Date dateTransaction) {
        this.dateTransaction = dateTransaction;
    }


    public boolean executerTransaction(Compte sender, Compte receiver) {
        System.out.println("Démarrage de la transaction...");

        boolean retraitOK = sender.retirer(montant);
        if (!retraitOK) {
            System.out.println("Échec du retrait depuis le compte expéditeur.");
            return false;
        }

        boolean depotOK = receiver.deposer(montant);
        if (!depotOK) {
            System.out.println("Le dépôt a échoué, tentative de remboursement au compte expéditeur...");
            sender.deposer(montant);
            return false;
        }
    try(Connection conn = ConnectionBD.getConnection()) {
        String insertSQL = "INSERT INTO transaction (montant, date_transaction, compte_source_numero, compte_destination_numero) VALUES (?, ?, ?, ?)";
        PreparedStatement stmt = conn.prepareStatement(insertSQL);
        stmt.setDouble(1, montant);
        stmt.setTimestamp(2, new java.sql.Timestamp(dateTransaction.getTime()));
        stmt.setString(3, senderID);
        stmt.setString(4, receiverID);

        int rowsInserted = stmt.executeUpdate();
        if (rowsInserted > 0) {
            System.out.println("✅ Transaction enregistrée avec succès dans la base de données.");
            return true;
        } else {
            System.out.println("❌ Transaction échouée à l’enregistrement.");
            return false;
        }

    } catch (SQLException e) {
        System.out.println("Erreur SQL lors de l'enregistrement de la transaction.");
        e.printStackTrace();
        return false;
    }
}


    @Override
    public String toString() {
        return "Transaction{" +
                "senderID='" + senderID + '\'' +
                ", receiverID='" + receiverID + '\'' +
                ", montant=" + montant +
                ", dateTransaction=" + dateTransaction +
                '}';
    }
}