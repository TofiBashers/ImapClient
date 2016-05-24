package com.example.tofixxx.imap_client.activities;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.example.tofixxx.imap_client.DAO.MailServerDAO;
import com.example.tofixxx.imap_client.IMAP.Attributes;
import com.example.tofixxx.imap_client.IMAP.IMAPResponse;
import com.example.tofixxx.imap_client.IMAP.ResponseParser;
import com.example.tofixxx.imap_client.IMAPClientApplication;
import com.example.tofixxx.imap_client.MailMessage;
import com.example.tofixxx.imap_client.R;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MessageActivity extends AppCompatActivity {
    private static final String LOG_TAG = IMAPClientApplication.class.getSimpleName()
            + ": " + MessageActivity.class.getSimpleName();

    MailServerDAO dao;
    int messageNumber;

    TextView messageTextView;
    TextView dateTextView;
    TextView senderTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);
        messageTextView = (TextView) findViewById(R.id.messageTextView);
        senderTextView = (TextView) findViewById(R.id.senderTextView);
        dateTextView = (TextView) findViewById(R.id.dateTextView);
        bindService(new Intent(this, MailServerDAO.class), sConn, 0);
        registerReceiver(IMAPReceiver, new IntentFilter("response"));
    }

    public void getMessage(int number) throws IOException {
        dao.getIMAPSession().fetch(number, String.format("%s[%s.%s (%s)]"
                , Attributes.BODY, Attributes.HEADER, Attributes.FIELDS, Attributes.FROM));
        dao.getIMAPSession().fetch(number, Attributes.INTERNALDATE);
        dao.getIMAPSession().fetch(number
                , String.format("%s[%s]", Attributes.BODY, Attributes.TEXT));
    }

    public void getMessage(){

    }

    public void getMessageSender(){

    }

    public void getMessageDate(){

    }

    ServiceConnection sConn = new ServiceConnection() {
        public void onServiceConnected(ComponentName name, IBinder binder) {
            Log.d(LOG_TAG, "MailBoxActivity onServiceConnected");
            dao = ((MailServerDAO.MyBinder) binder).getService();
            try {
                getMessage(messageNumber);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void onServiceDisconnected(ComponentName name) {
            Log.d(LOG_TAG, "MailBoxActivity onServiceDisconnected");
        }
    };

    public BroadcastReceiver IMAPReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            IMAPResponse response = (IMAPResponse) intent.getSerializableExtra("message");
            Log.d(LOG_TAG, "get response: " + response.msg);
            if(ResponseParser.getSenderFromResponse(response.msg) != null){ //сделать нормальную проверку!!
                senderTextView.setText("from: " + response.msg);
            }else if(ResponseParser.getDateFromResponse(response.msg) != null){
                dateTextView.setText("date: " + response.msg);
            }else{
                messageTextView.setText(response.msg);
            }
        }
    };

}
