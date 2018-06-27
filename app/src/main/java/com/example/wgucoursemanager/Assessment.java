package com.example.wgucoursemanager;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import java.time.LocalDateTime;
import java.util.Date;

public class Assessment{

    private String title;
    private Boolean isObjective;
    private Boolean isPerformance;
    private Date goalDate;

    public Assessment(String title, Boolean isObjective,
                      Boolean isPerformance, Date goalDate){

        this.title = title;
        this.isObjective = isObjective;
        this.isPerformance = isPerformance;
        this.goalDate = goalDate;

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
