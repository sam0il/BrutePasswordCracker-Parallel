package org.example;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class ParallelBruteForceEngine {
    private final HashValidator validator;
    private final String charSet;
    private final int maxLength;
    private final int threadCount = Runtime.getRuntime().availableProcessors();
    private final MaskConfig maskConfig;

    public ParallelBruteForceEngine(HashValidator validator, String charSet, int maxLength, MaskConfig maskConfig) {
        this.validator = validator;
        this.charSet = charSet;
        this.maxLength = maxLength;
        this.maskConfig = maskConfig;
    }

    public String crackPassword() {
        ResultHolder result = new ResultHolder();
        AtomicInteger attemptCounter = new AtomicInteger();

        // Try every length up to max
        for (int length = 1; length <= maxLength; length++) {
            // Apply mask only to valid indices
            Set<Integer> currentMaskIndices = new HashSet<>();
            for (int index : maskConfig.maskIndices) {
                if (index < length) currentMaskIndices.add(index);
            }

            Map<Integer, Character> maskMap = maskConfig.getMaskMap(currentMaskIndices);
            int variableLength = length - maskMap.size();

            long totalCombinations = (long) Math.pow(charSet.length(), variableLength);
            if (totalCombinations == 0 || totalCombinations > Integer.MAX_VALUE) {
                System.out.println("Skipping length " + length + " (invalid or too many combinations)");
                continue;
            }

            int intTotal = (int) totalCombinations;
            int chunkSize = (int) Math.ceil((double) intTotal / threadCount);
            ExecutorService executor = Executors.newFixedThreadPool(threadCount);

            // Assign ranges to threads
            for (int i = 0; i < threadCount; i++) {
                int start = i * chunkSize;
                int end = Math.min(start + chunkSize, intTotal);

                executor.submit(new BruteForceWorker(charSet, length, start, end, validator, result, attemptCounter, maskMap));
            }

            executor.shutdown();
            while (!executor.isTerminated()) {
                // Wait for all threads
            }

            if (result.isFound()) return result.getPassword();
        }
        return null;
    }
}
