package com.moubassher.accountsfx;

import com.context.our_utilities.FxmlHelper;
import com.context.our_utilities.ThemeUtils;
import javafx.application.Application;
import javafx.scene.Parent;
import javafx.stage.Stage;

import java.util.List;

///Logic Flow:
/// 1. The first login requires an admin account.
/// 2. After the creation of an admin account, other accounts can be created freely, but can only sign in after an admin's approval.
/// 3. The first admin has to approve of other admin accounts to be created.
/// 4. Other admins can then also approve of admin and non-admin accounts alike.
///
///
public class AccountsFxMain extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    private AccountsFX<TestFeature> accountsFX;

    @Override
    public void start(Stage primaryStage) {

        TestAppCtrl testAppCtrl = new TestAppCtrl();

        primaryStage.setOnCloseRequest(close -> {
            // close.consume();
            //deleteTestData();
        });

        accountsFX = new AccountsFX<>(TestFeature.class,
                "AccountsFxTest", primaryStage, ThemeUtils.themeProperty);

        accountsFX.enableDevelopmentMode();

        Parent root = FxmlHelper.loadFxml("testApp/testApp.fxml", testAppCtrl).getKey();
        accountsFX.onLogin(() -> root, account -> testAppCtrl.onUserLogin(account));

        testAppCtrl.onLogout(() -> {
            accountsFX.showLoginWindow();
        });

        accountsFX.initialize();;

    }

    public void deleteTestData() {
        AccountDao<TestFeature> accountDao = accountsFX.getAccountDao();
        accountDao.deleteAllAccounts();
        accountDao.deleteAllPrivileges();
        accountDao.deleteAccountTypes();
    }

    void createTestAccounts() {

        AccountDao<TestFeature> accountDao = new AccountDao<>(TestFeature.class, "AccountsFxTest");

        String hashedPassword = new PasswordValidator().hashPassword("123");

        List<TestFeature> adminPrivileges = List.of(TestFeature.FeatureA, TestFeature.FeatureB);
        AccountType<TestFeature> adminAccountType = new AccountType<>("Supervisor", adminPrivileges, AccountType.Class.ADMIN);

        accountDao.addAccountType(adminAccountType);

        Account<TestFeature> adminAccount = new Account<>("Kuroro", hashedPassword, adminAccountType);

        accountDao.approveNewAccount(adminAccount);
    }

}