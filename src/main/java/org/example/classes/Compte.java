package org.example.classes;

import java.util.Date;

public class Compte {
    private String numero;
    private String type;
    private double solde;
    private boolean estBlockee;
    private Date dateOuverture;

    public Compte(String numero, String type) {
        this(numero, type, 0.0, false); // default solde = 0.0, estBlockee = false
    }

    public Compte(String numero, String type, double solde, boolean estBlockee) {
        this.numero = numero;
        this.type = type;
        this.solde = solde;
        this.estBlockee = estBlockee;
        this.dateOuverture = new Date();
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(int RIB) {
        this.numero = numero;
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


    @Override
    public String toString() {
        return "Compte{" +
                "RIB=" + numero +
                ", type='" + type + '\'' +
                ", solde=" + solde +
                ", estBlockee=" + estBlockee +
                ", dateOuverture=" + dateOuverture +
                '}';
    }

    protected Date getDateOuverture() {
        return dateOuverture;
    }
}