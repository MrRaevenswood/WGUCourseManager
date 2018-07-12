package com.example.wgucoursemanager;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.R.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

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

        Toolbar actionBar = findViewById(R.id.toolbar);
        actionBar.setTitle("Add/Edit Course");
        setSupportActionBar(actionBar);

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
            case R.id.cancel:
                Intent goBackToCourses = new Intent(CourseAddActivity.this, CourseActivity.class );
                startActivity(goBackToCourses);
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

        addAssessment.setPositiveButton("YES", new AlertDialog.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                openAssessmentDialog();
            }
        });

        addAssessment.setNegativeButton("NO", new AlertDialog.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                        Courses newCourse = new Courses(courseTitle.getText().toString(), courseStart.getText().toString(),
                                courseEnd.getText().toString(), status.getText().toString(), mentorName.getText().toString(),
                                mentorEmail.getText().toString(), mentorPhone.getText().toString(), notes.getText().toString(),
                                courseStart.getText().toString() + " - " + courseEnd.getText().toString());
                        saveCourse(newCourse);
            }


        });

        addAssessment.create().show();
    }

    public void showPopup(View v, ArrayList<String> performance,
                          ArrayList<String> objective){
        PopupMenu popup = new PopupMenu(this, v);
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {

                item.setShowAsAction(MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
                item.setActionView(new View(getApplicationContext()));

                return false;
            }
        });
        popup.setOnDismissListener(new PopupMenu.OnDismissListener() {
            @Override
            public void onDismiss(PopupMenu menu) {
                int counter = 0;
                ArrayList<String> assessmentContainer = new ArrayList<>();
                ArrayList<Integer> assessmentKeys = new ArrayList<>();
                while(counter < menu.getMenu().size()){
                    MenuItem currentItem = menu.getMenu().getItem(counter);
                    if(currentItem.isChecked()){
                        assessmentContainer.add(currentItem.getTitle().toString());
                    }
                    counter += 1;
                }
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
        for(String s : performance){
            popup.getMenu().add(R.id.performanceAssessments, popup.getMenu().size(),0, s);
            popup.getMenu().getItem(popup.getMenu().size() - 1).setCheckable(true);
        }
        for(String s : objective){
            popup.getMenu().add(R.id.objectiveAssessments, popup.getMenu().size(), 0, s);
            popup.getMenu().getItem(popup.getMenu().size() - 1).setCheckable(true);
        }

        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.course_add_assessments,popup.getMenu());

        popup.show();
    }

    private void openAssessmentDialog() {

        //AlertDialog.Builder populateAssessments = new AlertDialog.Builder(this);
        //populateAssessments.setTitle("Choose your one objective or performance of Both");

        //ArrayAdapter<String> objectiveAssessmentTitles = new ArrayAdapter<>(populateAssessments.getContext(), R.layout.custom_dropdown_dialog);
        //ArrayAdapter<String> performanceAssessmentTitles = new ArrayAdapter<>(populateAssessments.getContext(), R.layout.custom_dropdown_dialog);

        ArrayList<String> performanceAssessmentTitles = new ArrayList<>();
        ArrayList<String> objectiveAssessmentTitles = new ArrayList<>();

        Cursor assignmentsToPopulate = getAllCurrentAssessments();
        while(assignmentsToPopulate.moveToNext()){

            String currentTitle = assignmentsToPopulate.getString(
                    assignmentsToPopulate.getColumnIndex(DBConnHelper.ASSESSMENT_TITLE)
            );

            String isPerformance = assignmentsToPopulate.getString(
                    assignmentsToPopulate.getColumnIndex(DBConnHelper.ASSESSMENT_ISPERFORMANCE)).
                    toLowerCase();

            if(isPerformance.equals("1") && !currentTitle.isEmpty()){

                performanceAssessmentTitles.add(currentTitle);

            }else if(isPerformance.equals("0") && !currentTitle.isEmpty()){

                objectiveAssessmentTitles.add(currentTitle);
            }

        }

        showPopup(this.getCurrentFocus(),performanceAssessmentTitles, objectiveAssessmentTitles);
        /*
        populateAssessments.setView(LayoutInflater.from(getApplicationContext()).inflate(R.layout.custom_dropdown_dialog, null));
        performanceAssessmentTitles.setDropDownViewResource(R.layout.custom_dropdown_dialog);
        objectiveAssessmentTitles.setDropDownViewResource(R.layout.custom_dropdown_dialog);

        final Spinner objectiveSpinner = findViewById(R.id.objectiveSpinner);

        if(!objectiveAssessmentTitles.isEmpty()){
            objectiveSpinner.setAdapter(objectiveAssessmentTitles);
        }
        final Spinner performanceSpinner = findViewById(R.id.performanceSpinner);
            if(!objectiveAssessmentTitles.isEmpty()){
            performanceSpinner.setAdapter(performanceAssessmentTitles);
        }


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

        populateAssessments.create().show(); */

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
            month += 1;
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


