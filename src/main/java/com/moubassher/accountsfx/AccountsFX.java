package com.moubassher.accountsfx;

import com.context.themes.vibrant_blue.MyThemes;
import javafx.fxml.FXMLLoader;
import javafx.geometry.NodeOrientation;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class AccountsFX <F extends Enum<F> & Feature<F>>{


    private Class<F> featureEnum;
    private String appName;
    private AccountDao<F> accountDao;

    public AccountsFX(Class<F> featureEnum, String appName) {
        this.featureEnum = featureEnum;
        this.appName = appName;
        accountDao = new AccountDao<>(featureEnum, appName);
    }

    public AccountDao<F> getAccountDao() {
        return accountDao;
    }

    public void initialize() throws IOException {

        Pane root;
        if(! accountDao.hasAccountTypes()) {
             root = loadMaintainerView(accountDao);
        } else {
            root = createLoginView(accountDao);
        }

        showView(root);

        boolean isAdminRequired = accountDao.isAdminRequired();

        if(isAdminRequired) {
            Optional<ButtonType> result = showMissingAdminAlert();

            if(result.isPresent() && result.get().getButtonData() == ButtonBar.ButtonData.OK_DONE) {

                accountDao.createMaintainerAccount();
                AccountCreateCtrl<F> accountCreateCtrl = new AccountCreateCtrl<>(accountDao,
                        List.of(), true);
                accountCreateCtrl.showCreateAdminAccountDialog();
            }
        }

    }



    private void showView(Pane pane) {
        Stage stage = new Stage(StageStyle.UTILITY);
        stage.initModality(Modality.APPLICATION_MODAL);

        Scene scene = new Scene(pane, 540, 440);
        scene.getStylesheets().add(MyThemes.vibrantBlue);

        stage.setScene(scene);
        stage.showAndWait();
    }

    private HBox loadMaintainerView(AccountDao<F> accountDao) throws IOException {
        MaintainerAccountCtrl<F> maintainerAccountCtrl = new MaintainerAccountCtrl<>(accountDao, featureEnum);
        FXMLLoader maintainerLoader = new FXMLLoader(AccountsFX.class.getResource("maintainer_account.fxml"));
        maintainerLoader.setController(maintainerAccountCtrl);

        HBox maintainerViewRoot = maintainerLoader.load();

        return maintainerViewRoot;
    }

    private VBox createLoginView(AccountDao<F> accountDao) throws IOException {

        LoginCtrl<F> loginCtrl = new LoginCtrl<>(accountDao);

        FXMLLoader loginLoader = new FXMLLoader(AccountsFX.class.getResource("login.fxml"));
        loginLoader.setController(loginCtrl);

        VBox loginRoot = loginLoader.load();

        return loginRoot;
    }

    private Optional<ButtonType> showMissingAdminAlert() {
        Alert alert =new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("المحاسب الذهبي");
        alert.setHeaderText("لا يوجد حساب إداري على النظام. يجب إنشاء على الأقل حساب إداري واحد لاستخدام النظام.");
        alert.getDialogPane().getStylesheets().add("GreyFabric.css");
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
}
