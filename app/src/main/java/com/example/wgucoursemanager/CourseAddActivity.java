package com.example.wgucoursemanager;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.R.*;
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
                    promptToAddAssessments();
                break;

        }

        return false;
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
        setResult(RESULT_OK);
        finish();

    }

    private void promptToAddAssessments() {

        AlertDialog.Builder addAssessment = new AlertDialog.Builder(this);
        addAssessment.setTitle("Add Objective/Performance Assessments?");

        final RadioButton yesOption = new RadioButton(this);
        final RadioButton noOption = new RadioButton(this);

        addAssessment.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if(yesOption.isChecked()){
                    Cursor allCurrentAssessments = getAllCurrentAssessments();
                    SimpleDateFormat df = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy");
                    if(allCurrentAssessments.moveToFirst()){
                        openAssessmentDialog();
                    }
                }

            }
        });

        addAssessment.create().show();
    }

    private void openAssessmentDialog() {

        ArrayAdapter<String> objectiveAssessmentTitles = new ArrayAdapter<>(this, R.layout.custom_dropdown_dialog);
        ArrayAdapter<String> performanceAssessmentTitles = new ArrayAdapter<>(this, R.layout.custom_dropdown_dialog );

        AlertDialog.Builder populateAssessments = new AlertDialog.Builder(this);
        populateAssessments.setTitle("Choose your one objective or performance of Both");

        Cursor assignmentsToPopulate = getAllCurrentAssessments();
        while(assignmentsToPopulate.moveToNext()){

            String currentTitle = assignmentsToPopulate.getString(
                    assignmentsToPopulate.getColumnIndex(DBConnHelper.ASSESSMENT_TITLE)
            );

            String isPerformance = assignmentsToPopulate.getString(
                    assignmentsToPopulate.getColumnIndex(DBConnHelper.ASSESSMENT_ISPERFORMANCE)).
                    toLowerCase();

            if(isPerformance.equals("true")){

                performanceAssessmentTitles.add(currentTitle);

            }else if(isPerformance.equals("false")){

                objectiveAssessmentTitles.add(currentTitle);
            }

        }

        populateAssessments.setView(LayoutInflater.from(getApplicationContext()).inflate(R.layout.custom_dropdown_dialog, null));
        final Spinner objectiveSpinner = findViewById(R.id.objectiveSpinner);
        objectiveSpinner.setAdapter(objectiveAssessmentTitles);
        final Spinner performanceSpinner = findViewById(R.id.performanceSpinner);
        performanceSpinner.setAdapter(performanceAssessmentTitles);

        populateAssessments.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                ArrayList<String> assessmentContainer = new ArrayList<>();
                ArrayList<Integer> assessmentKeys = new ArrayList<>();

                if(!(objectiveSpinner.getSelectedItem() == null))
                   assessmentContainer.add(objectiveSpinner.getSelectedItem().toString());
                if(!(performanceSpinner.getSelectedItem() == null))
                    assessmentContainer.add(performanceSpinner.getSelectedItem().toString());

                for (String assessment : assessmentContainer){
                    assessmentKeys.add(getAssessmentKey(assessment));
                }

                try {
                    saveCourse(buildCourse(assessmentKeys));
                } catch (ParseException e) {
                    e.printStackTrace();
                }

            }
        });

        populateAssessments.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {

            }
        });

        populateAssessments.create().show();

}

    public Cursor getAllCurrentAssessments(){
        return getContentResolver().query(Uri.parse(WGUProvider.CONTENT_URI + "/" + WGUProvider.ASSESSMENTS_ID),
                new String[]{DBConnHelper.PK_Assessment_ID, DBConnHelper.ASSESSMENT_TITLE, DBConnHelper.ASSESSMENT_ISOBJECTIVE, DBConnHelper.ASSESSMENT_ISPERFORMANCE},
                null, null, null);
    }

    private Integer getAssessmentKey(String assessmentTitle) {

        Cursor assessmentKey = getContentResolver().query(Uri.parse(WGUProvider.CONTENT_URI + "/" + WGUProvider.ASSESSMENTS_ID),
                new String[]{DBConnHelper.PK_Assessment_ID}, "Where " + DBConnHelper.ASSESSMENT_TITLE + "= " + assessmentTitle,
                null, null);
        assessmentKey.moveToFirst();

        return assessmentKey.getInt(0);
    }

    private Courses buildCourse(ArrayList<Integer> assessments) throws ParseException {

        String title = courseTitle.getText().toString();
        String start = courseStart.getText().toString();
        String end = courseEnd.getText().toString();
        String statusString = status.getText().toString();
        String mentorN = mentorName.getText().toString();
        String mentorE = mentorEmail.getText().toString();
        String mentorP = mentorPhone.getText().toString();
        String notesTaken = notes.getText().toString();
        ArrayList<Assessment> allCurrentAssessmentsForCourse = new ArrayList<>();

        for(Integer i : assessments){
            allCurrentAssessmentsForCourse.add(getAssessmentFromSelected(i));
        }

        if(assessments.isEmpty()){
            return new Courses(title, start, end, statusString, mentorN, mentorE, mentorP,
                    notesTaken, start + " - " + end);
        }else{
            return new Courses(title, start, end, statusString, mentorN, mentorE, mentorP,
                    notesTaken, start + " - " + end, allCurrentAssessmentsForCourse);
        }


    }
    private Assessment getAssessmentFromSelected(long position) throws ParseException {

        Cursor allCurrentAssessments;
        String selectedTitle, selectedGoalDate;
        Boolean selectedIsObjective, selectedIsPerformance;
        allCurrentAssessments  = getContentResolver().query(Uri.parse(WGUProvider.CONTENT_URI + "/" + WGUProvider.ASSESSMENTS_ID),
                   null, "Where " + DBConnHelper.PK_Assessment_ID + " = " + position, null, null);

        allCurrentAssessments.moveToFirst();

        selectedTitle = allCurrentAssessments.getString(
                allCurrentAssessments.getColumnIndex(DBConnHelper.ASSESSMENT_TITLE));

        selectedIsObjective = Boolean.parseBoolean(allCurrentAssessments.getString(
                allCurrentAssessments.getColumnIndex(DBConnHelper.ASSESSMENT_ISOBJECTIVE)));

        selectedIsPerformance = Boolean.parseBoolean(allCurrentAssessments.getString(
                allCurrentAssessments.getColumnIndex(DBConnHelper.ASSESSMENT_ISPERFORMANCE)));

        selectedGoalDate = allCurrentAssessments.getString(
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


