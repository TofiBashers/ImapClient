package com.example.tofixxx.imap_client.activities;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

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
    }

    @Override
    public void onClick(View v) {
        userNameEditText.getText().toString();
        LoginTask task = new LoginTask((String) spinner.getSelectedItem()
                , userNameEditText.getText().toString()
                , passwEditText.getText().toString());
        task.execute();
    }

    public class LoginTask extends AsyncTask<Void, Void, Void> {

        private final String LOG_TAG = IMAPClientApplication.class.getSimpleName()
                + ": " + LoginTask.class.getSimpleName();

        private String username;
        private String passw;
        private String server;

        LoginTask(String server, String username, String passw){
            super();
            this.username = username;
            this.passw = passw;
            this.server = server;
        }

        @Override
        protected Void doInBackground(Void... params) {
            Log.d(LOG_TAG, "Login started with server: " + server
                    + " username: " + username + "password: " + passw);
            try {
                MailServerDAO dao = MailServerDAO.newInstance(server, username, passw);
                dao.moveToINBOX();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Log.d(LOG_TAG, "onPostExecute");
            startActivity(new Intent(LoginActivity.this, MailBoxActivity.class));
            finish();
        }
    }
}
