package org.example.classes;
import org.example.database.ConnectionBD;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Date;
import java.util.Calendar;

public class CompteEpargne extends Compte {
    private static final double TAUX_INTERET = 0.05;

    public CompteEpargne(String numero, String type) {
        super(numero, "Epargne");
    }

    @Override
    public boolean retirer(double montant) {
        if (montant <= 0) {
            System.out.println("Montant invalide.");
            return false;
        }

        try (Connection conn = ConnectionBD.getConnection()) {
            String selectSql = "SELECT solde FROM compte WHERE numero = ?";
            PreparedStatement selectStmt = conn.prepareStatement(selectSql);
            selectStmt.setString(1, getNumero());

            try (var rs = selectStmt.executeQuery()) {
                if (rs.next()) {
                    double soldeActuel = rs.getDouble("solde");

                    if (montant > soldeActuel) {
                        return false;
                    }

                    if (montant > soldeActuel * 0.5) {
                        System.out.println("Vous ne pouvez pas retirer plus de 50% de votre solde en une seule fois.");
                        return false;
                    }

                    double nouveauSolde = soldeActuel - montant;

                    String updateSql = "UPDATE compte SET solde = ? WHERE numero = ?";
                    PreparedStatement updateStmt = conn.prepareStatement(updateSql);
                    updateStmt.setDouble(1, nouveauSolde);
                    updateStmt.setString(2, getNumero());

                    int rowsUpdated = updateStmt.executeUpdate();
                    if (rowsUpdated > 0) {
                        System.out.println("Fonds insuffisants pour un retrait d'un compte épargne.");
                        setSolde(nouveauSolde);
                        System.out.println("Retrait réussi. Nouveau solde: " + nouveauSolde);
                        return true;
                    }
                } else {
                    System.out.println("Compte épargne introuvable pour le RIB: " + getNumero());
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }


    public void appliquerInteretAnnuel() {
        // Ajoute 5% d'intérêt au solde
        double interet = getSolde() * TAUX_INTERET;
        setSolde(getSolde() + interet);
        System.out.println("Intérêts annuels appliqués : +" + interet + " TND");
    }

    public void afficherInfos() {
        System.out.println(this.toString() + " (Compte Épargne avec intérêts à 5%)");
    }
}