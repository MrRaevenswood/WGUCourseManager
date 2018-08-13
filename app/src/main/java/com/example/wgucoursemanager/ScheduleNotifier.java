package com.example.wgucoursemanager;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

public class ScheduleNotifier extends AppCompatActivity {

    private TextView alertText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule_notifier);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        alertText = findViewById(R.id.alertText);
        SharedPreferences prefs = getSharedPreferences("Alarms", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        if(getIntent().getStringExtra("notificationType").equals("course")) {

            if (getIntent().getStringExtra("startOrEnd").equals("start")) {
                alertText.setText(getIntent().getStringExtra("Title") + " course is about to start ");
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

    }

}
