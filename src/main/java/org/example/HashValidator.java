//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

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
            byte[] var5 = hashBytes;
            int var6 = hashBytes.length;

            for(int var7 = 0; var7 < var6; ++var7) {
                byte b = var5[var7];
                hashString.append(String.format("%02x", b));
            }

            return hashString.toString().equals(this.targetHash);
        } catch (NoSuchAlgorithmException var9) {
            throw new RuntimeException("MD5 is not supported on this system", var9);
        }
    }
}
