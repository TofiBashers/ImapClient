package com.example.tofixxx.imap_client.activities;

import android.app.ListActivity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;

import com.example.tofixxx.imap_client.IMAP.Attributes;
import com.example.tofixxx.imap_client.IMAP.IMAPResponse;
import com.example.tofixxx.imap_client.IMAP.ResponseParser;
import com.example.tofixxx.imap_client.IMAPClientApplication;
import com.example.tofixxx.imap_client.DAO.MailServerDAO;
import com.example.tofixxx.imap_client.MailMessage;
import com.example.tofixxx.imap_client.adapters.ShortMessagesListAdapter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MailBoxActivity extends ListActivity implements AbsListView.OnScrollListener
        , AdapterView.OnItemClickListener {

    private static final String LOG_TAG = IMAPClientApplication.class.getSimpleName()
            + ": " + MailBoxActivity.class.getSimpleName();

    private static final int DEFAULT_MESSAGES_LOADING_NUM = 20;

    private ShortMessagesListAdapter adapter;
    private int numberOfMessages;
    MailServerDAO dao;

    ServiceConnection sConn = new ServiceConnection() {
        public void onServiceConnected(ComponentName name, IBinder binder) {
            Log.d(LOG_TAG, "MailBoxActivity onServiceConnected");
            dao = ((MailServerDAO.MyBinder)binder).getService();
            try {
                moveToINBOX();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void onServiceDisconnected(ComponentName name) {
            Log.d(LOG_TAG, "MailBoxActivity onServiceDisconnected");
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(LOG_TAG, "onCreate");
        adapter = new ShortMessagesListAdapter(this);
        getListView().setAdapter(adapter);
        registerReceiver(IMAPReceiver, new IntentFilter("response"));
        bindService(new Intent(this, MailServerDAO.class), sConn, 0);
        getListView().setOnItemClickListener(this);
    }

    public void moveToINBOX() throws IOException {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    dao.getIMAPSession().examine("INBOX");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void getMoreMessages() throws IOException {
        new Thread(new Runnable() {
            @Override
            public void run() {
                int offset;
                if(DEFAULT_MESSAGES_LOADING_NUM > numberOfMessages - adapter.getCount()){
                    offset = numberOfMessages - adapter.getCount();
                }
                else {
                    offset = DEFAULT_MESSAGES_LOADING_NUM;
                }
                Log.d(LOG_TAG, "getMoreMessages" + offset + ": " + Integer.toString(offset));
                try {
                    dao.getIMAPSession().fetch(numberOfMessages - adapter.getCount()
                            , numberOfMessages - (adapter.getCount() + offset) + 1
                            , String.format("%s[%s]<0.30>"
                            , Attributes.BODY, Attributes.TEXT));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void getMoreMessageSenders() throws IOException{
        new Thread(new Runnable() {
            @Override
            public void run() {
                int offset;
                if(DEFAULT_MESSAGES_LOADING_NUM > numberOfMessages - adapter.getCount()){
                    offset = numberOfMessages - adapter.getCount();
                }
                else {
                    offset = DEFAULT_MESSAGES_LOADING_NUM;
                }
                Log.d(LOG_TAG, "getMoreMessages" + offset + ": " + Integer.toString(offset));
                try {
                    dao.getIMAPSession().fetch(numberOfMessages - adapter.getCount()
                            , numberOfMessages - (adapter.getCount() + offset) + 1
                            , String.format("%s[%s.%s (%s)]"
                            , Attributes.BODY, Attributes.HEADER, Attributes.FIELDS, Attributes.FROM));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    int scrollState;
    public boolean isUpdate;

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        Log.d(LOG_TAG, "onScrollStateChanged: " + scrollState);
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        Log.d(LOG_TAG, "onScroll, first: " + firstVisibleItem);
        if(visibleItemCount != 0 && firstVisibleItem != 0
                && totalItemCount == firstVisibleItem + visibleItemCount
                && scrollState == SCROLL_STATE_IDLE
                && !isUpdate){
            isUpdate = true;
            try {
                getMoreMessageSenders();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public BroadcastReceiver IMAPReceiver = new BroadcastReceiver() {

        private List<MailMessage> preLoadedMailList;

        @Override
        public void onReceive(Context context, Intent intent) {
            IMAPResponse response = (IMAPResponse) intent.getSerializableExtra("message");
            Log.d(LOG_TAG, "get response: " + response.msg);
            if(ResponseParser.getExistMessagesNumFromResponse(response.msg)!= null){
                Log.d(LOG_TAG, "type: messages num");
                numberOfMessages = Integer.parseInt(ResponseParser
                        .getExistMessagesNumFromResponse(response.msg));

                try {
                    getMoreMessageSenders();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return;
            }

            String[] messageLines = ResponseParser.parseLinesFromMultipleMessage(response.msg);
            if(ResponseParser.getSenderFromResponse(messageLines[1]) != null){ //сделать нормальную проверку!!
                Log.d(LOG_TAG, "type: sender");
                preLoadedMailList = new ArrayList<>();
                for (int i = 1; i < messageLines.length; i++){
                    MailMessage mailMessage = new MailMessage(null
                            , ResponseParser.getSenderFromResponse(messageLines[i])
                            , null, null);
                    preLoadedMailList.add(mailMessage);
                }
                try {
                    getMoreMessages();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }else{
                Log.d(LOG_TAG, "type: text");
                for (int i = 1; i < messageLines.length; i++){
                    preLoadedMailList.get(i-1).text = ResponseParser.getShortMessageFromResponse(messageLines[i]);
                    preLoadedMailList.get(i-1).uid = ResponseParser.getMessageNum(messageLines[i]);
                }
                adapter.addElems(preLoadedMailList);
            }
        }
    };

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        View v = (View)getListView().getItemAtPosition(position);
        ShortMessagesListAdapter.ViewHolder viewHolder = ((ShortMessagesListAdapter.ViewHolder) v.getTag());
        Intent intent = new Intent(this, MessageActivity.class);
        intent.putExtra("uid", viewHolder.uid);
        startActivity(intent);
    }
}
