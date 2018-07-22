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
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.design.widget.AppBarLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.R.*;

import org.w3c.dom.Text;

import java.lang.reflect.Array;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Dictionary;
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
    private Bundle activityBundle;
    private int courseIdToUpdate = -1;

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

            courseTitle.setText(courseData.get(1));
            courseStart.setText(courseData.get(2));
            courseEnd.setText(courseData.get(3));
            status.setText(courseData.get(4));
            mentorName.setText(courseData.get(5));
            mentorEmail.setText(courseData.get(6));
            mentorPhone.setText(courseData.get(7));
            notes.setText(courseData.get(8));

            courseIdToUpdate = Integer.parseInt(courseData.get(0));
        }

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

                break;
            case R.id.cancel:
                Intent goBackToCourses = new Intent(CourseAddActivity.this, CourseActivity.class );
                startActivity(goBackToCourses);
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
                ContentValues values = new ContentValues();

                values.put(DBConnHelper.COURSE_TITLE, courseTitle.getText().toString());
                values.put(DBConnHelper.COURSE_START, courseStart.getText().toString());
                values.put(DBConnHelper.COURSE_END, courseEnd.getText().toString());
                values.put(DBConnHelper.COURSE_START, status.getText().toString());
                values.put(DBConnHelper.COURSE_MENTOR_NAME, mentorName.getText().toString());
                values.put(DBConnHelper.COURSE_MENTOR_EMAIL, mentorEmail.getText().toString());
                values.put(DBConnHelper.COURSE_MENTOR_PHONE, mentorPhone.getText().toString());
                values.put(DBConnHelper.COURSE_NOTES, notes.getText().toString());
                values.put(DBConnHelper.COURSE_RANGE, courseStart.getText().toString() + " - "
                    + courseEnd.getText().toString());

                getContentResolver().update(Uri.parse(WGUProvider.CONTENT_URI + "/" + WGUProvider.COURSE_ID),
                        values, DBConnHelper.PK_COURSE_ID + " = " + courseIdToUpdate, null);
                setResult(RESULT_OK);
                finish();
            }
        });

        updateAssessments.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
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

            }

        }else if(newCourse.getObjectiveAssessment().isEmpty()
                && !newCourse.getPerformanceAssessment().isEmpty()){

            for(Assessment A : newCourse.getPerformanceAssessment()){
                assessmentValues.put(DBConnHelper.FK_ASSESSMENTS_ID_IN_COURSES, getAssessmentKey(A.getAssessmentTitle()));
            }

        }

        if(assessmentValues.size() > 1){
            getContentResolver().insert(Uri.parse(WGUProvider.CONTENT_URI + "/" +
                    WGUProvider.ASSESSMENTS_IN_COURSES_ID), assessmentValues);
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
                openAssessmentDialog(null, null);
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

    private boolean assessmentWasPreviouslyChecked(String assessmentTitle) {
        int id = getAssessmentKey(assessmentTitle);

        Cursor thisCourseWithThisAssessment = getContentResolver().query(Uri.parse(WGUProvider.CONTENT_URI + "\"" + WGUProvider.ASSESSMENTS_IN_COURSES_ID),
                DBConnHelper.TABLE_ASSESSMENTS_IN_COURSES_ALL_COLUMNS,DBConnHelper.FK_COURSE_ID_ASSESSMENTS + " = " + courseIdToUpdate +
                " AND " + DBConnHelper.FK_ASSESSMENTS_ID_IN_COURSES + " = " + id,null,null);

        return thisCourseWithThisAssessment == null;
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

    private void openAssessmentDialog(ArrayList<String> selectedObjective, ArrayList<String> selectedPerformance) {

        //AlertDialog.Builder populateAssessments = new AlertDialog.Builder(this);
        //populateAssessments.setTitle("Choose your one objective or performance of Both");

        //ArrayAdapter<String> objectiveAssessmentTitles = new ArrayAdapter<>(populateAssessments.getContext(), R.layout.custom_dropdown_dialog);
        //ArrayAdapter<String> performanceAssessmentTitles = new ArrayAdapter<>(populateAssessments.getContext(), R.layout.custom_dropdown_dialog);

        ArrayList<String> performanceAssessmentTitles = new ArrayList<>();
        ArrayList<String> objectiveAssessmentTitles = new ArrayList<>();

        Cursor assignmentsToPopulate = getAllCurrentAssessments();
        //String whichToPopulate = "";

        assignmentsToPopulate.moveToFirst();
        /*
        if(selectedPerformance.size() == 0 && selectedObjective.size() == 0) {
            whichToPopulate = "Both";
        }else if(selectedPerformance.size() == 0 && selectedObjective.size() > 0){
            whichToPopulate = "Performance";
        }else if(selectedObjective.size() == 0 && selectedPerformance.size() > 0){
            whichToPopulate = "Objective";
        }*/


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


        try {
            showPopupWindow(performanceAssessmentTitles, objectiveAssessmentTitles);
        } catch (ParseException e) {
            e.printStackTrace();
        }
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


