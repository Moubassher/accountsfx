package com.moubassher.accountsfx;

import com.context.our_utilities.MyNotifications;
import com.context.themes.vibrant_blue.MyThemes;
import javafx.fxml.FXMLLoader;
import javafx.geometry.NodeOrientation;
import javafx.scene.Scene;
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

    private ButtonType approveAdmin = new ButtonType("تنشيط حسابات");
    private ButtonType createAdmin = new ButtonType("إنشاء حسابات");
    private ButtonType cancel = new ButtonType("إغلاق البرنامج", ButtonBar.ButtonData.CANCEL_CLOSE);

    public AccountsFX(Class<F> featureEnum, String appName, Stage stage) {
        this.featureEnum = featureEnum;
        this.appName = appName;
        this.stage = stage;
        accountDao = new AccountDao<>(featureEnum, appName);
        loginCtrl = new LoginCtrl<>(accountDao);
    }

    public Account<F> initialize()  {

        accountDao.createMaintainerAccount();

        while(!accountDao.hasNonMaintainerAccountType()) {

            boolean confirmed = promptAccountTypesCreation();
            if (confirmed) {
                showLoginWindow(AccountType.Class.MAINTAINER);

                Account<F> loggedInUser = loginCtrl.getCurrentUser();
                if (loggedInUser != null) {
                    AccountType<F> accountType = loggedInUser.getAccountType();
                    if (accountType.getAccountClass().equals(AccountType.Class.MAINTAINER)) {
                        showMaintainerView(accountDao);
                    }
                }
            } else {
                System.exit(0);
            }
        }
        //TODO: if there are admin accounts pending approval, ask the user whether he would like
        //to approve them, or create a new admin account,
        while(accountDao.isAdminRequired()) {
            Optional<ButtonType> result = showMissingAdminAlert();

            result.ifPresent(button -> {
                if(button == createAdmin) {
                    showAdminCreationView();
                } else if(button == approveAdmin) {
                    showLoginWindow();
                    Account<F> loggedInUser = loginCtrl.getCurrentUser();
                    if (loggedInUser != null) {
                        AccountType<F> accountType = loggedInUser.getAccountType();
                        if (accountType.getAccountClass().equals(AccountType.Class.MAINTAINER)) {
                            showMaintainerView(accountDao);
                        }
                    }
                } else if(button.getButtonData() == ButtonBar.ButtonData.CANCEL_CLOSE) {
                    System.exit(0);
                }
            });
        }

        showLoginWindow();

        return loginCtrl.getCurrentUser();
    }

    private void showView(Pane pane) {
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);

        Scene scene = new Scene(pane, 540, 440);
        scene.getStylesheets().add(MyThemes.vibrantBlue);
        stage.setScene(scene);
        stage.setMaximized(true);
        stage.showAndWait();

    }

    public void showLoginWindow(AccountType.Class accountClass) {
        FXMLLoader loginLoader = new FXMLLoader(AccountsFX.class.getResource("login.fxml"));
        loginLoader.setController(loginCtrl);
        try {
            Pane loginRoot = loginLoader.load();
            if(accountClass != null) {
                loginCtrl.enforceAccountClass(accountClass);
            }
            showView(loginRoot);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void showLoginWindow() {
        showLoginWindow(null);
    }

    private void showMaintainerView(AccountDao<F> accountDao)  {
        MaintainerAccountCtrl<F> maintainerAccountCtrl = new MaintainerAccountCtrl<>(accountDao, featureEnum);
        FXMLLoader maintainerLoader = new FXMLLoader(AccountsFX.class.getResource("maintainer_account.fxml"));
        maintainerLoader.setController(maintainerAccountCtrl);

        Pane maintainerViewRoot = null;
        try {
            maintainerViewRoot = maintainerLoader.load();
            showView(maintainerViewRoot);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showAdminCreationView() {

        AccountCreateCtrl<F> accountCreateCtrl = new AccountCreateCtrl<>(accountDao,
                List.of(), true);
        accountCreateCtrl.showCreateAdminAccountDialog();
    }



    private Optional<ButtonType> showMissingAdminAlert() {
        String noAdminAccountMsg = "لا يوجد حساب إداري نشط على النظام. يجب إنشاء حساب إداري واحد على الأقل لاستخدام النظام.";
        return MyNotifications.getInstance().showConfirmAlert("", noAdminAccountMsg, List.of(approveAdmin, createAdmin, cancel), NodeOrientation.RIGHT_TO_LEFT);
    }

    private boolean promptAccountTypesCreation() {
        String noAccountTypesMsg = "لا توجد أي أنواع حسابات في النظام، يجب إنشاء نوع حساب إداري واحد على الأقل";
        return MyNotifications.getInstance().showConfirmAlert(appName, noAccountTypesMsg);
    }


    public void logOut(Stage stage) {
        stage.hide();
        showLoginWindow();

    }

    public AccountDao<F> getAccountDao() {
        return accountDao;
    }

}
