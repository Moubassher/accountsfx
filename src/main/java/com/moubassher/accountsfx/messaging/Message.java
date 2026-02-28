package com.moubassher.accountsfx.messaging;

import java.time.LocalDate;

public class Message {

    private String title, text, sender;
    private LocalDate date;


    public Message(String title, String text, String sender, LocalDate date) {
        this.title = title;
        this.text = text;
        this.sender = sender;
        this.date = date;
    }

    public String getTitle() {
        return title;
    }

    public String getText() {
        return text;
    }

    public String getSender() {
        return sender;
    }

    public LocalDate getDate() {
        return date;
    }
}
