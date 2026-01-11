package passwordmanager.cli;

// Java imports, for I/O and data structures
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

// AWT imports, for clipboard access
import java.awt.datatransfer.StringSelection;
import java.awt.Toolkit;

// Model imports, for Vault and Entry management
import passwordmanager.model.Entry;
import passwordmanager.model.Vault;
import passwordmanager.services.PasswordGenerator;

public class CommandlineInterface {
    private final Vault vault = new Vault();
    private final Scanner scanner = new Scanner(System.in);
    private List<Entry> currentEntries;
    private String masterPassword;

    public void start() {
        System.out.println("Welcome to the Password Manager CLI.");

        System.out.print("Enter your master password:");
        this.masterPassword = scanner.nextLine().trim();

        try {
            this.currentEntries = vault.load(this.masterPassword);
            System.out.println("Vault loaded successfully!");
        } catch (FileNotFoundException e) {
            System.out.println("Failed to load vault, File not found.");
            System.out.println("Do you wish to create a new vault? (y/n): ");
            String response = scanner.nextLine().trim();
            if (!response.equalsIgnoreCase("y")) {
                System.out.println("Exiting...");
                scanner.close();
                return;
            }
            this.currentEntries = new ArrayList<>();
        } catch (SecurityException e) {
            System.err.println("ERROR: " + e.getMessage());
            System.err.println("Please restart and try again.");
            scanner.close();
            return;
        } catch (IOException e) {
            if (e.getCause() instanceof javax.crypto.AEADBadTagException) {
                System.out.println("ERROR: Incorrect master password provided.");
                System.out.println("Please restart and try again.");
            } else {
                System.out.println("ERROR: I/O error while loading vault: " + e.getMessage());
            }
            scanner.close();
            return;
        } catch (Exception e) {
            System.out.println("An error occurred while loading the vault: " + e.getMessage() + "\n Stack trace:");
            e.printStackTrace();
            scanner.close();
            return;
        }

        commandMenu();
        scanner.close();
    }

    private void commandMenu() {
        System.out.println("Type 'help' to see available commands.");

        while (true) {
            System.out.print("\n> ");
            String input = scanner.nextLine().trim();
            if (input.equalsIgnoreCase("exit")) {
                System.out.println("Closing...");
                break;
            }
            handleCommand(input);
        }
    }

    private void handleCommand(String command) {
        if (command.isBlank()) {
            return;
        }

        String components[] = command.trim().split("\\s+");
        String instruction = components[0];

        switch (instruction) {
            case "help" -> printHelp();
            case "add" -> handleAdd(components);
            case "show" -> handleShow(components);
            case "delete" -> handleDelete(components);
            case "list" -> handleList();
            case "exit" -> {
                System.out.println("Closing...");
                System.exit(0);
            }
            default -> System.out.println("Unknown command...\nType 'help' to see available commands.");
        }
    }

    private void printHelp() {
        System.out.println(
                """
                        Available commands:
                        > add <name> <username> <password>\tAdds a new password for a named service along with a username.
                        > add <name> <username> ''\tAdds a new password for a named service along with a username, with a randomly generated password.
                        > list\tShow registered services with available passwords.
                        > show <name>\tObtain a particular password.
                        > delete <name>\tDelete a particular password. Requires master password.
                        > help\tShow this command.
                        > exit\tClose this app.
                        """);
    }

    private void handleAdd(String[] components) {
        if (components.length != 4 && components.length != 3) {
            System.out.println(
                    "Usage: add <name> <username> <password> or add <name> <username> '' to generate a password.");
            return;
        }
        String service = components[1];
        String name = components[2];
        String password = (components.length < 4) ? PasswordGenerator.generate(16) : components[3];
        Entry entry = new Entry(service, name, password);

        this.currentEntries.add(entry);

        try {
            System.out.println("Saving to disk...");
            vault.save(this.currentEntries, this.masterPassword);
            System.out.println("Entry for " + service + " saved successfully!");
        } catch (Exception e) {
            System.out.println("CRITICAL ERROR: Could not save data!");
            e.printStackTrace();
            this.currentEntries.remove(entry);
        }
    }

    private void handleShow(String[] components) {
        if (components.length != 2) {
            System.out.println("Usage: show <service_name>");
            return;
        }

        String searchService = components[1];
        boolean found = false;

        for (Entry entry : this.currentEntries) {
            if (entry.getService().equalsIgnoreCase(searchService)) {
                System.out.println("Found entry:");
                System.out.println(entry.toString());
                System.out.println("Would you like to copy the password to your clipboard? (y/n): ");
                String response = scanner.nextLine().trim();
                if (response.equalsIgnoreCase("y")) {
                    Toolkit.getDefaultToolkit()
                            .getSystemClipboard()
                            .setContents(new StringSelection(entry.getPassword()), null);
                    System.out.println("Password copied to clipboard.");
                }
                found = true;
                break;
            }
        }

        if (!found) {
            System.out.println("No entry found for service: " + searchService);
        }
    }

    private void handleDelete(String[] components) {
        // 1. check components
        if (components.length != 2) {
            System.out.println("Usage: delete <service_name>");
            return;
        }
        // 2. search entry
        String searchService = components[1];
        Entry toRemove = null;
        for (Entry entry : this.currentEntries) {
            if (entry.getService().equalsIgnoreCase(searchService)) {
                toRemove = entry;
                break;
            }
        }

        if (toRemove == null) {
            System.out.println("No entry found for service: " + searchService);
            return;
        }

        // 3. requires confirmation for master password
        System.out.println("WARNING: You are about to delete the password for: " + toRemove.getService());
        System.out.print("Please confirm your Master Password to continue: ");
        String confirmation = scanner.nextLine().trim();

        if (!confirmation.equals(this.masterPassword)) {
            System.out.println("Wrong password! Deletion cancelled.");
            return;
        }

        // 4 delete entry
        this.currentEntries.remove(toRemove);
        System.out.println("Entry for " + toRemove.getService() + " deleted.");

        try {
            vault.save(this.currentEntries, this.masterPassword);
            System.out.println("Changes saved to disk successfully.");
        } catch (Exception e) {
            System.out.println("ERROR: Could not save changes to disk.");
            e.printStackTrace();
            this.currentEntries.add(toRemove); // Rollback
        }
    }

    private void handleList() {
        if (this.currentEntries.isEmpty()) {
            System.out.println("There are no passwords currently stored.");
            return;
        }

        System.out.println("--- Stored Services ---");
        for (Entry entry : this.currentEntries) {
            System.out.println("> " + entry.getService() + " (" + entry.getUsername() + ")");
        }
    }
}
