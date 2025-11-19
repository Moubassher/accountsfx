package com.moubassher.accountsfx.accessControl;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;

import java.net.URL;
import java.util.ResourceBundle;

public class MessageCtrl implements Initializable {

    @FXML
    private Label  titleDisplay, dateDisplay, messageDisplay, msgCounterDisplay, senderDisplay;

    private Message message;

    public MessageCtrl(Message message) {
        this.message = message;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        String title = message.getTitle();
        String text = message.getText();
        String sender = message.getSender();
        String date = message.getDate().toString();

        titleDisplay.setText(title);
        messageDisplay.setText(text);
        senderDisplay.setText(sender);;
        dateDisplay.setText(date);

    }
}
