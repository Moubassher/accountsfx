package com.moubassher.accountsfx;

import com.context.our_utilities.DbConfig;
import com.context.our_utilities.FxmlHelper;
import com.context.our_utilities.MyNotifications;
import com.context.our_utilities.ThemeUtils;
import com.moubassher.accountsfx.expenses.ExpenseDao;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.event.EventHandler;
import javafx.geometry.NodeOrientation;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
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
    private ExpenseDao expenseDao;
    private Supplier<Parent> mainPaneFunc;

    private ObjectProperty<String> themeProperty;

    private Account<F> loggedInAccount;
    private boolean isDevelopmentMode;

    private Stage mainStage, loginStage, setupPage;
    private ButtonType approveAdmin = new ButtonType("تنشيط حسابات");
    private ButtonType createAdmin = new ButtonType("إنشاء حسابات");
    private ButtonType cancel = new ButtonType("إغلاق البرنامج", ButtonBar.ButtonData.CANCEL_CLOSE);

    public AccountsFX(Class<F> featureEnum, String appName, Stage mainStage, ObjectProperty<String> themeProperty) {
        this.featureEnum = featureEnum;
        this.appName = appName;
        this.mainStage = mainStage;
        this.setupPage = new Stage();
        setupPage.initModality(Modality.APPLICATION_MODAL);
        prepareLoginStage();
        accountDao = new AccountDao<>(featureEnum, appName);
        DbConfig dbConfig = new DbConfig(appName, "Users.db", "jdbc:sqlite:", this.getClass(), false);
        expenseDao = new ExpenseDao(dbConfig);

        loginCtrl = new LoginCtrl<>(accountDao, this);
        this.themeProperty = themeProperty;
    }

    private Consumer<Account<F>> loggedInAccountConsumer;
    public void onLogin(Supplier<Parent> mainPaneFunc, Consumer<Account<F>> loggedInAccountConsumer) {
        this.mainPaneFunc = mainPaneFunc;
        this.loggedInAccountConsumer = loggedInAccountConsumer;
    }

    public void initialize()  {

        accountDao.createMaintainerAccount();


        prepareMainWindowInBackground();

        if(isDevelopmentMode) {
            createTestAdmin("abc123!");
            showMainWindow();
        } else {
            showLoginWindow();
        }

        while(!accountDao.hasNonMaintainerAccountType()) {
            ensureAccountTypesCreated();
        }

        while(accountDao.isAdminRequired()) {
            ensureAdminAccountCreated();
        }

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
            Account<F> loggedInUser = showLoginWindow(AccountType.Class.MAINTAINER);
            if (loggedInUser != null) {
                AccountType<F> accountType = loggedInUser.getAccountType();
                if (accountType.getAccountClass().equals(AccountType.Class.MAINTAINER)) {
                    showMaintainerView();
                }
            } else {
                Platform.exit();
                System.exit(0);
            }
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
        setupPage.hide();
        mainStage.show();
    }

    private void prepareMainWindowInBackground() {

        Parent root = mainPaneFunc.get();

        Scene scene = new Scene(root);
        ThemeUtils.applyTheme(scene);
        Application.setUserAgentStylesheet(Application.STYLESHEET_CASPIAN);

        mainStage.setScene(scene);
        mainStage.setMaximized(true);
        EventHandler<WindowEvent> closeRequest = mainStage.getOnCloseRequest();
        mainStage.setOnCloseRequest(close -> {
            if(isDevelopmentMode) {
                closeRequest.handle(close);
            } else {
                confirmQuitting(closeRequest, close);
            }
        });
    }

    public Account<F> showLoginWindow(AccountType.Class accountClass) {
        mainStage.hide();
        Parent loginRoot = FxmlHelper.loadFxml("login.fxml", loginCtrl).getKey();
        if(accountClass != null) {
            loginCtrl.enforceAccountClass(accountClass);
        }

        Scene scene = new Scene(loginRoot);
        ThemeUtils.applyTheme(scene);
        loginStage.setScene(scene);

        loginStage.showAndWait();

        return loginCtrl.getLoggedInUser();
    }

    private void prepareLoginStage() {
        loginStage = new Stage(StageStyle.UTILITY);
        loginStage.initModality(Modality.APPLICATION_MODAL);
        loginStage.setResizable(false);

        EventHandler<WindowEvent> closeHandler = mainStage.getOnCloseRequest();
        loginStage.setOnCloseRequest(close -> {
            confirmQuitting(closeHandler, close);
        });
    }


    public Account<F> showLoginWindow() {
        mainStage.hide();
        return showLoginWindow(null);
    }

    private void showMaintainerView()  {
        MaintainerAccountCtrl<F> maintainerAccountCtrl = new MaintainerAccountCtrl<>(accountDao, featureEnum);
        Parent maintainerViewRoot = FxmlHelper.loadFxml("maintainer_account.fxml", maintainerAccountCtrl).getKey();
        show(maintainerViewRoot);

    }

    void show(Parent root) {
        mainStage.hide();
        Scene scene = new Scene(root);
        ThemeUtils.applyTheme(scene);
        setupPage.setScene(scene);
        setupPage.setMaximized(true);
        setupPage.showAndWait();
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


    public void showFeatureRestrictedAlert(F feature) {
        MyNotifications.getInstance()
                .showConfirmAlert("ليس لديك صلاحية لإجراء هذه العملية " + "(" +feature + ")",
                        "الرجاء تسجيل الدخول بحساب له صلاحية أو مراجعة الإدارة لإضافة الصلاحية إلى حسابك");
    }

    public AccountDao<F> getAccountDao() {
        return accountDao;
    }

    public Account<F> getLoggedInUser() {
        return loginCtrl.getLoggedInUser();
    }

    public ExpenseDao getExpenseDao() {
        return expenseDao;
    }

    public void enableDevelopmentMode() {
        isDevelopmentMode = true;
    }

    private void confirmQuitting(EventHandler<WindowEvent> closeHandler, WindowEvent close) {

        ButtonType yes = new ButtonType("نعم");
        ButtonType no = new ButtonType("لا");

        Optional<ButtonType> result = MyNotifications.getInstance()
                .showConfirmAlert(appName, "هل أنت متأكد من أنك تريد إغلاق البرنامج؟"
                        , List.of(yes, no), NodeOrientation.RIGHT_TO_LEFT);

        result.ifPresent(buttonType -> {
            if(buttonType == yes) {
                closeHandler.handle(close);
            } else {
                close.consume();
            }
        });
    }

    public void createTestAdmin(String rawPassword) {

        List<F> allPrivileges = List.of(featureEnum.getEnumConstants());
        AccountType<F> accountType = new AccountType<>("location manager",
                allPrivileges, AccountType.Class.ADMIN);

        accountDao.addAccountType(accountType);

        PasswordValidator passwordValidator = new PasswordValidator();

        String hashedPassword = passwordValidator.hashPassword(rawPassword);

        Account<F> admin = new Account<>("Abdallah", hashedPassword, accountType);

        accountDao.requestAccountApproval(admin);
        accountDao.approveNewAccount(admin);

    }

}