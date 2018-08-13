package com.example.wgucoursemanager;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.widget.Toast;

import java.io.FileDescriptor;
import java.util.Timer;
import java.util.TimerTask;

public class courseAssessmentStartEndNotifier extends Service {
    private final IBinder mBinder = new MyBinder();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        try {
            createAlertDialog(intent.getStringExtra("notificationType"),
                    intent.getStringExtra("startOrEnd"),  intent.getLongExtra("millsTillAlarm", 10000), intent.getStringExtra("Title"));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return mBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){

        if(intent.getExtras() == null){return Service.START_STICKY;}

        try {
            if(intent.getStringExtra("notificationType").equals("course")){
                createAlertDialog(intent.getStringExtra("notificationType"),
                        intent.getStringExtra("startOrEnd"),  intent.getLongExtra("millsTillAlarm", 10000), intent.getStringExtra("Title"));
            }else{
                createAlertDialog(intent.getStringExtra("notificationType"), null, intent.getLongExtra("millsTillAlarm", 10000), intent.getStringExtra("Title"));
            }

             } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return Service.START_STICKY;
    }

    public void createAlertDialog(final String notificationType, final String startOrEnd, long timeTillNotification, final String title) throws InterruptedException {

        //Timer task = new Timer();
        String contentTitle = "";

        if(notificationType.equals("course")) {

            if (startOrEnd.equals("start")) {
                contentTitle = title + " course is about to start ";
            } else if (startOrEnd.equals("end")) {
                contentTitle = title +" course is about to end ";
            }
        }else if(notificationType.equals("assessment")){
            contentTitle = title +  " assessment is about to end";
        }


                //.setContentIntent(pendingIntent).build();


/*
        task.schedule(new TimerTask() {
            @Override
            public void run() {
                showAlert(notificationType, startOrEnd, title);
                NotificationManager notify = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                Intent notifyIntent = new Intent(courseAssessmentStartEndNotifier.this, NotificationReceiver.class);

            }
        }, timeTillNotification);*/
    }

    public void showAlert(String notificationType, String startOrEnd, String title){

    }


    public class MyBinder implements IBinder {
        courseAssessmentStartEndNotifier getService(){
            return courseAssessmentStartEndNotifier.this;
        }

        @Nullable
        @Override
        public String getInterfaceDescriptor() throws RemoteException {
            return null;
        }

        @Override
        public boolean pingBinder() {
            return false;
        }

        @Override
        public boolean isBinderAlive() {
            return false;
        }

        @Nullable
        @Override
        public IInterface queryLocalInterface(@NonNull String descriptor) {
            return null;
        }

        @Override
        public void dump(@NonNull FileDescriptor fd, @Nullable String[] args) throws RemoteException {

        }

        @Override
        public void dumpAsync(@NonNull FileDescriptor fd, @Nullable String[] args) throws RemoteException {

        }

        @Override
        public boolean transact(int code, @NonNull Parcel data, @Nullable Parcel reply, int flags) throws RemoteException {
            return false;
        }

        @Override
        public void linkToDeath(@NonNull DeathRecipient recipient, int flags) throws RemoteException {

        }

        @Override
        public boolean unlinkToDeath(@NonNull DeathRecipient recipient, int flags) {
            return false;
        }
    }
}
