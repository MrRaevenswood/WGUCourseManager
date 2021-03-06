package com.example.wgucoursemanager;

import java.util.Date;

public class Assessment{

    private String title;
    private Boolean isObjective;
    private Boolean isPerformance;
    private String goalDate;

    public Assessment(String title, Boolean isObjective,
                      Boolean isPerformance, String goalDate){

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

    public String getGoalDate() {
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

    public void setGoalDate(String goalDate){
        this.goalDate = goalDate;
    }

    public void setAlert(Date goalDate){

    }
}
