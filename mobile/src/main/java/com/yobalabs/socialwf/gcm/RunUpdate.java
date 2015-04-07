package com.yobalabs.socialwf.gcm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class RunUpdate extends BroadcastReceiver {

    public RunUpdate() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent i = new Intent(context, WearUpdateService.class);
        context.startService(i);
    }
}
