package com.example.wgucoursemanager;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.TextView;
import android.support.v7.widget.Toolbar;

import java.lang.reflect.Array;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class termAddActivity extends AppCompatActivity {

    private static TextView termTitle;
    private static TextView termStart;
    private static TextView termEnd;
    private Bundle activityBundle;
    private int termIdToUpdate = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        termTitle = findViewById(R.id.termTitle);
        termStart = findViewById(R.id.termStartDate);
        termEnd = findViewById(R.id.termEndDate);
        activityBundle = getIntent().getExtras();

        if(activityBundle.get("Edit") != null){
           ArrayList<String> termData = activityBundle.getStringArrayList("selectedTerm");
           termTitle.setText(termData.get(1));
           termStart.setText(termData.get(2));
           termEnd.setText(termData.get(3));

           termIdToUpdate = Integer.parseInt(termData.get(0));
        }

        Toolbar toolbar = findViewById(R.id.addTermToolBar);
        toolbar.setTitle(R.string.Add_Edit_Term);
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

                if(checkIfTermTitleExists(termTitle.getText().toString())){
                    final AlertDialog.Builder duplicateTitle = new AlertDialog.Builder(this);
                    duplicateTitle.setTitle("There was a term with the same title found.");

                    duplicateTitle.setNeutralButton("Change Title", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });
                    duplicateTitle.create().show();
                    return false;
                }

                if(activityBundle.get("Edit") != null){
                    promptToUpdateCourses();

                }else{
                    promptToAddCourses();
                }
                break;
            case R.id.cancel:
                Intent returnToTermsView = new Intent(termAddActivity.this, TermsActivity.class);
                startActivity(returnToTermsView);
                break;
        }

        return true;
    }

    private void promptToUpdateCourses() {

        AlertDialog.Builder updateCourses = new AlertDialog.Builder(this);
        updateCourses.setTitle("Update Courses?");

        updateCourses.setNegativeButton("NO", new AlertDialog.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ContentValues values = new ContentValues();
                values.put(DBConnHelper.TERM_TITLE, termTitle.getText().toString());
                values.put(DBConnHelper.TERM_START, termStart.getText().toString());
                values.put(DBConnHelper.TERM_END, termEnd.getText().toString());
                values.put(DBConnHelper.TERM_RANGE, termStart.getText().toString() + " - " +
                    termEnd.getText().toString());

                getContentResolver().update(Uri.parse(WGUProvider.CONTENT_URI + "/" + WGUProvider.TERMS_ID), values,
                        DBConnHelper.TERM_ID + " = " + termIdToUpdate, null);
                setResult(RESULT_OK);
                finish();
            }
        });

        updateCourses.setPositiveButton("YES", new AlertDialog.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ArrayList<String> selectedCourseIds = new ArrayList<>();
                ArrayList<String> selectedCourseTitles = new ArrayList<>();
                Cursor allCoursesThisTerm = getContentResolver().query(Uri.parse(WGUProvider.CONTENT_URI + "/" + WGUProvider.COURSES_IN_TERM_ID),
                        DBConnHelper.COURSES_IN_TERM_ALL_COLUMNS, DBConnHelper.FK_TERM_ID + " = " + termIdToUpdate, null, null);
                allCoursesThisTerm.moveToFirst();
                do{
                    selectedCourseIds.add(allCoursesThisTerm.getString(allCoursesThisTerm.getColumnIndex(DBConnHelper.FK_COURSE_ID_TERMS)));
                }while(allCoursesThisTerm.moveToNext());

                Cursor allCourses = getAllCourses();
                allCourses.moveToFirst();
                do {
                    for(String id : selectedCourseIds){

                        if(allCourses.getString(allCourses.getColumnIndex(DBConnHelper.PK_COURSE_ID)).equals(id)){

                            selectedCourseTitles.add(allCourses.getString(allCourses.getColumnIndex(DBConnHelper.COURSE_TITLE)));
                        }
                    }
                }while(allCourses.moveToNext());

                openCoursesDialog(selectedCourseTitles);
            }
        });

        updateCourses.create().show();
    }

    private void saveTerm(Term newTerm) {

        ContentValues values = new ContentValues();

        values.put(DBConnHelper.TERM_TITLE, newTerm.getTermTitle());
        values.put(DBConnHelper.TERM_START, newTerm.getStartDate());
        values.put(DBConnHelper.TERM_END, newTerm.getEndDate());
        values.put(DBConnHelper.TERM_RANGE, newTerm.getTermRange());

        if(termIdToUpdate == -1){
            getContentResolver().insert(Uri.parse(WGUProvider.CONTENT_URI + "/" + WGUProvider.TERMS_ID), values);
        }else{
            getContentResolver().update(Uri.parse(WGUProvider.CONTENT_URI + "/" + WGUProvider.TERMS_ID), values
            , DBConnHelper.TERM_ID + " = " + termIdToUpdate, null);
        }


        Cursor termIdCursor = getContentResolver().query(Uri.parse(WGUProvider.CONTENT_URI + "/" + WGUProvider.TERMS_ID),
                new String[]{DBConnHelper.TERM_ID}, null, null, DBConnHelper.TERM_ID  + " DESC");
        termIdCursor.moveToFirst();
        int termId = termIdCursor.getInt(0);

        ContentValues courseValues = new ContentValues();
        courseValues.put(DBConnHelper.FK_TERM_ID, termId);

        if(newTerm.getCourses() != null) {
            for (Courses c : newTerm.getCourses()) {
                courseValues.put(DBConnHelper.FK_COURSE_ID_TERMS, getCoursesKey(c.getCourseTitle()));
            }
            if (courseValues.size() > 0) {
                getContentResolver().insert(Uri.parse(WGUProvider.CONTENT_URI + "/" + WGUProvider.COURSES_IN_TERM_ID),
                        courseValues);
            }
        }
        setResult(RESULT_OK);

        finish();

    }

    private void promptToAddCourses() {

        AlertDialog.Builder addAssessment = new AlertDialog.Builder(this);
        addAssessment.setTitle("Add Courses?");

        addAssessment.setPositiveButton("YES", new AlertDialog.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                openCoursesDialog(null);
            }
        });

        addAssessment.setNegativeButton("NO", new AlertDialog.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                String title = termTitle.getText().toString();
                String start = termStart.getText().toString();
                String end = termEnd.getText().toString();
                String termRange = start + " - " + end;
                Term newTerm = new Term(title,start,end,termRange);
                saveTerm(newTerm);
            }


        });

        addAssessment.create().show();
    }

    private void openCoursesDialog(ArrayList<String> selectedCourses) {
        ArrayList<String> courses = new ArrayList<>();
        Cursor coursesToPopulate = getAllCourses();
        while(coursesToPopulate.moveToNext()){
            courses.add(coursesToPopulate.getString(
                    coursesToPopulate.getColumnIndex(DBConnHelper.COURSE_TITLE)));
        }

        showPopupWindow(courses, selectedCourses);
    }

    private void showPopupWindow(ArrayList<String> courses, final ArrayList<String> coursesSelected) {

        ConstraintLayout to_add = findViewById(R.id.termAdd);
        ArrayList<View> viewsInOriginalLayout = new ArrayList<>();
        ArrayList<View> viewsInNewLayout = new ArrayList<>();

        for( int i = 0; i < to_add.getChildCount(); i++){
            View v = to_add.getChildAt(i);
            if(v.getId() == R.id.addTermToolBar){continue;}
            v.setVisibility(View.INVISIBLE);
            viewsInOriginalLayout.add(to_add.getChildAt(i));
        }
        ConstraintSet set = new ConstraintSet();

        final ArrayList<CheckBox> generatedCheckBoxIds = new ArrayList<>();

        TextView performanceTitle = new TextView(to_add.getContext()) ;
        performanceTitle.setText("Add Courses Below:");
        performanceTitle.setId(View.generateViewId());
        to_add.addView(performanceTitle);


        for(String p : courses){
            CheckBox performanceCheckBox = new CheckBox(to_add.getContext());
            performanceCheckBox.setId(View.generateViewId());
            performanceCheckBox.setText(p);
            if(coursesSelected != null){
                if(coursesSelected.indexOf(p) != -1 ){
                    performanceCheckBox.setChecked(true);
                }
            }
            to_add.addView(performanceCheckBox);

            generatedCheckBoxIds.add(performanceCheckBox);
        }



        Button addButton = new Button(to_add.getContext());
        addButton.setId(View.generateViewId());
        addButton.setText("Add Courses");
        addButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                ArrayList<String> coursesContainer = new ArrayList<>();
                ArrayList<Integer> coursesKeys = new ArrayList<>();
                for(CheckBox checkBox : generatedCheckBoxIds){

                    if(checkBox.isChecked()){coursesContainer.add(checkBox.getText().toString());}
/*
                    if(coursesSelected != null){
                        if(checkBox.isChecked() && coursesSelected.indexOf(checkBox.getText().toString()) == -1){
                            coursesContainer.add(checkBox.getText().toString());
                        }else if(!checkBox.isChecked() && coursesSelected.indexOf(checkBox.getText().toString()) != -1){
                            Cursor courses = getAllCourses();
                            courses.moveToFirst();
                            while(courses.moveToNext()){
                                if(courses.getString(courses.getColumnIndex(DBConnHelper.PK_COURSE_ID)).equals(checkBox.getText().toString())){
                                    getContentResolver().delete(Uri.parse(WGUProvider.CONTENT_URI + "/" + DBConnHelper.COURSES_IN_TERM_ID),
                                            DBConnHelper.FK_TERM_ID + " = " + termIdToUpdate + " AND " + DBConnHelper.FK_COURSE_ID_TERMS +
                                                    " = " + courses.getString(courses.getColumnIndex(DBConnHelper.PK_COURSE_ID)), null);
                                }
                            }
                        }
                    }*/
                }
                for (String courses : coursesContainer){
                    coursesKeys.add(getCoursesKey(courses));
                }

                try {
                    saveTerm(buildTerm(coursesKeys));
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

        set.connect(newView.get(0).getId(), ConstraintSet.TOP, R.id.addTermToolBar, ConstraintSet.BOTTOM, 200);
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

    private Term buildTerm(ArrayList<Integer> coursesKeys) throws ParseException {
        String title = termTitle.getText().toString();
        String startDate = termStart.getText().toString();
        String endDate = termEnd.getText().toString();
        String termRange = startDate + " - " + endDate;
        ArrayList<Courses> allCurrentCoursesForTerm = new ArrayList<>();

        for(Integer i : coursesKeys){
            allCurrentCoursesForTerm.add(getCoursesFromSelected(i));
        }

        if(coursesKeys.isEmpty()){
            return new Term(title, startDate, endDate, termRange);
        }else{
            return new Term(title, startDate, endDate, termRange, allCurrentCoursesForTerm);
        }
    }

    private Courses getCoursesFromSelected(Integer i) throws ParseException {
        Cursor allCurrentCourses;
        String title, start, end, status, mentorN, mentorE, mentorP, notesTaken;
        ArrayList<Assessment> allAssessmentsForCourse;

        allCurrentCourses = getAllCourses();
        allCurrentCourses.moveToFirst();

        title = allCurrentCourses.getString(
                allCurrentCourses.getColumnIndex(DBConnHelper.COURSE_TITLE)
        );

        start = allCurrentCourses.getString(allCurrentCourses.getColumnIndex(DBConnHelper.COURSE_START));
        end = allCurrentCourses.getString(allCurrentCourses.getColumnIndex(DBConnHelper.COURSE_END));
        status = allCurrentCourses.getString(allCurrentCourses.getColumnIndex(DBConnHelper.COURSE_STATUS));
        mentorN = allCurrentCourses.getString(allCurrentCourses.getColumnIndex(DBConnHelper.COURSE_MENTOR_NAME));
        mentorE = allCurrentCourses.getString(allCurrentCourses.getColumnIndex(DBConnHelper.COURSE_MENTOR_EMAIL));
        mentorP = allCurrentCourses.getString(allCurrentCourses.getColumnIndex(DBConnHelper.COURSE_MENTOR_PHONE));
        notesTaken = allCurrentCourses.getString(allCurrentCourses.getColumnIndex(DBConnHelper.COURSE_NOTES));
        allAssessmentsForCourse = getAssessmentsForSelectedCourse(allCurrentCourses.getInt(allCurrentCourses.getColumnIndex(DBConnHelper.PK_COURSE_ID)));

        if(allAssessmentsForCourse.isEmpty()){return new Courses(title, start, end, status, mentorN, mentorE, mentorP,
                notesTaken, start + " - " + end);}
        return new Courses(title,start,end,status,mentorN,mentorE,mentorP, notesTaken, start + " - " + end,
                allAssessmentsForCourse);
    }

    private ArrayList<Assessment> getAssessmentsForSelectedCourse(Integer courseKey) throws ParseException {
        Cursor allAssessments = getContentResolver().query(Uri.parse(WGUProvider.CONTENT_URI + "/" + WGUProvider.ASSESSMENTS_ID),
                DBConnHelper.ASSESSMENTS_ALL_COLUMNS, null, null,
                null);
        Cursor allAssociatedAssessments = getContentResolver().query(Uri.parse(WGUProvider.CONTENT_URI + "/" + WGUProvider.ASSESSMENTS_IN_COURSES_ID),
                DBConnHelper.TABLE_ASSESSMENTS_IN_COURSES_ALL_COLUMNS, DBConnHelper.FK_COURSE_ID_ASSESSMENTS + " = " + courseKey,
                null,null);

        allAssessments.moveToNext();
        allAssociatedAssessments.moveToNext();

        ArrayList<Assessment> allAssessmentsForCourse = new ArrayList<>();
        String title;
        Boolean isObjective, isPerformance;
        String goalDate;

        allAssessments.moveToFirst();
        while(allAssessments.moveToNext()){
            while(allAssociatedAssessments.moveToNext()){
                if(allAssociatedAssessments.getString(allAssociatedAssessments.getColumnIndex(DBConnHelper.FK_COURSE_ID_ASSESSMENTS)).equals(
                        allAssessments.getString(allAssessments.getColumnIndex(DBConnHelper.FK_COURSE_ID_ASSESSMENTS)))){

                    title = allAssessments.getString(allAssessments.getColumnIndex(DBConnHelper.ASSESSMENT_TITLE));
                    isObjective = Boolean.parseBoolean(allAssessments.getString(allAssessments.getColumnIndex(DBConnHelper.ASSESSMENT_ISOBJECTIVE)));
                    isPerformance = Boolean.parseBoolean(allAssessments.getString(allAssessments.getColumnIndex(DBConnHelper.ASSESSMENT_ISPERFORMANCE)));
                    goalDate = allAssessments.getString(allAssessments.getColumnIndex(DBConnHelper.ASSESSMENT_GOAL_DATE));

                    allAssessmentsForCourse.add(new Assessment(title,isObjective,isPerformance,goalDate));
                }
            }
            allAssociatedAssessments.moveToFirst();
        }
        return allAssessmentsForCourse;

    }

    private Integer getCoursesKey(String courseTitle) {
        Cursor coursesKey = getContentResolver().query(Uri.parse(WGUProvider.CONTENT_URI + "/" + WGUProvider.COURSE_ID),
                new String[]{DBConnHelper.PK_COURSE_ID}, DBConnHelper.COURSE_TITLE + "= " + "\"" + courseTitle + "\""
                , null,
                null);
        coursesKey.moveToFirst();
        return coursesKey.getInt(0);
    }

    private Cursor getAllCourses() {
        return getContentResolver().query(Uri.parse(WGUProvider.CONTENT_URI + "/" + WGUProvider.COURSE_ID),
                DBConnHelper.COURSES_ALL_COLUMNS, null, null, null);
    }

    private Boolean checkIfTermTitleExists(String title){

        Cursor matchingTermTitles = getContentResolver().query(Uri.parse(WGUProvider.CONTENT_URI + "/" + WGUProvider.TERMS_ID),
                null, DBConnHelper.TERM_TITLE + " = " + title.trim(), null, null );

        if(!matchingTermTitles.moveToNext()){
            return false;
        }else{
            return true;
        }

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
                termStart.setText(String.valueOf(month) + "/" + String.valueOf(dayOfMonth) + "/"
                        + String.valueOf(year));
            }else if(this.getArguments().containsKey("endDate")){
                termEnd.setText(String.valueOf(month) + "/" + String.valueOf(dayOfMonth) + "/"
                        + String.valueOf(year));
            };

        }

    }

}
