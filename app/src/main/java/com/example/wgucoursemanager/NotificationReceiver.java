package com.example.wgucoursemanager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class NotificationReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        NotificationUtil.scheduleNotification(context, intent.getLongExtra("timeTilNotification", 30));
        
    }
}
