/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Utilities;

import org.mindrot.jbcrypt.BCrypt;

/**
 *
 * @author LENOVO
 */
public class PasswordUtil {

    private static final int WORK_FACTOR = 12;

    /**
     * Hash a password using BCrypt
     *
     * @param plainPassword The plain text password
     * @return The hashed password
     */
    public static String hashPassword(String plainPassword) {
        if (plainPassword == null || plainPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or empty");
        }

        try {
            return BCrypt.hashpw(plainPassword, BCrypt.gensalt(WORK_FACTOR));
        } catch (Exception e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }

    /**
     * Check if a password meets minimum requirements
     *
     * @param password The password to validate
     * @return true if password is valid, false otherwise
     */
    public static boolean isValidPassword(String password) {
        if (password == null || password.length() < 8) {
            return false;
        }

        // Check for at least one digit
        boolean hasDigit = password.matches(".*\\d.*");

        // Check for at least one uppercase letter
        boolean hasUppercase = password.matches(".*[A-Z].*");

        // Check for at least one lowercase letter
        boolean hasLowercase = password.matches(".*[a-z].*");

        // Check for at least one special character
        boolean hasSpecialChar = password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*");

        return hasDigit && hasUppercase && hasLowercase && hasSpecialChar;
    }

    /**
     * Generate a random password
     *
     * @param length The length of the password to generate
     * @return A random password string
     */
    public static String generateRandomPassword(int length) {
        if (length < 8) {
            length = 8;
        }

        String upperCase = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String lowerCase = "abcdefghijklmnopqrstuvwxyz";
        String digits = "0123456789";
        String special = "!@#$%^&*()_+-=[]{}|;:,.<>?";

        String allChars = upperCase + lowerCase + digits + special;

        StringBuilder password = new StringBuilder();

        // Ensure at least one character from each category
        password.append(upperCase.charAt((int) (Math.random() * upperCase.length())));
        password.append(lowerCase.charAt((int) (Math.random() * lowerCase.length())));
        password.append(digits.charAt((int) (Math.random() * digits.length())));
        password.append(special.charAt((int) (Math.random() * special.length())));

        // Fill the rest randomly
        for (int i = 4; i < length; i++) {
            password.append(allChars.charAt((int) (Math.random() * allChars.length())));
        }

        // Shuffle the password
        return shuffleString(password.toString());
    }

    /**
     * Shuffle a string randomly
     */
    private static String shuffleString(String string) {
        char[] array = string.toCharArray();
        for (int i = array.length - 1; i > 0; i--) {
            int j = (int) (Math.random() * (i + 1));
            char temp = array[i];
            array[i] = array[j];
            array[j] = temp;
        }
        return new String(array);
    }

    /**
     * Verify a plain password against a hashed password
     *
     * @param plainPassword The plain text password
     * @param hashedPassword The hashed password stored in the database
     * @return true if the password matches, false otherwise
     */
    public static boolean verifyPassword(String plainPassword, String hashedPassword) {
        if (plainPassword == null || hashedPassword == null) {
            return false;
        }

        try {
            return BCrypt.checkpw(plainPassword, hashedPassword);
        } catch (Exception e) {
            return false;
        }
    }
}
