package com.moubassher.accountsfx;

import javafx.collections.ObservableList;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("Account (Sign up/sign-in) tests")
public class AccountTests {

    private AccountDao<TestFeature> accountDao;
    private List<AccountType<TestFeature>> testAccountTypes;
    private List<Account<TestFeature>> testAccounts;
    AccountValidator accountValidator;
    @BeforeAll
    void setUp() {
        accountDao = new AccountDao<>(TestFeature.class);
        testAccountTypes = new ArrayList<>();
        testAccounts = new ArrayList<>();

        accountValidator  = new AccountValidator();
    }



    @AfterAll
    void afterAll() {
        accountDao.deleteAccounts(testAccounts);
        accountDao.deleteAccountTypes(testAccountTypes);
    }



    @Test
    @DisplayName("Can validate a password")
    void testValidatingPassword() {
        String emptyPassword = "";
        String emptyConfirmedPassword = "";
        boolean isValid = accountValidator.validatePassword(emptyPassword, emptyConfirmedPassword);
        assertFalse(isValid, "password validation failed - password/confirmed password is empty. Shouldn't be valid");

        String password = "something";
        String confirmedPassword = "something else";
        boolean isValid2 = accountValidator.validatePassword(password, confirmedPassword);
        assertFalse(isValid2, "password validation failed - even though confirmed password was different from password");

        String actualPassword = "something";
        String validConfirmedPassword = "something";
        boolean actuallyValid = accountValidator.validatePassword(actualPassword, validConfirmedPassword);

        assertTrue(actuallyValid, "password was not validated correctly");

    }

    @Test
    @DisplayName("Can check password's strength")
    void testPasswordStrength( ) {
        String weakPassword = "abc";
        PasswordStrength weak = accountValidator.evaluateStrength(weakPassword);
        assertEquals(PasswordStrength.WEAK, weak, "A weak password was not determined as weak");

        String weakPassword2 = "1b";
        PasswordStrength weak2 = accountValidator.evaluateStrength(weakPassword2);
        assertEquals(PasswordStrength.WEAK, weak2, "A weak password (2) was not determined as weak");

        String mediumPassword = "abc1";
        PasswordStrength medium = accountValidator.evaluateStrength(mediumPassword);
        assertEquals(medium, PasswordStrength.MEDIUM, "A Medium-strength password was not determined as such.");

        String strongPassword = "ابت123#";
        PasswordStrength strong = accountValidator.evaluateStrength(strongPassword);
        assertEquals(strong, PasswordStrength.STRONG, "A strong password was not determined as such");

    }


    @Test
    @DisplayName("Can hash passwords")
    void testHashingPassword() {
        String password = "user123";
        String hashedPassword = accountValidator.hashPassword(password);

        assertNotEquals(password, hashedPassword, "password was not hashed");
    }

    @Test
    @DisplayName("Can add new account types")
    void testAddingAccountTypes() {
        List<TestFeature> features = List.of(TestFeature.FeatureB, TestFeature.FeatureA);

        AccountType<TestFeature> accountType1 = AccountType.of("account type 1", features, true);
        boolean added = accountDao.addAccountTypes(List.of(accountType1));
        assertTrue(added, "Account type not added");

        boolean isAccountTypeRead = accountDao.readAccountTypes().contains(accountType1);
        assertTrue(isAccountTypeRead, "added account type not read successfully");

    }

    @Test
    @DisplayName("Account CRUD: Delete")
    void testDeletingAccounts() {
        List<TestFeature> privileges = List.of(TestFeature.FeatureA);

        AccountType accountType = AccountType.of("account type x", privileges, true);
        accountDao.addAccountTypes(List.of(accountType));

        String password = "addddd234$";
        String hashedPassword = accountValidator.hashPassword(password);

        Account<TestFeature> account = new Account<>("Robert Adams", hashedPassword, accountType);
        boolean added = accountDao.addNewAccount(account);
        assertTrue(added, "test account not added and is required to testing deleting accounts");
        testAccounts.add(account);

        boolean deleted = accountDao.deleteAccount(account);
        assertTrue(deleted, "Account deletion failed");
    }

    @Test
    @DisplayName("Can insert an admin user")
    void testAddingAdmin() {
        Account<TestFeature> adminAccount = createTestAdminAccount();

        boolean accountTypeAdded = accountDao.addAccountTypes(List.of(adminAccount.getAccountType()));
        assertTrue(accountTypeAdded, "Admin account type was not added and is required for inserting admin");

        boolean added = accountDao.addNewAccount(adminAccount);
        assertTrue(added, "Admin account was not added");
        testAccounts.add(adminAccount);
    }

    @Test
    @DisplayName("Can read accounts")
    void testReadingAccounts() {
        Account<TestFeature> account = createTestAdminAccount();

        List<TestFeature> privileges = account.getAccountType().getPrivileges();

        AccountType accountType = account.getAccountType();
        boolean accountTypeAdded = accountDao.addAccountTypes(List.of(accountType));
        assertTrue(accountTypeAdded, "Account type not added and is required for test reading accounts");


        boolean created = accountDao.addNewAccount(account);
        if(created) {
            testAccounts.add(account);
            ObservableList<Account<TestFeature>> accounts = accountDao.readAccounts();
            assertNotNull(accounts, "Users is null");
            assertFalse(accounts.isEmpty());
        } else {
            throw new RuntimeException("Account could not have been added and is a requirement for this test");

        }
    }

