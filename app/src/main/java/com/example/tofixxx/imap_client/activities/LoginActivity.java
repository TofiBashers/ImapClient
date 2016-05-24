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
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.example.tofixxx.imap_client.IMAP.IMAPResponse;
import com.example.tofixxx.imap_client.IMAPClientApplication;
import com.example.tofixxx.imap_client.DAO.MailServerDAO;
import com.example.tofixxx.imap_client.R;

import java.io.IOException;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener{

    private static final String LOG_TAG = IMAPClientApplication.class.getSimpleName()
            + ": " + LoginActivity.class.getSimpleName();

    Spinner spinner;
    EditText userNameEditText;
    EditText passwEditText;

    MailServerDAO dao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        spinner = (Spinner) findViewById(R.id.choiceServer);
        spinner.setSelection(1);
        userNameEditText = (EditText) findViewById(R.id.usernameEditText);
        passwEditText = (EditText) findViewById(R.id.passwEditText);
        Button signIn = (Button) findViewById(R.id.sign_in);
        signIn.setOnClickListener(this);
        registerReceiver(IMAPReceiver, new IntentFilter("response"));
        startService(new Intent(this, MailServerDAO.class));
        bindService(new Intent(this, MailServerDAO.class), sConn, 0);
    }

    ServiceConnection sConn = new ServiceConnection() {
        public void onServiceConnected(ComponentName name, IBinder binder) {
            Log.d(LOG_TAG, "MailBoxActivity onServiceConnected");
            dao = ((MailServerDAO.MyBinder)binder).getService();
        }

        public void onServiceDisconnected(ComponentName name) {
            Log.d(LOG_TAG, "MailBoxActivity onServiceDisconnected");
        }
    };

    @Override
    public void onClick(View v) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    dao.connectAndLogin((String) spinner.getSelectedItem()
                            , userNameEditText.getText().toString()
                            , passwEditText.getText().toString());

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public BroadcastReceiver IMAPReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            IMAPResponse response = (IMAPResponse) intent.getSerializableExtra("message");
            Log.d(LOG_TAG, "get response: " + response.msg);
            startActivity(new Intent(LoginActivity.this, MailBoxActivity.class));
            unregisterReceiver(IMAPReceiver);
            finish();
        }
    };
}
