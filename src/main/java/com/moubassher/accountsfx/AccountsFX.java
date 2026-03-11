package com.moubassher.accountsfx;

import com.context.our_utilities.FxmlHelper;
import com.context.our_utilities.MyNotifications;
import com.context.themes.vibrant_blue.MyThemes;
import javafx.geometry.NodeOrientation;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.stage.Modality;
import javafx.stage.Stage;

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
    private AccountDao<F> accountDao;
    private LoginCtrl<F> loginCtrl;

    private ButtonType approveAdmin = new ButtonType("تنشيط حسابات");
    private ButtonType createAdmin = new ButtonType("إنشاء حسابات");
    private ButtonType cancel = new ButtonType("إغلاق البرنامج", ButtonBar.ButtonData.CANCEL_CLOSE);

    public AccountsFX(Class<F> featureEnum, String appName) {
        this.featureEnum = featureEnum;
        this.appName = appName;
        accountDao = new AccountDao<>(featureEnum, appName);
        loginCtrl = new LoginCtrl<>(accountDao);
    }

    public Account<F> initialize()  {

        accountDao.createMaintainerAccount();

        showFeatureRestrictedAlert();

        while(!accountDao.hasNonMaintainerAccountType()) {
            ensureAccountTypesCreated();
        }

        while(accountDao.isAdminRequired()) {
            ensureAdminAccountCreated();
        }

        showLoginWindow();

        return loginCtrl.getCurrentUser();
    }


    private void ensureAdminAccountCreated() {
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
                        showMaintainerView();
                    } else if(accountType.getAccountClass().equals(AccountType.Class.ADMIN)) {
                        showUserOverviewWindow();
                    }
                }
            } else if(button.getButtonData() == ButtonBar.ButtonData.CANCEL_CLOSE) {
                System.exit(0);
            }
        });
    }

    private void ensureAccountTypesCreated() {
        boolean confirmed = promptAccountTypesCreation();
        if (confirmed) {
            showLoginWindow(AccountType.Class.MAINTAINER);

            Account<F> loggedInUser = loginCtrl.getCurrentUser();
            if (loggedInUser != null) {
                AccountType<F> accountType = loggedInUser.getAccountType();
                if (accountType.getAccountClass().equals(AccountType.Class.MAINTAINER)) {
                    showMaintainerView();
                }
            }
        } else {
            System.exit(0);
        }
    }

    private void showUserOverviewWindow() {
        Parent pane = FxmlHelper.loadFxml("account_overview.fxml", new AccountOverviewCtrl<>(accountDao)).getKey();
        showView(pane);
    }

    private void showView(Parent pane) {
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);

        Scene scene = new Scene(pane, 540, 440);
        scene.getStylesheets().add(MyThemes.vibrantBlue);
        stage.setScene(scene);
        stage.setMaximized(true);
        stage.showAndWait();

    }

    public void showLoginWindow(AccountType.Class accountClass) {
        Parent loginRoot = FxmlHelper.loadFxml("login.fxml", loginCtrl, MyThemes.vibrantBlue).getKey();
        if(accountClass != null) {
            loginCtrl.enforceAccountClass(accountClass);
        }
        showView(loginRoot);
    }

    public void showLoginWindow() {
        showLoginWindow(null);
    }

    private void showMaintainerView()  {
        MaintainerAccountCtrl<F> maintainerAccountCtrl = new MaintainerAccountCtrl<>(accountDao, featureEnum);
        Parent maintainerViewRoot = FxmlHelper.loadFxml("maintainer_account.fxml", maintainerAccountCtrl).getKey();
        showView(maintainerViewRoot);
    }

    private void showAdminCreationView() {
        AccountCreateCtrl<F> accountCreateCtrl = new AccountCreateCtrl<>(accountDao, true);
        accountCreateCtrl.showCreateAdminAccountDialog();
    }

    private Optional<ButtonType> showMissingAdminAlert() {
        String noAdminAccountMsg = "لا يوجد حساب إداري نشط على النظام. يجب إنشاء حساب إداري واحد على الأقل لاستخدام النظام.";
        return MyNotifications.getInstance().showConfirmAlert("", noAdminAccountMsg, List.of(approveAdmin, createAdmin, cancel), NodeOrientation.RIGHT_TO_LEFT);
    }

    private boolean promptAccountTypesCreation() {
        String noAccountTypesMsg = "لا توجد أي فئات حسابات في النظام، يجب إنشاء فئة حساب إدارية واحدة على الأقل";
        return MyNotifications.getInstance().showConfirmAlert("لا يوجد فئات حسابات", noAccountTypesMsg);
    }


    public void logOut(Stage stage) {
        stage.hide();
        showLoginWindow();
    }

    public void showFeatureRestrictedAlert() {
        MyNotifications.getInstance().showConfirmAlert("ليس لديك صلاحية لإجراء هذه العملية", "الرجاء تسجيل الدخول بحساب له صلاحية أو مراجعة الإدارة لإضافة الصلاحية إلى حسابك");
    }

    public AccountDao<F> getAccountDao() {
        return accountDao;
    }

}