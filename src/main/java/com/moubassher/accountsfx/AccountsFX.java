package com.moubassher.accountsfx;

import com.context.themes.vibrant_blue.MyThemes;
import javafx.fxml.FXMLLoader;
import javafx.geometry.NodeOrientation;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

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
    }

    public Account<F> initialize()  {

        Pane root;

        boolean isAdminRequired = accountDao.isAdminRequired();

        if(!accountDao.hasAccountTypes()) {
            Optional<ButtonType> result = showNoAccountTypesAlert();
            if(result.isPresent() && result.get().getButtonData() == ButtonBar.ButtonData.OK_DONE) {
                root = loadMaintainerView(accountDao);
                showView(root);
            }
        } else if(isAdminRequired) {
            Optional<ButtonType> result = showMissingAdminAlert();
            showAdminCreationView(result);
        }

        root = loadLoginView(accountDao);

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
            accountDao.createMaintainerAccount();
            AccountCreateCtrl<F> accountCreateCtrl = new AccountCreateCtrl<>(accountDao,
                    List.of(), true);
            accountCreateCtrl.showCreateAdminAccountDialog();
        }
    }

    private VBox loadLoginView(AccountDao<F> accountDao) {

        loginCtrl = new LoginCtrl<>(accountDao);

        FXMLLoader loginLoader = new FXMLLoader(AccountsFX.class.getResource("login.fxml"));
        loginLoader.setController(loginCtrl);

        VBox loginRoot = null;
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
        Pane login = loadLoginView(accountDao);
        Scene scene = new Scene(login, 400, 400);
        stage.setScene(scene);
    }

    public AccountDao<F> getAccountDao() {
        return accountDao;
    }

}
