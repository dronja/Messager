package com.droid.testtabfragments;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;
import com.google.gson.Gson;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.chat.Chat;
import org.jivesoftware.smack.chat.ChatManager;
import org.jivesoftware.smack.chat.ChatManagerListener;
import org.jivesoftware.smack.chat.ChatMessageListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Stanza;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jivesoftware.smackx.receipts.DeliveryReceiptManager;
import org.jivesoftware.smackx.receipts.ReceiptReceivedListener;

import java.io.IOException;

public class MyXMPP {

    String TAG = "myXMPP";

    private static final String Domain = "xmpp.jp";
    private static final String Host = "xmpp.jp";
    private static final int Port = 5222;

    private String userName = "";
    private String password = "";

    public static AbstractXMPPConnection connection;
    XMPPConnectionListener connectionListener;

    Chat myChat;
    private boolean chat_created = false;

    public static boolean connected = false;
    public boolean loggedin = false;
    public static boolean isconnecting = false;
    public static boolean isToasted = true;

    Gson gson;
    XmppService context;

    public static MyXMPP instance = null;
    public static boolean instanceCreated = false;

    ChatManagerListenerImpl mChatManagerListener;
    MMessageListener mMessageListener;

    public MyXMPP(XmppService context, String logiUser, String passwordser) {
        this.userName = logiUser;
        this.password = passwordser;
        this.context = context;
        init();

    }

    public static MyXMPP getInstance(XmppService context, String user, String pass) {

        if (instance == null) {
            instance = new MyXMPP(context, user, pass);
            instanceCreated = true;
        }
        return instance;
    }


    public void init() {
        gson = new Gson();
        mMessageListener = new MMessageListener(context);
        mChatManagerListener = new ChatManagerListenerImpl();
        initialiseConnection();
    }

    private void initialiseConnection() {
        Log.i(TAG, "Start to init");

        XMPPTCPConnectionConfiguration.Builder config = XMPPTCPConnectionConfiguration.builder();
        //config.setUsernameAndPassword(userName, password);
        config.setSecurityMode(ConnectionConfiguration.SecurityMode.disabled);
        config.setServiceName(Domain);
        config.setHost(Host);
        config.setPort(Port);
        config.setResource("Android");
        config.setDebuggerEnabled(true);

        //XMPPTCPConnection.setUseStreamManagementResumptiodDefault(true);
        //XMPPTCPConnection.setUseStreamManagementDefault(true);
        connection = new XMPPTCPConnection(config.build());
        connectionListener = new XMPPConnectionListener();
        connection.addConnectionListener(connectionListener);
    }

