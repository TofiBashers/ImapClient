package com.example.tofixxx.imap_client.DAO;

import android.util.Log;

import com.example.tofixxx.imap_client.IMAP.Attributes;
import com.example.tofixxx.imap_client.IMAP.IMAPResponse;
import com.example.tofixxx.imap_client.IMAP.IMAPSession;
import com.example.tofixxx.imap_client.IMAP.ResponseParser;
import com.example.tofixxx.imap_client.IMAPClientApplication;
import com.example.tofixxx.imap_client.MailMessage;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by TofixXx on 16.05.2016.
 */
public class MailServerDAO{

    private static final String LOG_TAG = IMAPClientApplication.class.getSimpleName()
            + ": " + MailServerDAO.class.getSimpleName();

    private static final int PORT_NUMBER = 993;

    private static MailServerDAO mailServerDao;
    private IMAPSession imapSession;


    public static MailServerDAO getInstance(){
        return mailServerDao;
    }

    public static MailServerDAO newInstance(String serverName, String username, String passw) throws IOException {
        mailServerDao = new MailServerDAO(serverName, username, passw);
        return mailServerDao;
    }

    private MailServerDAO(String serverName, String username, String passw) throws IOException {
        imapSession = new IMAPSession(PORT_NUMBER, InetAddress.getByName(serverName));
        imapSession.login(username, passw);
    }

    public IMAPResponse moveToINBOX() throws IOException {
        return imapSession.examine("INBOX");
    }

    public MailMessage getMessage(int number) throws IOException {
        IMAPResponse messageDate = imapSession.fetch(number, Attributes.INTERNALDATE);
        IMAPResponse fullText = imapSession.fetch(number
                , String.format("%s[%s]", Attributes.BODY, Attributes.TEXT));
        IMAPResponse from = imapSession.fetch(number, String.format("%s[%s.%s (%s)]"
                , Attributes.BODY, Attributes.HEADER, Attributes.FIELDS, Attributes.FROM));
        MailMessage mailMessage = new MailMessage(ResponseParser.getDateFromResponse(messageDate.msg)
                , ResponseParser.getSenderFromResponse(from.msg)
                , fullText.msg);
        return mailMessage;
    }

    public List<MailMessage> getAllMessages(int offset, int num) throws IOException {
        Log.d(LOG_TAG, "getAllMessages, num: " + Integer.toString(num) + " offset: " + Integer.toString(offset));
        List<MailMessage> mailMessageList = new ArrayList<>();
        for(int i=0; i<20 && num-(offset+i) > 0; i++){
            Log.d(LOG_TAG, "getMessage, num: " + Integer.toString(num-(offset+i)));
            IMAPResponse shortText = imapSession.fetch(num - (offset + i)
                    , String.format("%s[%s]<0.10>", Attributes.BODY, Attributes.TEXT));
            IMAPResponse from = imapSession.fetch(num - (offset + i), String.format("%s[%s.%s (%s)]"
                    , Attributes.BODY, Attributes.HEADER, Attributes.FIELDS, Attributes.FROM));
            MailMessage msg = new MailMessage(null
                    , ResponseParser.getSenderFromResponse(from.msg)
                    , shortText.msg);
            mailMessageList.add(msg);
        }
        return mailMessageList;
    }
}