    private Account createTestNonAdminAccount() {

        String password = "user123";
        String hashedPassword = accountValidator.hashPassword(password);
        List<TestFeature> privileges = List.of(TestFeature.FeatureB, TestFeature.FeatureA);
        AccountType accountType = AccountType.of("regular accountType", privileges, false);
        Account<TestFeature> account = new Account("Robert C. Martin", hashedPassword, accountType);
        return account;
    }

    private Account createTestAdminAccount() {
        String password = "user123$";
        String hashedPassword = accountValidator.hashPassword(password);
        List<TestFeature> privileges = List.of(TestFeature.All);
        AccountType<TestFeature> adminAccountType =   AccountType.of("Admin", privileges, true);
        Account<TestFeature> admin = new Account<>("David Adam", hashedPassword, adminAccountType);

        return admin;
    }

    @Test
    @DisplayName("Cannot create a non-admin account if no admin account exists")
    void testCreatingNonAdminAccountWithoutAdmin() {

        //clear previous users (esp admins) for the test:
        accountDao.deleteAccounts(testAccounts);
        boolean isAccountsRemoved = accountDao.readAccounts().isEmpty();
        assertTrue(isAccountsRemoved, "Accounts were not deleted and this is required for the test");

        String password = "abc123";
        String hashedPassword = accountValidator.hashPassword(password);

        List<TestFeature> privileges = List.of(TestFeature.FeatureA);

        AccountType accountType =  AccountType.of("مبيعات", privileges, false);
        boolean accountTypeAdded = accountDao.addAccountTypes(List.of(accountType));
        assertTrue(accountTypeAdded, "Account type not added, and is required for this test");

        int salary = 300_000;

        LocalTime workStart = LocalTime.of(8, 0);
        LocalTime workFinish = workStart.plusHours(8);
        int userId = 1234567;
        Account<TestFeature> account = new Account<>(userId, "Mohammed Ahmed", hashedPassword,
                accountType, salary,  workStart, workFinish);
        assertThrows(UnsupportedOperationException.class,
                () -> accountDao.addNewAccount(account));

        testAccounts.add(account);
    }

    @Test
    @DisplayName("Cannot add a nameless employee")
    void testAddingNamelessEmployee() {
        Account<TestFeature> account = new Account<>();
        assertThrows(IllegalArgumentException.class,
                () -> accountDao.addNewAccount(account));

        testAccounts.add(account);

    }

    @Test
    @DisplayName("Can create a non-admin account after an admin one")
    void testAddingNonAdminAccount() {
        // Create a non-admin account type

        accountDao.deleteAccounts(testAccounts);
        int accountCount = accountDao.readAccounts().size();
        System.out.println("account count at testing non-admin after admin " + accountCount);

        List<TestFeature> employeePrivileges = Arrays.asList(TestFeature.FeatureA, TestFeature.FeatureB);

        AccountType employeeType = AccountType.of(
                "Employee",
                employeePrivileges,
                false   // isAdmin = false
        );
        accountDao.addAccountType(employeeType);
        testAccountTypes.add(employeeType);

// Create a non-admin account (using the short constructor)
        Account nonAdminAccount = new Account("John Doe", "hashedPassword123", employeeType);

        testAccounts.add(nonAdminAccount);
        assertThrows(UnsupportedOperationException.class,
                () -> accountDao.addNewAccount(nonAdminAccount));

        List<TestFeature> privileges = List.of(TestFeature.All);
        AccountType adminType = AccountType.of("Admin", privileges, true);
        boolean adminTypeAdded = accountDao.addAccountType(adminType);
        assertTrue(adminTypeAdded, "Admin type not added");

        Account adminAccount = new Account("David Adam", "hashedPassword123", adminType);
        boolean adminAdded = accountDao.addNewAccount(adminAccount);
        assertTrue(adminAdded, "Admin account not added");
        testAccounts.add(adminAccount);

        //now that an admin has been added, a non-admin account can be added:
        boolean nonAdminAdded = accountDao.addNewAccount(nonAdminAccount);
        assertTrue(nonAdminAdded, "Non-admin not added even after an admin has been added");

    }


    @Test
    @DisplayName("Cannot create an employee with illogical work hours")
    void testCreatingEmployeeWithIllogicalWorkhours() {
        String password = "abc123";
        String hashedPassword = accountValidator.hashPassword(password);
        List<TestFeature> privileges = List.of(TestFeature.FeatureB);
        AccountType userType = AccountType.of("مبيعات", privileges, false);
        int salary = 300_000;

        LocalTime illogicalWorkstart = LocalTime.of(8, 0);
        LocalTime illogicalWorkfinish = illogicalWorkstart.minusHours(8);
        int userId = 1234557543;

        assertThrows(IllegalArgumentException.class, () ->
                new Account(userId, "Ahmed Adam", hashedPassword,
                        userType, salary,  illogicalWorkstart, illogicalWorkfinish));
    }




}
