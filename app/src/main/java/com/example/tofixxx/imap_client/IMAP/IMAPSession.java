package com.example.tofixxx.imap_client.IMAP;

import android.util.Log;

import com.example.tofixxx.imap_client.IMAPClientApplication;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

/**
 * Created by TofixXx on 15.05.2016.
 */
public class IMAPSession {
    private static final String LOG_TAG =  IMAPClientApplication.class.getSimpleName()
            + ": " + IMAPSession.class.getSimpleName();

    public enum State{
        initial,
        connected,
        logged_in,
        mailbox_selected
    }

    private SSLSocket sock;
    private BufferedWriter sockWriter;
    private BufferedReader sockReader;
    private State state = State.initial;

    public IMAPSession(){}

    public synchronized void connectTo(int port, InetAddress host, IMAPResponseListener listener) throws IOException {
        Log.d(LOG_TAG, "create socket: " + Integer.toString(port) + " " + host.toString());
        sock = (SSLSocket) SSLSocketFactory.getDefault().createSocket(host, port);
        sockWriter = new BufferedWriter(new OutputStreamWriter(sock.getOutputStream()));
        sockReader = new BufferedReader(new InputStreamReader(sock.getInputStream()));
        Thread thread = new Thread(new Reader(listener, sockReader));
        thread.start();
        state = State.connected;
    }

    public synchronized void login(String userName, String pass) throws IOException {
        Log.d(LOG_TAG, "login: " + userName + " " + pass);
        Log.d(LOG_TAG, "request: " + RequestBuilder.build(Commands.LOGIN, userName + " " + pass));
        send(RequestBuilder.build(Commands.LOGIN, userName + " " + pass));
        state = State.logged_in;
    }

    public synchronized void fetch(int num, String args) throws IOException {
        Log.d(LOG_TAG, "request: " + RequestBuilder.build(Commands.FETCH, Integer.toString(num) + " " + args));
        send(RequestBuilder.build(Commands.FETCH, Integer.toString(num) + " " + args));
    }

    public synchronized void fetch(int from, int to, String args) throws IOException {
        Log.d(LOG_TAG, "request: " + RequestBuilder.build(Commands.FETCH, Integer.toString(from)
                + ":" + Integer.toString(to) + " " + args));
        send(RequestBuilder.build(Commands.FETCH, Integer.toString(from)
                + ":" + Integer.toString(to) + " " + args));
    }

    public synchronized void examine(String mailBoxName) throws IOException {
        Log.d(LOG_TAG, "request: " + RequestBuilder.build(Commands.EXAMINE, mailBoxName));
        send(RequestBuilder.build(Commands.EXAMINE, mailBoxName));
        state = State.mailbox_selected;
    }

    private void send(String msg) throws IOException {
        sockWriter.write(msg);
        sockWriter.flush();
    }

    public class Reader implements Runnable{
        IMAPResponseListener listener;
        BufferedReader reader;

        Reader(IMAPResponseListener listener, BufferedReader reader){
            this.listener = listener;
            this.reader = reader;
        }

        @Override
        public void run() {
            try {
                String msg = new String();
                String currentLine;
                while((currentLine = reader.readLine()) != null){
                    Log.d(LOG_TAG, "response line: " + currentLine);
                    if(ResponseParser.isLastLine(currentLine)){
                        Log.d(LOG_TAG, "last line detected");
                        listener.onResponseReceived(new IMAPResponse(msg, currentLine));
                        msg = new String();
                    }
                    else{
                        msg += currentLine;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
