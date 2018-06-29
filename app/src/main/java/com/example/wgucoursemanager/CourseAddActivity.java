package com.example.wgucoursemanager;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class CourseAddActivity extends AppCompatActivity{

    private static TextView courseTitle;
    private static TextView courseStart;
    private static TextView courseEnd;
    private static TextView status;
    private static TextView mentorName;
    private static TextView mentorEmail;
    private static TextView mentorPhone;
    private static ListView objectiveAssessments;
    private static ListView performanceAssessements;
    private static TextView notes;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_add);

        courseTitle = findViewById(R.id.courseTitle);
        courseStart = findViewById(R.id.courseStartDate);
        courseEnd = findViewById(R.id.courseEndDate);
        status = findViewById(R.id.status);
        mentorName = findViewById(R.id.mentorName);
        mentorEmail = findViewById(R.id.mentorEmail);
        mentorPhone = findViewById(R.id.mentorPhone);
        objectiveAssessments = findViewById(R.id.objectiveAssessments);
        performanceAssessements = findViewById(R.id.performanceAssessments);
        notes = findViewById(R.id.notes);

        Toolbar toolbar = findViewById(R.id.addCourseToolBar);
        toolbar.setTitle("Add/Edit Course");
        setSupportActionBar(toolbar);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.newmenu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case R.id.save:
                try {
                    saveCourse(buildCourse());
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                break;

        }
    }

    private void saveCourse(Courses newCourse) {

        ContentValues values = new ContentValues();

        values.put(DBConnHelper.COURSE_TITLE, newCourse.getCourseTitle());
        values.put(DBConnHelper.COURSE_START, newCourse.getStartDate());
        values.put(DBConnHelper.COURSE_END, newCourse.getAnticipatedEndDate());
        values.put(DBConnHelper.COURSE_STATUS, newCourse.getStatus());
        values.put(DBConnHelper.COURSE_MENTOR_NAME, newCourse.getMentorName());
        values.put(DBConnHelper.COURSE_MENTOR_EMAIL, newCourse.getMentorEmail());
        values.put(DBConnHelper.COURSE_MENTOR_PHONE, newCourse.getMentorPhone());
        values.put(DBConnHelper.COURSE_NOTES, newCourse.getNotes());


        if(!newCourse.getObjectiveAssessment().isEmpty()
                && newCourse.getPerformanceAssessment().isEmpty()){

            for(Assessment A : newCourse.getObjectiveAssessment()){

                values.put(DBConnHelper.FK_Assessment_ID,getAssessmentKey(A.getAssessmentTitle()));

            }

        }else if(newCourse.getObjectiveAssessment().isEmpty()
                && !newCourse.getPerformanceAssessment().isEmpty()){

            for(Assessment A : newCourse.getPerformanceAssessment()){
                values.put(DBConnHelper.FK_Assessment_ID, getAssessmentKey(A.getAssessmentTitle()));
            }

        }

        getContentResolver().insert(Uri.parse(WGUProvider.CONTENT_URI + "/" +
                WGUProvider.COURSE_ID), values);


    }

    private Integer getAssessmentKey(String assessmentTitle) {

        Cursor assessmentKey = getContentResolver().query(Uri.parse(WGUProvider.CONTENT_URI + "/" + WGUProvider.ASSESSMENTS_ID),
                new String[]{DBConnHelper.PK_Assessment_ID}, "Where " + DBConnHelper.ASSESSMENT_TITLE + "= " + assessmentTitle,
                null, null);
        assessmentKey.moveToFirst();

        return assessmentKey.getInt(0);
    }

    private Courses buildCourse() throws ParseException {

        Assessment selectedAssessment;

        String title = courseTitle.getText().toString();
        String start = courseStart.getText().toString();
        String end = courseEnd.getText().toString();
        String statusString = status.getSelectedItem().toString();
        String mentorN = mentorName.getText().toString();
        String mentorE = mentorEmail.getText().toString();
        String mentorP = mentorPhone.getText().toString();
        ArrayList<String> selectedObjectiveAssessment = new ArrayList<>();
            selectedObjectiveAssessment.add(objectiveAssessments.getSelectedItem().toString());
        ArrayList<String> selectedPerformanceAssessments = new ArrayList<>();
            selectedPerformanceAssessments.add(performanceAssessements.getSelectedItem().toString());
        String notesTaken = notes.getText().toString();
        ArrayList<Assessment> assessmentsContainer = new ArrayList<>();


        if(!selectedObjectiveAssessment.isEmpty()){
            selectedAssessment = getAssessmentFromSelected(
                    objectiveAssessments.getSelectedItemPosition(), true);

            assessmentsContainer.add(selectedAssessment);
        }else{

            selectedAssessment = getAssessmentFromSelected(
                    performanceAssessements.getSelectedItemPosition(), false);

            assessmentsContainer.add(selectedAssessment);
        }

        if(assessmentsContainer.isEmpty()){
            return new Courses(title, start, end, statusString, mentorN, mentorE, mentorP,
                    notesTaken, start + " - " + end);
        }else{
            return new Courses(title, start, end, statusString, mentorN, mentorE, mentorP,
                    notesTaken, start + " - " + end, assessmentsContainer);
        }


    }

    private Assessment getAssessmentFromSelected(long position, Boolean isObjective) throws ParseException {

        Cursor allCurrentAssessments;

        if(isObjective){
           allCurrentAssessments  = getContentResolver().query(Uri.parse(WGUProvider.CONTENT_URI + "/" + WGUProvider.ASSESSMENTS_ID),
                    null, "Where isObjective = True", null, null);
        }else{
           allCurrentAssessments  = getContentResolver().query(Uri.parse(WGUProvider.CONTENT_URI + "/" + WGUProvider.ASSESSMENTS_ID),
                   null, "Where isPerformance = True", null, null);
        }


        allCurrentAssessments.moveToFirst();

        while(position > 0){
            allCurrentAssessments.moveToNext();
            position -= 1;
        }

        String selectedTitle = allCurrentAssessments.getString(
                allCurrentAssessments.getColumnIndex(DBConnHelper.ASSESSMENT_TITLE));

        Boolean selectedIsObjective = Boolean.parseBoolean(allCurrentAssessments.getString(
                allCurrentAssessments.getColumnIndex(DBConnHelper.ASSESSMENT_ISOBJECTIVE)));

        Boolean selectedIsPerformance = Boolean.parseBoolean(allCurrentAssessments.getString(
                allCurrentAssessments.getColumnIndex(DBConnHelper.ASSESSMENT_ISPERFORMANCE)));

        String selectedGoalDate = allCurrentAssessments.getString(
                allCurrentAssessments.getColumnIndex(DBConnHelper.ASSESSMENT_GOAL_DATE));

        SimpleDateFormat df = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy");
        Date newselectedGoalDate = df.parse(selectedGoalDate);


        return new Assessment(selectedTitle, selectedIsObjective,
                selectedIsPerformance, newselectedGoalDate);

    }

    public void showStartDatePickerDialog(View view){
        Bundle startBundle = new Bundle();
        startBundle.putString("startDate","startDate");
        DatePicker startDatePicker = new DatePicker();
        startDatePicker.setArguments(startBundle);

        startDatePicker.show(getFragmentManager(), "datePicker");
    }

    public void showEndDatePickerDialog(View view) {
        Bundle endBundle = new Bundle();
        endBundle.putString("endDate", "endDate");

        DatePicker endDatePicker = new DatePicker();
        endDatePicker.setArguments(endBundle);

        endDatePicker.show(getFragmentManager(), "datePicker");
    }

    public static class DatePicker extends DialogFragment
            implements DatePickerDialog.OnDateSetListener {

        public int year;
        public int month;
        public int day;

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current date as the default date in the picker
            final Calendar c = Calendar.getInstance();
            year = c.get(Calendar.YEAR);
            month = c.get(Calendar.MONTH);
            day = c.get(Calendar.DAY_OF_MONTH);

            // Create a new instance of DatePickerDialog and return it
            return new DatePickerDialog(getActivity(), this, year, month, day);
        }

        @Override
        public void onDateSet(android.widget.DatePicker view, int year, int month, int dayOfMonth) {

            if(this.getArguments().containsKey("startDate")){
                courseStart.setText(String.valueOf(month) + "/" + String.valueOf(dayOfMonth) + "/"
                        + String.valueOf(year));
            }else if(this.getArguments().containsKey("endDate")){
                courseEnd.setText(String.valueOf(month) + "/" + String.valueOf(dayOfMonth) + "/"
                        + String.valueOf(year));
            };

        }

    }

}

}
