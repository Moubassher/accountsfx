package com.moubassher.accountsfx;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;

import java.net.URL;
import java.util.ResourceBundle;

class TestAppCtrl implements Initializable {

    @FXML
    private MenuItem logOut;

    @FXML
    private Label loggedInUserDisplay;


    public void onUserLogin(Account<TestFeature> loggedInUser) {
        loggedInUserDisplay.setText(loggedInUser.toString());
    }

    private Runnable logoutRunnable;
    public void onLogout(Runnable logoutRunnable) {
        this.logoutRunnable = logoutRunnable;
    }

        @Override
    public void initialize(URL location, ResourceBundle resources) {

        logOut.setOnAction(x -> {
            logout();
        });

    }

    private void logout() {
        logoutRunnable.run();
    }

}
