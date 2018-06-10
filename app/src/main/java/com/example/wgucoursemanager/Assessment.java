package com.example.wgucoursemanager;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import java.time.LocalDateTime;

public class Assessment extends AppCompatActivity {

    private String title;
    private Boolean isObjective;
    private Boolean isPerformance;
    private LocalDateTime goalDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assessment);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    public String getAssessmentTitle(){
        return title;
    }

    public Boolean getIsObjective(){
        return isObjective;
    }

    public Boolean getIsPerformance(){
        return isPerformance;
    }

    public LocalDateTime getGoalDate() {
        return goalDate;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setIsObjective(Boolean isObjective){
        this.isObjective = isObjective;
    }

    public void setIsPerformance(Boolean isPerformance){
        this.isPerformance = isPerformance;
    }

    public void setGoalDate(LocalDateTime goalDate){
        this.goalDate = goalDate;
    }

    public void setAlert(LocalDateTime goalDate){

    }
}
