package com.example.wgucoursemanager;

import android.app.Service;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Intent;

public class scheduleNotification extends JobService {
    private static final String TAG = "NotificationAlarm";

    @Override
    public boolean onStartJob(JobParameters params) {

        Intent service = new Intent(getApplicationContext(), courseAssessmentStartEndNotifier.class );
        getApplicationContext().startService(service);
        NotificationUtil.scheduleNotification(getApplicationContext(), params.getExtras().getLong("timeTillNotification"));
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return false;
    }
}
