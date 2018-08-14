package com.example.wgucoursemanager;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.RequiresApi;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.TextView;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.widget.Toast;

public class CourseAddActivity extends AppCompatActivity{

    private static TextView courseTitle;
    private static TextView courseStart;
    private static TextView courseEnd;
    private static Spinner status;
    private static TextView mentorName;
    private static TextView mentorEmail;
    private static TextView mentorPhone;
    private static TextView notes;
    private Bundle activityBundle;
    private int courseIdToUpdate = -1;
    private int allowSaveCancel = 1;
    private courseAssessmentStartEndNotifier s;
    private String[] statusItems = {" ", "In Progress", "Completed" , "Dropped","Plan to Take"};

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

        activityBundle = getIntent().getExtras();
        if(activityBundle.get("Edit") != null){
            ArrayList<String> courseData = activityBundle.getStringArrayList("selectedCourse");
            for(int i = 0; i < statusItems.length; i++){
                if(statusItems[i].equals(courseData.get(4))){
                    status.setSelection(i);
                }
            }

            courseTitle.setText(courseData.get(1));
            courseStart.setText(courseData.get(2));
            courseEnd.setText(courseData.get(3));
            mentorName.setText(courseData.get(5));
            mentorEmail.setText(courseData.get(6));
            mentorPhone.setText(courseData.get(7));
            notes.setText(courseData.get(8));

            courseIdToUpdate = Integer.parseInt(courseData.get(0));
        }

