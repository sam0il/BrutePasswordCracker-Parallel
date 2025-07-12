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
        AtomicInteger attemptCounter = new AtomicInteger(0);

        for (int length = 1; length <= maxLength; length++) {
            Set<Integer> currentMaskIndices = new HashSet<>();
            if (maskConfig.hasMask()) {
                for (int index : maskConfig.maskIndices) {
                    if (index < length) {
                        currentMaskIndices.add(index);
                    }
                }
            }
            Map<Integer, Character> maskMap = maskConfig.getMaskMap(currentMaskIndices);
            int variableLength = length - maskMap.size();

            // Calculate total combinations safely
            long totalCombinations = (long) Math.pow(charSet.length(), variableLength);
            if (totalCombinations == 0) continue;
            if (totalCombinations > Integer.MAX_VALUE) {
                System.out.println("Skipping length " + length + " (too many combinations)");
                continue;
            }
            int intTotal = (int) totalCombinations;

            ExecutorService executor = Executors.newFixedThreadPool(threadCount);
            int chunkSize = (int) Math.ceil((double) intTotal / threadCount);

            for (int i = 0; i < threadCount; i++) {
                // FIX: Declare startIndex and endIndex here
                int startIndex = i * chunkSize;
                int endIndex = Math.min(startIndex + chunkSize, intTotal);

                executor.submit(new BruteForceWorker(
                        charSet, length, startIndex, endIndex,
                        validator, result, attemptCounter, maskMap
                ));
            }

            executor.shutdown();
            while (!executor.isTerminated()) {
                // Wait for threads to finish
            }

            if (result.isFound()) return result.getPassword();
        }
        return null;
    }
}