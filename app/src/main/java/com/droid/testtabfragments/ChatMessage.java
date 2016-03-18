package com.droid.testtabfragments;


import java.util.Random;

public class ChatMessage {

    private String sender;
    private String receiver;
    private String message;
    private String ID;
    private boolean isMine;

    public ChatMessage(String sender, String receiver, String message, String ID, boolean isMine) {

        this.sender = sender;
        this.receiver = receiver;
        this.message = message;
        this.ID = ID;
        this.isMine = isMine;

    }
    public String getMessage(){
        return this.message;
    }
    public boolean isMyMessage(){
        return this.isMine;
    }
    public String getID(){
        return this.ID;
    }
    public String getSender(){
        return this.sender;
    }
    public String getReceiver(){
        return this.receiver;
    }
    public void setMine(boolean is){
        this.isMine = is;
    }
    public String setMsgID(){
        ID += "-"+String.format("%02d", new Random().nextInt(100));
        return ID;
    }
}
