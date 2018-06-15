package com.example.wgucoursemanager;

import java.util.ArrayList;

public class Term {

    private String title;
    private String startDate;
    private String endDate;
    private ArrayList<Courses> courses;

    public Term (String title, String startDate,
                 String endDate, Courses course){
        this.setTitle(title);
        this.setStartDate(startDate);
        this.setEndDate(endDate);
        this.addCourse(course);
    }

    public Term (String title, String startDate,
                 String endDate){
        this.setTitle(title);
        this.setStartDate(startDate);
        this.setEndDate(endDate);
    }

    public String getTermTitle(){
        return title;
    }

    public String getStartDate(){
        return startDate;
    }

    public String getEndDate(){
        return endDate;
    }

    public ArrayList<Courses> getCourses() {
        return courses;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public void setEndDate(String endDate){
        this.endDate = endDate;
    }

    public void addCourse(Courses course){
        courses.add(course);
    }

    public void removeCourse(Courses course){
        courses.remove(course);
    }
}
