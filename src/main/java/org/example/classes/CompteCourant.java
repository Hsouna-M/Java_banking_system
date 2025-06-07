package org.example.classes;

public class CompteCourant extends Compte {
    private double decouvert;

    public CompteCourant(int RIB, String type, double decouvert) {
        super(RIB, "courant");
        this.decouvert = decouvert;
    }

    public double getDecouvert() {
        return decouvert;
    }

    public void setDecouvert(double decouvert) {
        this.decouvert = decouvert;
    }

    @Override
    public boolean retirer(double montant) {
        if (montant > (getSolde() + decouvert)) {
            System.out.println("Montant dépasse le découvert autorisé.");
            return false;
        } else {
            setSolde(getSolde() - montant);
            return true;
        }
    }

    @Override
    public void afficherInfos() {
        System.out.println(this.toString() + ", découvert autorisé = " + decouvert);
    }
}