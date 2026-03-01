package com.moubassher.accountsfx;

import at.favre.lib.crypto.bcrypt.BCrypt;

public class PasswordValidator {


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

        int length = password.length();
        boolean hasLetter = password.matches(".*[a-zA-Z\\u0600-\\u06FF].*");
        boolean hasDigit = password.matches(".*\\d.*");
        boolean hasSpecial = password.matches(".*[^a-zA-Z\\u0600-\\u06FF\\d].*");

        // very short → weak
        if (length <= 2) {
            return PasswordStrength.WEAK;
        }

        // strong → length 6+ AND letters + digits + special
        if (length >= 6 && hasLetter && hasDigit && hasSpecial) {
            return PasswordStrength.STRONG;
        }

        // medium → length 4-5 AND contains at least 2 types (letters/digits/special)
        int types = 0;
        if (hasLetter) types++;
        if (hasDigit) types++;
        if (hasSpecial) types++;
        
        if (length >= 4 && types >= 2) {
            return PasswordStrength.MEDIUM;
        }


        // fallback: anything else is weak
        return PasswordStrength.WEAK;
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
