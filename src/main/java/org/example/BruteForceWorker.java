package org.example;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class BruteForceWorker implements Runnable {
    private final String charSet;
    private final int length, startIndex, endIndex;
    private final HashValidator validator;
    private final ResultHolder result;
    private final AtomicInteger attemptCounter;
    private final Map<Integer, Character> maskMap;

    public BruteForceWorker(String charSet, int length, int startIndex, int endIndex,
                            HashValidator validator, ResultHolder result,
                            AtomicInteger attemptCounter, Map<Integer, Character> maskMap) {
        this.charSet = charSet;
        this.length = length;
        this.startIndex = startIndex;
        this.endIndex = endIndex;
        this.validator = validator;
        this.result = result;
        this.attemptCounter = attemptCounter;
        this.maskMap = maskMap;
    }

    @Override
    public void run() {
        int base = charSet.length();
        int variableLength = length - maskMap.size();

        for (int i = startIndex; i < endIndex && !result.isFound(); i++) {
            String partial = indexToPassword(i, base, variableLength);
            String candidate = buildCandidate(partial);

            int currentAttempt = attemptCounter.incrementAndGet();
            if (currentAttempt % 1000 == 0) {
                System.out.println("Attempt [" + currentAttempt + "]: Trying " + candidate);
            }

            if (validator.validate(candidate)) {
                result.setPassword(candidate);
                System.out.println("Password found: " + candidate + " at attempt [" + currentAttempt + "]");
                break;
            }
        }
    }

    // Fills in mask and inserts variable characters
    private String buildCandidate(String partial) {
        char[] candidate = new char[length];
        int partialIndex = 0;
        for (int i = 0; i < length; i++) {
            candidate[i] = maskMap.containsKey(i) ? maskMap.get(i) : partial.charAt(partialIndex++);
        }
        return new String(candidate);
    }

    // Converts index to password using given base
    private String indexToPassword(int index, int base, int len) {
        char[] password = new char[len];
        for (int i = len - 1; i >= 0; i--) {
            password[i] = charSet.charAt(index % base);
            index /= base;
        }
        return new String(password);
    }
}
