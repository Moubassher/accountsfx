package com.moubassher.accountsfx.accessControl;

import at.favre.lib.crypto.bcrypt.BCrypt;

public class AccountValidator {


    /**
     *  evaluate a password's strength based on the following criteria:
     *  1. Password contains letters only = weak
     *  2. Password contains letters + digits + medium
     *  3. Password contains letters + digits + special characters = strong.
     * @param password
     * @return
     */
    PasswordStrength evaluateStrength(String password) {
        if (password == null || password.isEmpty()) {
            return null;
        }

        boolean hasLetter = password.matches(".*[a-zA-Z\\u0600-\\u06FF].*");
        boolean hasDigit = password.matches(".*\\d.*");
        boolean hasSpecial = password.matches(".*[^a-zA-Z\\u0600-\\u06FF\\d].*");

        if (hasLetter && !hasDigit && !hasSpecial) {
            return PasswordStrength.WEAK;
        } else if (hasLetter && hasDigit && !hasSpecial) {
            return PasswordStrength.MEDIUM;
        } else if (hasLetter && hasDigit && hasSpecial) {
            return PasswordStrength.STRONG;
        } else {
            // No letters at all (only numbers or symbols) → treat as weak
            return PasswordStrength.WEAK;
        }
    }


    boolean validatePassword(String password, String confirmedPassword) {
        if(password.isBlank() || confirmedPassword.isBlank()) {
            return false;
        }
        return isPasswordsMatch(password, confirmedPassword);
    }

    private boolean isPasswordsMatch(String password, String confirmedPassword) {
        return password.equals(confirmedPassword);
    }


    String hashPassword(String password) {
        return BCrypt.withDefaults()
                .hashToString(14, password.toCharArray());
    }



}
