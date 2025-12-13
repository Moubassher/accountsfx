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
    private PasswordValidator passwordValidator;

    @BeforeAll
    void setUp() {
        accountDao = new AccountDao<>(TestFeature.class);

        accountDao.deleteAllAccounts();
        accountDao.deleteAllPrivileges();
        accountDao.deleteAllAccountTypes();

        passwordValidator = new PasswordValidator();
    }


    @AfterAll
    void afterAll() {
        accountDao.deleteAllAccounts();
        accountDao.deleteAllPrivileges();
        accountDao.deleteAllAccountTypes();
    }


    @Test
    @DisplayName("Can add new account types")
    void testAddingAccountTypes() {
        List<TestFeature> features = List.of(TestFeature.FeatureB, TestFeature.FeatureA);

        AccountType<TestFeature> accountType1 = new AccountType<>("account type 1", features, true);
        boolean added = accountDao.addAccountType(accountType1);
        assertTrue(added, "Account type not added");

        boolean isAccountTypeRead = accountDao.readAccountTypes().contains(accountType1);
        assertTrue(isAccountTypeRead, "added account type not read successfully");
    }


    @Test
    @DisplayName("Can update an account type's name")
    void testUpdatingAccountTypeName() {
        List<TestFeature> privileges = List.of(TestFeature.FeatureA);
        AccountType<TestFeature> accountType = new AccountType<>("salesman", privileges, false);

        boolean added = accountDao.addAccountType(accountType);
        assertTrue(added, "Account type was not added and is required for this update test");

        String updatedName = "Senior accountant";
        AccountType<TestFeature> updatedAccountType = new AccountType<>(updatedName, privileges, false);
        int accountTypeId = accountType.getAccountTypeId();

        boolean updated = accountDao.updateAccountType(accountTypeId, updatedAccountType);
        assertTrue(updated, "Account type's name was not updated");

        String newName = updatedAccountType.getType();
        AccountType<TestFeature> readAccountType = accountDao.getAccountType(accountTypeId);
        String readName = readAccountType.getType();
        assertEquals(newName, readName, "Account type's name was not updated");

    }

    @Test
    @DisplayName("Can delete an account type")
    void testDeletingAccountType() {
        List<TestFeature> privileges = List.of(TestFeature.FeatureA);
        AccountType<TestFeature> accountType = new AccountType<>("deletable account type", privileges, false);

        boolean added = accountDao.addAccountType(accountType);
        assertTrue(added, "Account type was not added and is required for deletion test");

        int accountTypeId = accountType.getAccountTypeId();
        boolean deleted = accountDao.deleteAccountType(accountTypeId);
        assertTrue(deleted, "Account type was not deleted");

        boolean accountTypeExists = accountDao.readAccountTypes().contains(accountType);
        assertFalse(accountTypeExists, "Account type exists after it was supposedly deleted");

    }

    @Test
    @DisplayName("Can update an account type's privileges")
    void testUpdatingAccountTypePrivileges() {
        List<TestFeature> privileges = List.of(TestFeature.FeatureA);
        AccountType<TestFeature> accountType = new AccountType<>("accountant", privileges, false);

        boolean added = accountDao.addAccountType(accountType);
        assertTrue(added, "Account type was not added and is required for this update test");

        List<TestFeature> updatedPrivileges = List.of(TestFeature.FeatureB);
        accountType.setPrivileges(updatedPrivileges);

        int accountTypeId = accountType.getAccountTypeId();
        boolean updated = accountDao.updateAccountType(accountTypeId, accountType);
        assertTrue(updated, "Account type's privileges were not updated");

        AccountType<TestFeature> readAccountType = accountDao.getAccountType(accountTypeId);
        privileges = readAccountType.getPrivileges();

        assertEquals(updatedPrivileges, privileges, "Account type's privileges were not updated");

    }

    @Test
    @DisplayName("Account CRUD: Delete")
    void testDeletingAccounts() {
        List<TestFeature> privileges = List.of(TestFeature.FeatureA);

        AccountType<TestFeature> accountType = new AccountType<>("account type x", privileges, true);
        accountDao.addAccountType(accountType);

        String password = "addddd234$";
        String hashedPassword = passwordValidator.hashPassword(password);

        Account<TestFeature> account = new Account<>("Robert Adams", hashedPassword, accountType);
        boolean added = accountDao.addNewAccount(account);
        assertTrue(added, "test account not added and is required to testing deleting accounts");


        boolean deleted = accountDao.deleteAccount(account);
        assertTrue(deleted, "Account deletion failed");
    }

    @Test
    @DisplayName("Can insert an admin user")
    void testAddingAdmin() {
        Account<TestFeature> adminAccount = createTestAdminAccount("Admin");

        boolean accountTypeAdded = accountDao.addAccountType(adminAccount.getAccountType());
        assertTrue(accountTypeAdded, "Admin account type was not added and is required for inserting admin");

        boolean added = accountDao.addNewAccount(adminAccount);
        assertTrue(added, "Admin account was not added");

    }

    @Test
    @DisplayName("Can read accounts")
    void testReadingAccounts() {
        Account<TestFeature> account = createTestAdminAccount("admin x");

        List<TestFeature> privileges = account.getAccountType().getPrivileges();

        AccountType accountType = account.getAccountType();
        boolean accountTypeAdded = accountDao.addAccountType(accountType);
        assertTrue(accountTypeAdded, "Account type not added and is required for test reading accounts");


        boolean created = accountDao.addNewAccount(account);
        if(created) {
    
            ObservableList<Account<TestFeature>> accounts = accountDao.readAccounts();
            assertNotNull(accounts, "Users is null");
            assertFalse(accounts.isEmpty());
        } else {
            throw new RuntimeException("Account could not have been added and is a requirement for this test");

        }
    }

    private Account createTestNonAdminAccount() {

        String password = "user123";
        String hashedPassword = passwordValidator.hashPassword(password);
        List<TestFeature> privileges = List.of(TestFeature.FeatureB, TestFeature.FeatureA);
        AccountType accountType = new AccountType("regular accountType", privileges, false);
        Account<TestFeature> account = new Account("Robert C. Martin", hashedPassword, accountType);
        return account;
    }

    private Account createTestAdminAccount(String name) {
        String password = "user123$";
        String hashedPassword = passwordValidator.hashPassword(password);
        List<TestFeature> privileges = List.of(TestFeature.All);
        AccountType<TestFeature> adminAccountType =   new AccountType(name, privileges, true);
        Account<TestFeature> admin = new Account<>("David Adam", hashedPassword, adminAccountType);

        return admin;
    }

    @Test
    @DisplayName("Cannot create a non-admin account if no admin account exists")
    void testCreatingNonAdminAccountWithoutAdmin() {

        //clear previous users (esp admins) for the test:
        accountDao.deleteAllAccounts();

        boolean isAccountsRemoved = accountDao.readAccounts().isEmpty();
        assertTrue(isAccountsRemoved, "Accounts were not deleted and this is required for the test");

        String password = "abc123";
        String hashedPassword = passwordValidator.hashPassword(password);

        List<TestFeature> privileges = List.of(TestFeature.FeatureA);

        AccountType accountType =  new AccountType("مبيعات", privileges, false);
        boolean accountTypeAdded = accountDao.addAccountType(accountType);
        assertTrue(accountTypeAdded, "Account type not added, and is required for this test");

        int salary = 300_000;

        LocalTime workStart = LocalTime.of(8, 0);
        LocalTime workFinish = workStart.plusHours(8);
        int userId = 1234567;
        Account<TestFeature> account = new Account<>(userId, "Mohammed Ahmed", hashedPassword,
                accountType, salary,  workStart, workFinish);
        assertThrows(UnsupportedOperationException.class,
                () -> accountDao.addNewAccount(account));


    }

    @Test
    @DisplayName("Cannot add a nameless account")
    void testAddingNamelessAccount() {
        Account<TestFeature> account = new Account<>();
        assertThrows(IllegalArgumentException.class,
                () -> accountDao.addNewAccount(account));


    }

    @Test
    @DisplayName("Cannot add a nameless account type")
    void testAddingNamelessAccountType() {
        List<TestFeature> privileges = List.of();
        AccountType<TestFeature> namelessAccountType = new AccountType<>(null, privileges, false);

        assertThrows(IllegalArgumentException.class,
                () -> accountDao.addAccountType(namelessAccountType));
    }

    @Test
    @DisplayName("Cannot add an account type with no privileges at all")
    void testAddingAccountTypeWithNoPrivileges() {
        List<TestFeature> privileges = List.of();
        AccountType<TestFeature> unprivileged = new AccountType<>("poor account type", privileges, true);

        assertThrows(IllegalArgumentException.class,
                () -> accountDao.addAccountType(unprivileged));
    }

    @Test
    @DisplayName("Cannot add a duplicate account type")
    void testAddingDuplicateAccountType() {
        List<TestFeature> privileges = List.of(TestFeature.All);
        AccountType<TestFeature> accountType = new AccountType<>("Some name", privileges, false);
        boolean added = accountDao.addAccountType(accountType);
        assertTrue(added, "Account type not added and is required for the test");

        added  = accountDao.addAccountType(accountType);
        assertFalse(added, "duplicate account type was added when it shouldn't have");

    }


    @Test
    @DisplayName("Can create a non-admin account after an admin one")
    void testAddingNonAdminAccount() {
        // Create a non-admin account type

        accountDao.deleteAllAccounts();

        List<TestFeature> employeePrivileges = Arrays.asList(TestFeature.FeatureA, TestFeature.FeatureB);

        AccountType employeeType = new AccountType(
                "Employee",
                employeePrivileges,
                false   // isAdmin = false
        );
        accountDao.addAccountType(employeeType);

// Create a non-admin account (using the short constructor)
        Account nonAdminAccount = new Account("John Doe", "hashedPassword123", employeeType);

        assertThrows(UnsupportedOperationException.class,
                () -> accountDao.addNewAccount(nonAdminAccount));

        List<TestFeature> privileges = List.of(TestFeature.All);
        AccountType adminType = new AccountType("Admin Y", privileges, true);
        boolean adminTypeAdded = accountDao.addAccountType(adminType);
        assertTrue(adminTypeAdded, "Admin type not added");

        Account adminAccount = new Account("David Adam", "hashedPassword123", adminType);
        boolean adminAdded = accountDao.addNewAccount(adminAccount);
        assertTrue(adminAdded, "Admin account not added");
        

        //now that an admin has been added, a non-admin account can be added:
        boolean nonAdminAdded = accountDao.addNewAccount(nonAdminAccount);
        assertTrue(nonAdminAdded, "Non-admin not added even after an admin has been added");

    }


    @Test
    @DisplayName("Cannot create an employee with illogical work hours")
    void testCreatingEmployeeWithIllogicalWorkhours() {
        String password = "abc123";
        String hashedPassword = passwordValidator.hashPassword(password);
        List<TestFeature> privileges = List.of(TestFeature.FeatureB);
        AccountType<TestFeature> userType = new AccountType<>("مبيعات", privileges, false);
        int salary = 300_000;

        LocalTime illogicalWorkstart = LocalTime.of(8, 0);
        LocalTime illogicalWorkfinish = illogicalWorkstart.minusHours(8);
        int userId = 1234557543;

        assertThrows(IllegalArgumentException.class, () ->
                new Account(userId, "Smith Peterson", hashedPassword,
                        userType, salary,  illogicalWorkstart, illogicalWorkfinish));
    }

    @Test
    @DisplayName("Can register a login timestamp")
    void testRegisteringLogin() {
        String password = "user123";
        String hashedPassword = passwordValidator.hashPassword(password);
        List<TestFeature> privileges = List.of(TestFeature.FeatureB, TestFeature.FeatureA);
        AccountType accountType = new AccountType("User 154", privileges, true);
        boolean accountTypeAdded = accountDao.addAccountType(accountType);

        if(accountTypeAdded) {
            Account<TestFeature> account = new Account("Early bird", hashedPassword, accountType);

            //attempt to log in before an account is created
            boolean isUserLoggedIn = accountDao.loginAccount(account);
            assertFalse(isUserLoggedIn, "An invalid user was able to log in");

            //now create the account:
            boolean created = accountDao.addNewAccount(account);
            assertTrue(created, "Account was not created and so login test will fail");

            isUserLoggedIn = accountDao.loginAccount(account);
            assertTrue(isUserLoggedIn, "A valid user was not able to log in");
        }
    }

}
