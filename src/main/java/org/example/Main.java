package org.example;

import org.example.classes.*;
import java.util.Scanner;
import java.util.List;

public class Main {
    private static Scanner scanner = new Scanner(System.in);
    private static Agent authenticatedAgent = null;
    private static Client authenticatedClient = null;

    public static void main(String[] args) {
        System.out.println("Welcome to the Banking System!");

        while (true) {
            System.out.println("\n--- Main Menu ---");
            System.out.println("Are you a Client or an Agent?");
            System.out.println("1. Client Login");
            System.out.println("2. Agent Login");
            System.out.println("0. Exit");
            System.out.print("Enter your choice: ");

            int choice = getIntInput();

            switch (choice) {
                case 1:
                    handleClientLogin();
                    break;
                case 2:
                    handleAgentLogin();
                    break;
                case 0:
                    System.out.println("Thank you for using our system. Goodbye!");
                    scanner.close();
                    return;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    private static void handleClientLogin() {
        System.out.print("Enter your email: ");
        String email = scanner.nextLine();
        System.out.print("Enter your password: ");
        String password = scanner.nextLine();

        authenticatedClient = new Client();
        boolean success = authenticatedClient.sauthentifier(email, password);

        if (success) {
            System.out.println("\nWelcome, " + authenticatedClient.getPrenom() + " " + authenticatedClient.getNom() + "!");
            showClientMenu();
        } else {
            System.out.println("Authentication failed. Please check your credentials.");
        }
    }

    private static void showClientMenu() {
        while (true) {
            System.out.println("\n--- Client Menu ---");
            System.out.println("1. Create Account"); // New Option
            System.out.println("2. List my accounts");
            System.out.println("3. View a specific account's details");
            System.out.println("4. Make a transfer");
            System.out.println("5. Send a message to the bank");
            System.out.println("6. View transaction history"); // New Option
            System.out.println("0. Logout");
            System.out.print("Enter your choice: ");

            int choice = getIntInput();

            switch (choice) {
                case 2:
                    listClientAccounts();
                    break;
                case 3:
                    viewSpecificAccount();
                    break;
                case 4:
                    makeTransfer();
                    break;
                case 5:
                    sendMessage();
                    break;
                case 6:
                    viewTransactionHistory(); // New Case
                    break;
                case 1:
                    createNewAccount(); // New Case
                    break;
                case 0:
                    authenticatedClient = null;
                    System.out.println("You have been logged out.");
                    return;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    // New method to handle creating a new bank account
    private static void createNewAccount() {
        System.out.println("\n--- Create New Bank Account ---");
        System.out.println("Select account type:");
        System.out.println("1. Checking Account (Courant)");
        System.out.println("2. Savings Account (Epargne)");
        System.out.print("Enter your choice: ");

        int typeChoice = getIntInput();
        String accountType;

        if (typeChoice == 1) {
            accountType = "Courant";
        } else if (typeChoice == 2) {
            accountType = "Epargne";
        } else {
            System.out.println("Invalid choice. Account creation cancelled.");
            return;
        }

        System.out.print("Enter the initial deposit amount: ");
        double initialDeposit = getDoubleInput();
        if (initialDeposit < 0) {
            System.out.println("Initial deposit cannot be negative. Account creation cancelled.");
            return;
        }

        boolean success = authenticatedClient.createAccount(accountType, initialDeposit);

        if (success) {
            // The success message with the account number is already printed inside the createAccount method
            System.out.println("Your new account has been added to your profile.");
        } else {
            System.out.println("Failed to create the account. Please try again later.");
        }
    }

    // New method to handle viewing transaction history
    private static void viewTransactionHistory() {
        System.out.print("\nEnter the account number to view its history: ");
        String accNum = scanner.nextLine();

        // Security check to ensure the account belongs to the logged-in client
        if (!authenticatedClient.getListCompteNumeros().contains(accNum)) {
            System.out.println("Error: This account does not belong to you or does not exist.");
            return;
        }

        List<Transaction> transactions = Transaction.getTransactionsForAccount(accNum);

        if (transactions.isEmpty()) {
            System.out.println("No transactions found for this account.");
        } else {
            System.out.println("\n--- Transaction History for " + accNum + " ---");
            for (Transaction tx : transactions) {
                System.out.println(tx.toString());
            }
            System.out.println("-------------------------------------------");
        }
    }

   /* private static void showClientMenu() {
        while (true) {
            System.out.println("\n--- Client Menu ---");
            System.out.println("1. List my accounts");
            System.out.println("2. View a specific account's details");
            System.out.println("3. Make a transfer");
            System.out.println("4. Send a message to the bank");
            System.out.println("0. Logout");
            System.out.print("Enter your choice: ");

            int choice = getIntInput();

            switch (choice) {
                case 1:
                    listClientAccounts();
                    break;
                case 2:
                    viewSpecificAccount();
                    break;
                case 3:
                    makeTransfer();
                    break;
                case 4:
                    sendMessage();
                    break;
                case 0:
                    authenticatedClient = null;
                    System.out.println("You have been logged out.");
                    return;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }*/

    private static void listClientAccounts() {
        List<String> accountNumbers = authenticatedClient.getListCompteNumeros();
        if (accountNumbers.isEmpty()) {
            System.out.println("You do not have any accounts yet.");
            return;
        }
        System.out.println("\nYour accounts:");
        for (String accNum : accountNumbers) {
            System.out.println(Compte.getCompteByNumero(accNum).toString());
        }
    }

    private static void viewSpecificAccount() {
        System.out.print("Enter the account number to view: ");
        String accNum = scanner.nextLine();
        if (!authenticatedClient.getListCompteNumeros().contains(accNum)) {
            System.out.println("Error: This account does not belong to you or does not exist.");
            return;
        }
        Compte compte = Compte.getCompteByNumero(accNum);
        if (compte != null) {
            System.out.println(compte.toString());
        } else {
            System.out.println("Account not found.");
        }
    }

    private static void makeTransfer() {
        System.out.print("Enter your account number to transfer from: ");
        String sourceAcc = scanner.nextLine();
        System.out.print("Enter the destination account number: ");
        String destAcc = scanner.nextLine();
        System.out.print("Enter the amount to transfer: ");
        double amount = getDoubleInput();

        boolean success = authenticatedClient.effectuerVirement(sourceAcc, destAcc, amount);
        if (success) {
            System.out.println("Transfer completed successfully!");
        } else {
            System.out.println("Transfer failed. Please check account details and balance.");
        }
    }

    private static void sendMessage() {
        System.out.print("Enter the subject of your message: ");
        String subject = scanner.nextLine();
        System.out.print("Enter your message content: ");
        String content = scanner.nextLine();

        boolean success = authenticatedClient.createMessage(subject, content);
        if (success) {
            System.out.println("Message sent successfully.");
        } else {
            System.out.println("Failed to send message.");
        }
    }


    private static void handleAgentLogin() {
        System.out.println("\n--- Agent Authentication ---");
        System.out.print("Enter your login: ");
        String login = scanner.nextLine();
        System.out.print("Enter your password: ");
        String password = scanner.nextLine();

        authenticatedAgent = new Agent(login, password);
        if (authenticatedAgent.sauthentifier(login, password)) {
            System.out.println("Authentication successful! Welcome, " + authenticatedAgent.getLogin() + ".");
            showAgentMenu();
        } else {
            System.out.println("Authentication failed.");
            authenticatedAgent = null;
        }
    }

    private static void showAgentMenu() {
        while (true) {
            System.out.println("\n--- Agent Menu ---");
            System.out.println("1. Add a new client");
            System.out.println("2. Block a client account");
            System.out.println("3. Unblock a client account");
            System.out.println("4. Delete a client");
            System.out.println("5. View Data");
            System.out.println("0. Logout");
            System.out.print("Enter your choice: ");

            int choice = getIntInput();

            switch (choice) {
                case 1:
                    addNewClient();
                    break;
                case 2:
                    blockClientAccount();
                    break;
                case 3:
                    unblockClientAccount();
                    break;
                case 4:
                    deleteClient();
                    break;
                case 5:
                    showConsultationMenu();
                    break;
                case 0:
                    authenticatedAgent = null;
                    System.out.println("You have been logged out.");
                    return;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    private static void showConsultationMenu() {
        while(true) {
            System.out.println("\n--- Agent Consultation Menu ---");
            System.out.println("1. View Client Information");
            System.out.println("2. View Account Information");
            System.out.println("3. View Messages");
            System.out.println("4. View Transactions");
            System.out.println("0. Return to Main Agent Menu");
            System.out.print("Enter your choice: ");
            int choice = getIntInput();

            switch(choice) {
                case 1:
                    handleClientConsultation();
                    break;
                case 2:
                    handleAccountConsultation();
                    break;
                case 3:
                    handleMessageConsultation();
                    break;
                case 4:
                    handleTransactionConsultation();
                    break;
                case 0:
                    return; // Exit this menu
                default:
                    System.out.println("Invalid choice.");
            }
        }
    }

    private static void handleTransactionConsultation() {
        System.out.println("1. View all transactions\n2. View transactions for a specific account");
        System.out.print("Choice: ");
        int choice = getIntInput();
        if (choice == 1) {
            List<Transaction> transactions = authenticatedAgent.consulterTransactions();
            if (transactions.isEmpty()) System.out.println("No transactions found.");
            for (Transaction t : transactions) System.out.println(t.toString());
        } else if (choice == 2) {
            System.out.print("Enter account number: ");
            String accNum = scanner.nextLine();
            List<Transaction> transactions = Transaction.getTransactionsForAccount(accNum);
            if (transactions.isEmpty()) System.out.println("No transactions found for this account.");
            for (Transaction t : transactions) System.out.println(t.toString());
        }
    }

    private static void handleClientConsultation() {
        System.out.println("1. View all clients\n2. View a specific client by ID");
        System.out.print("Choice: ");
        int choice = getIntInput();
        if (choice == 1) {
            List<Client> clients = authenticatedAgent.consulterClients();
            if (clients.isEmpty()) System.out.println("No clients found.");
            for (Client c : clients) {
                System.out.println("ID: " + c.getId() + ", Name: " + c.getPrenom() + " " + c.getNom() + ", Email: " + c.getEmail());
                System.out.println("  Accounts: " + c.getListCompteNumeros());
            }
        } else if (choice == 2) {
            System.out.print("Enter client ID: ");
            int id = getIntInput();
            Client client = Client.getClientById(id);
            if (client != null) {
                System.out.println("ID: " + client.getId() + ", Name: " + client.getPrenom() + " " + client.getNom() + ", Email: " + client.getEmail());
                System.out.println("  Accounts: " + client.getListCompteNumeros());
            } else {
                System.out.println("Client not found.");
            }
        }
    }

    private static void handleAccountConsultation() {
        System.out.print("Enter account number: ");
        String accNum = scanner.nextLine();
        Compte compte = Compte.getCompteByNumero(accNum);
        if (compte != null) {
            System.out.println("--- Account Details ---");
            System.out.println(compte.toString());

            Client owner = Client.getClientById(compte.getClientId());
            if (owner != null) {
                System.out.println("--- Owner Details ---");
                System.out.println("Owner ID: " + owner.getId() + ", Name: " + owner.getPrenom() + " " + owner.getNom());
            }
        } else {
            System.out.println("Account not found.");
        }
    }

    private static void handleMessageConsultation() {
        System.out.println("1. View all unread messages\n2. View a specific message by ID");
        System.out.print("Choice: ");
        int choice = getIntInput();
        if (choice == 1) {
            List<Message> messages = Message.getAllUnreadMessages();
            if (messages.isEmpty()) System.out.println("No unread messages.");
            for (Message m : messages) System.out.println(m.toString());
        } else if (choice == 2) {
            System.out.print("Enter message ID: ");
            int id = getIntInput();
            Message msg = Message.getMessageById(id);
            if(msg != null) System.out.println(msg.toString());
            else System.out.println("Message not found.");
        }
    }

    private static void addNewClient() {
        System.out.println("\n--- Add New Client ---");
        System.out.print("Client's first name: ");
        String prenom = scanner.nextLine();
        System.out.print("Client's last name: ");
        String nom = scanner.nextLine();
        System.out.print("Client's email: ");
        String email = scanner.nextLine();
        System.out.print("Client's phone number: ");
        String telephone = scanner.nextLine();
        System.out.print("Client's address: ");
        String adresse = scanner.nextLine();
        System.out.print("Set a temporary password for the client: ");
        String clientMdp = scanner.nextLine(); // Should be hashed in a real application

        boolean clientAdded = authenticatedAgent.ajouterClient(nom, prenom, email, telephone, adresse, clientMdp); //
        if (clientAdded) {
            System.out.println("Client " + prenom + " " + nom + " added successfully.");
        } else {
            System.out.println("Failed to add client. The email might already be in use.");
        }
    }

    private static void blockClientAccount() {
        System.out.println("\n--- Block Account ---");
        System.out.print("Enter the account number to block: ");
        String numCompte = scanner.nextLine();
        boolean success = authenticatedAgent.bloquerCompte(numCompte); //
        if (success) {
            System.out.println("Account " + numCompte + " has been successfully blocked.");
        } else {
            System.out.println("Failed to block account " + numCompte + ". It may not exist.");
        }
    }

    private static void unblockClientAccount() {
        System.out.println("\n--- Unblock Account ---");
        System.out.print("Enter the account number to unblock: ");
        String numCompte = scanner.nextLine();
        boolean success = authenticatedAgent.debloquerCompte(numCompte); //
        if (success) {
            System.out.println("Account " + numCompte + " has been successfully unblocked.");
        } else {
            System.out.println("Failed to unblock account " + numCompte + ". It may not exist.");
        }
    }

    private static void deleteClient() {
        System.out.println("\n--- Delete Client ---");
        System.out.print("Enter the ID of the client to delete: ");
        int clientId = getIntInput();

        // Optional: Add a confirmation step
        System.out.print("Are you sure you want to delete client with ID " + clientId + "? This action is irreversible. (yes/no): ");
        String confirmation = scanner.nextLine();

        if (confirmation.equalsIgnoreCase("yes")) {
            boolean success = authenticatedAgent.supprimerClient(clientId); //
            if (success) {
                System.out.println("Client with ID " + clientId + " and all associated data has been deleted.");
            } else {
                System.out.println("Failed to delete client. Make sure the client ID is correct.");
            }
        } else {
            System.out.println("Client deletion cancelled.");
        }
    }

    private static int getIntInput() {
        while (!scanner.hasNextInt()) {
            System.out.println("Invalid input. Please enter a number.");
            scanner.next(); // consume the invalid input
        }
        int input = scanner.nextInt();
        scanner.nextLine(); // consume the newline
        return input;
    }

    private static double getDoubleInput() {
        while (!scanner.hasNextDouble()) {
            System.out.println("Invalid input. Please enter a valid amount.");
            scanner.next(); // consume the invalid input
        }
        double input = scanner.nextDouble();
        scanner.nextLine(); // consume the newline
        return input;
    }
}