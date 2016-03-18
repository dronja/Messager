package com.droid.testtabfragments;

import android.os.Binder;

import java.lang.ref.WeakReference;


public class LocalBinder<S> extends Binder {
    private final WeakReference<S> xmppService;

    public LocalBinder(final S service){
        xmppService = new WeakReference<>(service);
    }

    public S getService(){
        return xmppService.get();
    }
}
