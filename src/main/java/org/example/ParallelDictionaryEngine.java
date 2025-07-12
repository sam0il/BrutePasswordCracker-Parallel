package org.example;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class ParallelDictionaryEngine {
    private final HashValidator validator;
    private final String dictionaryPath;
    private final AtomicInteger attempts = new AtomicInteger(0);
    private final ResultHolder result = new ResultHolder();

    public ParallelDictionaryEngine(HashValidator validator, String dictionaryPath) {
        this.validator = validator;
        this.dictionaryPath = dictionaryPath;
    }

    public String crackPassword() {
        // Read all passwords into memory
        List<String> passwords = readAllPasswords();
        if (passwords == null || passwords.isEmpty()) {
            System.out.println("Dictionary is empty or could not be read");
            return null;
        }

        int threadCount = Runtime.getRuntime().availableProcessors();
        System.out.println("Using " + threadCount + " threads for dictionary attack");

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        // Split passwords into chunks for each thread
        int chunkSize = (passwords.size() + threadCount - 1) / threadCount;

        for (int i = 0; i < threadCount; i++) {
            int start = i * chunkSize;
            int end = Math.min(start + chunkSize, passwords.size());
            if (start < end) {
                List<String> chunk = new ArrayList<>(passwords.subList(start, end));
                executor.submit(new DictionaryWorker(chunk));
            }
        }

        executor.shutdown();
        try {
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            System.out.println("Dictionary attack interrupted");
        }

        return result.getPassword();
    }

    private List<String> readAllPasswords() {
        List<String> passwords = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(dictionaryPath))) {
            String password;
            while ((password = reader.readLine()) != null) {
                passwords.add(password.trim());
            }
            System.out.println("Loaded " + passwords.size() + " passwords from dictionary");
        } catch (IOException e) {
            System.out.println("Error reading dictionary file: " + e.getMessage());
        }
        return passwords;
    }

    private class DictionaryWorker implements Runnable {
        private final List<String> passwords;

        public DictionaryWorker(List<String> passwords) {
            this.passwords = passwords;
        }

        @Override
        public void run() {
            for (String password : passwords) {
                // Early termination if password was found by another thread
                if (result.isFound()) {
                    return;
                }

                int currentAttempt = attempts.incrementAndGet();
                if (currentAttempt % 1000 == 0) {
                    System.out.println("Attempt: [" + currentAttempt + "] Trying: " + password);
                }

                if (validator.validate(password)) {
                    result.setPassword(password);
                    return;
                }
            }
        }
    }
}