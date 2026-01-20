package com.moubassher.accountsfx;

import com.context.themes.vibrant_blue.MyThemes;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.io.IOException;

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

        AccountsFX<TestFeature> accountsFX = new AccountsFX<>(TestFeature.class, "AccountsFxTest");
        accountsFX.initialize();

        Scene scene = new Scene(new HBox(), 540, 440);
        scene.getStylesheets().add(MyThemes.vibrantBlue);

        stage.setScene(scene);
        stage.show();

    }



}
