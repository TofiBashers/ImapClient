package com.example.tofixxx.imap_client.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.tofixxx.imap_client.MailMessage;
import com.example.tofixxx.imap_client.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by TofixXx on 16.05.2016.
 */
public class ShortMessagesListAdapter extends BaseAdapter{

    private List<MailMessage> mailMessageList = new ArrayList<>();
    private Context context;

    public ShortMessagesListAdapter(Context context){
        this.context = context;
    }

    public void addElems(List<MailMessage> list) {
        mailMessageList.addAll(list);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        if(mailMessageList != null) {
            return mailMessageList.size();
        }
        return 0;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TextView fromTextView;
        TextView messageTextView;
        if(convertView == null){
            convertView = LayoutInflater.from(context)
                    .inflate(R.layout.message_list_elem, parent, false);
            fromTextView = (TextView) convertView.findViewById(R.id.fromTextView);
            messageTextView = (TextView) convertView.findViewById(R.id.shortMessageTextView);
        }
        else{
            fromTextView = ((ViewHolder)convertView.getTag()).fromTextView;
            messageTextView = ((ViewHolder)convertView.getTag()).messageTextView;
        }
        fromTextView.setText(mailMessageList.get(position).from);
        messageTextView.setText(mailMessageList.get(position).text);
        ((ViewHolder)convertView.getTag()).position = position;
        return convertView;
    }

    public static class ViewHolder{
        int position;
        TextView fromTextView;
        TextView messageTextView;
    }
}
