package org.example;

import java.util.Scanner;
import java.util.HashSet;
import java.util.Set;

public class Main {
    private static final String dictionaryPath = "C:\\Users\\samoi\\IdeaProjects\\ProektoTest1\\src\\resources\\Passwords.txt";
    public static String crackedPassword;
    public static String mask = "";
    private static String maskIndicesInput;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter the target MD5 hash: ");
        String targetHash = scanner.nextLine().trim();

        if (targetHash.length() != 32 || !targetHash.matches("[a-fA-F0-9]+")) {
            System.out.println("Invalid MD5 hash. Must be 32 hex characters.");
            return;
        }

        System.out.print("Choose attack mode: 1 for Brute Force 2 for Dictionary Attack: ");
        int mode = scanner.nextInt();
        scanner.nextLine();

        if (mode == 1) {
            System.out.print("Enter the maximum password length to try and get to: ");
            int maxLength = scanner.nextInt();
            scanner.nextLine();

            System.out.print("Enter the character set to use (e.g., abcdef) or press Enter for default: ");
            String charset = scanner.nextLine().trim();
            if (charset.isEmpty()) {
                charset = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
                System.out.println("Using default character set: " + charset);
            }

            System.out.print("Enter mask indices (comma-separated, e.g. 0,2): ");
            maskIndicesInput = scanner.nextLine().trim();

            System.out.print("Enter a mask (optional, press Enter for no mask): ");
            mask = scanner.nextLine().trim();

            // Parse mask indices
            Set<Integer> indices = new HashSet<>();
            if (!maskIndicesInput.isEmpty()) {
                String[] parts = maskIndicesInput.split(",");
                for (String part : parts) {
                    try {
                        int index = Integer.parseInt(part.trim());
                        if (index < 0) {
                            System.out.println("Error: Mask index must be non-negative");
                            return;
                        }
                        indices.add(index);
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid mask index: " + part);
                        return;
                    }
                }
            }

            MaskConfig maskConfig = new MaskConfig(mask, indices);
            HashValidator validator = new HashValidator(targetHash);

            System.out.println("Starting brute-force attack...");
            long startTime = System.currentTimeMillis();

            ParallelBruteForceEngine engine = new ParallelBruteForceEngine(
                    validator, charset, maxLength, maskConfig
            );
            crackedPassword = engine.crackPassword();

            long endTime = System.currentTimeMillis();
            System.out.println("Time taken: " + (endTime - startTime) + " ms");

        } else {
            System.out.println("Invalid mode selected. Only Parallel Brute Force is available.");
            return;
        }

        if (crackedPassword != null) {
            System.out.println("Password cracked: " + crackedPassword);
        } else {
            System.out.println("Failed to crack the password.");
        }
    }
}