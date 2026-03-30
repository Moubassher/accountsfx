package com.moubassher.accountsfx;

import com.context.our_utilities.FxmlHelper;
import com.context.our_utilities.MyNotifications;
import com.context.themes.vibrant_blue.MyThemes;
import javafx.application.Application;
import javafx.geometry.NodeOrientation;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

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
    private Supplier<Parent> mainPaneFunc;


    private Stage mainStage, loginStage;
    private ButtonType approveAdmin = new ButtonType("تنشيط حسابات");
    private ButtonType createAdmin = new ButtonType("إنشاء حسابات");
    private ButtonType cancel = new ButtonType("إغلاق البرنامج", ButtonBar.ButtonData.CANCEL_CLOSE);

    public AccountsFX(Class<F> featureEnum, String appName, Stage mainStage) {
        this.featureEnum = featureEnum;
        this.appName = appName;
        this.mainStage = mainStage;
        accountDao = new AccountDao<>(featureEnum, appName);
        loginCtrl = new LoginCtrl<>(accountDao, this);
    }

    public void onLogin(Supplier<Parent> mainPaneFunc) {
        this.mainPaneFunc = mainPaneFunc;
    }

    void initialize()  {

        accountDao.createMaintainerAccount();

        while(!accountDao.hasNonMaintainerAccountType()) {
            ensureAccountTypesCreated();
        }

        while(accountDao.isAdminRequired()) {
            ensureAdminAccountCreated();
        }

        showLoginWindow();

    }


    private void ensureAdminAccountCreated() {
        Optional<ButtonType> result = showMissingAdminAlert();

        result.ifPresent(button -> {
            if(button == createAdmin) {
                showAdminCreationView();
            } else if(button == approveAdmin) {
                showLoginWindow();

                Account<F> loggedInUser = loginCtrl.getLoggedInUser();
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
        } else {
            System.exit(0);
        }
    }

    private void showUserOverviewWindow() {
        Parent pane = FxmlHelper.loadFxml("account_overview.fxml", new AccountOverviewCtrl<>(accountDao)).getKey();
        show(pane);
    }


    public void showMainWindow() {
        loginStage.hide();
        Parent root = mainPaneFunc.get();
        Scene scene = new Scene(root);
        Application.setUserAgentStylesheet(Application.STYLESHEET_CASPIAN);

        scene.getStylesheets().add(MyThemes.vibrantBlue);

        mainStage.setScene(scene);
        mainStage.setOnCloseRequest(close -> {
            // close.consume();
            //deleteTestData();
        });
        mainStage.show();

    }

    public void showLoginWindow(AccountType.Class accountClass) {
        Parent loginRoot = FxmlHelper.loadFxml("login.fxml", loginCtrl, MyThemes.vibrantBlue).getKey();
        if(accountClass != null) {
            loginCtrl.enforceAccountClass(accountClass);
        }

        loginStage = new Stage();
        loginStage.initModality(Modality.WINDOW_MODAL);
        loginStage.initOwner(mainStage);
        Scene scene = new Scene(loginRoot);
        loginStage.setScene(scene);
        loginStage.showAndWait();

        Account<F> loggedInUser = loginCtrl.getLoggedInUser();
        if (loggedInUser != null) {
            AccountType<F> accountType = loggedInUser.getAccountType();
            if (accountType.getAccountClass().equals(AccountType.Class.MAINTAINER)) {
                showMaintainerView();
            }
        } else {
            mainStage.hide();
        }
    }


    public void showLoginWindow() {
        mainStage.hide();
        showLoginWindow(null);
    }

    private void showMaintainerView()  {
        MaintainerAccountCtrl<F> maintainerAccountCtrl = new MaintainerAccountCtrl<>(accountDao, featureEnum);
        Parent maintainerViewRoot = FxmlHelper.loadFxml("maintainer_account.fxml", maintainerAccountCtrl).getKey();
        show(maintainerViewRoot);
    }

    void show(Parent root) {
        Scene scene = new Scene(root);
        scene.getStylesheets().add(MyThemes.vibrantBlue);
        mainStage.setScene(scene);
        mainStage.show();
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


    public void showFeatureRestrictedAlert() {
        MyNotifications.getInstance().showConfirmAlert("ليس لديك صلاحية لإجراء هذه العملية", "الرجاء تسجيل الدخول بحساب له صلاحية أو مراجعة الإدارة لإضافة الصلاحية إلى حسابك");
    }

    public AccountDao<F> getAccountDao() {
        return accountDao;
    }

}