    public void disconnect() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.i(TAG,"Disconnect");
                connection.disconnect();
            }
        }).start();
    }

    public void connect(final String caller) {
        AsyncTask<Void, Void, Boolean> connectionThread = new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected synchronized Boolean doInBackground(Void... arg0) {
                Log.i(TAG,"Try connect");
                if (connection.isConnected())
                    return false;
                isconnecting = true;

                if (isToasted)
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(context, caller + "=>connecting....", Toast.LENGTH_LONG).show();
                        }
                    });

                try {
                    connection.connect();
                    //login();
                    DeliveryReceiptManager dm = DeliveryReceiptManager.getInstanceFor(connection);
                    dm.setAutoReceiptMode(DeliveryReceiptManager.AutoReceiptMode.always);
                    dm.addReceiptReceivedListener(new ReceiptReceivedListener() {

                        @Override
                        public void onReceiptReceived(final String fromid, final String toid, final String msgid, final Stanza packet) {
                            Log.d(TAG,"From "+fromid+" to "+toid+" message "+msgid+" stanza "+packet.toString());
                        }
                    });
                    connected = true;

                } catch (IOException e) {
                    Log.e(TAG,"connect "+e.getLocalizedMessage());
                    if (isToasted)
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(context, "(" + caller + ")" + "IOException: ", Toast.LENGTH_SHORT).show();
                            }
                        });

                } catch (SmackException e) {
                    Log.e(TAG, "connect "+e.getLocalizedMessage());
                    if(isToasted)
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(context, "(" + caller + ")" + "SMACKException: ", Toast.LENGTH_SHORT).show();
                            }
                        });
                } catch (XMPPException e) {
                    Log.e(TAG,"connect "+e.getLocalizedMessage());
                    if (isToasted)
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(context, "(" + caller + ")" + "XMPPException: ", Toast.LENGTH_SHORT).show();
                                    }
                                });
                }
                return isconnecting = false;
            }
        };
        connectionThread.execute();
    }

    public void login() {

        try {
            connection.login(userName, password);
            Log.i(TAG, "Login ok!");

        } catch (XMPPException | SmackException | IOException e) {
            Log.e(TAG, "login "+e.getLocalizedMessage());
        } catch (Exception e) {
            Log.e(TAG, "login "+e.getLocalizedMessage());
        }

    }

    public void sendMessage(ChatMessage chatMessage) {
        String body = gson.toJson(chatMessage);

        if (!chat_created) {
            myChat = ChatManager.getInstanceFor(connection).createChat(chatMessage.getReceiver() + "@" + Domain, mMessageListener);
            chat_created = true;
        }
        final Message message = new Message();
        message.setBody(body);
        message.setStanzaId(chatMessage.getID());
        message.setType(Message.Type.chat);

        try {
            if (connection.isAuthenticated()) {
                myChat.sendMessage(message);
            } else {
                login();
            }
        } catch (SmackException.NotConnectedException e) {
            Log.e(TAG, "sendMessage "+e.getLocalizedMessage());

        } catch (Exception e) {
            Log.e(TAG, "sendMessage "+e.getLocalizedMessage());
        }

    }

    public class XMPPConnectionListener implements ConnectionListener {
        @Override
        public void connected(final XMPPConnection connection) {
            Log.d(TAG, "Connected!");
            connected = true;
            if (!connection.isAuthenticated()) {
                login();
            }
        }

        @Override
        public void connectionClosed() {
            Log.d(TAG, "Connection closed!");
            if (isToasted)
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(context, "ConnectionClosed!", Toast.LENGTH_SHORT).show();
                    }
                });
            connected = false;
            chat_created = false;
            loggedin = false;
        }

        @Override
        public void connectionClosedOnError(Exception arg0) {
            Log.d(TAG, "ConnectionClosedOn Error!");
            if (isToasted)
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(context, "ConnectionClosedOn Error!!", Toast.LENGTH_SHORT).show();
                    }
                });
            connected = false;
            chat_created = false;
            loggedin = false;
        }

        @Override
        public void reconnectingIn(int arg0) {
            Log.d(TAG, "Reconnectingin " + arg0);
            loggedin = false;
        }

        @Override
        public void reconnectionFailed(Exception arg0) {
            Log.d(TAG, "ReconnectionFailed!");
            if (isToasted)
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(context, "ReconnectionFailed!", Toast.LENGTH_SHORT).show();
                    }
                });
            connected = false;

            chat_created = false;
            loggedin = false;
        }

        @Override
        public void reconnectionSuccessful() {
            Log.d(TAG, "ReconnectionSuccessful");
            if (isToasted)
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(context, "REConnected!", Toast.LENGTH_SHORT).show();
                    }
                });
            connected = true;

            chat_created = false;
            loggedin = false;
        }

        @Override
        public void authenticated(XMPPConnection arg0, boolean arg1) {
            Log.d(TAG, "Authenticated!");
            loggedin = true;
            chat_created = false;
            ChatManager.getInstanceFor(connection).addChatListener(mChatManagerListener);

            new Thread(new Runnable() {

                @Override
                public void run() {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {

                        e.printStackTrace();
                    }

                }
            }).start();
            if (isToasted)
                new Handler(Looper.getMainLooper()).post(new Runnable() {

                    @Override
                    public void run() {
                        Toast.makeText(context, "Connected!", Toast.LENGTH_SHORT).show();

                    }
                });
        }
    }
    private class ChatManagerListenerImpl implements ChatManagerListener {
        @Override
        public void chatCreated(final org.jivesoftware.smack.chat.Chat chat,
                                final boolean createdLocally) {
            if (!createdLocally)
                chat.addMessageListener(mMessageListener);

        }

    }
    private class MMessageListener implements ChatMessageListener {

        public MMessageListener(Context contxt) {
        }

        @Override
        public void processMessage(final Chat chat, final Message message) {
            Log.d(TAG, "Xmpp message received: '" + message);

            if (message.getType() == Message.Type.chat && message.getBody() != null) {
                final ChatMessage chatMessage = new ChatMessage(message.getFrom(),message.getTo(),message.getBody(),message.getStanzaId(),false);
                processMessage(chatMessage);
            }
        }

        private void processMessage(final ChatMessage chatMessage) {

            chatMessage.setMine(false);
            Chats.chatMessages.add(chatMessage);
            new Handler(Looper.getMainLooper()).post(new Runnable() {

                @Override
                public void run() {
                    Chats.chatAdapter.notifyDataSetChanged();

                }
            });
        }

    }
}
