package com.example.wgucoursemanager;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.telephony.SmsManager;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.RadioButton;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

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
        scheduleAlertDialog(newAssessment);

        setResult(RESULT_OK);
        finish();

    }

    private boolean checkIfAssessmentExists(String title){
        Cursor getAllCurrentAssessments = getContentResolver().query(Uri.parse(WGUProvider.CONTENT_URI + "/" + WGUProvider.ASSESSMENTS_ID),
                DBConnHelper.ASSESSMENTS_ALL_COLUMNS, DBConnHelper.ASSESSMENT_TITLE + " = " + "\"" + title.trim() + "\"",
                null,null);

        if(!getAllCurrentAssessments.moveToNext() || getAllCurrentAssessments.getInt(getAllCurrentAssessments.getColumnIndex(DBConnHelper.PK_Assessment_ID))
                == assessmentIdToUpdate){
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
            String monthString = String.valueOf(month);
            if(String.valueOf(month).length() != 2){
                monthString = "0".concat(monthString);
            }
            String day = String.valueOf(dayOfMonth);
            if(day.length() !=2){
                day = "0" + day;
            }

                goalDate.setText( String.valueOf(year) + "-" + monthString + "-"
                        + day);

            TimePicker startTimePicker = new TimePicker();
            //startTimePicker.setArguments(args);
            startTimePicker.show(getFragmentManager(), "timePicker");
        }

    }

    public static class TimePicker extends DialogFragment
            implements TimePickerDialog.OnTimeSetListener{

        public int hour;
        public int minute;

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState){
            final Calendar t = Calendar.getInstance();
            hour = t.get(Calendar.HOUR);
            minute = t.get(Calendar.MINUTE);

            return new TimePickerDialog(getActivity(),this, hour, minute, true);

        }

        @Override
        public void onTimeSet(android.widget.TimePicker view, int hourOfDay, int minute) {

            String hourString = String.valueOf(hourOfDay);
            String minuteString = String.valueOf(minute);

            if(hourString.length() != 2){
                hourString = "0".concat(hourString);
            }

            if(minuteString.length() != 2){
                minuteString = "0".concat(minuteString);
            }

            goalDate.append("T" + hourString + ":" + minuteString + ":00");
        }
    }

    private void scheduleAlertDialog(Assessment assessment) {

        Calendar c = Calendar.getInstance();
        int test = c.get(Calendar.MONTH) + 1;
        String month = String.valueOf(test);
        if (month.length() != 2) {
            month = "0" + month;
        }

        String dayOfMonth = String.valueOf(c.get(Calendar.DAY_OF_MONTH));
        if (dayOfMonth.length() != 2) {
            dayOfMonth = "0" + dayOfMonth;
        }

        String hour = String.valueOf(c.get(Calendar.HOUR));
        if (hour.length() != 2) {
            hour = "0" + hour;
        }

        String minute = String.valueOf(c.get(Calendar.MINUTE));
        if (minute.length() != 2) {
            minute = "0" + minute;
        }
        String second = String.valueOf(c.get(Calendar.SECOND));
        if(second.length() != 2){
            second = "0" + second;
        }

        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        LocalDateTime goalDate = LocalDateTime.parse(assessment.getGoalDate(), formatter);
        LocalDateTime current = LocalDateTime.parse(String.valueOf(c.get(Calendar.YEAR)) + "-" + month
                + "-" + dayOfMonth + "T" + hour + ":" + minute + ":" +second,formatter);

        SharedPreferences prefs = getSharedPreferences("Alarms", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        long millisToGoal = current.until(goalDate, ChronoUnit.MILLIS);
        int resourceId = (int) (c.getTimeInMillis() + millisToGoal);

        if(millisToGoal > 0){
            Intent notifiyIntent = new Intent(getApplicationContext(), NotificationReceiver.class);
            notifiyIntent.putExtra("title","Assessment " + assessment.getAssessmentTitle() + " just reached its goal date");
            notifiyIntent.putExtra("removeAlarm", assessment.getAssessmentTitle());
            notifiyIntent.putExtra("requestCode", resourceId);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(this, resourceId, notifiyIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT);


            editor.putString(assessment.getAssessmentTitle(), "Assessment " + assessment.getAssessmentTitle() + " will fire at " +
                assessment.getGoalDate());

            AlarmManager alarmManager = (AlarmManager) getBaseContext().getSystemService(Context.ALARM_SERVICE);
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, c.getTimeInMillis() + millisToGoal, pendingIntent);

        }

        editor.commit();
    }
}
