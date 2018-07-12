package com.example.wgucoursemanager;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.ArrayList;

public class Courses {

    private String title;
    private String startDate;
    private String anticipatedEndDate;
    private String status;
    private String mentorName;
    private String mentorEmail;
    private String mentorPhone;
    private ArrayList<Assessment> objectiveAssessment = new ArrayList<>();
    private ArrayList<Assessment> performanceAssessment = new ArrayList<>();
    private String notes;
    private String courseRange;

    public Courses(String title, String start, String end, String status,
                   String mentorName, String mentorEmail, String mentorPhone,
                   String notes, String courseRange,
                   ArrayList<Assessment> testList){
        this.title = title;
        this.startDate = start;
        this.anticipatedEndDate = end;
        this.status = status;
        this.mentorName = mentorName;
        this.mentorEmail = mentorEmail;
        this.mentorPhone = mentorPhone;
        this.notes = notes;
        this.courseRange = courseRange;

        for(Assessment test : testList){
            if(test.getIsObjective()){
                objectiveAssessment.add(test);
            }else if(test.getIsPerformance()){
                performanceAssessment.add(test);
            }
        }


    }

    public Courses(String title, String start, String end, String status,
                   String mentorName, String mentorEmail, String mentorPhone,
                   String notes, String courseRange){
        this.title = title;
        this.startDate = start;
        this.anticipatedEndDate = end;
        this.status = status;
        this.mentorName = mentorName;
        this.mentorEmail = mentorEmail;
        this.mentorPhone = mentorPhone;
        this.notes = notes;
        this.courseRange = courseRange;
    }

    public String getCourseTitle(){
        return this.title;
    }

    public String getStartDate() {
        return startDate;
    }

    public String getAnticipatedEndDate() {
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

    public ArrayList<Assessment> getObjectiveAssessment() {
        return objectiveAssessment;
    }

    public ArrayList<Assessment> getPerformanceAssessment() {
        return performanceAssessment;
    }

    public String getNotes() {
        return notes;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public void setAnticipatedEndDate(String anticipatedEndDate) {
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
