package util;

import model.User;
import java.util.Arrays;
import model.Client;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class for validating user input.
 */
public class UserInputValidator {

    /**
     * Validates an email format.
     *
     * @param email The email to validate.
     * @return true if the email is valid; false otherwise.
     */
    public static boolean isValidEmail(String email) {
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        Pattern pattern = Pattern.compile(emailRegex);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    /**
     * Validates a phone number format.
     * The phone number must start with '0' and contain exactly 10 digits.
     *
     * @param phone The phone number to validate.
     * @return true if the phone number is valid; false otherwise.
     */
    public static boolean isValidPhoneNumber(String phone) {
        String phoneRegex = "^0\\d{9}$"; // Starts with '0' and followed by 9 digits (total 10 digits)
        Pattern pattern = Pattern.compile(phoneRegex);
        Matcher matcher = pattern.matcher(phone);
        return matcher.matches();
    }

    /**
     * Validates an address format.
     * The address must have exactly 4 parts separated by commas (e.g., "street, app, city, country").
     *
     * @param address The address to validate.
     * @return true if the address is valid; false otherwise.
     */
    public static boolean isValidAddress(String address) {
        if (address == null || address.isBlank()) {
            return false;
        }

        String[] parts = address.split("\\s*,\\s*"); // Split by commas and trim whitespace
        if (parts.length != 4) {
            System.out.println("Invalid address: " + address + " (Parts: " + Arrays.toString(parts) + ")");
            return false;
        }

        for (String part : parts) {
            if (part.isBlank()) {
                System.out.println("Empty part in address: " + Arrays.toString(parts));
                return false;
            }
        }

        return true;
    }


    /**
     * Validates a username based on specific rules:
     * - Must contain two words separated by a single space.
     * - Each word must be non-empty and contain valid characters.
     *
     * @param username The username to validate.
     * @return true if the username is valid; false otherwise.
     */
    public static boolean isValidUsername(String username) {
        if (username == null || username.isBlank()) {
            return false;
        }

        String[] parts = username.trim().split("\\s+");
        if (parts.length != 2) {
            return false;
        }

        for (String part : parts) {
            if (part.isEmpty() || !part.matches("[a-zA-Z]+")) {
                return false;
            }
        }
        return true;
    }

    /**
     * Validates a password.
     * Must be at least 8 characters long and contain both letters and numbers.
     *
     * @param password The password to validate.
     * @return true if the password is valid; false otherwise.
     */
    public static boolean isValidPassword(String password) {
        return password != null && password.length() >= 8 && password.matches(".*[A-Za-z].*") && password.matches(".*\\d.*");
    }

    /**
     * Validates user input based on role and field-specific rules.
     *
     * @param user The user to validate.
     * @throws IllegalArgumentException if any validation rule is violated.
     */
    public static void validateUserInput(User user) {
        if (!isValidUsername(user.getUsername())) {
            throw new IllegalArgumentException("Invalid username. It must consist of a first and last name separated by a space.");
        }

        if (user instanceof Client client) {
           
            if (!isValidAddress(client.getAddress())) {
                throw new IllegalArgumentException("Invalid address. Ensure it follows the format: street, house or apartment number, city, country (separated by commas).");
            }

            if (!isValidPhoneNumber(client.getPhone())) {
                throw new IllegalArgumentException("Invalid phone number. It must start with '0' and contain exactly 10 digits.");
            }
        }

        if (!isValidPassword(user.getPassword())) {
            throw new IllegalArgumentException("Password must be at least 8 characters long and contain both letters and numbers.");
        }
    }
}
