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
    AccountValidator accountValidator;
    @BeforeAll
    void setUp() {
        accountDao = AccountDao.getInstance();
        testAccounts = new ArrayList<>();
        accountValidator  = new AccountValidator();
    }

    @AfterEach
    void tearDown() {
        testAccounts.forEach(account -> accountDao.deleteAccount(account));
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
    @DisplayName("Can read users")
    void testReadingUsers() {
        String password = "user123";
        String hashedPassword = accountValidator.hashPassword(password);
        Account account = new Account("Robert C. Martin", hashedPassword, "account type");
        boolean created = accountDao.addNewAccount(account);
        if(created) {
            ObservableList<Account> accounts = AccountDao.getInstance().readUsers();
            assertNotNull(accounts, "Users is null");
            assertFalse(accounts.isEmpty());
        } else {
            throw new RuntimeException("Account could not have been added and is a requirement for this test");

        }
    }

    @Test
    @DisplayName("Cannot create a non-admin account if no admin account exists")
    void testCreatingNonAdminAccountWithoutAdmin() {
        String password = "abc123";
        String hashedPassword = accountValidator.hashPassword(password);
        String userType = "مبيعات";
        int salary = 300_000;


        LocalTime workStart = LocalTime.of(8, 0);
        LocalTime workFinish = workStart.plusHours(8);
        int userId = 1234567;
        Account account = new Account(userId, "Mohammed Ahmed", hashedPassword,
                userType, salary,  workStart, workFinish);
        assertThrows(UnsupportedOperationException.class,
                () -> AccountDao.getInstance().addNewAccount(account));
    }

    @Test
    @DisplayName("Cannot add a nameless employee")
    void testAddingNamelessEmployee() {
        Account account = new Account();
        assertThrows(IllegalArgumentException.class,
                () -> AccountDao.getInstance().addNewAccount(account));

    }



    @Test
    @DisplayName("Cannot create an employee with illogical work hours")
    void testCreatingEmployeeWithIllogicalWorkhours() {
        String password = "abc123";
        String hashedPassword = accountValidator.hashPassword(password);
        String userType = "مبيعات";
        int salary = 300_000;

        LocalTime illogicalWorkstart = LocalTime.of(8, 0);
        LocalTime illogicalWorkfinish = illogicalWorkstart.minusHours(8);
        int userId = 1234557543;

        assertThrows(IllegalArgumentException.class, () -> new Account(userId, "Ahmed Adam", hashedPassword,
                userType, salary,  illogicalWorkstart, illogicalWorkfinish));
    }


    //TODO: implement creating an employee account with a manager one present.
  /*  @Test
    @DisplayName("Can create an employee account after creating a manager one")
    void testCreatingEmployeeAfterManager() {
    }*/

    private Account createTestEmployee() {
        String password = "abc123";
        String hashedPassword = accountValidator.hashPassword(password);
        String userType = "مبيعات";
        int salary = 300_000;

        LocalTime workStart = LocalTime.of(8, 0);
        LocalTime workFinish = workStart.plusHours(8);
        int userId = 1234567;
        Account account = new Account(userId, "Mohammed Ahmed", hashedPassword,
                userType, salary,  workStart, workFinish);
        return account;
    }

    //TODO: implement adding a manager account:
/*
    @Test
    @DisplayName("Can create a manager account")
    void testCreatingManagerAccount() {
        String userName;
        String password;
        String userType;

    }
*/

}
