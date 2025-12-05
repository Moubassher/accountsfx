package com.moubassher.accountsfx;

import atlantafx.base.theme.NordLight;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.NodeOrientation;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

///Logic Flow:
/// 1. The first login requires an admin account.
/// 2. After the creation of an admin account, other accounts can be created freely, but can only sign in after an admin's approval.
/// 3. The first admin has to approve of other admin accounts to be created.
/// 4. Other admins can then also approve of admin and non-admin accounts alike.
///
///
public class AccountsFxMain extends Application {


    public static void main(String[] args) {
        launch(args);
    }


    @Override
    public void start(Stage stage) throws IOException {

        Application.setUserAgentStylesheet(new NordLight().getUserAgentStylesheet());


        VBox loginRoot = FXMLLoader.load(this.getClass().getResource("login.fxml"));

        Scene scene = new Scene(loginRoot, 540, 440);

        AccountDao<TestFeature> accountDao = new AccountDao<>(TestFeature.class);

        MaintainerAccountCtrl<TestFeature> maintainerAccountCtrl = new MaintainerAccountCtrl<>(accountDao, TestFeature.class);
        FXMLLoader maintainerLoader = new FXMLLoader(this.getClass().getResource("maintainer_account.fxml"));
        maintainerLoader.setController(maintainerAccountCtrl);
        HBox maintainerViewRoot = maintainerLoader.load();
        scene.setRoot(maintainerViewRoot);

        boolean isAdminRequired = accountDao.isAdminRequired();

        if(isAdminRequired) {
            Optional<ButtonType> result = showMissingAdminAlert();

            if(result.isPresent()) {
                ButtonBar.ButtonData userChoice = result.get().getButtonData();
                if (userChoice == ButtonBar.ButtonData.CANCEL_CLOSE) {
                    System.exit(0);
                }else if (userChoice == ButtonBar.ButtonData.OK_DONE) {

                    accountDao.createMaintainerAccount(TestFeature.class);
                    AccountCreateCtrl<TestFeature> accountCreateCtrl = new AccountCreateCtrl<>(accountDao,
                            List.of(), true);
                    accountCreateCtrl.showCreateAdminAccountDialog();

                }
            }

        }

        stage.setScene(scene);
        stage.show();

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

        return result;
    }

}
