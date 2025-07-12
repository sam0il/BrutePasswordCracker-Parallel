package org.example;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class BruteForceWorker implements Runnable {
    private final String charSet;
    private final int length;
    private final int startIndex;
    private final int endIndex;
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

            if (currentAttempt % 1000 == 0) { // from 4284ms to 1642
                System.out.println("Attempt [" + currentAttempt + "]: Trying " + candidate);
            }

            if (validator.validate(candidate)) {
                result.setPassword(candidate);
                System.out.println("Password found: " + candidate + " at attempt [" + currentAttempt + "]");
                break;
            }
        }
    }

    private String buildCandidate(String partial) {
        char[] candidate = new char[length];
        int partialIndex = 0;

        for (int position = 0; position < length; position++) {
            if (maskMap.containsKey(position)) {
                candidate[position] = maskMap.get(position);
            } else {
                candidate[position] = partial.charAt(partialIndex++);
            }
        }
        return new String(candidate);
    }

    private String indexToPassword(int index, int base, int len) {
        if (len == 0) {
            return ""; // Handle case where there are no variable characters
        }

        char[] password = new char[len];
        for (int i = len - 1; i >= 0; i--) {
            password[i] = charSet.charAt(index % base);
            index /= base;
        }
        return new String(password);
    }
}