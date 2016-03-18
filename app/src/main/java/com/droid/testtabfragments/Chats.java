package com.droid.testtabfragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Random;


public class Chats extends Fragment implements View.OnClickListener {
    private static final String TAG = "myChats";
    private EditText messageEditText;
    private ListView msgListView;
    private ImageButton sendMessageButton;

    public static ArrayList<ChatMessage> chatMessages;
    public static ChatAdapter chatAdapter;

    String sender = "dronja";
    String receiver = "dronja1";
    

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.chat_layout,container,false);

        //нахожу виджеты
        messageEditText = (EditText)view.findViewById(R.id.messageEditText);
        msgListView = (ListView)view.findViewById(R.id.msgListView);
        sendMessageButton = (ImageButton)view.findViewById(R.id.sendMessageButton);
        sendMessageButton.setOnClickListener(this);

        msgListView.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
        msgListView.setStackFromBottom(true);

        chatMessages = new ArrayList<>();
        chatAdapter = new ChatAdapter(getActivity(),chatMessages);

        msgListView.setAdapter(chatAdapter);


        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.sendMessageButton:
                sendMessage(v);
        }
    }

    public void sendMessage(View v){
        String msg = messageEditText.getEditableText().toString();

        if(!msg.equalsIgnoreCase("")){
            final ChatMessage chatMessage = new ChatMessage(sender, receiver, msg, String.valueOf(new Random().nextInt(1000)),true);
            chatMessage.setMsgID();
            messageEditText.setText("");

            chatAdapter.add(chatMessage);
            chatAdapter.notifyDataSetChanged();

            MainActivity activity = ((MainActivity) getActivity());

            XmppService service = activity.getXmppService();
            service.xmpp.sendMessage(chatMessage);

        }

    }
}
