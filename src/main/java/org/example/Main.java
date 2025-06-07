package org.example;

import org.example.classes.Agent;

public class Main {
    public static void main(String[] args) {

        String login = "[AGENT]";
        String motDePasse = "[******]";

        Agent agent = new Agent(login, motDePasse);

        boolean resultat = agent.sauthentifier(login, motDePasse);

        if (resultat) {
            System.out.println("Test d'authentification réussi !");
        } else {
            System.out.println("Test d'authentification échoué.");
        }


    }
}



