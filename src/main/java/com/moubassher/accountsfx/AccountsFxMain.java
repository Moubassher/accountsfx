package com.moubassher.accountsfx;

import com.moubassher.accountsfx.accessControl.AccountCreateCtrl;
import com.moubassher.accountsfx.accessControl.LoginCtrl;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.NodeOrientation;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Optional;

///Logic Flow:
/// 1. The first login requires an admin account.
/// 2. After the creation of an admin account, other accounts can be created freely, but can only sign in after an admin's approval.
/// 3. The first admin has to approve of other admin accounts to be created.
/// 4. Other admins can then also approve of admin and non-admin accounts alike.
///
///
public class AccountsFxMain extends Application {

    @Override
    public void start(Stage stage) throws IOException {

        VBox loginRoot = FXMLLoader.load(this.getClass().getResource("accessControl/login.fxml"));

        Scene scene = new Scene(loginRoot, 540, 440);

        //  boolean isAdminRequired = AccountDao.getInstance().isAdminRequired();
        boolean isAdminRequired = false;
        if(isAdminRequired) {
            Optional<ButtonType> result = showMissingAdminAlert();

            if(result.isPresent()) {
                ButtonBar.ButtonData userChoice = result.get().getButtonData();
                if (userChoice == ButtonBar.ButtonData.CANCEL_CLOSE) {
                    System.exit(0);
                }else if (userChoice == ButtonBar.ButtonData.OK_DONE) {
                    showCreateAdminAccountDialog(scene);
                }
            }

        }

        stage.setScene(scene);
        stage.show();
    }

    private void showCreateAdminAccountDialog(Scene scene) {
        try {
            AccountCreateCtrl accountCreateCtrl = new AccountCreateCtrl(true);
            FXMLLoader fxmlLoader = new FXMLLoader(AccountCreateCtrl.class.getResource("create_account.fxml"));
            fxmlLoader.setController(accountCreateCtrl);
            Pane accountCreateRoot = fxmlLoader.load();
            scene.setRoot(accountCreateRoot);
        } catch (IOException e) {
            e.printStackTrace();
        }
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
