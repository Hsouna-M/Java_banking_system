package org.example.classes;

import org.example.database.ConnectionBD;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

public class CompteCourant extends Compte {
    private double decouvert;

    public CompteCourant(String numero) {
        super(numero, "courant");
        this.decouvert = 500.0;
    }

    public double getDecouvert() {
        return decouvert;
    }

    public void setDecouvert(double decouvert) {
        this.decouvert = decouvert;
    }

    @Override
    public boolean retirer(double montant) {
        try (Connection conn = ConnectionBD.getConnection()) {
            String fetchSQL = "SELECT solde FROM compte WHERE numero = ?";
            PreparedStatement fetchStmt = conn.prepareStatement(fetchSQL);
                fetchStmt.setString(1, getNumero());
            var rs = fetchStmt.executeQuery();

            if (rs.next()) {
                double soldeActuel = rs.getDouble("solde");

                if (montant > soldeActuel + decouvert) {
                    System.out.println("Montant dépasse le découvert autorisé.");
                    return false;
                }

                double nouveauSolde = soldeActuel - montant;

                String updateSQL = "UPDATE compte SET solde = ? WHERE numero = ?";
                PreparedStatement updateStmt = conn.prepareStatement(updateSQL);
                updateStmt.setDouble(1, nouveauSolde);
                updateStmt.setString(2, getNumero());

                int rowsUpdated = updateStmt.executeUpdate();
                if (rowsUpdated > 0) {
                    setSolde(nouveauSolde); // Update the object too
                    System.out.println("Retrait effectué avec succès. Nouveau solde : " + nouveauSolde);
                    return true;
                } else {
                    System.out.println("Erreur lors de la mise à jour du solde.");
                    return false;
                }
            } else {
                System.out.println("Compte introuvable.");
                return false;
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


    @Override
    public boolean deposer(double montant) {
        try (Connection conn = ConnectionBD.getConnection()) {
            String fetchSQL = "SELECT solde FROM compte WHERE numero = ?";
            PreparedStatement fetchStmt = conn.prepareStatement(fetchSQL);
            fetchStmt.setString(1, getNumero());
            var rs = fetchStmt.executeQuery();

            if (rs.next()) {
                double soldeActuel = rs.getDouble("solde");
                double nouveauSolde = soldeActuel + montant;

                String updateSQL = "UPDATE compte SET solde = ? WHERE numero = ?";
                PreparedStatement updateStmt = conn.prepareStatement(updateSQL);
                updateStmt.setDouble(1, nouveauSolde);
                updateStmt.setString(2, getNumero());

                int rowsUpdated = updateStmt.executeUpdate();
                if (rowsUpdated > 0) {
                    setSolde(nouveauSolde); // update object
                    System.out.println("Dépôt effectué avec succès. Nouveau solde : " + nouveauSolde);
                    return true;
                } else {
                    System.out.println("Erreur lors de la mise à jour du solde.");
                    return false;
                }
            } else {
                System.out.println("Compte introuvable.");
                return false;
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }




    public void afficherInfos() {
        System.out.println(this.toString() + ", découvert autorisé = " + decouvert);
    }
}