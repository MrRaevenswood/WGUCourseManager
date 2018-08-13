package com.example.wgucoursemanager;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;

import java.util.Timer;
import java.util.TimerTask;

public class NotificationReceiver extends BroadcastReceiver {


    @Override
    public void onReceive(final Context context, final Intent intent) {

        SharedPreferences prefs = getSharedPreferences("Alarms", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        if(intent.getStringExtra("notificationType").equals("course")) {

            if (intent.getStringExtra("startOrEnd").equals("start")) {
                alertText.setText(intent.getStringExtra("Title") + " course is about to start ");
                editor.remove(getIntent().getStringExtra("Title"));
            } else if (getIntent().getStringExtra("startOrEnd").equals("end")) {
                alertText.setText(getIntent().getStringExtra("Title") +" course is about to end ");
                editor.remove(getIntent().getStringExtra("Title"));
            }
        }else if(getIntent().getStringExtra("notificationType").equals("assessment")){
            alertText.setText(getIntent().getStringExtra("Title") + " assessment is about to end");
            editor.remove(getIntent().getStringExtra("Title"));
        }

        editor.commit();

                NotificationChannel channel =
                        new NotificationChannel("Channel" + System.currentTimeMillis(),intent.getStringExtra("notify"), NotificationManager.IMPORTANCE_HIGH);

                NotificationManager notify = context.getSystemService(NotificationManager.class);
                notify.createNotificationChannel(channel);


                PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
                NotificationCompat.Builder n = new NotificationCompat.Builder(context, channel.getId())
                        .setContentTitle("Scheduled Notification")
                        .setContentText(intent.getStringExtra("title"))
                        .setSmallIcon(R.drawable.ic_launcher_background)
                        .setContentIntent(pendingIntent)
                        .setAutoCancel(true);
                notify.notify((int)System.currentTimeMillis() ,n.build());

    }
}
