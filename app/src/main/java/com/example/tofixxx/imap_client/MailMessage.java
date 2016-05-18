package com.example.tofixxx.imap_client;

/**
 * Created by TofixXx on 16.05.2016.
 */
public class MailMessage {
    public String from;
    public String date;
    public String text;

    public MailMessage(String date, String from, String text) {
        this.date = date;
        this.from = from;
        this.text = text;
    }
}
