package com.example.tofixxx.imap_client.DAO;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.example.tofixxx.imap_client.IMAP.Attributes;
import com.example.tofixxx.imap_client.IMAP.IMAPResponse;
import com.example.tofixxx.imap_client.IMAP.IMAPResponseListener;
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
public class MailServerDAO extends Service implements IMAPResponseListener{

    private static final String LOG_TAG = IMAPClientApplication.class.getSimpleName()
            + ": " + MailServerDAO.class.getSimpleName();

    private static final int PORT_NUMBER = 993;

    private IMAPSession imapSession;

    public IMAPSession getIMAPSession(){
        return imapSession;
    }

    public void connectAndLogin(String serverName, String username, String passw) throws IOException {
        Log.d(LOG_TAG, "connect to: " + serverName + " " + username + " " + passw);
        imapSession = new IMAPSession();
        Log.d(LOG_TAG, (InetAddress.getByName(serverName)).toString());
        Log.d(LOG_TAG, (InetAddress.getByName(serverName)).toString());
        imapSession.connectTo(PORT_NUMBER, InetAddress.getByName(serverName), this);
        imapSession.login(username, passw);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(LOG_TAG, "onBind");
        return new MyBinder();
    }

    @Override
    public void onResponseReceived(IMAPResponse response) {
        sendBroadcast(new Intent("response")
                        .putExtra("message", response));
    }

    public class MyBinder extends Binder{
        public MailServerDAO getService(){
            return MailServerDAO.this;
        }
    }
}
