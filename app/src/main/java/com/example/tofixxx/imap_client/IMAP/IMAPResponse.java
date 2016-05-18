package com.example.tofixxx.imap_client.IMAP;

/**
 * Created by TofixXx on 16.05.2016.
 */
public class IMAPResponse {
    public String msg;
    public String code;

    public IMAPResponse(String msg, String lastLine){
        this.msg = msg;
        this.code = ResponseParser.getResponseCode(lastLine);
    }
}
