package com.example.wgucoursemanager;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private DBConnHelper appDataConn;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        appDataConn = new DBConnHelper(getApplicationContext());
        DBConnHelper db = new DBConnHelper(MainActivity.this);

        SharedPreferences prefs = getSharedPreferences("Alarms", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        Calendar c = Calendar.getInstance();
        int test = c.get(Calendar.MONTH) + 1;
        String month = String.valueOf(test);
        if (month.length() != 2) {
            month = "0" + month;
        }

        String dayOfMonth = String.valueOf(c.get(Calendar.DAY_OF_MONTH));
        if(dayOfMonth.length() != 2){
            dayOfMonth = "0" + dayOfMonth;
        }

        String hour = String.valueOf(c.get(Calendar.HOUR));
        if(hour.length() != 2){
            hour = "0" + hour;
        }

        String minute = String.valueOf(c.get(Calendar.MINUTE));
        if(minute.length() != 2){
            minute = "0" + minute;
        }

        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        LocalDateTime current = LocalDateTime.parse(String.valueOf(c.get(Calendar.YEAR)) + "-" + month
                + "-" + dayOfMonth + "T" + hour + ":" + minute + ":00",formatter);

        Object[] prefMap = prefs.getAll().values().toArray();
        Object[] prefMapKey = prefs.getAll().keySet().toArray();

        for(int i = 0; i < prefMap.length; i++){
          LocalDateTime timeInPref = null;
          timeInPref = LocalDateTime.parse(prefMap[i].toString().substring(prefMap[i].toString().length() - 19));
          if(current.isAfter(timeInPref)){
              editor.remove(prefMapKey[i].toString());
          }
        }
        editor.commit();


    }

    public void openTermsWindow(View view) {

        Intent intent = new Intent(MainActivity.this, TermsActivity.class);
        intent.setData(WGUProvider.CONTENT_URI);
        startActivity(intent);
    }

    public void openCoursesWindow(View view){
        Intent intent = new Intent(MainActivity.this, CourseActivity.class);
        intent.setData(WGUProvider.CONTENT_URI);
        startActivity(intent);
    }

    public void openAssessmentWindow(View view){
        Intent intent = new Intent(MainActivity.this,AssessmentsActivity.class);
        intent.setData(WGUProvider.CONTENT_URI);
        startActivity(intent);
    }
}
