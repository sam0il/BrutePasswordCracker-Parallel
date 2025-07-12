package org.example;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class HashValidator {
    private final String targetHash;

    public HashValidator(String targetHash) {
        this.targetHash = targetHash;
    }

    public boolean validate(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            byte[] hashBytes = digest.digest(input.getBytes());

            StringBuilder hashString = new StringBuilder();
            for (byte b : hashBytes) {
                hashString.append(String.format("%02x", b));
            }

            return hashString.toString().equals(this.targetHash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5 is not supported", e);
        }
    }
}
