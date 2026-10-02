package com.usermanagement.util;

import org.mindrot.jbcrypt.BCrypt;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Utility for hashing and verifying passwords using BCrypt.
 * Protects against rainbow tables and brute force attacks with adaptive work factors.
 */
public class PasswordUtil {

    private static final int BCRYPT_LOG_ROUNDS = 10;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    /**
     * Hash a plaintext password using BCrypt.
     *
     * @param plainPassword Plaintext password
     * @return BCrypt hashed string containing algorithm, cost, salt, and hash
     */
    public static String hashPassword(String plainPassword) {
        if (plainPassword == null || plainPassword.isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or empty");
        }
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(BCRYPT_LOG_ROUNDS));
    }

    /**
     * Verify a plaintext password against a stored BCrypt hash.
     *
     * @param plainPassword Plaintext password candidate
     * @param storedHash    Hashed password from the database
     * @return true if password matches, false otherwise
     */
    public static boolean verifyPassword(String plainPassword, String storedHash) {
        if (plainPassword == null || storedHash == null || storedHash.isEmpty()) {
            return false;
        }
        try {
            return BCrypt.checkpw(plainPassword, storedHash);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Generates a random cryptographic salt for audit/user metadata.
     *
     * @return Base64-encoded random salt string
     */
    public static String generateRandomSalt() {
        byte[] salt = new byte[16];
        SECURE_RANDOM.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    public static void main(String[] args) {
        String adminHash = hashPassword("Admin@123");
        String managerHash = hashPassword("Manager@123");
        String userHash = hashPassword("User@123");
        System.out.println("ADMIN_HASH=" + adminHash);
        System.out.println("MANAGER_HASH=" + managerHash);
        System.out.println("USER_HASH=" + userHash);
        System.out.println("ADMIN_VERIFY=" + verifyPassword("Admin@123", adminHash));
    }
}
