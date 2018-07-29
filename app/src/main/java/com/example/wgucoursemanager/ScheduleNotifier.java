package com.example.wgucoursemanager;

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

        if(getIntent().getStringExtra("notificationType").equals("course")) {

            if (getIntent().getStringExtra("startOrEnd").equals("start")) {
                alertText.setText("Course is about to start ");
            } else if (getIntent().getStringExtra("startOrEnd").equals("end")) {
                alertText.setText("Course is about to end ");
            }
        }else if(getIntent().getStringExtra("notificationType").equals("assessment")){
            alertText.setText("Assessment is about to end");
        }

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

}
