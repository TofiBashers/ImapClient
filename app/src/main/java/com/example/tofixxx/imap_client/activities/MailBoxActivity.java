package com.example.tofixxx.imap_client.activities;

import android.app.ListActivity;
import android.app.LoaderManager;
import android.content.AsyncTaskLoader;
import android.content.Context;
import android.content.Loader;
import android.os.Bundle;
import android.util.Log;

import com.example.tofixxx.imap_client.IMAP.IMAPResponse;
import com.example.tofixxx.imap_client.IMAP.ResponseParser;
import com.example.tofixxx.imap_client.IMAPClientApplication;
import com.example.tofixxx.imap_client.DAO.MailServerDAO;
import com.example.tofixxx.imap_client.MailMessage;
import com.example.tofixxx.imap_client.adapters.ShortMessagesListAdapter;

import java.io.IOException;
import java.util.List;

public class MailBoxActivity extends ListActivity implements LoaderManager.LoaderCallbacks{

    private static final String LOG_TAG = IMAPClientApplication.class.getSimpleName()
            + ": " + MailBoxActivity.class.getSimpleName();

    private ShortMessagesListAdapter adapter;
    private int numberOfMessages;
    MailServerDAO dao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(LOG_TAG, "onCreate");
        dao = MailServerDAO.getInstance();
        adapter = new ShortMessagesListAdapter(this);
        getListView().setAdapter(adapter);
        getLoaderManager().initLoader(MessageExistsNumLoader.ID, null, this).forceLoad();
    }

    @Override
    public Loader onCreateLoader(int id, Bundle args) {
        Log.d(LOG_TAG, "loader created, id: " + String.valueOf(id));
        switch (id){
            case MessageExistsNumLoader.ID:
                return new MessageExistsNumLoader(this
                        , dao);
            case MessageListLoader.ID:
                return new MessageListLoader(this, args.getInt("offset")
                        , numberOfMessages, dao);
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader loader, Object data) {
        switch (loader.getId()){
            case MessageExistsNumLoader.ID:
                numberOfMessages = (Integer)data;
                Bundle bundle = new Bundle();
                bundle.putInt("offset", adapter.getCount());
                getLoaderManager().initLoader(MessageListLoader.ID
                        , bundle, this).forceLoad();
            case MessageListLoader.ID:
                List<MailMessage> mailMessageList = (List<MailMessage>)data;
                adapter.addElems(mailMessageList);
        }
    }

    @Override
    public void onLoaderReset(Loader loader) {

    }

    public static class MessageExistsNumLoader extends AsyncTaskLoader<Integer>{
        private static final String LOG_TAG = IMAPClientApplication.class.getSimpleName()
                + ": " + MessageExistsNumLoader.class.getSimpleName();

        public static final int ID = 1;

        MailServerDAO mailServerDAO;

        public MessageExistsNumLoader(Context context, MailServerDAO mailServerDAO) {
            super(context);
            this.mailServerDAO = mailServerDAO;
        }

        @Override
        public Integer loadInBackground() {
            Log.d(LOG_TAG, "start inbox loading");
            try {
                IMAPResponse response = mailServerDAO.moveToINBOX();
                String str = ResponseParser.getExistMessagesNumFromResponse(response.msg);
                return Integer.parseInt(str);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    public static class MessageListLoader extends AsyncTaskLoader<List<MailMessage>> {
        private static final String LOG_TAG = IMAPClientApplication.class.getSimpleName()
                + ": " + MessageListLoader.class.getSimpleName();

        public static final int ID = 2;

        MailServerDAO mailServerDAO;
        int offset;
        int numberOfMessages;

        public MessageListLoader(Context context, int offset
                , int numberOfMessages, MailServerDAO mailServerDAO) {
            super(context);
            this.mailServerDAO = mailServerDAO;
            this.offset = offset;
            this.numberOfMessages = numberOfMessages;
        }

        @Override
        public List<MailMessage> loadInBackground() {
            Log.d(LOG_TAG, "message list loading starts");
            List<MailMessage> mailMessageList = null;
            try {
                mailMessageList =  mailServerDAO.getAllMessages(offset, numberOfMessages);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return mailMessageList;
        }
    }

}
