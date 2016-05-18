package com.example.tofixxx.imap_client.IMAP;

import com.example.tofixxx.imap_client.IMAP.Attributes;

/**
 * Created by TofixXx on 15.05.2016.
 */
public class RequestBuilder {
    public static String build (String command, String attributes){
        return String.format("%s %s %s\r\n", Attributes.DEFAULT_TAG, command, attributes);
    }

    public static String build (String command){
        return String.format("%s %s\r\n", Attributes.DEFAULT_TAG, command);
    }
}
