package org.example;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.TimeUnit;

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
        AtomicLong attemptCounter = new AtomicLong();

        for (int length = 1; length <= maxLength; length++) {
            // Mask indices valid for this length
            Set<Integer> currentMaskIndices = new HashSet<>();
            for (int index : maskConfig.maskIndices) {
                if (index < length) currentMaskIndices.add(index);
            }

            Map<Integer, Character> maskMap = maskConfig.getMaskMap(currentMaskIndices);
            int variableLength = length - maskMap.size();
            if (variableLength < 0) {
                // mask fixes more characters than the length -> skip
                continue;
            }

            long totalCombinations = powLong(charSet.length(), variableLength);
            if (totalCombinations <= 0) {
                System.out.println("Skipping length " + length + " (invalid combination count)");
                continue;
            }

            long chunkSize = (totalCombinations + threadCount - 1) / threadCount;
            ExecutorService executor = Executors.newFixedThreadPool(threadCount);

            for (int i = 0; i < threadCount; i++) {
                long start = i * chunkSize;
                long end = Math.min(start + chunkSize, totalCombinations);
                if (start >= end) break;

                executor.submit(new BruteForceWorker(
                        charSet, length, start, end,
                        validator, result, attemptCounter, maskMap
                ));
            }

            executor.shutdown();
            try {
                executor.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            if (result.isFound()) return result.getPassword();
        }
        return null;
    }

    private static long powLong(int base, int exp) {
        long res = 1L;
        for (int i = 0; i < exp; i++) {
            if (res > Long.MAX_VALUE / base) {
                return -1; // overflow
            }
            res *= base;
        }
        return res;
    }
}
