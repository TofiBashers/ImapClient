package com.example.tofixxx.imap_client.IMAP;

import java.io.Serializable;

/**
 * Created by TofixXx on 16.05.2016.
 */
public class IMAPResponse implements Serializable{
    public String msg;
    public String code;

    public IMAPResponse(String msg, String lastLine){
        this.msg = msg;
        this.code = ResponseParser.getResponseCode(lastLine);
    }
}
