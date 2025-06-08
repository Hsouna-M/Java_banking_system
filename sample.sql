-- Create the database if it doesn't already exist
CREATE DATABASE IF NOT EXISTS banking_system_db;

-- Switch to the newly created database
USE banking_system_db;

-- -----------------------------------------------------
-- Table `clients`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS clients (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nom VARCHAR(100) NOT NULL,
    prenom VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    telephone VARCHAR(20),
    adresse VARCHAR(255),
    mot_de_passe_hash VARCHAR(255) NOT NULL
);

-- -----------------------------------------------------
-- Table `compte`
-- -----------------------------------------------------
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

-- -----------------------------------------------------
-- Table `agent`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS Agent (
    id INT AUTO_INCREMENT PRIMARY KEY,
    login VARCHAR(50) NOT NULL UNIQUE,
    mot_de_passe_hash VARCHAR(255) NOT NULL
);

-- -----------------------------------------------------
-- Table `transaction`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS transaction (
    id INT AUTO_INCREMENT PRIMARY KEY,
    montant DOUBLE NOT NULL,
    date_transaction DATETIME NOT NULL,
    compte_source_numero VARCHAR(50) NOT NULL,
    compte_destination_numero VARCHAR(50),
    FOREIGN KEY (compte_source_numero) REFERENCES compte(numero),
    FOREIGN KEY (compte_destination_numero) REFERENCES compte(numero)
);

-- -----------------------------------------------------
-- Table `message`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS message (
    id INT AUTO_INCREMENT PRIMARY KEY,
    sujet VARCHAR(50) NOT NULL,
    contenu VARCHAR(255) NOT NULL,
    date_message DATETIME NOT NULL,
    client_id INT,
    lu BOOLEAN NOT NULL,
    FOREIGN KEY (client_id) REFERENCES clients(id)
);

-- -----------------------------------------------------
-- Table `JournalAction`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS JournalAction (
    id INT AUTO_INCREMENT PRIMARY KEY,
    action_type VARCHAR(100) NOT NULL,
    action_date DATETIME NOT NULL,
    actor VARCHAR(100),
    details VARCHAR(255)
);

-- -----------------------------------------------------
-- Sample Data Insertion
-- -----------------------------------------------------

-- Data for table `clients`
INSERT INTO clients (nom, prenom, email, telephone, adresse, mot_de_passe_hash) VALUES
('Ben Ali', 'Amir', 'amir.benali@email.com', '21622123456', '12 Rue de la Liberté, Tunis', 'hash_pw_amir123'),
('Trabelsi', 'Fatma', 'fatma.trabelsi@email.com', '21655987654', '45 Avenue Habib Bourguiba, Sousse', 'hash_pw_fatma456'),
('Gupta', 'Rohan', 'rohan.gupta@email.com', '21698321654', '7 Rue du Lac, Berges du Lac', 'hash_pw_rohan789'),
('Chen', 'Li', 'li.chen@email.com', '21620555888', '88 Boulevard de l''Environnement, Sfax', 'hash_pw_li101'),
('Dupont', 'Marie', 'marie.dupont@email.com', '21650112233', '33 Rue Charles de Gaulle, Bizerte', 'hash_pw_marie112');

-- Data for table `Agent`
INSERT INTO Agent (login, mot_de_passe_hash) VALUES
('agent_sami', 'hashed_agent_pw_001'),
('agent_leila', 'hashed_agent_pw_002'),
('agent_karim', 'hashed_agent_pw_003'),
('agent_sarra', 'hashed_agent_pw_004'),
('agent_mehdi', 'hashed_agent_pw_005');

-- Data for table `compte`
INSERT INTO compte (numero, solde, date_ouverture, client_id, type_compte, estBlockee, tauxInteret) VALUES
('TN591001002003004005006', 5250.75, '2022-01-15', 1, 'Courant', FALSE, NULL),
('TN591001002003004005007', 15800.00, '2021-11-20', 2, 'Epargne', FALSE, 2.5),
('TN591001002003004005008', 730.50, '2023-05-10', 3, 'Courant', FALSE, NULL),
('TN591001002003004005009', 120500.20, '2020-03-01', 4, 'Investissement', FALSE, 4.75),
('TN591001002003004005010', -250.00, '2023-09-01', 5, 'Courant', TRUE, NULL);

-- Data for table `transaction`
INSERT INTO transaction (montant, date_transaction, compte_source_numero, compte_destination_numero) VALUES
(150.00, '2025-06-01 10:30:00', 'TN591001002003004005006', 'TN591001002003004005008'),
(2500.00, '2025-06-02 14:00:00', 'TN591001002003004005009', 'TN591001002003004005007'),
(75.50, '2025-06-03 09:15:25', 'TN591001002003004005007', 'TN591001002003004005006'),
(1200.00, '2025-06-05 18:45:00', 'TN591001002003004005006', 'TN591001002003004005009'),
(50.00, '2025-06-07 11:05:10', 'TN591001002003004005008', NULL);

-- Data for table `message`
INSERT INTO message (sujet, contenu, date_message, client_id, lu) VALUES
('Question sur le solde', 'Bonjour, je voudrais avoir une clarification sur le dernier mouvement de mon compte.', '2025-06-05 11:20:00', 1, FALSE),
('Demande de chéquier', 'Pourrais-je commander un nouveau chéquier s''il vous plaît ?', '2025-05-28 15:10:30', 2, TRUE),
('Problème de connexion', 'Je n''arrive pas à me connecter à mon espace en ligne.', '2025-06-08 09:00:05', 3, FALSE),
('Taux d''intérêt', 'Le taux de mon compte épargne a-t-il changé récemment ?', '2025-06-01 17:45:00', 4, TRUE),
('Compte bloqué', 'Pourquoi mon compte est-il bloqué ? Je n''arrive à faire aucune opération.', '2025-06-07 19:00:00', 5, FALSE);

-- Data for table `JournalAction`
INSERT INTO JournalAction (action_type, action_date, actor, details) VALUES
('CLIENT_LOGIN_SUCCESS', '2025-06-08 13:05:12', 'amir.benali@email.com', 'Client logged in from IP 192.168.1.10'),
('AGENT_BLOCK_ACCOUNT', '2025-06-07 14:22:01', 'agent_leila', 'Agent blocked account TN591001002003004005010 due to negative balance.'),
('TRANSFER_EXECUTION', '2025-06-05 18:45:00', 'SYSTEM', 'Transfer of 1200.00 from TN591001002003004005006 to TN591001002003004005009.'),
('CLIENT_LOGIN_FAIL', '2025-06-08 10:00:45', 'marie.dupont@email.com', 'Failed login attempt for client from IP 198.51.100.2'),
('MESSAGE_READ', '2025-06-02 09:30:00', 'agent_sami', 'Agent read message with ID 2 from client 2.');
