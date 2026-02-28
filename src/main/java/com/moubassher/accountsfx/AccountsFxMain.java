package com.moubassher.accountsfx;

import com.context.themes.vibrant_blue.MyThemes;
import com.moubassher.accountsfx.testApp.TestAppCtrl;
import com.moubassher.accountsfx.testApp.TestFeature;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.io.IOException;
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


    @Override
    public void start(Stage stage) throws IOException {

        TestAppCtrl testAppCtrl = new TestAppCtrl(stage);
        FXMLLoader loader = new FXMLLoader(TestAppCtrl.class.getResource("testApp.fxml"));
        loader.setController(testAppCtrl);
        Pane pane = loader.load();

        AdminPrivileges<TestFeature> adminPrivileges = new AdminPrivileges<>(TestFeature.class, "TestApp");
        FXMLLoader fxmlLoader = new FXMLLoader(this.getClass().getResource("admin_privileges.fxml"));
        fxmlLoader.setController(adminPrivileges);
        //Pane pane = fxmlLoader.load();

        Scene scene = new Scene(pane, 540, 440);
        scene.getStylesheets().add(MyThemes.vibrantBlue);

        stage.setScene(scene);
        stage.setOnCloseRequest(close -> {
           // testAppCtrl.deleteTestData();
        });

        stage.show();
    }

    void createTestAccounts() {

        AccountDao<TestFeature> accountDao = new AccountDao<>(TestFeature.class, "AccountsFxTest");

        String hashedPassword = new PasswordValidator().hashPassword("123");

        List<TestFeature> adminPrivileges = List.of(TestFeature.FeatureA, TestFeature.FeatureB);
        AccountType<TestFeature> adminAccountType = new AccountType<>("Supervisor", adminPrivileges, AccountType.Class.ADMIN);

        accountDao.addAccountType(adminAccountType);

        Account<TestFeature> adminAccount = new Account<>("Kuroro", hashedPassword, adminAccountType);

        accountDao.addNewAccount(adminAccount);
    }

}