        Toolbar actionBar = findViewById(R.id.toolbar);
        actionBar.setTitle("Add/Edit Course");
        //actionBar.setTitleTextColor(R.color.);
        setSupportActionBar(actionBar);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.newmenu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){

        AlertDialog.Builder clickAddCourse = new AlertDialog.Builder(this);
        clickAddCourse.setTitle("Please click the Add Assessment Button Instead");
        clickAddCourse.setPositiveButton("OK", new AlertDialog.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        switch (item.getItemId()){
            case R.id.save:
                if (allowSaveCancel != -1) {
                    if(checkIfCourseExists(courseTitle.getText().toString())){
                        AlertDialog.Builder duplicateTitle = new AlertDialog.Builder(this);
                        duplicateTitle.setTitle("There was an course with the same title found.");

                        duplicateTitle.setNeutralButton("Change Title", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        });
                        duplicateTitle.create().show();
                        return false;

                    }
                    if(activityBundle.get("Edit") != null){
                        promptToUpdateAssessments();
                    }else{
                        promptToAddAssessments();
                    }
                }else{
                    clickAddCourse.create().show();
                }


                break;
            case R.id.cancel:
                if (allowSaveCancel != -1) {
                    Intent goBackToCourses = new Intent(CourseAddActivity.this, CourseActivity.class );
                    startActivity(goBackToCourses);
                }else{
                    clickAddCourse.create().show();
                }

                break;
            case R.id.alarms:
                AlertDialog.Builder currentAlarms = new AlertDialog.Builder(this);
                String currentAlarmsTitle = "These are the current alarms below: \r\n ";

                SharedPreferences prefs = getSharedPreferences("Alarms", Context.MODE_PRIVATE);
                Map<String,?> allCurrentAlarms = prefs.getAll();
                Object[] listOfAlarms = allCurrentAlarms.values().toArray();

                for(int i = 0; i < listOfAlarms.length; i++){
                   currentAlarmsTitle = currentAlarmsTitle.concat(listOfAlarms[i].toString()) + " \r\n ";
                }
                currentAlarms.setTitle("Current Alarms: \r\n");
                currentAlarms.setMessage(currentAlarmsTitle);
                currentAlarms.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });

                AlertDialog aD = currentAlarms.create();
                aD.show();

                DisplayMetrics displayMetrics = new DisplayMetrics();
                getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

                int displayWidth = displayMetrics.widthPixels;
                int displayHeight = displayMetrics.heightPixels;

                WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
                layoutParams.copyFrom(aD.getWindow().getAttributes());

                int displayWindowWidth = (int) (displayWidth * 0.7f);
                int displayWindowHeight = (int) (displayHeight * 0.7f);

                layoutParams.width = displayWindowWidth;
                layoutParams.height = displayWindowHeight;

                aD.getWindow().setAttributes(layoutParams);

                break;

        }

        return false;
    }

    private boolean checkIfCourseExists(String s) {
        Cursor getAllCurrentCourses = getContentResolver().query(Uri.parse(WGUProvider.CONTENT_URI + "/" + WGUProvider.COURSE_ID),
                DBConnHelper.COURSES_ALL_COLUMNS, DBConnHelper.COURSE_TITLE + " = " + "\"" + s.trim() + "\"",
                null,null);
        if(!getAllCurrentCourses.moveToNext() || getAllCurrentCourses.getInt(getAllCurrentCourses.getColumnIndex(DBConnHelper.PK_COURSE_ID))
                == courseIdToUpdate){
            return false;
        }else{
            return true;
        }
    }

    private void promptToUpdateAssessments() {

        AlertDialog.Builder updateAssessments = new AlertDialog.Builder(this);
        updateAssessments.setTitle("Update Assessments?");

        updateAssessments.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    Courses updatedCourse = buildCourse(new ArrayList<Integer>());
                    ContentValues values = new ContentValues();

                    values.put(DBConnHelper.COURSE_TITLE, updatedCourse.getCourseTitle());
                    values.put(DBConnHelper.COURSE_START, updatedCourse.getStartDate());
                    values.put(DBConnHelper.COURSE_END, updatedCourse.getAnticipatedEndDate());
                    values.put(DBConnHelper.COURSE_MENTOR_NAME, updatedCourse.getMentorName());
                    values.put(DBConnHelper.COURSE_MENTOR_EMAIL, updatedCourse.getMentorEmail());
                    values.put(DBConnHelper.COURSE_MENTOR_PHONE, updatedCourse.getMentorPhone());
                    values.put(DBConnHelper.COURSE_NOTES, updatedCourse.getNotes());
                    values.put(DBConnHelper.COURSE_RANGE, updatedCourse.getStartDate() + " - "
                            + updatedCourse.getAnticipatedEndDate());

                    getContentResolver().update(Uri.parse(WGUProvider.CONTENT_URI + "/" + WGUProvider.COURSE_ID),
                            values, DBConnHelper.PK_COURSE_ID + " = " + courseIdToUpdate, null);

                    scheduleAlertDialog(updatedCourse);

                    setResult(RESULT_OK);
                    finish();
                } catch (ParseException e) {
                    e.printStackTrace();
                }

            }
        });

        updateAssessments.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                allowSaveCancel = -1;
                ArrayList<String> selectedAssessmentIds = new ArrayList<>();
                try {
                    ArrayList<String> selectedAssessmentTitles = getAllAssignedAssignments();
                    if(selectedAssessmentTitles != null){
                        for(String title : selectedAssessmentTitles){
                            selectedAssessmentIds.add(getAssessmentKey(title).toString());
                        }
                    }

                    ArrayList<String> selectedObjective = new ArrayList<>();
                    ArrayList<String> selectedPerformance = new ArrayList<>();

                    Cursor allAssessments = getAllCurrentAssessments();
                    allAssessments.moveToFirst();
                    do{
                        for(String id: selectedAssessmentIds){
                            String s = allAssessments.getString(allAssessments.getColumnIndex(DBConnHelper.PK_Assessment_ID));
                            if(allAssessments.getString(allAssessments.getColumnIndex(DBConnHelper.PK_Assessment_ID)).equals(id)){
                                if(allAssessments.getString(allAssessments.getColumnIndex(DBConnHelper.ASSESSMENT_ISPERFORMANCE)).toLowerCase().equals("1")){
                                    selectedPerformance.add(allAssessments.getString(allAssessments.getColumnIndex(DBConnHelper.ASSESSMENT_TITLE)));
                                }else{
                                    selectedObjective.add(allAssessments.getString(allAssessments.getColumnIndex(DBConnHelper.ASSESSMENT_TITLE)));
                                }
                            }
                        }
                    }while(allAssessments.moveToNext());

                    openAssessmentDialog(selectedObjective, selectedPerformance);
                } catch (ParseException e) {
                    e.printStackTrace();
                }


            }
        });
        updateAssessments.create().show();
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

        scheduleAlertDialog(newCourse);

        if(courseIdToUpdate == -1){
            getContentResolver().insert(Uri.parse(WGUProvider.CONTENT_URI + "/" +
                    WGUProvider.COURSE_ID), values);
        }else{
            getContentResolver().update(Uri.parse(WGUProvider.CONTENT_URI + "/" +
                    WGUProvider.COURSE_ID), values, DBConnHelper.PK_COURSE_ID +
                    " = " + courseIdToUpdate, null);
        }

        Cursor courseIdCursor = getContentResolver().query(Uri.parse(WGUProvider.CONTENT_URI + "/" + WGUProvider.COURSE_ID),
                new String[]{DBConnHelper.PK_COURSE_ID}, null, null, DBConnHelper.PK_COURSE_ID + " DESC");
        courseIdCursor.moveToFirst();
        int courseId = courseIdCursor.getInt(0);

        ContentValues assessmentValues = new ContentValues();
        assessmentValues.put(DBConnHelper.FK_COURSE_ID_ASSESSMENTS, courseId);

        if(!newCourse.getObjectiveAssessment().isEmpty()
                && newCourse.getPerformanceAssessment().isEmpty()){

            for(Assessment A : newCourse.getObjectiveAssessment()){

                assessmentValues.put(DBConnHelper.FK_ASSESSMENTS_ID_IN_COURSES,getAssessmentKey(A.getAssessmentTitle()));
                getContentResolver().insert(Uri.parse(WGUProvider.CONTENT_URI + "/" +
                        WGUProvider.ASSESSMENTS_IN_COURSES_ID), assessmentValues);
            }

        }else if(newCourse.getObjectiveAssessment().isEmpty()
                && !newCourse.getPerformanceAssessment().isEmpty()){

            for(Assessment A : newCourse.getPerformanceAssessment()){
                assessmentValues.put(DBConnHelper.FK_ASSESSMENTS_ID_IN_COURSES, getAssessmentKey(A.getAssessmentTitle()));
                getContentResolver().insert(Uri.parse(WGUProvider.CONTENT_URI + "/" +
                        WGUProvider.ASSESSMENTS_IN_COURSES_ID), assessmentValues);
            }

        }

        setResult(RESULT_OK);
        finish();

    }

    private void promptToAddAssessments() {

        AlertDialog.Builder addAssessment = new AlertDialog.Builder(this);
        addAssessment.setTitle("Add Objective/Performance Assessments?");

        addAssessment.setPositiveButton("YES", new AlertDialog.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                allowSaveCancel = -1;
                openAssessmentDialog(null, null);
            }
        });

        addAssessment.setNegativeButton("NO", new AlertDialog.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                        Courses newCourse = new Courses(courseTitle.getText().toString(), courseStart.getText().toString(),
                                courseEnd.getText().toString(), status.getSelectedItem().toString(), mentorName.getText().toString(),
                                mentorEmail.getText().toString(), mentorPhone.getText().toString(), notes.getText().toString(),
                                courseStart.getText().toString() + " - " + courseEnd.getText().toString());
                        saveCourse(newCourse);
            }


        });

        addAssessment.create().show();
    }

    public void showPopupWindow(ArrayList<String> performance,
                                ArrayList<String> objective) throws ParseException {

        ConstraintLayout to_add = findViewById(R.id.courseAdd);
        ArrayList<View> viewsInOriginalLayout = new ArrayList<>();
        ArrayList<View> viewsInNewLayout = new ArrayList<>();

        final ArrayList<String> currentlyAssignedAssessments = getAllAssignedAssignments();

        for( int i = 0; i < to_add.getChildCount(); i++){
            View v = to_add.getChildAt(i);
            if(v.getId() == R.id.toolbar){continue;}
            v.setVisibility(View.INVISIBLE);
            viewsInOriginalLayout.add(to_add.getChildAt(i));
        }
        ConstraintSet set = new ConstraintSet();

        final ArrayList<CheckBox> generatedCheckBoxIds = new ArrayList<>();

        TextView performanceTitle = new TextView(to_add.getContext()) ;
        performanceTitle.setText("Add Performance Assessments Below:");
        performanceTitle.setId(View.generateViewId());
        to_add.addView(performanceTitle);


        for(String p : performance){
            CheckBox performanceCheckBox = new CheckBox(to_add.getContext());
            performanceCheckBox.setId(View.generateViewId());
            performanceCheckBox.setText(p);
            to_add.addView(performanceCheckBox);
            if(currentlyAssignedAssessments != null){
                for(String s : currentlyAssignedAssessments){
                    if(s.equals(p)){
                        performanceCheckBox.setChecked(true);
                    }
                }
            }
            generatedCheckBoxIds.add(performanceCheckBox);
        }

        TextView objectiveTitle = new TextView(to_add.getContext());
        objectiveTitle.setText("Add Objective Assessments Below:");
        objectiveTitle.setId(View.generateViewId());
        to_add.addView(objectiveTitle);

        for(String o: objective){
            CheckBox objectiveCheckBox = new CheckBox(to_add.getContext());
            objectiveCheckBox.setId(View.generateViewId());
            objectiveCheckBox.setText(o);
            to_add.addView(objectiveCheckBox);
            if(currentlyAssignedAssessments != null){
                for(String s : currentlyAssignedAssessments){
                    if(s.equals(o)){
                        objectiveCheckBox.setChecked(true);
                    }
                }
            }
            generatedCheckBoxIds.add(objectiveCheckBox);
        }

        Button addButton = new Button(to_add.getContext());
        addButton.setId(View.generateViewId());
        addButton.setText("Add Assessments");
        addButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                ArrayList<String> assessmentContainer = new ArrayList<>();
                ArrayList<Integer> assessmentKeys = new ArrayList<>();
                for(CheckBox checkBox : generatedCheckBoxIds){
                    if(checkBox.isChecked()){
                        if(courseIdToUpdate == -1) {
                            assessmentContainer.add(checkBox.getText().toString());
                        }else{
                                int counter = 0;
                                if(currentlyAssignedAssessments != null){
                                    for(String title : currentlyAssignedAssessments) {
                                        if(title.equals(checkBox.getText().toString())){
                                            counter+= 1;
                                        }
                                    }
                                    if(counter == 0) { assessmentContainer.add(checkBox.getText().toString());}
                                }else{
                                    assessmentContainer.add(checkBox.getText().toString());
                                }

                            }
                    }else if(!checkBox.isChecked()){
                        if(courseIdToUpdate != -1 && currentlyAssignedAssessments != null){
                            int counter = 0;
                            for(String title : currentlyAssignedAssessments) {
                                if(title.equals(checkBox.getText().toString())){
                                    counter+= 1;
                                }
                            }
                            if(counter != 0){
                                getContentResolver().delete(Uri.parse(WGUProvider.CONTENT_URI + "/" + WGUProvider.ASSESSMENTS_IN_COURSES_ID),
                                        DBConnHelper.FK_ASSESSMENTS_ID_IN_COURSES + " = " + getAssessmentKey(checkBox.getText().toString())+
                                                " AND " + DBConnHelper.FK_COURSE_ID_ASSESSMENTS +  " = " + courseIdToUpdate ,null);
                            }

                        }
                    }
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

        to_add.addView(addButton);

        for( int i = 0; i < to_add.getChildCount(); i++){
            viewsInNewLayout.add(to_add.getChildAt(i));
        }

        ArrayList<View> newView = new ArrayList<>();

        for(View v : viewsInNewLayout){
            if(!viewsInOriginalLayout.contains(v)){
                newView.add(v);
            }
        }



        set.connect(newView.get(0).getId(), ConstraintSet.TOP, R.id.toolbar, ConstraintSet.BOTTOM, 200);
        //newView.get(0).setTranslationY(300);

        int move = 200;

        for(int i = 0; i < newView.size(); i++){
            if(i == 0){continue;}
            else {
                set.connect(newView.get(i - 1).getId(), ConstraintSet.TOP, newView.get(i).getId(), ConstraintSet.BOTTOM, 60);
                newView.get(i).setTranslationY(move);
                move+= 100;
            }
        }
    }



    private void openAssessmentDialog(ArrayList<String> selectedObjective, ArrayList<String> selectedPerformance) {

        ArrayList<String> performanceAssessmentTitles = new ArrayList<>();
        ArrayList<String> objectiveAssessmentTitles = new ArrayList<>();

        Cursor assignmentsToPopulate = getAllCurrentAssessments();
        assignmentsToPopulate.moveToNext();

        if(assignmentsToPopulate.getCount() != 0){
            do{

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
            }while(assignmentsToPopulate.moveToNext());
        }

        try {
            showPopupWindow(performanceAssessmentTitles, objectiveAssessmentTitles);
        } catch (ParseException e) {
            e.printStackTrace();
        }
}

    public Cursor getAllCurrentAssessments(){
        return getContentResolver().query(Uri.parse(WGUProvider.CONTENT_URI + "/" + WGUProvider.ASSESSMENTS_ID),
                new String[]{DBConnHelper.PK_Assessment_ID, DBConnHelper.ASSESSMENT_TITLE, DBConnHelper.ASSESSMENT_ISOBJECTIVE, DBConnHelper.ASSESSMENT_ISPERFORMANCE},
                null, null, null);
    }

    public ArrayList<String> getAllAssignedAssignments() throws ParseException {
        if(courseIdToUpdate == -1) {return null;}
        Cursor getAssessments = getContentResolver().query(Uri.parse(WGUProvider.CONTENT_URI + "/" + WGUProvider.ASSESSMENTS_IN_COURSES_ID),
                DBConnHelper.TABLE_ASSESSMENTS_IN_COURSES_ALL_COLUMNS, DBConnHelper.FK_COURSE_ID_ASSESSMENTS + " = " + courseIdToUpdate, null, null);
        ArrayList<String> selectedAssessments = new ArrayList<>();
        if(getAssessments == null || getAssessments.getCount() == 0){return null;}
        getAssessments.moveToFirst();
        do{
            String assessmentTitle = getAssessmentFromSelected(getAssessments.getInt(getAssessments.getColumnIndex(DBConnHelper.FK_ASSESSMENTS_ID_IN_COURSES))).getAssessmentTitle();
            if(assessmentTitle != null){
                selectedAssessments.add(assessmentTitle);
            }
        }while(getAssessments.moveToNext());
        return selectedAssessments;
    }

    private Integer getAssessmentKey(String assessmentTitle) {

        Cursor assessmentKey = getContentResolver().query(Uri.parse(WGUProvider.CONTENT_URI + "/" + WGUProvider.ASSESSMENTS_ID),
                new String[]{DBConnHelper.PK_Assessment_ID}, DBConnHelper.ASSESSMENT_TITLE + "= " +  "\"" + assessmentTitle + "\"",
                null, null);
        assessmentKey.moveToFirst();

        return assessmentKey.getInt(0);
    }

    private Courses buildCourse(ArrayList<Integer> assessments) throws ParseException {

        String title = courseTitle.getText().toString();
        String start = courseStart.getText().toString();
        String end = courseEnd.getText().toString();
        String statusString = status.getSelectedItem().toString();
        String mentorN = mentorName.getText().toString();
        String mentorE = mentorEmail.getText().toString();
        String mentorP = mentorPhone.getText().toString();
        String notesTaken = notes.getText().toString();
        ArrayList<Assessment> allCurrentAssessmentsForCourse = new ArrayList<>();

        for(int i : assessments){
            allCurrentAssessmentsForCourse.add(getAssessmentFromSelected(i));
        }


        if(assessments.isEmpty()){
            for(Integer i : assessments){
                allCurrentAssessmentsForCourse.add(getAssessmentFromSelected(i));
            }

            return new Courses(title, start, end, statusString, mentorN, mentorE, mentorP,
                    notesTaken, start + " - " + end);
        }else{
            return new Courses(title, start, end, statusString, mentorN, mentorE, mentorP,
                    notesTaken, start + " - " + end, allCurrentAssessmentsForCourse);
        }


    }
    private Assessment getAssessmentFromSelected(long position) throws ParseException {

        Cursor allCurrentAssessments = getContentResolver().query(Uri.parse(WGUProvider.CONTENT_URI + "/" + WGUProvider.ASSESSMENTS_ID),
                DBConnHelper.ASSESSMENTS_ALL_COLUMNS, DBConnHelper.PK_Assessment_ID + " = " + position,null, null);
        String selectedTitle = ""
                , selectedGoalDate = "";
        Boolean selectedIsObjective = false;
        Boolean selectedIsPerformance = false;

        allCurrentAssessments.moveToFirst();

        selectedTitle = allCurrentAssessments.getString(
                allCurrentAssessments.getColumnIndex(DBConnHelper.ASSESSMENT_TITLE));

        if(allCurrentAssessments.getString(
                allCurrentAssessments.getColumnIndex(DBConnHelper.ASSESSMENT_ISPERFORMANCE)) == "1"){
            selectedIsPerformance = true;
        }else{
            selectedIsObjective = true;
        }

        selectedGoalDate = allCurrentAssessments.getString(
                allCurrentAssessments.getColumnIndex(DBConnHelper.ASSESSMENT_GOAL_DATE));

        return new Assessment(selectedTitle, selectedIsObjective,
                selectedIsPerformance, selectedGoalDate);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void scheduleAlertDialog(Courses course){

        Calendar c = Calendar.getInstance();
        int test = c.get(Calendar.MONTH) + 1;
        String month = String.valueOf(test);
        if (month.length() != 2) {
            month = "0" + month;
        }

        String dayOfMonth = String.valueOf(c.get(Calendar.DAY_OF_MONTH));
        if(dayOfMonth.length() != 2){
            dayOfMonth = "0" + dayOfMonth;
        }

        String hour = String.valueOf(c.get(Calendar.HOUR));
        if(hour.length() != 2){
            hour = "0" + hour;
        }

        String minute = String.valueOf(c.get(Calendar.MINUTE));
        if(minute.length() != 2){
            minute = "0" + minute;
        }

        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        LocalDateTime startDate = LocalDateTime.parse(course.getStartDate(),formatter);
        LocalDateTime endDate = LocalDateTime.parse(course.getAnticipatedEndDate(), formatter);
        LocalDateTime current = LocalDateTime.parse(String.valueOf(c.get(Calendar.YEAR)) + "-" + month
                + "-" + dayOfMonth + "T" + hour + ":" + minute + ":00",formatter);

        long millisToStart = current.until(startDate, ChronoUnit.MILLIS);
        long millisToEnd = current.until(endDate, ChronoUnit.MILLIS);

        int resourcedIdStart = (int) (c.getTimeInMillis() + millisToStart);
        int resourceIdEnd = (int) (c.getTimeInMillis() + millisToEnd);

        SharedPreferences prefs = getSharedPreferences("Alarms", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

       if(millisToStart > 0){
            Intent courseStartService = new Intent(getApplicationContext(), NotificationReceiver.class);
            courseStartService.putExtra("removeAlarm", course.getCourseTitle() + "-Start");
            courseStartService.putExtra("title", "The course " + course.getCourseTitle() + " justStarted");
            courseStartService.putExtra("requestCode", resourcedIdStart);

           PendingIntent pendingIntent = PendingIntent.getBroadcast(this, resourcedIdStart, courseStartService,
                   PendingIntent.FLAG_UPDATE_CURRENT);

            editor.putString(course.getCourseTitle() + "-Start", course.getCourseTitle() + " will fire an alarm at " +
                course.getStartDate());

           AlarmManager alarmManager = (AlarmManager) getBaseContext().getSystemService(Context.ALARM_SERVICE);
           alarmManager.setExact(AlarmManager.RTC_WAKEUP, c.getTimeInMillis() + millisToStart, pendingIntent);
        }

        if(millisToEnd > 0){
            Intent courseEndService = new Intent(getApplicationContext(), NotificationReceiver.class);
            courseEndService.putExtra("removeAlarm", course.getCourseTitle() + "-END");
            courseEndService.putExtra("title", "The course " + course.getCourseTitle() + " just ended");
            courseEndService.putExtra("requestCode", resourceIdEnd);

            PendingIntent pendingIntent = PendingIntent.getBroadcast(this, resourceIdEnd, courseEndService,
                    PendingIntent.FLAG_UPDATE_CURRENT);

            editor.putString(course.getCourseTitle() + "-END", course.getCourseTitle() + " will fire an alarm at " +
                course.getAnticipatedEndDate());

            AlarmManager alarmManager = (AlarmManager) getBaseContext().getSystemService(Context.ALARM_SERVICE);
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, c.getTimeInMillis() + millisToEnd, pendingIntent);

        }

        editor.commit();
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
        public Bundle args = new Bundle();

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
            String monthString = String.valueOf(month);
            if(String.valueOf(month).length() != 2){
                monthString = "0".concat(monthString);
            }
            String dayString = String.valueOf(dayOfMonth);
            if(dayString.length() !=2){
                dayString = "0".concat(dayString);
            }
            if(this.getArguments().containsKey("startDate")){
                courseStart.setText( String.valueOf(year) + "-" + monthString + "-"
                        + dayString);
                args.putString("startDate","startDate");
            }else if(this.getArguments().containsKey("endDate")){
                courseEnd.setText(String.valueOf(year) + "-" + monthString + "-"
                        + dayString);
                args.putString("endDate", "endDate");
            }

            TimePicker startTimePicker = new TimePicker();
            startTimePicker.setArguments(args);
            startTimePicker.show(getFragmentManager(), "timePicker");
        }

    }

    public static class TimePicker extends DialogFragment
            implements TimePickerDialog.OnTimeSetListener {

        public int hour;
        public int minute;

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Calendar t = Calendar.getInstance();
            hour = t.get(Calendar.HOUR);
            minute = t.get(Calendar.MINUTE);

            return new TimePickerDialog(getActivity(), this, hour, minute, true);

        }

        @Override
        public void onTimeSet(android.widget.TimePicker view, int hourOfDay, int minute) {

            String hourString = String.valueOf(hourOfDay);
            String minuteString = String.valueOf(minute);

            if (hourString.length() != 2) {
                hourString = "0".concat(hourString);
            }

            if (minuteString.length() != 2) {
                minuteString = "0".concat(minuteString);
            }

            if (this.getArguments().containsKey("startDate")) {
                courseStart.append("T" + hourString + ":" + minuteString + ":00");
            } else if (this.getArguments().containsKey("endDate")) {
                courseEnd.append("T" + hourString + ":" + minuteString + ":00");
            }

        }
    }
}


