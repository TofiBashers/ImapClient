package com.example.tofixxx.imap_client.IMAP;

import android.util.Log;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by TofixXx on 15.05.2016.
 */
public class ResponseParser {

    private static final Pattern mailSenderPattern = Pattern.compile("From:\\s[./n]*? <(.+)>");
    private static final Pattern mailDatePattern = Pattern.compile("INTERNALDATE\\s\"(.+)\"");
    private static final Pattern existMessagesNumPattern = Pattern.compile("\\*\\s([0-9]+)\\sEXISTS");

    public static String getResponseCode(String resp){
        return resp.split(" ")[1];
    }

    public static String getSenderFromResponse(String response){
        return mailSenderPattern.matcher(response).group(1);
    }

    public static String getDateFromResponse(String response){
        Matcher m = mailDatePattern.matcher(response);
        if(m.find()){
            return m.group(1);
        }
        return null;
    }

    public static String getExistMessagesNumFromResponse(String response){
        Matcher m = existMessagesNumPattern.matcher(response);
        if(m.find()){
            return m.group(1);
        }
        return null;
    }

    public static boolean isLastLine(String response, String command){
        Pattern lastLinePattern = Pattern.compile("\\" + Attributes.DEFAULT_TAG + "\\s.+\\s" + command);
        return lastLinePattern.matcher(response).find();
    }


}
