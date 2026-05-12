package com.moubassher.accountsfx.testApp;

import com.context.our_utilities.FxmlHelper;
import com.context.our_utilities.ThemeUtils;
import com.moubassher.accountsfx.AccountDao;
import com.moubassher.accountsfx.AccountsFX;
import javafx.application.Application;
import javafx.scene.Parent;
import javafx.stage.Stage;

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

        primaryStage.setOnCloseRequest(close -> {
            // close.consume();
            //deleteTestData();
        });

        TestAppCtrl testAppCtrl = new TestAppCtrl();
        Parent mainRoot =  FxmlHelper.loadFxml("testApp.fxml", testAppCtrl);
        accountsFX = new AccountsFX<>(
                TestFeature.class,
                "AccountsFxTest",
                primaryStage,
                mainRoot,
                testAppCtrl,
                ThemeUtils.themeProperty);

        accountsFX.enableDevelopmentMode();

        accountsFX.initialize();
    }

    public void deleteTestData() {
        AccountDao<TestFeature> accountDao = accountsFX.getAccountDao();
        accountDao.deleteAllAccounts();
        accountDao.deleteAllPrivileges();
        accountDao.deleteAccountTypes();
    }

}