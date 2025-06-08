package org.example.classes;

import java.util.Date;

public abstract class Compte {
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

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public double getSolde() {
        return solde;
    }

    public void setSolde(double solde) {
        this.solde = solde;
    }
    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type=type;
    }

    public abstract boolean retirer(double montant);
    public abstract boolean deposer(double montant);

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