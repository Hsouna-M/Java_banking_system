package org.example.classes;

import java.util.ArrayList;

public class Client implements ServiceAuthentification{
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

    public void setListComptes(ArrayList<Compte> listComptes) {
        this.listComptes = listComptes;
    }

    @Override
    public boolean sauthetifier(String email,String mdp) {
        return false;
    }
}
