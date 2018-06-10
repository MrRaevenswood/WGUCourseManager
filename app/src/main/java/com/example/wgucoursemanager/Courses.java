package com.example.wgucoursemanager;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import java.time.LocalDateTime;
import java.util.ArrayList;

public class Courses extends AppCompatActivity {

    private String title;
    private LocalDateTime startDate;
    private LocalDateTime anticipatedEndDate;
    private String status;
    private String mentorName;
    private String mentorEmail;
    private String mentorPhone;
    private ArrayList<String> objectiveAssessment;
    private ArrayList<String> performanceAssessment;
    private String notes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_courses);
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

    public String getCourseTitle(){
        return this.title;
    }

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public LocalDateTime getAnticipatedEndDate() {
        return anticipatedEndDate;
    }

    public String getStatus(){
        return this.status;
    }

    public String getMentorName(){
        return mentorName;
    }

    public String getMentorEmail(){
        return mentorEmail;
    }

    public String getMentorPhone(){
        return mentorPhone;
    }

    public ArrayList<String> getObjectiveAssessment() {
        return objectiveAssessment;
    }

    public ArrayList<String> getPerformanceAssessment() {
        return performanceAssessment;
    }

    public String getNotes() {
        return notes;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }

    public void setAnticipatedEndDate(LocalDateTime anticipatedEndDate) {
        this.anticipatedEndDate = anticipatedEndDate;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setMentorName(String mentorName) {
        this.mentorName = mentorName;
    }

    public void setMentorEmail(String mentorEmail) {
        this.mentorEmail = mentorEmail;
    }

    public void setMentorPhone(String mentorPhone) {
        this.mentorPhone = mentorPhone;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public void setAlert(LocalDateTime start, LocalDateTime end){

    }

    public void shareNotes(String email, String notes){

    }
}
