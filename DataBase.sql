-- banking_system_db.sql - Script de mise à jour pour la base de données
CREATE DATABASE IF NOT EXISTS banking_system_db;
USE banking_system_db;

-- Table pour les clients (aucune modification si déjà créée comme précédemment)
CREATE TABLE IF NOT EXISTS clients (
    id VARCHAR(50) PRIMARY KEY,
    nom VARCHAR(100) NOT NULL,
    prenom VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    telephone VARCHAR(20),
    adresse VARCHAR(255),
    mot_de_passe_hash VARCHAR(255) NOT NULL
);

-- Table pour les comptes
-- Si elle existe déjà, vous devrez peut-être la DROPper et la recréer si les colonnes changent
-- ou utiliser ALTER TABLE pour ajouter/modifier des colonnes.
-- Pour simplifier ici, si vous avez des problèmes, supprimez la table et recréez-la.
-- DROP TABLE IF EXISTS compte; -- Uncomment this line if you need to drop the table before recreation
CREATE TABLE IF NOT EXISTS compte (
    numero VARCHAR(50) PRIMARY KEY,
    solde DOUBLE NOT NULL DEFAULT 0.0,
    date_ouverture DATE NOT NULL,
    client_id VARCHAR(50) NOT NULL, -- Clé étrangère vers la table clients
    type_compte VARCHAR(20) NOT NULL, -- 'COURANT' ou 'EPARGNE'
    estBlockee BOOLEAN, -- Applicable seulement pour les comptes courants (Changed BOOL to BOOLEAN for broader compatibility)
    tauxInteret DOUBLE,
    FOREIGN KEY (client_id) REFERENCES clients(id)
);

-- Table pour les administrateurs (aucune modification)
CREATE TABLE IF NOT EXISTS Agent (
    id VARCHAR(50) PRIMARY KEY,
    login VARCHAR(50) NOT NULL UNIQUE,
    mot_de_passe_hash VARCHAR(255) NOT NULL
);

-- Nouvelle table pour les transactions
CREATE TABLE IF NOT EXISTS transaction (
    id VARCHAR(50) PRIMARY KEY,
    montant DOUBLE NOT NULL,
    date_transaction DATETIME NOT NULL,
    compte_source_numero VARCHAR(50) NOT NULL, -- Numéro du compte source
    compte_destination_numero VARCHAR(50), -- Numéro du compte destination (nullable si type_transaction est DEBIT/CREDIT)
    FOREIGN KEY (compte_source_numero) REFERENCES compte(numero), -- Corrected 'comptes' to 'compte'
    FOREIGN KEY (compte_destination_numero) REFERENCES compte(numero) -- Corrected 'comptes' to 'compte'
);

-- Table pour les messages
CREATE TABLE IF NOT EXISTS Message (
    id VARCHAR(50) PRIMARY KEY,
    sujet VARCHAR(50) NOT NULL,
    contenu VARCHAR(255) NOT NULL, -- Corrected 'contenue' to 'contenu' and added appropriate length
    date_message DATETIME NOT NULL, -- Changed 'date' to 'date_message' to avoid keyword conflict and added appropriate type
    client_id VARCHAR(50),
    lu BOOLEAN NOT NULL, -- Changed BOOL to BOOLEAN
    FOREIGN KEY (client_id) REFERENCES clients(id)
);
CREATE TABLE IF NOT EXISTS JournalAction (
    id VARCHAR(50) PRIMARY KEY,
    action_type VARCHAR(100) NOT NULL,
    action_date DATETIME NOT NULL,
    actor VARCHAR(100),
    details VARCHAR(255)
    );
