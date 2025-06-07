package org.example.classes;

import java.util.Date;

public abstract class Compte {
    private int RIB;
    private String type;
    private double solde;
    private boolean estBlockee;
    private Date dateOuverture;

    public Compte(int RIB, String type) {
        this(RIB, type, 0.0, false); // default solde = 0.0, estBlockee = false
    }


    public Compte(int RIB, String type, double solde, boolean estBlockee) {
        this.RIB = RIB;
        this.type = type;
        this.solde = solde;
        this.estBlockee = estBlockee;
        this.dateOuverture = new Date();
    }

    public int getRIB() {
        return RIB;
    }

    public void setRIB(int RIB) {
        this.RIB = RIB;
    }

    public double getSolde() {
        return solde;
    }

    public void setSolde(double solde) {
        this.solde = solde;
    }

    public boolean retirer(double montant) {
        if (montant > solde) {
            System.out.println("Vous ne disposez pas de ce montant.\nVeuillez insérer un montant équivalent.");
            return false;
        } else {
            solde -= montant;
            return true;
        }
    }

    public abstract void afficherInfos(); // Abstract method

    @Override
    public String toString() {
        return "Compte{" +
                "RIB=" + RIB +
                ", type='" + type + '\'' +
                ", solde=" + solde +
                ", estBlockee=" + estBlockee +
                ", dateOuverture=" + dateOuverture +
                '}';
    }
}