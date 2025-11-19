package com.moubassher.accountsfx;

import com.moubassher.accountsfx.accessControl.EmployeeDB;
import javafx.application.Application;
import javafx.geometry.NodeOrientation;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

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
    public void start(Stage stage) {

        boolean isManagerExists = EmployeeDB.getInstance().isManagerExists();
        if(!isManagerExists) {
            showMissingAdminAlert();
        }
        Scene scene = new Scene(new VBox(new Label("Nothing here")), 400, 400);
        stage.setScene(scene);
        stage.show();
    }

    private void showMissingAdminAlert() {
        Alert alert =new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("المحاسب الذهبي");
        alert.setHeaderText("لا يوجد حساب إداري على النظام. يجب إنشاء على الأقل حساب إداري واحد لاستخدام النظام.");
        alert.getDialogPane().getStylesheets().add("GreyFabric.css");

        ButtonType ok = new ButtonType("موافق");
        ButtonType quit = new ButtonType("خروج", ButtonBar.ButtonData.CANCEL_CLOSE);

        alert.getButtonTypes().setAll(ok, quit);
        alert.getDialogPane().setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);

        Optional<ButtonType> result = alert.showAndWait();

        if((result.isPresent() && result.get() == quit) || result.isEmpty()) {
            System.out.println("user quit");
            System.exit(0);
        }
    }

}
