package com.example.tofixxx.imap_client.IMAP;

import com.example.tofixxx.imap_client.IMAP.IMAPResponse;

/**
 * Created by TofixXx on 21.05.2016.
 */
public interface IMAPResponseListener {
    void onResponseReceived(IMAPResponse response);
}
