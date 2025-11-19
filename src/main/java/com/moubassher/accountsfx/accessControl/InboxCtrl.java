package com.moubassher.accountsfx.accessControl;

import com.context.our_utilities.MyNotifications;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;

public class InboxCtrl implements Initializable {

    @FXML
    private Button inboxBtn, sentMessagesBtn;

    @FXML
    private VBox conversationsPane;
    @Override
    public void initialize(URL location, ResourceBundle resources) {

        for(int i = 0; i < 25; i++) {
            Message message = new Message("تخفيض 50%",
                    "تخفيضات على كل المضادات الحيوية في شهر مارس"
                    , "Alshifa Pharmaceuticals", LocalDate.now());

            MessageCtrl messageCtrl = new MessageCtrl(message);
            FXMLLoader fxmlLoader = new FXMLLoader(this.getClass().getResource("message_pane.fxml"));
            fxmlLoader.setController(messageCtrl);
            Pane messagePane = null;
            try {
                messagePane = fxmlLoader.load();
            } catch (IOException e) {
                MyNotifications.getInstance().showExceptionAlert(e);
            }
            conversationsPane.getChildren().add(messagePane);

        }


    }
}
