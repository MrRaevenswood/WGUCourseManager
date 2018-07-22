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
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RadioButton;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class AssessmentAddActivity extends AppCompatActivity {

    private static TextView assessmentTitle;
    private static RadioButton isObjective;
    private static RadioButton isPerformance;
    private static TextView goalDate;
    private Bundle activityBundle;
    private int assessmentIdToUpdate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assessment_add);

        assessmentTitle = findViewById(R.id.txt_AssessmentTitle);
        isObjective = findViewById(R.id.rb_Objective);
        isPerformance = findViewById(R.id.rb_Performance);
        goalDate = findViewById(R.id.txt_AssessmentGoalDate);

        activityBundle = getIntent().getExtras();

        if(activityBundle.get("Edit") != null){
            ArrayList<String> assessmentData = activityBundle.getStringArrayList("selectedAssessment");
            assessmentTitle.setText(assessmentData.get(1));
            if(assessmentData.get(2).equals("0")){
                isObjective.setChecked(false);
                isPerformance.setChecked(true);
            }else if (assessmentData.get(3).equals("0")){
                isObjective.setChecked(true);
                isPerformance.setChecked(false);
            }
            goalDate.setText(assessmentData.get(4));

            assessmentIdToUpdate = Integer.parseInt(assessmentData.get(0));
        }

        Toolbar toolbar = findViewById(R.id.addAssessmentToolBar);
        toolbar.setTitle("Add/Edit Assessment");
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

                String selectedGoalDate = goalDate.getText().toString();
                String title = assessmentTitle.getText().toString();
                Boolean isObj = isObjective.isChecked();
                Boolean isPerf = isPerformance.isChecked();

                if(checkIfAssessmentExists(assessmentTitle.getText().toString())){

                    final AlertDialog.Builder duplicateTitle = new AlertDialog.Builder(this);
                    duplicateTitle.setTitle("There was an assessment with the same title found.");

                    duplicateTitle.setNeutralButton("Change Title", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });
                    duplicateTitle.create().show();
                    return false;
                }

                Assessment newAssessment = new Assessment(title, isObj, isPerf, selectedGoalDate);
                if(activityBundle.get("Edit") != null){
                    updateAssessment(newAssessment);
                }else{
                    saveAssessment(newAssessment);
                }

                break;
            case R.id.cancel:
                Intent returnToAssessmentsView = new Intent(AssessmentAddActivity.this, AssessmentsActivity.class);
                startActivity(returnToAssessmentsView);
        }
        return true;
    }

    private void updateAssessment(final Assessment newAssessment) {
        ContentValues values = new ContentValues();

        values.put(DBConnHelper.ASSESSMENT_TITLE, newAssessment.getAssessmentTitle());
        values.put(DBConnHelper.ASSESSMENT_ISOBJECTIVE, newAssessment.getIsObjective());
        values.put(DBConnHelper.ASSESSMENT_ISPERFORMANCE, newAssessment.getIsPerformance());
        values.put(DBConnHelper.ASSESSMENT_GOAL_DATE, newAssessment.getGoalDate().toString());

        getContentResolver().update(Uri.parse(WGUProvider.CONTENT_URI + "/" + WGUProvider.ASSESSMENTS_ID),values, DBConnHelper.PK_Assessment_ID
            + " = " + assessmentIdToUpdate, null);
        setResult(RESULT_OK);
        finish();
    }

    private void saveAssessment(Assessment newAssessment) {

        ContentValues values = new ContentValues();

        values.put(DBConnHelper.ASSESSMENT_TITLE, newAssessment.getAssessmentTitle());
        values.put(DBConnHelper.ASSESSMENT_ISOBJECTIVE, newAssessment.getIsObjective());
        values.put(DBConnHelper.ASSESSMENT_ISPERFORMANCE, newAssessment.getIsPerformance());
        values.put(DBConnHelper.ASSESSMENT_GOAL_DATE, newAssessment.getGoalDate().toString());

        getContentResolver().insert(Uri.parse(WGUProvider.CONTENT_URI + "/" + WGUProvider.ASSESSMENTS_ID),values);

        setResult(RESULT_OK);
        finish();

    }

    private boolean checkIfAssessmentExists(String title){
        Cursor getAllCurrentAssessments = getContentResolver().query(Uri.parse(WGUProvider.CONTENT_URI + "/" + WGUProvider.ASSESSMENTS_ID),
                DBConnHelper.ASSESSMENTS_ALL_COLUMNS, DBConnHelper.ASSESSMENT_TITLE + " = " + "\"" + title.trim() + "\"",
                null,null);

        if(!getAllCurrentAssessments.moveToNext()){
            return false;
        }else{
            return true;
        }
    }

    public void showGoalDatePickerDialog(View view){
        Bundle goalBundle = new Bundle();
        goalBundle.putString("goalDate","goalDate");
        DatePicker startGoalDatePicker = new DatePicker();
        startGoalDatePicker.setArguments(goalBundle);

        startGoalDatePicker.show(getFragmentManager(), "datePicker");
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
            if(this.getArguments().containsKey("goalDate")){
                goalDate.setText(String.valueOf(month) + "/" + String.valueOf(dayOfMonth) + "/"
                        + String.valueOf(year));
            }

        }

    }
}
