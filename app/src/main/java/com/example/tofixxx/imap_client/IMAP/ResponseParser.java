package com.example.tofixxx.imap_client.IMAP;

import android.util.Log;

import org.w3c.dom.Attr;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by TofixXx on 15.05.2016.
 */
public class ResponseParser {

    private static final Pattern mailSenderPattern = Pattern.compile("(From:\\s.*\\s<(.+)>)|(From:\\s(.+)\\))");
    private static final Pattern mailDatePattern = Pattern.compile("INTERNALDATE\\s\"(.+)\"");
    private static final Pattern existMessagesNumPattern = Pattern.compile("\\*\\s([0-9]+)\\sEXISTS");
    private static final Pattern shortMessagePattern = Pattern.compile("\\(" + Attributes.BODY
            + "\\[" + Attributes.TEXT + "\\]" + "<[0-9]+>\\s\\{[0-9]+\\}(.+|\n+)" );
    private static final Pattern messageNumPattern = Pattern.compile("\\*\\s([0-9]+)");


    public static String getSenderFromResponse(String response){
        Matcher m = mailSenderPattern.matcher(response);
        if(m.find()){
            return m.group(2) != null ? m.group(2) : m.group(4);
        }
        return null;
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

    public static boolean isLastLine(String response){
        Pattern lastLinePattern = Pattern.compile("\\" + Attributes.DEFAULT_TAG + "\\s(OK|NO|BAD)\\s");
        return lastLinePattern.matcher(response).find();
    }

    public static String[] parseLinesFromMultipleMessage(String msg){
        return msg.split("\\*");
    }

    public static String getShortMessageFromResponse(String response){
        Matcher m = shortMessagePattern.matcher(response);
        if(m.find()){
            return m.group(1);
        }
        return null;
    }

    public static String getResponseCode(String response){
        return response.split(" ")[1];
    }

    public static String getMessageNum(String response){
        Matcher m = messageNumPattern.matcher(response);
        if(m.find()){
            return m.group(1);
        }
        return null;
    }
}
