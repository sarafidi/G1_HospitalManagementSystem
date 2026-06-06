package util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class HashUtil {
    public static String sha256(String input) {
        try {
            // create a MessageDigest instance for SHA-256
            MessageDigest md = MessageDigest.getInstance("SHA-256");

            // perform hash computation
            byte[] encodedHash = md.digest(input.getBytes());

            //convert byte array into hexadecimal string
            StringBuilder hexString = new StringBuilder();
            for (byte b : encodedHash) {
                hexString.append(String.format("%02x", b));
            }
            return hexString.toString();
            
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }
    
    public static boolean verify(String rawInput, String storedHash) {
        return sha256(rawInput).equals(storedHash);
    }
}