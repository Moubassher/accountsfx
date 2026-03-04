package com.moubassher.accountsfx;

import com.context.themes.vibrant_blue.MyThemes;
import javafx.fxml.FXMLLoader;
import javafx.geometry.NodeOrientation;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * The system starts by checking if there are account types in the database. If there are none,
 * a maintainer is required to log in to assign some, with at least one being of the admin class.
 * Once there is at least one admin, non-admin accounts can be created (approval of which,
 * is obtained by an admin user logging in and approving each account individually).
 *
 * @param <F>
 */
public class AccountsFX <F extends Enum<F> & Feature<F>>{

    private final Class<F> featureEnum;
    private final String appName;
    private Stage stage;
    private AccountDao<F> accountDao;
    private LoginCtrl<F> loginCtrl;

    public AccountsFX(Class<F> featureEnum, String appName, Stage stage) {
        this.featureEnum = featureEnum;
        this.appName = appName;
        this.stage = stage;
        accountDao = new AccountDao<>(featureEnum, appName);
        loginCtrl = new LoginCtrl<>(accountDao);
    }

    public Account<F> initialize()  {

        Pane root;

        root = loadMaintainerView(accountDao);
        showView(root);

        while(!accountDao.hasNonMaintainerAccountType()) {
            Optional<ButtonType> result = showNoAccountTypesAlert();
            if(result.isPresent() && result.get().getButtonData() == ButtonBar.ButtonData.OK_DONE) {
                accountDao.createMaintainerAccount();

                Account<F> loggedInUser = loginCtrl.getCurrentUser();
                if (loggedInUser == null) {
                    root = loadLoginView();
                    showView(root);
                } else {
                    AccountType<F> accountType = loggedInUser.getAccountType();
                    if(accountType.getAccountClass().equals(AccountType.Class.MAINTAINER)) {
                        root = loadMaintainerView(accountDao);
                        showView(root);
                    }
                }
            }
        }

        if(accountDao.isAdminRequired()) {
            Optional<ButtonType> result = showMissingAdminAlert();
            showAdminCreationView(result);
        }

        root = loadLoginView();

        showView(root);

        return loginCtrl.getCurrentUser();
    }

    private void showView(Pane pane) {
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);

        Scene scene = new Scene(pane, 540, 440);
        scene.getStylesheets().add(MyThemes.vibrantBlue);

        stage.setScene(scene);
        stage.showAndWait();
    }

    public void showLoginWindow() {
        Pane root = loadLoginView();
        showView(root);
    }

    private Pane loadMaintainerView(AccountDao<F> accountDao)  {
        MaintainerAccountCtrl<F> maintainerAccountCtrl = new MaintainerAccountCtrl<>(accountDao, featureEnum);
        FXMLLoader maintainerLoader = new FXMLLoader(AccountsFX.class.getResource("maintainer_account.fxml"));
        maintainerLoader.setController(maintainerAccountCtrl);

        Pane maintainerViewRoot = null;
        try {
            maintainerViewRoot = maintainerLoader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return maintainerViewRoot;
    }

    private void showAdminCreationView(Optional<ButtonType> result) {
        if(result.isPresent() && result.get().getButtonData() == ButtonBar.ButtonData.OK_DONE) {
            AccountCreateCtrl<F> accountCreateCtrl = new AccountCreateCtrl<>(accountDao,
                    List.of(), true);
            accountCreateCtrl.showCreateAdminAccountDialog();
        }
    }

    public Pane loadLoginView() {

        FXMLLoader loginLoader = new FXMLLoader(AccountsFX.class.getResource("login.fxml"));
        loginLoader.setController(loginCtrl);

        Pane loginRoot = null;
        try {
            loginRoot = loginLoader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return loginRoot;
    }


    private Optional<ButtonType> showMissingAdminAlert() {
        String noAdminAccountMsg = "لا يوجد حساب إداري على النظام. يجب إنشاء على الأقل حساب إداري واحد لاستخدام النظام.";
        return showAlert(noAdminAccountMsg);
    }
    private Optional<ButtonType> showNoAccountTypesAlert() {
        String noAccountTypesMsg = "لا توجد أي أنواع حسابات في النظام، يجب إنشاء على الأقل نوع حساب إداري واحد";
        return showAlert(noAccountTypesMsg);
    }

    private Optional<ButtonType> showAlert(String msg) {
        Alert alert =new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(appName);
        alert.setHeaderText(msg);

        ButtonType ok = new ButtonType("موافق", ButtonBar.ButtonData.OK_DONE);
        ButtonType quit = new ButtonType("خروج", ButtonBar.ButtonData.CANCEL_CLOSE);

        alert.getButtonTypes().setAll(ok, quit);
        alert.getDialogPane().setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
        Optional<ButtonType> result = alert.showAndWait();

        if(result.isPresent()) {
            ButtonBar.ButtonData userChoice = result.get().getButtonData();
            if (userChoice == ButtonBar.ButtonData.CANCEL_CLOSE) {
                System.exit(0);
            }
        }

        return result;
    }

    public void logOut(Stage stage) {
        stage.hide();
        showLoginWindow();

    }

    public AccountDao<F> getAccountDao() {
        return accountDao;
    }

}
