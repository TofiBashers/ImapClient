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
    private static final String LOG_TAG = IMAPClientApplication.class.getSimpleName()
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

    public IMAPSession(int port, InetAddress host) throws IOException {
        Log.d(LOG_TAG, "create socket: " + Integer.toString(port) + " " + host.toString());
        sock = (SSLSocket) SSLSocketFactory.getDefault().createSocket(host, port);
        sockWriter = new BufferedWriter(new OutputStreamWriter(sock.getOutputStream()));
        sockReader = new BufferedReader(new InputStreamReader(sock.getInputStream()));
        state = State.connected;
    }

    public String login(String userName, String pass) throws IOException {
        Log.d(LOG_TAG, "login: " + userName + " " + pass);
        Log.d(LOG_TAG, "request: " + RequestBuilder.build(Commands.LOGIN, userName + " " + pass));
        send(RequestBuilder.build(Commands.LOGIN, userName + " " + pass));
        String lastLine = null;
        String currentLine;
        while((currentLine = sockReader.readLine()) != null){
            Log.d(LOG_TAG, "login message: " + currentLine);
            if(!ResponseParser.isLastLine(currentLine, Commands.LOGIN)){
                Log.d(LOG_TAG, "last line detected");
                lastLine = currentLine;
                break;
            }
        }
        String responseCode = ResponseParser.getResponseCode(lastLine);
        if(responseCode == ResponseCodes.CODE_OK){
            Log.d(LOG_TAG, "logging success");
            state = State.logged_in;
        }
        return responseCode;
    }

    public IMAPResponse fetch(int num, String args) throws IOException {
        Log.d(LOG_TAG, "request: " + RequestBuilder.build(Commands.FETCH, Integer.toString(num) + " " + args));
        send(RequestBuilder.build(Commands.FETCH, Integer.toString(num) + " " + args));
        String response = new String();
        String lastLine = null;
        String currentLine;
        while((currentLine = sockReader.readLine()) != null){
            Log.d(LOG_TAG, "response line: " + currentLine);
            if(ResponseParser.isLastLine(currentLine, Commands.FETCH)){
                Log.d(LOG_TAG, "last line detected");
                lastLine = currentLine;
                break;
            }
            else{
                response += currentLine;
            }
        }
        return new IMAPResponse(response, lastLine);
    }

    public IMAPResponse examine(String mailBoxName) throws IOException {
        Log.d(LOG_TAG, "request: " + RequestBuilder.build(Commands.EXAMINE, mailBoxName));
        send(RequestBuilder.build(Commands.EXAMINE, mailBoxName));
        String msg = new String();
        String lastLine = null;
        String currentLine;
        while((currentLine = sockReader.readLine()) != null){
            Log.d(LOG_TAG, "response line: " + currentLine);
            if(ResponseParser.isLastLine(currentLine, Commands.EXAMINE)){
                Log.d(LOG_TAG, "last line detected");
                lastLine = currentLine;
                break;
            }
            else{
                msg += currentLine;
            }
        }
        IMAPResponse resp = new IMAPResponse(msg, lastLine);
        if(resp.code == ResponseCodes.CODE_OK){
            Log.d(LOG_TAG, "Going to selected state");
            state = State.mailbox_selected;
        }
        return resp;
    }

    private void send(String msg) throws IOException {
        sockWriter.write(msg);
        sockWriter.flush();
    }
}
