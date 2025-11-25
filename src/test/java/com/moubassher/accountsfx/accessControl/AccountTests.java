package com.moubassher.accountsfx.accessControl;

import javafx.collections.ObservableList;
import org.junit.jupiter.api.*;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("Account (Sign up/sign-in) tests")
public class AccountTests {

    private AccountDao accountDao;
    private List<Account> testAccounts;
    private List<String> testFeatures;
    AccountValidator accountValidator;
    @BeforeAll
    void setUp() {
        accountDao = AccountDao.getInstance();
        testAccounts = new ArrayList<>();
        testFeatures = new ArrayList<>(List.of("all", "delete_sales", "accept_orders"));
        accountDao.addNewFeatures(testFeatures);

        accountValidator  = new AccountValidator();
    }



    @AfterEach
    void tearDown() {
        accountDao.deleteAccounts(testAccounts);
        accountDao.deleteFeatures(testFeatures);

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


        String mediumPassword = "abc123";
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
        List<String> features = List.of("feature a", "feature b");
        accountDao.addNewFeatures(features);
        testFeatures.addAll(features);

        AccountType accountType1 = new AccountType("account type 1", features, true);
        boolean added = accountDao.addAccountTypes(List.of(accountType1));
        assertTrue(added, "Account type not added");

        boolean isAccountTypeRead = accountDao.readAccountTypes().contains(accountType1);
        assertTrue(isAccountTypeRead, "added account type not read successfully");

    }


    @Test
    @DisplayName("Account CRUD: Delete")
    void testDeletingAccounts() {
        List<String> privileges = List.of("feature X");
        accountDao.addNewFeatures(privileges);

        AccountType accountType = new AccountType("account type x", privileges, true);
        accountDao.addAccountTypes(List.of(accountType));

        String password = "addddd234$";
        String hashedPassword = accountValidator.hashPassword(password);


        Account account = new Account("Robert Adams", hashedPassword, accountType);
        boolean added = accountDao.addNewAccount(account);
        assertTrue(added, "test account not added and is required to testing deleting accounts");
        testAccounts.add(account);

        boolean deleted = accountDao.deleteAccount(account);
        assertTrue(deleted, "Account deletion failed");
    }

    @Test
    @DisplayName("Can insert an admin user")
    void testAddingAdmin() {
        Account adminAccount = createTestAdminAccount();

        List<String> privileges = adminAccount.getAccountType().getPrivileges();
        accountDao.addNewFeatures(privileges);
        testFeatures.addAll(privileges);

        boolean accountTypeAdded = accountDao.addAccountTypes(List.of(adminAccount.getAccountType()));
        assertTrue(accountTypeAdded, "Admin account type was not added and is required for inserting admin");

        boolean added = accountDao.addNewAccount(adminAccount);
        assertTrue(added, "Admin account was not added");
        testAccounts.add(adminAccount);
    }

    @Test
    @DisplayName("Can read accounts")
    void testReadingUsers() {
        Account account = createTestAdminAccount();

        List<String> privileges = account.getAccountType().getPrivileges();
        accountDao.addNewFeatures(privileges);

        AccountType accountType = account.getAccountType();
        boolean accountTypeAdded = accountDao.addAccountTypes(List.of(accountType));
        assertTrue(accountTypeAdded, "Account type not added and is required for test reading accounts");


        boolean created = accountDao.addNewAccount(account);
        if(created) {
            ObservableList<Account> accounts = AccountDao.getInstance().readAccounts();
            assertNotNull(accounts, "Users is null");
            assertFalse(accounts.isEmpty());
        } else {
            throw new RuntimeException("Account could not have been added and is a requirement for this test");

        }
    }

    private Account createTestNonAdminAccount() {

        String password = "user123";
        String hashedPassword = accountValidator.hashPassword(password);
        List<String> privileges = List.of("sales", "orders");
        AccountType accountType = new AccountType("regular accountType", privileges, false);
        Account account = new Account("Robert C. Martin", hashedPassword, accountType);
        return account;
    }

    private Account createTestAdminAccount() {
        String password = "user123$";
        String hashedPassword = accountValidator.hashPassword(password);
        List<String> privileges = List.of("all");
        AccountType adminAccountType =  new AccountType("Admin", privileges, true);
        Account admin = new Account("David Adam", hashedPassword, adminAccountType);

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

        List<String> privileges = List.of("sales", "storage");

        AccountType accountType = new AccountType("مبيعات", privileges, false);
        boolean accountTypeAdded = accountDao.addAccountTypes(List.of(accountType));
        assertTrue(accountTypeAdded, "Account type not added, and is required for this test");

        int salary = 300_000;

        LocalTime workStart = LocalTime.of(8, 0);
        LocalTime workFinish = workStart.plusHours(8);
        int userId = 1234567;
        Account account = new Account(userId, "Mohammed Ahmed", hashedPassword,
                accountType, salary,  workStart, workFinish);
        assertThrows(UnsupportedOperationException.class,
                () -> AccountDao.getInstance().addNewAccount(account));

        testAccounts.add(account);
    }

    @Test
    @DisplayName("Cannot add a nameless employee")
    void testAddingNamelessEmployee() {
        Account account = new Account();
        assertThrows(IllegalArgumentException.class,
                () -> AccountDao.getInstance().addNewAccount(account));

        testAccounts.add(account);

    }


    @Test
    @DisplayName("Cannot create an employee with illogical work hours")
    void testCreatingEmployeeWithIllogicalWorkhours() {
        String password = "abc123";
        String hashedPassword = accountValidator.hashPassword(password);
        List<String> privileges = List.of("sales");
        AccountType userType = new AccountType("مبيعات", privileges, false);
        int salary = 300_000;

        LocalTime illogicalWorkstart = LocalTime.of(8, 0);
        LocalTime illogicalWorkfinish = illogicalWorkstart.minusHours(8);
        int userId = 1234557543;

        assertThrows(IllegalArgumentException.class, () ->
                new Account(userId, "Ahmed Adam", hashedPassword,
                userType, salary,  illogicalWorkstart, illogicalWorkfinish));
    }


    //TODO: implement creating an employee account with a manager one present.
  /*  @Test
    @DisplayName("Can create an employee account after creating a manager one")
    void testCreatingEmployeeAfterManager() {
    }*/

}
