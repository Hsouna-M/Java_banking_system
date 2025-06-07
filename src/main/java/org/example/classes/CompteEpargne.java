package org.example.classes;

import java.util.Date;
import java.util.Calendar;

public class CompteEpargne extends Compte {
    private static final double TAUX_INTERET = 0.05;

    public CompteEpargne(int RIB, String type) {
        super(RIB, "Epargne");
    }

    @Override
    public boolean retirer(double montant) {
        if (montant > getSolde()) {
            System.out.println("Fonds insuffisants pour un retrait d'un compte épargne.");
            return false;
        } else {
            setSolde(getSolde() - montant);
            return true;
        }
    }

    public void appliquerInteretAnnuel() {
        // Ajoute 5% d'intérêt au solde
        double interet = getSolde() * TAUX_INTERET;
        setSolde(getSolde() + interet);
        System.out.println("Intérêts annuels appliqués : +" + interet + " TND");
    }

    @Override
    public void afficherInfos() {
        System.out.println(this.toString() + " (Compte Épargne avec intérêts à 5%)");
    }
}