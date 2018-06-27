package com.example.wgucoursemanager;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;

public class CourseAddActivity extends AppCompatActivity{

    private static TextView courseTitle;
    private static TextView courseStart;
    private static TextView courseEnd;
    private static Spinner status;
    private static TextView mentorName;
    private static TextView mentorEmail;
    private static TextView mentorPhone;
    private static ListView objectiveAssessments;
    private static ListView performanceAssessements;
    private static TextView notes;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_edit);

        courseTitle = findViewById(R.id.courseTitle);
        courseStart = findViewById(R.id.courseStartDate);
        courseEnd = findViewById(R.id.courseEndDate);
        status = findViewById(R.id.statusSpinner);

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
                saveCourse(buildCourse());
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

        if(!newCourse.getObjectiveAssessment().isEmpty())
            values.put()

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


        if(!selectedObjectiveAssessment.isEmpty()){
            selectedAssessment = getAssessmentFromSelected(
                    objectiveAssessments.getSelectedItemPosition(), true);

            ArrayList<Assessment> objectiveAssessmentsContainer = new ArrayList<>();
            objectiveAssessmentsContainer.add(selectedAssessment);
        }else{

            selectedAssessment = getAssessmentFromSelected(
                    objectiveAssessments.getSelectedItemPosition(), false);
            ArrayList<Assessment> performanceAssessmentContainer = new ArrayList<>();
            performanceAssessmentContainer.add(selectedAssessment);
        }

        return new Courses(title, start, end, statusString, mentorN, mentorE, mentorP,
                notesTaken, start + " - " + end);
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
}
