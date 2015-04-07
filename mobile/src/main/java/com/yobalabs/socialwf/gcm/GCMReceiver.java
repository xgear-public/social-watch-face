package com.yobalabs.socialwf.gcm;

import com.yobalabs.socialwf.model.Feeds;
import com.yobalabs.socialwf.model.Image;
import com.yobalabs.socialwf.model.InstagramFeed;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v4.content.WakefulBroadcastReceiver;

import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import timber.log.Timber;

public class GCMReceiver extends WakefulBroadcastReceiver {

    public GCMReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle extras = intent.getExtras();
        String message = "";
        if (extras != null) {
            for (String key : extras.keySet()) {
                message += key + "=" + extras.getString(key) + "\n";
            }
        }

        Timber.d("GCMReceiver onReceive message = %s", message);

        ComponentName comp = new ComponentName(context.getPackageName(),
                GCMService.class.getName());
        startWakefulService(context, (intent.setComponent(comp)));
        setResultCode(Activity.RESULT_OK);
    }

}
