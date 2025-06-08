CREATE DATABASE IF NOT EXISTS banking_system_db;
USE banking_system_db;

-- Table clients
CREATE TABLE IF NOT EXISTS clients (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nom VARCHAR(100) NOT NULL,
    prenom VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    telephone VARCHAR(20),
    adresse VARCHAR(255),
    mot_de_passe_hash VARCHAR(255) NOT NULL
    );

-- Table compte
CREATE TABLE IF NOT EXISTS compte (
                                      numero VARCHAR(50) PRIMARY KEY,
    solde DOUBLE NOT NULL DEFAULT 0.0,
    date_ouverture DATE NOT NULL,
    client_id INT NOT NULL,
    type_compte VARCHAR(20) NOT NULL,
    estBlockee BOOLEAN,
    tauxInteret DOUBLE,
    FOREIGN KEY (client_id) REFERENCES clients(id)
    );

-- Table agent
CREATE TABLE IF NOT EXISTS Agent (
   id INT AUTO_INCREMENT PRIMARY KEY,
    login VARCHAR(50) NOT NULL UNIQUE,
    mot_de_passe_hash VARCHAR(255) NOT NULL
    );

-- Table transaction
CREATE TABLE IF NOT EXISTS transaction (
    id INT AUTO_INCREMENT PRIMARY KEY,
    montant DOUBLE NOT NULL,
    date_transaction DATETIME NOT NULL,
    compte_source_numero VARCHAR(50) NOT NULL,
    compte_destination_numero VARCHAR(50),
    FOREIGN KEY (compte_source_numero) REFERENCES compte(numero),
    FOREIGN KEY (compte_destination_numero) REFERENCES compte(numero)
    );

-- Table message
CREATE TABLE IF NOT EXISTS message (
    id INT AUTO_INCREMENT PRIMARY KEY,
    sujet VARCHAR(50) NOT NULL,
    contenu VARCHAR(255) NOT NULL,
    date_message DATETIME NOT NULL,
    client_id INT,
    lu BOOLEAN NOT NULL,
    FOREIGN KEY (client_id) REFERENCES clients(id)
    );

-- Table journal_action
CREATE TABLE IF NOT EXISTS JournalAction (
    id INT AUTO_INCREMENT PRIMARY KEY,
    action_type VARCHAR(100) NOT NULL,
    action_date DATETIME NOT NULL,
    actor VARCHAR(100),
    details VARCHAR(255)
    );
