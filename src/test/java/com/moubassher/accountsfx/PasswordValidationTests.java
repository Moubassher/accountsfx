package com.moubassher.accountsfx;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("Password validation tests")
public class PasswordValidationTests {


    PasswordValidator passwordValidator;
    @BeforeAll
    void setUp() {
        passwordValidator = new PasswordValidator();
    }
        @Test
    @DisplayName("Can validate a password")
    void testValidatingPassword() {
        String emptyPassword = "";
        String emptyConfirmedPassword = "";
        boolean isValid = passwordValidator.validatePassword(emptyPassword, emptyConfirmedPassword);
        assertFalse(isValid, "password validation failed - password/confirmed password is empty. Shouldn't be valid");

        String password = "something";
        String confirmedPassword = "something else";
        boolean isValid2 = passwordValidator.validatePassword(password, confirmedPassword);
        assertFalse(isValid2, "password is erroneously considered valid - even though confirmed password was different from password");

        String actualPassword = "something";
        String validConfirmedPassword = "something";
        boolean actuallyValid = passwordValidator.validatePassword(actualPassword, validConfirmedPassword);

        assertTrue(actuallyValid, "password was not validated correctly");

    }

    @Test
    @DisplayName("Can check password's strength")
    void testPasswordStrength( ) {
        String weakPassword = "abc";
        PasswordStrength weak = passwordValidator.evaluateStrength(weakPassword);
        assertEquals(PasswordStrength.WEAK, weak, "A weak password was not determined as weak");

        String weakPassword2 = "1b";
        PasswordStrength weak2 = passwordValidator.evaluateStrength(weakPassword2);
        assertEquals(PasswordStrength.WEAK, weak2, "A weak password (2) was not determined as weak");

        String mediumPassword = "abc1";
        PasswordStrength medium = passwordValidator.evaluateStrength(mediumPassword);
        assertEquals(PasswordStrength.MEDIUM, medium, "A Medium-strength password was not determined as such.");

        String strongPassword = "ابت123#";
        PasswordStrength strong = passwordValidator.evaluateStrength(strongPassword);
        assertEquals(PasswordStrength.STRONG, strong, "A strong password was not determined as such");

    }


    @Test
    @DisplayName("Can hash passwords")
    void testHashingPassword() {
        String password = "user123";
        String hashedPassword = passwordValidator.hashPassword(password);

        assertNotEquals(password, hashedPassword, "password was not hashed");
    }


}
