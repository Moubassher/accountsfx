package com.moubassher.accountsfx.testApp;

import com.context.our_utilities.FxmlHelper;
import com.context.our_utilities.ThemeUtils;
import com.moubassher.accountsfx.Account;
import com.moubassher.accountsfx.AccountDao;
import com.moubassher.accountsfx.MainController;
import com.moubassher.accountsfx.audit.UserActivityReview;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class TestAppCtrl implements Initializable, MainController<TestFeature> {

    @FXML
    private MenuItem logOut, showUserActivity;

    @FXML
    private Label loggedInUserDisplay;

    private Account<TestFeature> loggedInUser;

    private AccountDao<TestFeature> accountDao;
    private Stage viewStage;


    @Override
    public void setAccountDao(AccountDao<TestFeature> accountDao) {
        this.accountDao = accountDao;
    }

    @Override
    public void onLogin(Account<TestFeature> loggedInUser) {
        this.loggedInUser = loggedInUser;
        loggedInUserDisplay.setText(loggedInUser.toString());

    }

    private Runnable logoutRunnable;
    @Override
    public void onLogout(Runnable logoutRunnable) {
        this.logoutRunnable = logoutRunnable;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        prepareViewStage();

        logOut.setOnAction(x -> {
            logout();
        });

        showUserActivity.setOnAction(x -> {
            UserActivityReview activityReview = new UserActivityReview(accountDao, TestFeature.class);
            Parent activityView = FxmlHelper.loadFxml("user_activity_review.fxml", activityReview);
            showView(activityView);
        });

    }


    private void prepareViewStage() {
        viewStage = new Stage();
        viewStage.initModality(Modality.APPLICATION_MODAL);
        Scene scene = new Scene(new Pane());
        ThemeUtils.applyTheme(scene);
        viewStage.setScene(scene);
        viewStage.setMaximized(true);
    }

    protected void showView(Parent root) {
        root.getStyleClass().add("root");
        viewStage.getScene().setRoot(root);
        viewStage.show();
    }
    private void logout() {
        logoutRunnable.run();
    }

}
