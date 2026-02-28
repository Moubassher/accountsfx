package com.moubassher.accountsfx.testApp;

import com.moubassher.accountsfx.Account;
import com.moubassher.accountsfx.AccountDao;
import com.moubassher.accountsfx.AccountsFX;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class TestAppCtrl implements Initializable {

    @FXML
    private MenuItem logOut;

    @FXML
    private Label loggedInUserDisplay;

    private Stage stage;

    private AccountsFX<TestFeature> accountsFX;
    public TestAppCtrl(Stage stage) {
        this.stage = stage;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        accountsFX = new AccountsFX<>(TestFeature.class,
                "AccountsFxTest", stage);
    //  this.createTestAccounts();
        Account<TestFeature> loggedInUser = accountsFX.initialize();

        loggedInUserDisplay.setText(loggedInUser.toString());

        logOut.setOnAction(x -> {

        });

    }
    public void deleteTestData() {
        AccountDao<TestFeature> accountDao = accountsFX.getAccountDao();
        accountDao.deleteAllAccounts();
        accountDao.deleteAllPrivileges();
        accountDao.deleteAccountTypes();
    }
}
