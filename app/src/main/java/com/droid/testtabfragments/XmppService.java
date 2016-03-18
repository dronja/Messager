package com.droid.testtabfragments;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;


public class XmppService extends Service {
    private static final String TAG = "myXmppService";
    public static MyXMPP xmpp;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG,"Сервис видит бинд");
        return new LocalBinder<>(this);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(TAG,"Сервис разорвал бинд");
        return super.onUnbind(intent);
    }

    @Override
    public void onCreate() {
        Log.d(TAG,"Создаем сервис");
        super.onCreate();
        xmpp = MyXMPP.getInstance(XmppService.this, "dronja", "testtest12345678");
        xmpp.connect("onCreateService");
    }

    @Override
    public void onDestroy() {
        Log.d(TAG,"Уничтожаем сервис");
        xmpp.disconnect();
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_NOT_STICKY;
    }
}
