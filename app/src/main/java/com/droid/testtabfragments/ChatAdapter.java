package com.droid.testtabfragments;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;


public class ChatAdapter extends BaseAdapter {
    String TAG = "myChatAdapter";
    ArrayList<ChatMessage> chatMessages;
    private static LayoutInflater inflater = null;

    public ChatAdapter(Activity activity, ArrayList<ChatMessage> chatMessages){
        this.chatMessages = chatMessages;
        inflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        try {
            return chatMessages.size();
        }catch (NullPointerException e){
            Log.e(TAG, e.getLocalizedMessage());
            return 0;
        }
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ChatMessage chatMessage = chatMessages.get(position);
        View view = convertView;
        if(convertView == null){
            view = inflater.inflate(R.layout.chatbubble, null);
        }

        //отображаем сообщение
        TextView msg = (TextView)view.findViewById(R.id.message_text);
        msg.setText(chatMessage.getMessage());
        msg.setTextColor(Color.BLACK);

        //меняем вид в зависимости от сообщения(слева, справа)
        LinearLayout linearLayout = (LinearLayout)view.findViewById(R.id.bubble_layout);
        LinearLayout parent_layout = (LinearLayout)view.findViewById(R.id.bubble_layout_parent);

        if(chatMessage.isMyMessage()){
            linearLayout.setBackgroundResource(R.drawable.bubble2);
            parent_layout.setGravity(Gravity.RIGHT);
        }else{
            linearLayout.setBackgroundResource(R.drawable.bubble1);
            parent_layout.setGravity(Gravity.LEFT);
        }
        return view;
    }

    public void add(ChatMessage message){
        chatMessages.add(message);
    }
}
