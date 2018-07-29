package com.example.wgucoursemanager;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;

public class NotificationUtil {

    public static void scheduleNotification(Context context, long timeTilNotification){

        ComponentName serviceComponent = new ComponentName(context, scheduleNotification.class);
        JobInfo.Builder builder = new JobInfo.Builder(0, serviceComponent);
        builder.setMinimumLatency(timeTilNotification);
        builder.setOverrideDeadline(timeTilNotification);

        JobScheduler jobScheduler = context.getSystemService(JobScheduler.class);
        jobScheduler.schedule(builder.build());

    }

}